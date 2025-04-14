package com.webproject.jandi_ide_backend.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper 필드 추가 (final이어야 RequiredArgsConstructor가 처리)

    // 채팅방 생성 - 파라미터 변경
    public ChatRoom createRoom(ChatRoomDTO chatRoomDto) {
        // DTO에서 사용자 이름 가져오기
        String username = chatRoomDto.getCreatedBy();
        // 사용자 이름 유효성 검사 (선택 사항)
        if (!StringUtils.hasText(username)) {
            log.error("Username is required to create a chat room.");
            // 예외를 던지거나 null을 반환하는 등의 처리 필요
            throw new IllegalArgumentException("Username cannot be empty when creating a room.");
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .name(chatRoomDto.getName())
                .description(chatRoomDto.getDescription())
                .createdBy(username) // DTO에서 가져온 이름 사용
                .createdAt(LocalDateTime.now().toString())
                .build();

        redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, chatRoom.getRoomId(), chatRoom);
        log.info("Created chat room: {}", chatRoom);
        return chatRoom;
    }

    // 전체 채팅방 조회
    public List<ChatRoom> findAllRooms() {
        log.debug("Attempting to find all chat rooms from Redis key: {}", CHAT_ROOMS_KEY);
        List<ChatRoom> chatRooms = new ArrayList<>();
        try {
            // HGETALL 과 유사하게 Map<Object, Object> 형태로 가져오기
            Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(CHAT_ROOMS_KEY);
            log.debug("Found {} raw entries in Redis hash.", rawEntries.size());

            if (rawEntries.isEmpty()) {
                log.debug("No chat rooms found in Redis.");
                return chatRooms; // 빈 리스트 반환
            }

            for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
                String roomId = "Unknown"; // 기본값
                Object rawValue = entry.getValue();
                try {
                    // ObjectMapper를 사용하여 rawValue (아마도 LinkedHashMap)를 ChatRoom 객체로 변환
                    ChatRoom room = objectMapper.convertValue(rawValue, ChatRoom.class);
                    if (room != null) {
                        chatRooms.add(room);
                        log.debug("Successfully converted entry for roomId {} to ChatRoom.", roomId);
                    } else {
                        log.warn("ObjectMapper conversion resulted in null for roomId {}", roomId);
                    }
                } catch (IllegalArgumentException e) {
                    // 변환 중 오류 발생 시 (예: 필드 불일치)
                    log.error("Failed to convert raw value to ChatRoom for roomId {}. Value type: [{}], Value: {}",
                            roomId, rawValue.getClass().getName(), rawValue.toString(), e);
                }
            }
            log.info("Successfully processed {} chat rooms.", chatRooms.size()); // 정보 레벨로 변경
            return chatRooms;

        } catch (Exception e) {
            // Redis 접근 자체 또는 전체 처리 중 예외 발생 시 로깅
            log.error("FATAL: Failed to retrieve or process chat rooms from Redis key {}: {}", CHAT_ROOMS_KEY, e.getMessage(), e);
            // 예외를 다시 던져서 ControllerAdvice 등에서 처리하거나, 빈 리스트/null 반환 고려
            // 여기서 RuntimeException을 던지면 보통 500 Internal Server Error가 됩니다.
            // 400 Bad Request가 계속 반환된다면 다른 원인(ControllerAdvice 등)일 가능성이 높습니다.
            throw new RuntimeException("Error fetching chat rooms from Redis", e);
        }
    }

    // 특정 채팅방 조회
    public ChatRoom findRoomById(String roomId) {
        return (ChatRoom) redisTemplate.opsForHash().get(CHAT_ROOMS_KEY, roomId);
    }

    // 채팅방 삭제
    public boolean deleteRoom(String roomId) {
        return redisTemplate.opsForHash().delete(CHAT_ROOMS_KEY, roomId) == 1;
    }
}
