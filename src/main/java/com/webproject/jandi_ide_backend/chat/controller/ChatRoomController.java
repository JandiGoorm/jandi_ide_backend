package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import com.webproject.jandi_ide_backend.chat.service.ChatRoomService;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅방(ChatRoom) 관련 CRUD API 요청을 처리하는 REST 컨트롤러입니다.
 * '/api/chat/rooms' 기본 경로 하위의 요청들을 처리합니다.
 */
@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(
            @RequestBody ChatRoomDTO chatRoomDTO,
            @RequestHeader("Authorization") String token) {
        // 토큰 검증
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "");
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);

        // 사용자 정보 확인
        User user = userRepository.findByGithubId(tokenInfo.getGithubId())
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 채팅방 생성 정보에 사용자 정보 추가
        chatRoomDTO.setCreatedBy(user.getNickname());

        ChatRoom createdRoom = chatRoomService.createRoom(chatRoomDTO);
        return ResponseEntity.ok(createdRoom);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getRooms(
            @RequestHeader("Authorization") String token) {
        // 토큰 검증
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "");
        jwtTokenProvider.decodeToken(accessToken);

        List<ChatRoom> allRooms = chatRoomService.findAllRooms();
        return ResponseEntity.ok(allRooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token) {
        // 토큰 검증
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "");
        jwtTokenProvider.decodeToken(accessToken);

        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room != null) {
            return ResponseEntity.ok(room);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token) {
        // 토큰 검증
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "");
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);

        // 사용자 정보 확인
        User user = userRepository.findByGithubId(tokenInfo.getGithubId())
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 채팅방 정보 확인
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        // 채팅방 생성자만 삭제 가능
        if (!room.getCreatedBy().equals(user.getNickname())) {
            throw new CustomException(CustomErrorCodes.PERMISSION_DENIED);
        }

        boolean deleted = chatRoomService.deleteRoom(roomId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}