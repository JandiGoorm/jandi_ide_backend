package com.webproject.jandi_ide_backend.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 채팅방(ChatRoom) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * Redis를 주 데이터 저장소로 사용하여 채팅방 정보를 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    // Redis에서 채팅방 목록 데이터를 저장하는 Hash의 키
    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";
    // Redis 작업을 위한 Template (Key: String, Value: Object - JSON 직렬화)
    private final RedisTemplate<String, Object> redisTemplate;
    // Redis에서 가져온 Object(주로 LinkedHashMap)를 ChatRoom DTO로 변환하기 위한 ObjectMapper
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 새로운 채팅방을 생성하고 Redis에 저장합니다.
     *
     * @param chatRoomDto 생성할 채팅방의 정보를 담은 DTO (name, description, createdBy 포함)
     * @return 생성된 채팅방 정보 객체 (ChatRoom)
     * @throws IllegalArgumentException 사용자 이름(createdBy)이 DTO에 없을 경우 발생
     */
    public ChatRoom createRoom(ChatRoomDTO chatRoomDto) {
        String username = chatRoomDto.getCreatedBy();
        if (!StringUtils.hasText(username)) {
            log.error("Username is required to create a chat room.");
            throw new IllegalArgumentException("Username cannot be empty when creating a room.");
        }

        // ChatRoom 객체 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(UUID.randomUUID().toString()) // 고유 ID 생성
                .name(chatRoomDto.getName())
                .description(chatRoomDto.getDescription())
                .createdBy(username)
                .createdAt(LocalDateTime.now().toString()) // 생성 시간 기록
                .roomType(chatRoomDto.getRoomType()) // 채팅방 유형 설정
                .build();

        // Redis Hash에 채팅방 정보 저장 (Key: CHAT_ROOMS_KEY, HashKey: roomId, Value: chatRoom 객체 -> JSON)
        try {
            redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, chatRoom.getRoomId(), chatRoom);
            log.info("Created and saved chat room to Redis: {}", chatRoom);
        } catch (Exception e) {
            log.error("Failed to save chat room {} to Redis: {}", chatRoom.getRoomId(), e.getMessage(), e);
            // 저장 실패 시 예외를 다시 던지거나, null 반환 또는 다른 오류 처리 로직 추가 가능
            throw new RuntimeException("Failed to save chat room to Redis", e);
        }
        return chatRoom;
    }

    /**
     * Redis에 저장된 모든 채팅방 목록을 조회합니다.
     * Redis에서 가져온 값들을 ChatRoom 객체로 변환하여 반환합니다.
     *
     * @return 모든 채팅방 정보가 담긴 List<ChatRoom>
     */
    public List<ChatRoom> findAllRooms() {
        log.debug("Attempting to find all chat rooms from Redis key: {}", CHAT_ROOMS_KEY);
        List<ChatRoom> chatRooms = new ArrayList<>();
        try {
            // Redis Hash의 모든 필드-값 쌍을 가져옴 (Map<roomId, chatRoomJson>)
            Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(CHAT_ROOMS_KEY);
            log.debug("Found {} raw entries in Redis hash.", rawEntries.size());

            if (rawEntries.isEmpty()) {
                log.debug("No chat rooms found in Redis.");
                return chatRooms; // 빈 리스트 반환
            }

            // 각 항목을 순회하며 ChatRoom 객체로 변환 시도
            for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
                String roomId = entry.getKey().toString(); // Hash 키는 String으로 가정
                Object rawValue = entry.getValue();

                if (rawValue == null) {
                    log.warn("Raw value for roomId {} is null. Skipping.", roomId);
                    continue;
                }

                try {
                    // ObjectMapper를 사용하여 Redis에서 가져온 값(Object)을 ChatRoom 클래스로 변환
                    ChatRoom room = objectMapper.convertValue(rawValue, ChatRoom.class);
                    if (room != null) {
                        // 변환 성공 시 리스트에 추가
                        chatRooms.add(room);
                        log.trace("Successfully converted entry for roomId {} to ChatRoom.", roomId); // 상세 로그는 Trace 레벨로
                    } else {
                        // 변환 결과가 null인 경우 (드문 경우)
                        log.warn("ObjectMapper conversion resulted in null for roomId {}", roomId);
                    }
                } catch (IllegalArgumentException e) {
                    // 변환 실패 시 (예: JSON 구조가 ChatRoom과 맞지 않음)
                    log.error("Failed to convert raw value to ChatRoom for roomId {}. Value type: [{}], Value: {}",
                            roomId, rawValue.getClass().getName(), rawValue.toString().substring(0, Math.min(rawValue.toString().length(), 100)), e); // 로그 길이를 제한하여 출력
                } catch (Exception e) {
                    // 기타 예외 처리
                    log.error("Unexpected error converting raw value for roomId {}: {}", roomId, e.getMessage(), e);
                }
            }
            log.info("Successfully processed {} chat rooms.", chatRooms.size());
            return chatRooms;

        } catch (Exception e) {
            // Redis 접근 자체에서 오류 발생 시
            log.error("FATAL: Failed to retrieve or process chat rooms from Redis key {}: {}", CHAT_ROOMS_KEY, e.getMessage(), e);
            // 서비스 사용자에게 예외를 전파
            throw new RuntimeException("Error fetching chat rooms from Redis", e);
        }
    }

    /**
     * 특정 ID에 해당하는 채팅방 정보를 Redis에서 조회합니다.
     *
     * @param roomId 조회할 채팅방의 ID
     * @return 조회된 채팅방 정보 객체 (ChatRoom), 없거나 변환 실패 시 null 반환
     */
    public ChatRoom findRoomById(String roomId) {
        log.debug("Attempting to find chat room by ID: {}", roomId);
        try {
            // Redis Hash에서 특정 roomId에 해당하는 값을 가져옴
            Object rawValue = redisTemplate.opsForHash().get(CHAT_ROOMS_KEY, roomId);
            if (rawValue == null) {
                log.debug("Chat room not found in Redis for ID: {}", roomId);
                return null; // 방이 없으면 null 반환
            }

            // ObjectMapper를 사용하여 가져온 값(Object)을 ChatRoom 클래스로 변환
            ChatRoom room = objectMapper.convertValue(rawValue, ChatRoom.class);
            log.debug("Successfully found and converted chat room for ID: {}", roomId);
            return room;

        } catch (IllegalArgumentException e) {
            // 변환 실패 시
            log.error("Failed to convert raw value to ChatRoom for roomId {}. Value retrieved: {}",
                    roomId, redisTemplate.opsForHash().get(CHAT_ROOMS_KEY, roomId), e);
            return null;
        } catch (Exception e) {
            // Redis 접근 오류 등 기타 예외
            log.error("Error finding chat room by ID {} from Redis: {}", roomId, e.getMessage(), e);
            throw new RuntimeException("Error finding chat room from Redis", e); // 또는 null 반환
        }
    }

    /**
     * 특정 ID에 해당하는 채팅방을 Redis에서 삭제합니다.
     *
     * @param roomId 삭제할 채팅방의 ID
     * @return 삭제 성공 시 true, 대상이 없거나 실패 시 false
     */
    public boolean deleteRoom(String roomId) {
        log.debug("Attempting to delete chat room by ID: {}", roomId);
        try {
            // Redis Hash에서 특정 roomId 필드를 삭제하고, 삭제된 필드 개수(1 또는 0)를 반환받음
            Long deletedCount = redisTemplate.opsForHash().delete(CHAT_ROOMS_KEY, roomId);
            boolean success = deletedCount != null && deletedCount == 1;
            if (success) {
                log.info("Successfully deleted chat room with ID: {}", roomId);
            } else {
                log.warn("Chat room with ID {} not found or not deleted.", roomId);
            }
            return success;
        } catch (Exception e) {
            log.error("Error deleting chat room with ID {} from Redis: {}", roomId, e.getMessage(), e);
            // 삭제 실패 시 false 반환 또는 예외 처리
            return false;
        }
    }

    /**
     * 채팅방에 참여자를 추가합니다.
     *
     * @param roomId 참여할 채팅방의 ID
     * @param username 참여하는 사용자 이름
     * @return 업데이트된 채팅방 정보 객체 (ChatRoom), 없거나 변환 실패 시 null 반환
     */
    public ChatRoom addParticipant(String roomId, String username) {
        log.debug("Attempting to add participant {} to chat room {}", username, roomId);
        try {
            // 채팅방 정보 조회
            ChatRoom room = findRoomById(roomId);
            if (room == null) {
                log.warn("Chat room not found for ID: {}", roomId);
                return null;
            }

            // 이미 참여중인 경우 그대로 반환
            if (room.getParticipants().contains(username)) {
                log.debug("User {} is already a participant in room {}", username, roomId);
                return room;
            }

            // 참여자 추가
            room.getParticipants().add(username);
            
            // Redis에 업데이트
            redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, roomId, room);
            log.info("Successfully added participant {} to chat room {}", username, roomId);
            
            return room;
        } catch (Exception e) {
            log.error("Error adding participant {} to chat room {}: {}", username, roomId, e.getMessage(), e);
            throw new RuntimeException("Error adding participant to chat room", e);
        }
    }

    /**
     * 채팅방에서 참여자를 제거합니다.
     *
     * @param roomId 나갈 채팅방의 ID
     * @param username 나가는 사용자 이름
     * @return 업데이트된 채팅방 정보 객체 (ChatRoom), 없거나 변환 실패 시 null 반환
     */
    public ChatRoom removeParticipant(String roomId, String username) {
        log.debug("Attempting to remove participant {} from chat room {}", username, roomId);
        try {
            // 채팅방 정보 조회
            ChatRoom room = findRoomById(roomId);
            if (room == null) {
                log.warn("Chat room not found for ID: {}", roomId);
                return null;
            }

            // 참여하지 않은 경우 그대로 반환
            if (!room.getParticipants().contains(username)) {
                log.debug("User {} is not a participant in room {}", username, roomId);
                return room;
            }

            // 참여자 제거
            room.getParticipants().remove(username);
            
            // Redis에 업데이트
            redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, roomId, room);
            log.info("Successfully removed participant {} from chat room {}", username, roomId);
            
            return room;
        } catch (Exception e) {
            log.error("Error removing participant {} from chat room {}: {}", username, roomId, e.getMessage(), e);
            throw new RuntimeException("Error removing participant from chat room", e);
        }
    }

    /**
     * JWT 토큰을 검증하고 사용자 정보를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 정보
     * @throws CustomException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    public User validateTokenAndGetUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "");
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);

        // 사용자 정보 확인
        return userRepository.findByGithubId(tokenInfo.getGithubId())
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));
    }

    /**
     * 특정 유형의 채팅방 목록을 조회합니다.
     *
     * @param roomType 조회할 채팅방 유형
     * @return 특정 유형의 채팅방 목록
     */
    public List<ChatRoom> findRoomsByType(String roomType) {
        try {
            ChatRoom.RoomType type = ChatRoom.RoomType.valueOf(roomType.toUpperCase());
            List<ChatRoom> allRooms = findAllRooms();
            return allRooms.stream()
                    .filter(room -> room.getRoomType() == type)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Invalid room type: {}", roomType, e);
            throw new IllegalArgumentException("Invalid room type: " + roomType);
        }
    }
}