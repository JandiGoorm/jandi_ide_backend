package com.webproject.jandi_ide_backend.chat.repository;

import com.webproject.jandi_ide_backend.chat.entity.ChatMessage;
import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 엔티티를 MongoDB에 저장하고 조회하기 위한 리포지토리 인터페이스입니다.
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 특정 채팅방의 모든 메시지를 조회합니다.
     * @param roomId 채팅방 ID
     * @return 해당 채팅방의 모든 메시지 목록
     */
    List<ChatMessage> findByRoomId(String roomId);

    /**
     * 특정 채팅방의 메시지를 페이징 처리하여 조회합니다.
     * @param roomId 채팅방 ID
     * @param pageable 페이징 정보
     * @return 페이징 처리된 메시지 목록
     */
    Page<ChatMessage> findByRoomId(String roomId, Pageable pageable);

    /**
     * 특정 채팅방의 특정 시간 이후 메시지를 조회합니다.
     * @param roomId 채팅방 ID
     * @param timestamp 기준 시간
     * @return 기준 시간 이후의 메시지 목록
     */
    List<ChatMessage> findByRoomIdAndTimestampAfter(String roomId, LocalDateTime timestamp);

    /**
     * 특정 사용자가 보낸 메시지를 조회합니다.
     * @param sender 발신자
     * @return 해당 사용자가 보낸 메시지 목록
     */
    List<ChatMessage> findBySender(String sender);

    /**
     * 메시지 내용에 특정 키워드가 포함된 메시지를 조회합니다.
     * @param keyword 검색할 키워드
     * @return 키워드를 포함한 메시지 목록
     */
    List<ChatMessage> findByMessageContaining(String keyword);

    /**
     * 특정 채팅방의 특정 타입 메시지를 조회합니다.
     * @param roomId 채팅방 ID
     * @param type 메시지 타입
     * @return 해당 채팅방의 특정 타입 메시지 목록
     */
    List<ChatMessage> findByRoomIdAndType(String roomId, ChatMessageDTO.MessageType type);

    /**
     * 특정 채팅방의 특정 타입 메시지를 페이징 처리하여 조회합니다.
     * @param roomId 채팅방 ID
     * @param type 메시지 타입
     * @param pageable 페이징 정보
     * @return 페이징 처리된 특정 타입 메시지 목록
     */
    Page<ChatMessage> findByRoomIdAndType(String roomId, ChatMessageDTO.MessageType type, Pageable pageable);
} 