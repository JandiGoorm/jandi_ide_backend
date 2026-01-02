package com.webproject.jandi_ide_backend.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import com.webproject.jandi_ide_backend.common.TestFixtures;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatRoomService 단위 테스트
 * 
 * 채팅방 CRUD 및 참여/퇴장 기능을 블랙박스 테스트 방식으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatRoomService 테스트")
class ChatRoomServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private User mockUser;
    private ChatRoom mockChatRoom;
    private ChatRoomDTO mockChatRoomDTO;

    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";

    @BeforeEach
    void setUp() {
        mockUser = TestFixtures.createMockUser();
        mockChatRoom = TestFixtures.createMockChatRoom();
        mockChatRoomDTO = TestFixtures.createMockChatRoomDTO();
        
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Nested
    @DisplayName("createRoom 메서드 테스트")
    class CreateRoomTest {

        /**
         * 유효한 입력으로 채팅방 생성 성공
         */
        @Test
        @DisplayName("유효한 입력으로 채팅방 생성 시 ChatRoom 반환")
        void createRoom_validInput_returnsChatRoom() {
            // Arrange
            doNothing().when(hashOperations).put(anyString(), anyString(), any());

            // Act
            ChatRoom result = chatRoomService.createRoom(mockChatRoomDTO);

            // Assert
            assertNotNull(result);
            assertEquals(mockChatRoomDTO.getName(), result.getName());
            assertEquals(mockChatRoomDTO.getDescription(), result.getDescription());
            assertEquals(mockChatRoomDTO.getRoomType(), result.getRoomType());
            assertNotNull(result.getRoomId());
            verify(hashOperations).put(eq(CHAT_ROOMS_KEY), anyString(), any(ChatRoom.class));
        }

        /**
         * 빈 username으로 채팅방 생성 시 예외 발생
         */
        @Test
        @DisplayName("빈 username으로 채팅방 생성 시 IllegalArgumentException 발생")
        void createRoom_emptyUsername_throwsException() {
            // Arrange
            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setName("Test Room");
            dto.setCreatedBy(""); // 빈 username
            dto.setRoomType(ChatRoom.RoomType.TECH_STACK);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> chatRoomService.createRoom(dto));
        }

        /**
         * null username으로 채팅방 생성 시 예외 발생
         */
        @Test
        @DisplayName("null username으로 채팅방 생성 시 IllegalArgumentException 발생")
        void createRoom_nullUsername_throwsException() {
            // Arrange
            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setName("Test Room");
            dto.setCreatedBy(null); // null username
            dto.setRoomType(ChatRoom.RoomType.TECH_STACK);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> chatRoomService.createRoom(dto));
        }
    }

    @Nested
    @DisplayName("findAllRooms 메서드 테스트")
    class FindAllRoomsTest {

        /**
         * 채팅방이 존재할 때 목록 반환
         */
        @Test
        @DisplayName("채팅방이 존재하면 목록 반환")
        void findAllRooms_roomsExist_returnsList() {
            // Arrange
            Map<Object, Object> roomMap = new HashMap<>();
            roomMap.put(mockChatRoom.getRoomId(), mockChatRoom);
            when(hashOperations.entries(CHAT_ROOMS_KEY)).thenReturn(roomMap);

            // Act
            List<ChatRoom> result = chatRoomService.findAllRooms();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        /**
         * 채팅방이 없을 때 빈 목록 반환
         */
        @Test
        @DisplayName("채팅방이 없으면 빈 목록 반환")
        void findAllRooms_noRooms_returnsEmptyList() {
            // Arrange
            when(hashOperations.entries(CHAT_ROOMS_KEY)).thenReturn(Collections.emptyMap());

            // Act
            List<ChatRoom> result = chatRoomService.findAllRooms();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findRoomById 메서드 테스트")
    class FindRoomByIdTest {

        /**
         * 존재하는 ID로 조회 시 ChatRoom 반환
         */
        @Test
        @DisplayName("존재하는 ID로 조회 시 ChatRoom 반환")
        void findRoomById_existingId_returnsChatRoom() {
            // Arrange
            when(hashOperations.get(CHAT_ROOMS_KEY, mockChatRoom.getRoomId())).thenReturn(mockChatRoom);

            // Act
            ChatRoom result = chatRoomService.findRoomById(mockChatRoom.getRoomId());

            // Assert
            assertNotNull(result);
            assertEquals(mockChatRoom.getRoomId(), result.getRoomId());
        }

        /**
         * 존재하지 않는 ID로 조회 시 null 반환
         */
        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 null 반환")
        void findRoomById_nonExistingId_returnsNull() {
            // Arrange
            when(hashOperations.get(CHAT_ROOMS_KEY, "non-existing-id")).thenReturn(null);

            // Act
            ChatRoom result = chatRoomService.findRoomById("non-existing-id");

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("findRoomsByType 메서드 테스트")
    class FindRoomsByTypeTest {

        /**
         * COMPANY 유형 채팅방 조회
         */
        @Test
        @DisplayName("COMPANY 유형 채팅방 조회 시 해당 유형만 반환")
        void findRoomsByType_company_returnsCompanyRooms() {
            // Arrange
            ChatRoom companyRoom = TestFixtures.createMockChatRoom();
            companyRoom.setRoomType(ChatRoom.RoomType.COMPANY);
            
            ChatRoom techRoom = TestFixtures.createMockChatRoom();
            techRoom.setRoomId("tech-room-id");
            techRoom.setRoomType(ChatRoom.RoomType.TECH_STACK);

            Map<Object, Object> roomMap = new HashMap<>();
            roomMap.put(companyRoom.getRoomId(), companyRoom);
            roomMap.put(techRoom.getRoomId(), techRoom);
            
            when(hashOperations.entries(CHAT_ROOMS_KEY)).thenReturn(roomMap);

            // Act
            List<ChatRoom> result = chatRoomService.findRoomsByType("COMPANY");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(ChatRoom.RoomType.COMPANY, result.get(0).getRoomType());
        }

        /**
         * TECH_STACK 유형 채팅방 조회
         */
        @Test
        @DisplayName("TECH_STACK 유형 채팅방 조회 시 해당 유형만 반환")
        void findRoomsByType_techStack_returnsTechStackRooms() {
            // Arrange
            ChatRoom techRoom = TestFixtures.createMockChatRoom();
            techRoom.setRoomType(ChatRoom.RoomType.TECH_STACK);

            Map<Object, Object> roomMap = new HashMap<>();
            roomMap.put(techRoom.getRoomId(), techRoom);
            
            when(hashOperations.entries(CHAT_ROOMS_KEY)).thenReturn(roomMap);

            // Act
            List<ChatRoom> result = chatRoomService.findRoomsByType("TECH_STACK");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(ChatRoom.RoomType.TECH_STACK, result.get(0).getRoomType());
        }

        /**
         * 잘못된 유형으로 조회 시 예외 발생
         */
        @Test
        @DisplayName("잘못된 유형으로 조회 시 IllegalArgumentException 발생")
        void findRoomsByType_invalidType_throwsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                    () -> chatRoomService.findRoomsByType("INVALID_TYPE"));
        }
    }

    @Nested
    @DisplayName("deleteRoom 메서드 테스트")
    class DeleteRoomTest {

        /**
         * 존재하는 채팅방 삭제 성공
         */
        @Test
        @DisplayName("존재하는 채팅방 삭제 시 true 반환")
        void deleteRoom_existingRoom_returnsTrue() {
            // Arrange
            when(hashOperations.delete(CHAT_ROOMS_KEY, mockChatRoom.getRoomId())).thenReturn(1L);

            // Act
            boolean result = chatRoomService.deleteRoom(mockChatRoom.getRoomId());

            // Assert
            assertTrue(result);
            verify(hashOperations).delete(CHAT_ROOMS_KEY, mockChatRoom.getRoomId());
        }

        /**
         * 존재하지 않는 채팅방 삭제 시 false 반환
         */
        @Test
        @DisplayName("존재하지 않는 채팅방 삭제 시 false 반환")
        void deleteRoom_nonExistingRoom_returnsFalse() {
            // Arrange
            when(hashOperations.delete(CHAT_ROOMS_KEY, "non-existing-id")).thenReturn(0L);

            // Act
            boolean result = chatRoomService.deleteRoom("non-existing-id");

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("addParticipant 메서드 테스트")
    class AddParticipantTest {

        /**
         * 채팅방 참여 성공
         */
        @Test
        @DisplayName("채팅방 참여 시 참여자 목록에 추가")
        void addParticipant_validRoom_addsParticipant() {
            // Arrange
            mockChatRoom.setParticipants(new HashSet<>());
            when(hashOperations.get(CHAT_ROOMS_KEY, mockChatRoom.getRoomId())).thenReturn(mockChatRoom);
            doNothing().when(hashOperations).put(anyString(), anyString(), any());

            // Act
            ChatRoom result = chatRoomService.addParticipant(mockChatRoom.getRoomId(), mockUser.getNickname());

            // Assert
            assertNotNull(result);
            assertTrue(result.getParticipants().contains(mockUser.getNickname()));
            verify(hashOperations).put(eq(CHAT_ROOMS_KEY), eq(mockChatRoom.getRoomId()), any(ChatRoom.class));
        }

        /**
         * 존재하지 않는 채팅방 참여 시 null 반환
         */
        @Test
        @DisplayName("존재하지 않는 채팅방 참여 시 null 반환")
        void addParticipant_nonExistingRoom_returnsNull() {
            // Arrange
            when(hashOperations.get(CHAT_ROOMS_KEY, "non-existing-id")).thenReturn(null);

            // Act
            ChatRoom result = chatRoomService.addParticipant("non-existing-id", mockUser.getNickname());

            // Assert
            assertNull(result);
        }

        /**
         * 이미 참여 중인 사용자 - 그대로 반환
         */
        @Test
        @DisplayName("이미 참여 중인 사용자일 경우 그대로 반환")
        void addParticipant_alreadyParticipating_returnsRoom() {
            // Arrange
            Set<String> participants = new HashSet<>();
            participants.add(mockUser.getNickname());
            mockChatRoom.setParticipants(participants);
            when(hashOperations.get(CHAT_ROOMS_KEY, mockChatRoom.getRoomId())).thenReturn(mockChatRoom);

            // Act
            ChatRoom result = chatRoomService.addParticipant(mockChatRoom.getRoomId(), mockUser.getNickname());

            // Assert
            assertNotNull(result);
            assertTrue(result.getParticipants().contains(mockUser.getNickname()));
            // put이 호출되지 않음
            verify(hashOperations, never()).put(anyString(), anyString(), any(ChatRoom.class));
        }
    }

    @Nested
    @DisplayName("removeParticipant 메서드 테스트")
    class RemoveParticipantTest {

        /**
         * 채팅방 퇴장 성공
         */
        @Test
        @DisplayName("채팅방 퇴장 시 참여자 목록에서 제거")
        void removeParticipant_validRoom_removesParticipant() {
            // Arrange
            Set<String> participants = new HashSet<>();
            participants.add(mockUser.getNickname());
            mockChatRoom.setParticipants(participants);
            when(hashOperations.get(CHAT_ROOMS_KEY, mockChatRoom.getRoomId())).thenReturn(mockChatRoom);
            doNothing().when(hashOperations).put(anyString(), anyString(), any());

            // Act
            ChatRoom result = chatRoomService.removeParticipant(mockChatRoom.getRoomId(), mockUser.getNickname());

            // Assert
            assertNotNull(result);
            assertFalse(result.getParticipants().contains(mockUser.getNickname()));
            verify(hashOperations).put(eq(CHAT_ROOMS_KEY), eq(mockChatRoom.getRoomId()), any(ChatRoom.class));
        }

        /**
         * 참여하지 않은 사용자 퇴장 - 그대로 반환
         */
        @Test
        @DisplayName("참여하지 않은 사용자 퇴장 시 그대로 반환")
        void removeParticipant_notParticipating_returnsRoom() {
            // Arrange
            mockChatRoom.setParticipants(new HashSet<>());
            when(hashOperations.get(CHAT_ROOMS_KEY, mockChatRoom.getRoomId())).thenReturn(mockChatRoom);

            // Act
            ChatRoom result = chatRoomService.removeParticipant(mockChatRoom.getRoomId(), mockUser.getNickname());

            // Assert
            assertNotNull(result);
            // put이 호출되지 않음
            verify(hashOperations, never()).put(anyString(), anyString(), any(ChatRoom.class));
        }
    }

    @Nested
    @DisplayName("validateTokenAndGetUser 메서드 테스트")
    class ValidateTokenTest {

        /**
         * 유효한 토큰으로 사용자 조회 성공
         */
        @Test
        @DisplayName("유효한 토큰으로 사용자 조회 시 User 반환")
        void validateTokenAndGetUser_validToken_returnsUser() {
            // Arrange
            String token = "Bearer valid-token";
            TokenInfo tokenInfo = new TokenInfo(mockUser.getGithubId(), "github-token");
            when(jwtTokenProvider.decodeToken("valid-token")).thenReturn(tokenInfo);
            when(userRepository.findByGithubId(mockUser.getGithubId())).thenReturn(Optional.of(mockUser));

            // Act
            User result = chatRoomService.validateTokenAndGetUser(token);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser.getGithubId(), result.getGithubId());
        }

        /**
         * Bearer 없는 토큰으로 조회 시 예외 발생
         */
        @Test
        @DisplayName("Bearer 없는 토큰으로 조회 시 CustomException 발생")
        void validateTokenAndGetUser_noBearerPrefix_throwsException() {
            // Arrange
            String token = "invalid-token-without-bearer";

            // Act & Assert
            assertThrows(CustomException.class, () -> chatRoomService.validateTokenAndGetUser(token));
        }

        /**
         * null 토큰으로 조회 시 예외 발생
         */
        @Test
        @DisplayName("null 토큰으로 조회 시 CustomException 발생")
        void validateTokenAndGetUser_nullToken_throwsException() {
            // Act & Assert
            assertThrows(CustomException.class, () -> chatRoomService.validateTokenAndGetUser(null));
        }
    }
}