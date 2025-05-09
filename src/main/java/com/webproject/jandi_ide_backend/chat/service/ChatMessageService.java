package com.webproject.jandi_ide_backend.chat.service;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatMessage;
import com.webproject.jandi_ide_backend.chat.repository.ChatMessageRepository;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 메시지 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * MongoDB를 활용하여 채팅 메시지 저장 및 조회 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 채팅 메시지를 MongoDB에 저장합니다.
     *
     * @param messageDTO 저장할 채팅 메시지 DTO
     * @return 저장된 채팅 메시지 엔티티
     */
    public ChatMessage saveMessage(ChatMessageDTO messageDTO) {
        try {
            log.debug("채팅 메시지 저장 시도: {}", messageDTO);
            
            // 타임스탬프가 없는 경우 현재 시간으로 설정
            if (messageDTO.getTimestamp() == null || messageDTO.getTimestamp().isEmpty()) {
                messageDTO.setTimestamp(LocalDateTime.now().toString());
            }
            
            // DTO를 엔티티로 변환하여 저장
            ChatMessage chatMessage = convertToChatMessage(messageDTO);
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            
            log.info("채팅 메시지 저장 완료: {}", savedMessage);
            return savedMessage;
        } catch (Exception e) {
            log.error("채팅 메시지 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("채팅 메시지 저장 중 오류 발생", e);
        }
    }

    /**
     * 채팅 메시지 DTO를 엔티티로 변환합니다.
     *
     * @param messageDTO 변환할 DTO
     * @return 변환된 엔티티
     */
    private ChatMessage convertToChatMessage(ChatMessageDTO messageDTO) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(messageDTO.getRoomId());
        chatMessage.setType(messageDTO.getType());
        chatMessage.setSender(messageDTO.getSender());
        chatMessage.setMessage(messageDTO.getMessage());
        chatMessage.setProfileImage(messageDTO.getProfileImage());
        
        // 타임스탬프 문자열을 LocalDateTime으로 변환
        try {
            chatMessage.setTimestamp(LocalDateTime.parse(messageDTO.getTimestamp()));
        } catch (DateTimeParseException e) {
            log.warn("타임스탬프 형식이 올바르지 않아 현재 시간으로 설정: {}", messageDTO.getTimestamp());
            chatMessage.setTimestamp(LocalDateTime.now());
        }
        
        return chatMessage;
    }

    /**
     * 특정 채팅방의 모든 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 해당 채팅방의 모든 메시지 목록
     */
    public List<ChatMessageDTO> getMessagesByRoomId(String roomId) {
        log.debug("방 ID로 채팅 메시지 조회: {}", roomId);
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);
        return convertToChatMessageDTOList(messages);
    }

    /**
     * 특정 채팅방의 메시지를 페이징 처리하여 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param pageable 페이징 정보
     * @return 페이징 처리된 메시지 목록
     */
    public Page<ChatMessageDTO> getMessagesByRoomIdPaged(String roomId, Pageable pageable) {
        log.debug("방 ID로 채팅 메시지 페이징 조회: {}, {}", roomId, pageable);
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomId(roomId, pageable);
        return messagePage.map(this::convertToChatMessageDTO);
    }

    /**
     * 특정 채팅방의 특정 시간 이후 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param timestamp 기준 시간
     * @return 기준 시간 이후의 메시지 목록
     */
    public List<ChatMessageDTO> getMessagesByRoomIdAfterTimestamp(String roomId, LocalDateTime timestamp) {
        log.debug("방 ID와 시간 이후로 채팅 메시지 조회: {}, {}", roomId, timestamp);
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdAndTimestampAfter(roomId, timestamp);
        return convertToChatMessageDTOList(messages);
    }

    /**
     * 특정 사용자가 보낸 메시지를 조회합니다.
     *
     * @param sender 발신자
     * @return 해당 사용자가 보낸 메시지 목록
     */
    public List<ChatMessageDTO> getMessagesBySender(String sender) {
        log.debug("발신자로 채팅 메시지 조회: {}", sender);
        List<ChatMessage> messages = chatMessageRepository.findBySender(sender);
        return convertToChatMessageDTOList(messages);
    }

    /**
     * 메시지 내용에 특정 키워드가 포함된 메시지를 조회합니다.
     *
     * @param keyword 검색할 키워드
     * @return 키워드를 포함한 메시지 목록
     */
    public List<ChatMessageDTO> searchMessagesByKeyword(String keyword) {
        log.debug("키워드로 채팅 메시지 검색: {}", keyword);
        List<ChatMessage> messages = chatMessageRepository.findByMessageContaining(keyword);
        return convertToChatMessageDTOList(messages);
    }

    /**
     * 채팅 메시지 엔티티 목록을 DTO 목록으로 변환합니다.
     *
     * @param messages 변환할 엔티티 목록
     * @return 변환된 DTO 목록
     */
    private List<ChatMessageDTO> convertToChatMessageDTOList(List<ChatMessage> messages) {
        return messages.stream()
                .map(this::convertToChatMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * 채팅 메시지 엔티티를 DTO로 변환합니다.
     *
     * @param message 변환할 엔티티
     * @return 변환된 DTO
     */
    private ChatMessageDTO convertToChatMessageDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setRoomId(message.getRoomId());
        dto.setType(message.getType());
        dto.setSender(message.getSender());
        dto.setMessage(message.getMessage());
        dto.setTimestamp(message.getTimestamp().toString());
        dto.setProfileImage(message.getProfileImage());
        return dto;
    }

    /**
     * WebSocket 메시지를 처리하고 관련 사용자 정보를 설정합니다.
     *
     * @param message 처리할 메시지 DTO
     * @param headerAccessor WebSocket 헤더 정보
     * @return 처리된 메시지 DTO, 유효하지 않은 사용자일 경우 null
     */
    public ChatMessageDTO processMessage(ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        // 현재 인증된 사용자 정보 확인
        Principal user = headerAccessor.getUser();
        if (user == null) {
            log.warn("인증되지 않은 사용자의 메시지 - 무시됨: {}", message);
            return null;
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
                // 사용자의 프로필 이미지 설정
                message.setProfileImage(userEntity.getProfileImage());
            }
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패, 기본 sender 사용: {}", e.getMessage());
            // 기존 sender 유지
        }
        
        // 메시지에 현재 시간 추가
        message.setTimestamp(LocalDateTime.now().toString());
        
        return message;
    }

    /**
     * 특정 채팅방의 특정 타입 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param type 메시지 타입
     * @return 해당 채팅방의 특정 타입 메시지 목록
     */
    public List<ChatMessageDTO> getMessagesByRoomIdAndType(String roomId, ChatMessageDTO.MessageType type) {
        log.debug("방 ID와 타입으로 채팅 메시지 조회: {}, {}", roomId, type);
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdAndType(roomId, type);
        return convertToChatMessageDTOList(messages);
    }

    /**
     * 특정 채팅방의 특정 타입 메시지를 페이징 처리하여 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param type 메시지 타입
     * @param pageable 페이징 정보
     * @return 페이징 처리된 특정 타입 메시지 목록
     */
    public Page<ChatMessageDTO> getMessagesByRoomIdAndTypePaged(String roomId, ChatMessageDTO.MessageType type, Pageable pageable) {
        log.debug("방 ID와 타입으로 채팅 메시지 페이징 조회: {}, {}, {}", roomId, type, pageable);
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomIdAndType(roomId, type, pageable);
        return messagePage.map(this::convertToChatMessageDTO);
    }
} 