package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import com.webproject.jandi_ide_backend.chat.service.ChatMessageService;
import com.webproject.jandi_ide_backend.chat.service.ChatRoomService;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.entity.User.UserRole;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

/**
 * 채팅방(ChatRoom) 관련 CRUD API 요청을 처리하는 REST 컨트롤러입니다.
 * '/api/chat/rooms' 기본 경로 하위의 요청들을 처리합니다.
 */
@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "채팅방 관리", description = "채팅방 CRUD 및 참여 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ChatMessageService chatMessageService;

    /**
     * 새 채팅방을 생성합니다. ADMIN 권한을 가진 사용자만 접근 가능합니다.
     */
    @PostMapping
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다. ADMIN 권한을 가진 사용자만 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 생성 성공", 
                     content = @Content(schema = @Schema(implementation = ChatRoom.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN만 가능)"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ChatRoom> createRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "채팅방 생성 정보", required = true, 
                                               content = @Content(schema = @Schema(implementation = ChatRoomDTO.class)))
            @RequestBody ChatRoomDTO chatRoomDTO,
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증 및 사용자 가져오기
        User user = validateTokenAndGetUser(token);
        
        // ADMIN 권한 검사
        if (user.getRole() != UserRole.ADMIN) {
            throw new CustomException(CustomErrorCodes.PERMISSION_DENIED);
        }

        // 채팅방 생성 정보에 사용자 정보 추가
        chatRoomDTO.setCreatedBy(user.getNickname());

        ChatRoom createdRoom = chatRoomService.createRoom(chatRoomDTO);
        return ResponseEntity.ok(createdRoom);
    }

    /**
     * 모든 채팅방 목록을 조회합니다. 로그인한 모든 사용자가 접근 가능합니다.
     */
    @GetMapping
    @Operation(summary = "채팅방 목록 조회", description = "모든 채팅방 목록을 조회합니다. 로그인한 모든 사용자가 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<ChatRoom>> getRooms(
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증만 수행 (모든 인증된 사용자 접근 가능)
        validateTokenAndGetUser(token);

        List<ChatRoom> allRooms = chatRoomService.findAllRooms();
        return ResponseEntity.ok(allRooms);
    }

    /**
     * 채팅방 유형별로 필터링된 목록을 조회합니다. 로그인한 모든 사용자가 접근 가능합니다.
     */
    @GetMapping("/type/{roomType}")
    @Operation(summary = "채팅방 유형별 목록 조회", description = "특정 유형(COMPANY 또는 TECH_STACK)에 해당하는 채팅방 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 채팅방 유형"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<ChatRoom>> getRoomsByType(
            @Parameter(description = "채팅방 유형 (COMPANY 또는 TECH_STACK)", required = true)
            @PathVariable String roomType,
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증만 수행 (모든 인증된 사용자 접근 가능)
        validateTokenAndGetUser(token);
        
        try {
            ChatRoom.RoomType type = ChatRoom.RoomType.valueOf(roomType.toUpperCase());
            List<ChatRoom> allRooms = chatRoomService.findAllRooms();
            List<ChatRoom> filteredRooms = allRooms.stream()
                    .filter(room -> room.getRoomType() == type)
                    .toList();
            return ResponseEntity.ok(filteredRooms);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 ID의 채팅방을 조회합니다. 로그인한 모든 사용자가 접근 가능합니다.
     */
    @GetMapping("/{roomId}")
    @Operation(summary = "특정 채팅방 조회", description = "특정 ID의 채팅방을 조회합니다. 로그인한 모든 사용자가 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 조회 성공", 
                     content = @Content(schema = @Schema(implementation = ChatRoom.class))),
        @ApiResponse(responseCode = "404", description = "채팅방 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ChatRoom> getRoom(
            @Parameter(description = "채팅방 ID", required = true) 
            @PathVariable String roomId,
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증만 수행 (모든 인증된 사용자 접근 가능)
        validateTokenAndGetUser(token);

        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room != null) {
            return ResponseEntity.ok(room);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 채팅방의 참여자 목록을 조회합니다. 로그인한 모든 사용자가 접근 가능합니다.
     */
    @GetMapping("/{roomId}/participants")
    @Operation(summary = "채팅방 참여자 목록 조회", description = "특정 채팅방의 참여자 목록을 조회합니다. 로그인한 모든 사용자가 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "참여자 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "채팅방 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Set<String>> getRoomParticipants(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증만 수행 (모든 인증된 사용자 접근 가능)
        validateTokenAndGetUser(token);

        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room != null) {
            return ResponseEntity.ok(room.getParticipants());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 ID의 채팅방을 삭제합니다. ADMIN 권한을 가진 사용자만 접근 가능합니다.
     */
    @DeleteMapping("/{roomId}")
    @Operation(summary = "채팅방 삭제", description = "특정 ID의 채팅방을 삭제합니다. ADMIN 권한을 가진 사용자만 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN만 가능)"),
        @ApiResponse(responseCode = "404", description = "채팅방 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증 및 사용자 가져오기
        User user = validateTokenAndGetUser(token);
        
        // ADMIN 권한 검사
        if (user.getRole() != UserRole.ADMIN) {
            throw new CustomException(CustomErrorCodes.PERMISSION_DENIED);
        }

        // 채팅방 정보 확인
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = chatRoomService.deleteRoom(roomId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 채팅방에 참여합니다. 로그인한 모든 사용자가 접근 가능합니다.
     */
    @PostMapping("/{roomId}/join")
    @Operation(summary = "채팅방 참여", description = "채팅방에 참여합니다. 로그인한 모든 사용자가 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 참여 성공", 
                     content = @Content(schema = @Schema(implementation = ChatRoom.class))),
        @ApiResponse(responseCode = "404", description = "채팅방 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ChatRoom> joinRoom(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증 및 사용자 가져오기
        User user = validateTokenAndGetUser(token);

        // 채팅방 정보 확인
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        // 채팅방 참여 처리 (중복 참여 체크는 Service에서 처리)
        ChatRoom updatedRoom = chatRoomService.addParticipant(roomId, user.getNickname());
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * 채팅방에서 나갑니다. 로그인한 모든 사용자가 접근 가능합니다.
     */
    @PostMapping("/{roomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나갑니다. 로그인한 모든 사용자가 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 나가기 성공", 
                     content = @Content(schema = @Schema(implementation = ChatRoom.class))),
        @ApiResponse(responseCode = "404", description = "채팅방 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ChatRoom> leaveRoom(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(description = "Bearer 인증 토큰", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증 및 사용자 가져오기
        User user = validateTokenAndGetUser(token);

        // 채팅방 정보 확인
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        // 채팅방 나가기 처리
        ChatRoom updatedRoom = chatRoomService.removeParticipant(roomId, user.getNickname());
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * JWT 토큰을 검증하고 사용자 정보를 반환하는 유틸리티 메서드
     */
    private User validateTokenAndGetUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "");
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);

        // 사용자 정보 확인
        return userRepository.findByGithubId(tokenInfo.getGithubId())
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));
    }

    @Operation(summary = "특정 채팅방의 메시지 목록 조회", description = "채팅방 ID로 해당 채팅방의 모든 메시지를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getRoomMessages(
            @Parameter(description = "채팅방 ID", required = true) @PathVariable String roomId) {
        // 채팅방 존재 여부 먼저 확인
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomId(roomId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "채팅방 메시지 페이징 조회", description = "채팅방 ID로 해당 채팅방의 메시지를 페이징 처리하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/{roomId}/messages/paged")
    public ResponseEntity<Page<ChatMessageDTO>> getRoomMessagesPaged(
            @Parameter(description = "채팅방 ID", required = true) @PathVariable String roomId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        // 채팅방 존재 여부 먼저 확인
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomIdPaged(roomId, pageable);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "특정 시간 이후 채팅 메시지 조회", description = "채팅방 ID와 기준 시간을 입력받아 해당 시간 이후의 메시지를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (시간 형식 오류 등)"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/{roomId}/messages/after")
    public ResponseEntity<?> getRoomMessagesAfterTimestamp(
            @Parameter(description = "채팅방 ID", required = true) @PathVariable String roomId,
            @Parameter(description = "기준 시간 (ISO-8601 형식)", example = "2023-08-01T12:00:00", required = true) 
            @RequestParam String timestamp) {
        // 채팅방 존재 여부 먼저 확인
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp);
            List<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomIdAfterTimestamp(roomId, dateTime);
            return ResponseEntity.ok(messages);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid timestamp format. Use ISO-8601 format (yyyy-MM-ddTHH:mm:ss).");
        }
    }

    @Operation(summary = "사용자별 메시지 조회", description = "특정 사용자가 보낸 모든 메시지를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/messages/user/{sender}")
    public ResponseEntity<List<ChatMessageDTO>> getUserMessages(
            @Parameter(description = "메시지 발신자", required = true) @PathVariable String sender) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesBySender(sender);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "메시지 키워드 검색", description = "메시지 내용에 특정 키워드가 포함된 메시지를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/messages/search")
    public ResponseEntity<List<ChatMessageDTO>> searchMessages(
            @Parameter(description = "검색 키워드", required = true) @RequestParam String keyword) {
        List<ChatMessageDTO> messages = chatMessageService.searchMessagesByKeyword(keyword);
        return ResponseEntity.ok(messages);
    }
}