package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import com.webproject.jandi_ide_backend.chat.service.ChatMessageService;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * WebSocket STOMP 기반의 채팅 메시지를 처리하는 컨트롤러입니다.
 * 클라이언트가 특정 경로로 메시지를 발행(send)하면 해당 메시지를 수신하여 처리합니다.
 */
@Slf4j
@Controller
@RequestMapping("/api/chat")
@Tag(name = "채팅 메시지", description = "채팅 메시지 송수신 및 조회 API")
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ChatMessageService chatMessageService;  // 추가: 메시지 저장 서비스

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository,
                          ChatMessageService chatMessageService) {
        this.messagingTemplate = messagingTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/chat/message")
    @Operation(summary = "채팅 메시지 전송", description = "WebSocket을 통해 채팅 메시지를 전송합니다.")
    public void message(@Payload ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("메시지 수신: {}", message);
            
            // 현재 인증된 사용자 정보 확인
            Principal user = headerAccessor.getUser();
            if (user == null) {
                log.warn("인증되지 않은 사용자의 메시지 - 무시됨: {}", message);
                return;
            }
            
            // 인증 정보에서 사용자 이름 추출 (Spring Security에서 설정한 정보)
            String username = user.getName();
            log.info("인증된 사용자: {}", username);
            
            // 헤더에서 직접 토큰 추출하여 유효성 확인
            try {
                List<String> authHeaders = headerAccessor.getNativeHeader("Authorization");
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    String authHeader = authHeaders.get(0);
                    if (authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        
                        // 토큰 유효성 명시적 검사
                        if (!jwtTokenProvider.validateToken(token)) {
                            log.warn("유효하지 않은 토큰으로 메시지 전송 시도. 사용자: {}", username);
                            // 메시지는 처리하되 로그 남김
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("토큰 검증 중 오류: {}", e.getMessage());
                // 오류가 있어도 메시지 처리는 계속 진행
            }
            
            try {
                // 사용자의 실제 닉네임 설정 (GitHub 사용자의 경우)
                User userEntity = userRepository.findByGithubId(username)
                        .orElse(null);
                if (userEntity != null) {
                    message.setSender(userEntity.getNickname());
                }
            } catch (Exception e) {
                log.warn("사용자 정보 조회 실패, 기본 sender 사용: {}", e.getMessage());
                // 기존 sender 유지
            }
            
            // 메시지에 현재 시간 추가
            message.setTimestamp(LocalDateTime.now().toString());
            
            // MongoDB에 메시지 저장
            try {
                chatMessageService.saveMessage(message);
                log.debug("MongoDB에 메시지 저장 완료");
            } catch (Exception e) {
                log.error("MongoDB에 메시지 저장 실패: {}", e.getMessage(), e);
                // 저장 실패해도 메시지 전송은 진행
            }
            
            // 메시지 전송
            messagingTemplate.convertAndSend("/topic/chat/room/" + message.getRoomId(), message);
            
            log.info("메시지 전송 완료: {}", message);
        } catch (Exception e) {
            log.error("메시지 처리 오류: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 채팅방의 모든 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 해당 채팅방의 모든 메시지 목록
     */
    @GetMapping("/rooms/{roomId}/messages")
    @Operation(
        summary = "채팅방 메시지 조회", 
        description = "특정 채팅방의 모든 메시지를 조회합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "메시지 조회 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = ChatMessageDTO.class))
            )
        ),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    public ResponseEntity<List<ChatMessageDTO>> getRoomMessages(
            @Parameter(description = "채팅방 ID", required = true, example = "room123") 
            @PathVariable String roomId) {
        log.debug("채팅방 메시지 조회 요청: {}", roomId);
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomId(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 특정 채팅방의 메시지를 페이징 처리하여 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param pageable 페이징 정보
     * @return 페이징 처리된 메시지 목록
     */
    @GetMapping("/rooms/{roomId}/messages/paged")
    @Operation(
        summary = "채팅방 메시지 페이징 조회", 
        description = "특정 채팅방의 메시지를 페이징 처리하여 조회합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "메시지 페이징 조회 성공",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    public ResponseEntity<Page<ChatMessageDTO>> getRoomMessagesPaged(
            @Parameter(description = "채팅방 ID", required = true, example = "room123") 
            @PathVariable String roomId,
            @Parameter(description = "페이지 정보 (size, page)", example = "?page=0&size=20") 
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("채팅방 메시지 페이징 조회 요청: {}, {}", roomId, pageable);
        Page<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomIdPaged(roomId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * 특정 채팅방의 특정 시간 이후 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param timestamp 기준 시간 (ISO-8601 형식)
     * @return 기준 시간 이후의 메시지 목록
     */
    @GetMapping("/rooms/{roomId}/messages/after")
    @Operation(
        summary = "특정 시간 이후 메시지 조회", 
        description = "특정 채팅방의 특정 시간 이후 메시지를 조회합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "메시지 조회 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = ChatMessageDTO.class))
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 시간 형식"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    public ResponseEntity<?> getRoomMessagesAfterTimestamp(
            @Parameter(description = "채팅방 ID", required = true, example = "room123") 
            @PathVariable String roomId,
            @Parameter(description = "기준 시간 (ISO-8601 형식)", required = true, example = "2025-04-22T15:30:45") 
            @RequestParam String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp);
            log.debug("채팅방 메시지 시간 이후 조회 요청: {}, {}", roomId, dateTime);
            List<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomIdAfterTimestamp(roomId, dateTime);
            return ResponseEntity.ok(messages);
        } catch (DateTimeParseException e) {
            log.error("잘못된 타임스탬프 형식: {}", timestamp, e);
            return ResponseEntity.badRequest().body("Invalid timestamp format. Use ISO-8601 format.");
        }
    }

    /**
     * 특정 사용자가 보낸 메시지를 조회합니다.
     *
     * @param sender 발신자
     * @return 해당 사용자가 보낸 메시지 목록
     */
    @GetMapping("/messages/user/{sender}")
    @Operation(
        summary = "사용자별 메시지 조회", 
        description = "특정 사용자가 보낸 모든 메시지를 조회합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "메시지 조회 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = ChatMessageDTO.class))
            )
        )
    })
    public ResponseEntity<List<ChatMessageDTO>> getUserMessages(
            @Parameter(description = "메시지 발신자", required = true, example = "홍길동") 
            @PathVariable String sender) {
        log.debug("사용자 메시지 조회 요청: {}", sender);
        List<ChatMessageDTO> messages = chatMessageService.getMessagesBySender(sender);
        return ResponseEntity.ok(messages);
    }

    /**
     * 메시지 내용에 특정 키워드가 포함된 메시지를 조회합니다.
     *
     * @param keyword 검색할 키워드
     * @return 키워드를 포함한 메시지 목록
     */
    @GetMapping("/messages/search")
    @Operation(
        summary = "메시지 키워드 검색", 
        description = "메시지 내용에 특정 키워드가 포함된 메시지를 검색합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "메시지 검색 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = ChatMessageDTO.class))
            )
        )
    })
    public ResponseEntity<List<ChatMessageDTO>> searchMessages(
            @Parameter(description = "검색 키워드", required = true, example = "안녕하세요") 
            @RequestParam String keyword) {
        log.debug("메시지 검색 요청: {}", keyword);
        List<ChatMessageDTO> messages = chatMessageService.searchMessagesByKeyword(keyword);
        return ResponseEntity.ok(messages);
    }
}