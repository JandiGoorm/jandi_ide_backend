package com.webproject.jandi_ide_backend.user.service;

import com.webproject.jandi_ide_backend.common.TestFixtures;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.dto.UserResponseDTO;
import com.webproject.jandi_ide_backend.user.dto.UserUpdateDTO;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 * 
 * 사용자 인증 및 프로필 관리 기능을 테스트한다.
 * 동등 분할 기법을 적용하여 유효/무효 입력에 대한 동작을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = TestFixtures.createMockUser();
        // 빈 리스트로 초기화하여 NPE 방지
        mockUser.setUserTechStacks(new ArrayList<>());
        mockUser.setFavoriteCompanies(new ArrayList<>());
    }

    @Nested
    @DisplayName("getUser 메서드 테스트")
    class GetUserTest {

        /**
         * 존재하는 ID로 조회 성공
         */
        @Test
        @DisplayName("존재하는 ID로 조회 시 UserResponseDTO 반환")
        void getUser_existingId_returnsUserResponseDTO() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

            // Act
            UserResponseDTO result = userService.getUser(1);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser.getGithubId(), result.getGithubId());
        }

        /**
         * 존재하지 않는 ID로 조회 시 예외 발생
         */
        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 CustomException 발생")
        void getUser_nonExistingId_throwsException() {
            // Arrange
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> userService.getUser(999)
            );

            assertEquals(CustomErrorCodes.USER_NOT_FOUND, exception.getCustomErrorCode());
        }

        /**
         * 경계값 테스트 - ID 1 (최소 유효값)
         */
        @Test
        @DisplayName("ID 1(최소 유효값)으로 조회 시 UserResponseDTO 반환")
        void getUser_idOne_returnsUserResponseDTO() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

            // Act
            UserResponseDTO result = userService.getUser(1);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getUserById 메서드 테스트")
    class GetUserByIdTest {

        /**
         * 존재하는 ID로 조회 성공
         */
        @Test
        @DisplayName("존재하는 ID로 조회 시 User 반환")
        void getUserById_existingId_returnsUser() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

            // Act
            User result = userService.getUserById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser.getId(), result.getId());
        }

        /**
         * 존재하지 않는 ID로 조회 시 RuntimeException 발생
         */
        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 RuntimeException 발생")
        void getUserById_nonExistingId_throwsException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.getUserById(999L)
            );

            assertTrue(exception.getMessage().contains("User not found"));
        }
    }

    @Nested
    @DisplayName("getMe 메서드 테스트")
    class GetMeTest {

        /**
         * 유효한 토큰으로 내 정보 조회 성공
         */
        @Test
        @DisplayName("유효한 토큰으로 내 정보 조회 성공")
        void getMe_validToken_returnsUserResponseDTO() {
            // Arrange
            String token = "Bearer valid.jwt.token";
            TokenInfo tokenInfo = new TokenInfo("testUser", "githubAccessToken");
            when(jwtTokenProvider.decodeToken("valid.jwt.token")).thenReturn(tokenInfo);
            when(userRepository.findByGithubId("testUser")).thenReturn(Optional.of(mockUser));

            // Act
            UserResponseDTO result = userService.getMe(token);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser.getGithubId(), result.getGithubId());
        }

        /**
         * 존재하지 않는 사용자 토큰으로 조회 시 예외 발생
         */
        @Test
        @DisplayName("존재하지 않는 사용자 토큰으로 조회 시 CustomException 발생")
        void getMe_nonExistingUser_throwsException() {
            // Arrange
            String token = "Bearer valid.jwt.token";
            TokenInfo tokenInfo = new TokenInfo("unknownUser", "githubAccessToken");
            when(jwtTokenProvider.decodeToken("valid.jwt.token")).thenReturn(tokenInfo);
            when(userRepository.findByGithubId("unknownUser")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CustomException.class, () -> userService.getMe(token));
        }
    }

    @Nested
    @DisplayName("updateUser 메서드 테스트")
    class UpdateUserTest {

        /**
         * 유효한 데이터로 프로필 업데이트 성공
         */
        @Test
        @DisplayName("유효한 데이터로 프로필 업데이트 성공")
        void updateUser_validData_updatesSuccessfully() {
            // Arrange
            String token = "Bearer valid.jwt.token";
            Integer userId = 1;
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setNickname("Updated Name");
            updateDTO.setEmail("updated@test.com");
            updateDTO.setIntroduction("New intro");
            updateDTO.setProfileImage("https://new-image.url");

            TokenInfo tokenInfo = new TokenInfo("testUser", "githubAccessToken");
            when(jwtTokenProvider.decodeToken("valid.jwt.token")).thenReturn(tokenInfo);
            when(userRepository.findByIdAndGithubId(userId, "testUser")).thenReturn(Optional.of(mockUser));
            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            // Act
            UserResponseDTO result = userService.updateUser(token, userId, updateDTO);

            // Assert
            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }

        /**
         * 존재하지 않는 사용자 프로필 업데이트 시 예외
         */
        @Test
        @DisplayName("존재하지 않는 사용자 프로필 업데이트 시 예외 발생")
        void updateUser_nonExistingUser_throwsException() {
            // Arrange
            String token = "Bearer valid.jwt.token";
            Integer userId = 999;
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            
            TokenInfo tokenInfo = new TokenInfo("unknownUser", "githubAccessToken");
            when(jwtTokenProvider.decodeToken("valid.jwt.token")).thenReturn(tokenInfo);
            when(userRepository.findByIdAndGithubId(userId, "unknownUser")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CustomException.class, 
                    () -> userService.updateUser(token, userId, updateDTO));
        }
    }

    @Nested
    @DisplayName("deleteUser 메서드 테스트")
    class DeleteUserTest {

        /**
         * 유효한 사용자 삭제 성공
         */
        @Test
        @DisplayName("유효한 사용자 삭제 성공")
        void deleteUser_validUser_deletesSuccessfully() {
            // Arrange
            String token = "Bearer valid.jwt.token";
            Integer userId = 1;

            TokenInfo tokenInfo = new TokenInfo("testUser", "githubAccessToken");
            when(jwtTokenProvider.decodeToken("valid.jwt.token")).thenReturn(tokenInfo);
            when(userRepository.findByIdAndGithubId(userId, "testUser")).thenReturn(Optional.of(mockUser));
            doNothing().when(userRepository).delete(mockUser);

            // Act
            assertDoesNotThrow(() -> userService.deleteUser(token, userId));

            // Assert
            verify(userRepository).delete(mockUser);
        }

        /**
         * 존재하지 않는 사용자 삭제 시 예외
         */
        @Test
        @DisplayName("존재하지 않는 사용자 삭제 시 예외 발생")
        void deleteUser_nonExistingUser_throwsException() {
            // Arrange
            String token = "Bearer valid.jwt.token";
            Integer userId = 999;

            TokenInfo tokenInfo = new TokenInfo("testUser", "githubAccessToken");
            when(jwtTokenProvider.decodeToken("valid.jwt.token")).thenReturn(tokenInfo);
            when(userRepository.findByIdAndGithubId(userId, "testUser")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CustomException.class, 
                    () -> userService.deleteUser(token, userId));
        }
    }

    @Nested
    @DisplayName("validateGithubCode 메서드 테스트")
    class ValidateGithubCodeTest {

        /**
         * 유효한 코드 검증 성공
         */
        @Test
        @DisplayName("유효한 GitHub 코드로 검증 성공")
        void validateGithubCode_validCode_noException() {
            // Act & Assert
            assertDoesNotThrow(() -> userService.validateGithubCode("valid_code_123"));
        }

        /**
         * 동등 분할 - null 코드
         */
        @Test
        @DisplayName("null 코드로 검증 시 예외 발생")
        void validateGithubCode_nullCode_throwsException() {
            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> userService.validateGithubCode(null)
            );
            assertEquals(CustomErrorCodes.INVALID_GITHUB_CODE, exception.getCustomErrorCode());
        }

        /**
         * 동등 분할 - 빈 코드
         */
        @Test
        @DisplayName("빈 코드로 검증 시 예외 발생")
        void validateGithubCode_emptyCode_throwsException() {
            // Act & Assert
            assertThrows(CustomException.class, () -> userService.validateGithubCode(""));
        }

        /**
         * 동등 분할 - 공백만 있는 코드
         */
        @Test
        @DisplayName("공백만 있는 코드로 검증 시 예외 발생")
        void validateGithubCode_blankCode_throwsException() {
            // Act & Assert
            assertThrows(CustomException.class, () -> userService.validateGithubCode("   "));
        }
    }

    @Nested
    @DisplayName("validateRefreshToken 메서드 테스트")
    class ValidateRefreshTokenTest {

        /**
         * 유효한 리프레시 토큰 검증 성공
         */
        @Test
        @DisplayName("유효한 리프레시 토큰으로 검증 성공")
        void validateRefreshToken_validToken_noException() {
            // Act & Assert
            assertDoesNotThrow(() -> userService.validateRefreshToken("valid.refresh.token"));
        }

        /**
         * 동등 분할 - null 토큰
         */
        @Test
        @DisplayName("null 토큰으로 검증 시 예외 발생")
        void validateRefreshToken_nullToken_throwsException() {
            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> userService.validateRefreshToken(null)
            );
            assertEquals(CustomErrorCodes.INVALID_JWT_TOKEN, exception.getCustomErrorCode());
        }

        /**
         * 동등 분할 - 빈 토큰
         */
        @Test
        @DisplayName("빈 토큰으로 검증 시 예외 발생")
        void validateRefreshToken_emptyToken_throwsException() {
            // Act & Assert
            assertThrows(CustomException.class, () -> userService.validateRefreshToken(""));
        }
    }

    @Nested
    @DisplayName("사용자 역할 테스트")
    class UserRoleTest {

        /**
         * 일반 사용자 역할 확인
         */
        @Test
        @DisplayName("일반 사용자 역할이 USER인지 확인")
        void userRole_regularUser_hasUserRole() {
            // Arrange
            mockUser.setRole(User.UserRole.USER);

            // Assert
            assertEquals(User.UserRole.USER, mockUser.getRole());
        }

        /**
         * 관리자 역할 확인
         */
        @Test
        @DisplayName("관리자 역할이 ADMIN인지 확인")
        void userRole_admin_hasAdminRole() {
            // Arrange
            mockUser.setRole(User.UserRole.ADMIN);

            // Assert
            assertEquals(User.UserRole.ADMIN, mockUser.getRole());
        }

        /**
         * 스태프 역할 확인
         */
        @Test
        @DisplayName("스태프 역할이 STAFF인지 확인")
        void userRole_staff_hasStaffRole() {
            // Arrange
            mockUser.setRole(User.UserRole.STAFF);

            // Assert
            assertEquals(User.UserRole.STAFF, mockUser.getRole());
        }
    }
}
