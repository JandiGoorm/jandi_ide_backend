package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import com.webproject.jandi_ide_backend.chat.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;

/**
 * WebSocket STOMP 기반의 채팅 메시지를 처리하는 컨트롤러입니다.
 * 클라이언트가 특정 경로로 메시지를 발행(send)하면 해당 메시지를 수신하여 처리합니다.
 */
@Slf4j
@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "채팅 메시지", description = "채팅 메시지 송수신 및 조회 API")
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/message")
    @Operation(summary = "채팅 메시지 전송", description = "WebSocket을 통해 채팅 메시지를 전송합니다.")
    public void message(@Payload ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("메시지 수신: {}", message);
            
            // 서비스에서 메시지 처리 및 사용자 정보 설정
            ChatMessageDTO processedMessage = chatMessageService.processMessage(message, headerAccessor);
            if (processedMessage == null) {
                // 인증되지 않은 사용자인 경우 무시
                return;
            }
            
            // MongoDB에 메시지 저장
            try {
                chatMessageService.saveMessage(processedMessage);
                log.debug("MongoDB에 메시지 저장 완료");
            } catch (Exception e) {
                log.error("MongoDB에 메시지 저장 실패: {}", e.getMessage(), e);
                // 저장 실패해도 메시지 전송은 진행
            }
            
            // 메시지 전송
            messagingTemplate.convertAndSend("/topic/chat/room/" + processedMessage.getRoomId(), processedMessage);
            
            log.info("메시지 전송 완료: {}", processedMessage);
        } catch (Exception e) {
            log.error("메시지 처리 오류: {}", e.getMessage(), e);
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
        log.debug("사용자별 메시지 조회 요청: {}", sender);
        List<ChatMessageDTO> messages = chatMessageService.getMessagesBySender(sender);
        return ResponseEntity.ok(messages);
    }

    /**
     * 특정 키워드가 포함된 메시지를 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 메시지 목록
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
        log.debug("메시지 키워드 검색 요청: {}", keyword);
        List<ChatMessageDTO> messages = chatMessageService.searchMessagesByKeyword(keyword);
        return ResponseEntity.ok(messages);
    }
}