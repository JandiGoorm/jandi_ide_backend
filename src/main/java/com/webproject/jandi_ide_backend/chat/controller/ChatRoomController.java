package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import com.webproject.jandi_ide_backend.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅방(ChatRoom) 관련 CRUD API 요청을 처리하는 REST 컨트롤러입니다.
 * '/api/chat/rooms' 기본 경로 하위의 요청들을 처리합니다.
 */
@RestController // 이 클래스가 RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequestMapping("/api/chat/rooms") // 이 컨트롤러의 모든 핸들러 메소드에 대한 기본 경로를 설정합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다 (Lombok).
public class ChatRoomController {

    // 채팅방 관련 비즈니스 로직을 처리하는 서비스 클래스 (의존성 주입)
    private final ChatRoomService chatRoomService;

    /**
     * 새로운 채팅방을 생성합니다. (HTTP POST 요청 처리)
     * 요청 본문에 담긴 채팅방 생성 정보(DTO)를 사용하여 서비스를 호출합니다.
     *
     * @param chatRoomDTO 요청 본문(JSON)으로부터 매핑된 채팅방 생성 정보 DTO (name, description, createdBy 포함)
     * @return 생성된 채팅방 정보(ChatRoom)를 포함하는 ResponseEntity (HTTP 상태 코드 200 OK)
     */
    @PostMapping // HTTP POST 요청을 "/api/chat/rooms" 경로에 매핑합니다.
    public ResponseEntity<ChatRoom> createRoom(
            @RequestBody ChatRoomDTO chatRoomDTO) { // 요청 본문을 ChatRoomDTO 객체로 변환하여 받습니다.
        // ChatRoomService를 호출하여 채팅방 생성 로직 수행
        ChatRoom createdRoom = chatRoomService.createRoom(chatRoomDTO);
        // 생성된 채팅방 정보를 담아 200 OK 응답 반환
        return ResponseEntity.ok(createdRoom);
    }

    /**
     * 전체 채팅방 목록을 조회합니다. (HTTP GET 요청 처리)
     *
     * @return 모든 채팅방 정보(List<ChatRoom>)를 포함하는 ResponseEntity (HTTP 상태 코드 200 OK)
     */
    @GetMapping // HTTP GET 요청을 "/api/chat/rooms" 경로에 매핑합니다.
    public ResponseEntity<List<ChatRoom>> getRooms() {
        // ChatRoomService를 호출하여 모든 채팅방 목록 조회
        List<ChatRoom> allRooms = chatRoomService.findAllRooms();
        // 조회된 목록을 담아 200 OK 응답 반환
        return ResponseEntity.ok(allRooms);
    }

    /**
     * 경로 변수로 전달된 ID를 사용하여 특정 채팅방의 정보를 조회합니다. (HTTP GET 요청 처리)
     *
     * @param roomId URL 경로에서 추출한 조회할 채팅방의 ID (예: /api/chat/rooms/abc-123)
     * @return 조회된 채팅방 정보(ChatRoom)와 200 OK 상태 코드, 또는 방이 없을 경우 404 Not Found 상태 코드를 포함하는 ResponseEntity
     */
    @GetMapping("/{roomId}") // HTTP GET 요청을 "/api/chat/rooms/{roomId}" 형태의 경로에 매핑합니다.
    public ResponseEntity<ChatRoom> getRoom(@PathVariable String roomId) { // 경로 변수 {roomId} 값을 파라미터로 받습니다.
        // ChatRoomService를 호출하여 ID로 채팅방 조회
        ChatRoom room = chatRoomService.findRoomById(roomId);
        if (room != null) {
            // 채팅방이 존재하면 해당 정보와 200 OK 응답 반환
            return ResponseEntity.ok(room);
        } else {
            // 채팅방이 존재하지 않으면 404 Not Found 응답 반환
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 경로 변수로 전달된 ID를 사용하여 특정 채팅방을 삭제합니다. (HTTP DELETE 요청 처리)
     *
     * @param roomId URL 경로에서 추출한 삭제할 채팅방의 ID
     * @return 삭제 성공 시 내용 없는(Void) 200 OK 상태 코드, 또는 대상 방이 없을 경우 404 Not Found 상태 코드를 포함하는 ResponseEntity
     */
    @DeleteMapping("/{roomId}") // HTTP DELETE 요청을 "/api/chat/rooms/{roomId}" 형태의 경로에 매핑합니다.
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) { // 경로 변수 {roomId} 값을 파라미터로 받습니다.
        // ChatRoomService를 호출하여 ID로 채팅방 삭제 시도
        boolean deleted = chatRoomService.deleteRoom(roomId);
        if (deleted) {
            // 삭제 성공 시 내용 없이 200 OK 응답 반환 (204 No Content도 일반적)
            return ResponseEntity.ok().build();
        } else {
            // 삭제할 방이 없거나 삭제 실패 시 404 Not Found 응답 반환
            return ResponseEntity.notFound().build();
        }
    }
}