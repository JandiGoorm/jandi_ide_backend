package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import com.webproject.jandi_ide_backend.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(
            @RequestBody ChatRoomDTO chatRoomDTO) { // @RequestHeader 제거, DTO에서 모든 정보 받음
        // DTO에 createdBy 필드가 있으므로 username을 따로 받을 필요 없음
        return ResponseEntity.ok(
                chatRoomService.createRoom(chatRoomDTO) // 서비스 호출 시 DTO만 전달
        );
    }

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getRooms() {
        return ResponseEntity.ok(chatRoomService.findAllRooms());
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable String roomId) {
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room != null) {
            return ResponseEntity.ok(room);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        if (chatRoomService.deleteRoom(roomId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}