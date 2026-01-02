package com.webproject.jandi_ide_backend.common;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.chat.dto.ChatRoomDTO;
import com.webproject.jandi_ide_backend.chat.entity.ChatRoom;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.dto.SaveSolutionDto;
import com.webproject.jandi_ide_backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 테스트에 사용되는 공통 픽스처 클래스
 * 
 * 테스트 데이터 생성을 위한 팩토리 메서드를 제공한다.
 */
public class TestFixtures {

    // ==================== User ====================
    
    public static User createMockUser() {
        User user = new User();
        user.setId(1);
        user.setGithubId("testuser");
        user.setNickname("Test User");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.USER);
        user.setProfileImage("https://example.com/profile.jpg");
        return user;
    }
    
    public static User createMockUserWithRole(User.UserRole role) {
        User user = createMockUser();
        user.setRole(role);
        return user;
    }
    
    public static User createMockAdmin() {
        return createMockUserWithRole(User.UserRole.ADMIN);
    }
    
    public static User createMockStaff() {
        return createMockUserWithRole(User.UserRole.STAFF);
    }

    // ==================== Problem ====================
    
    public static Problem createMockProblem() {
        Problem problem = new Problem();
        problem.setId(1);
        problem.setTitle("두 수의 합");
        problem.setDescription("두 정수 A와 B를 입력받은 다음, A+B를 출력하는 프로그램을 작성하시오.");
        problem.setTimeLimit(1);
        problem.setMemory(128);
        problem.setLevel(1);
        return problem;
    }
    
    public static Problem createMockProblemWithLevel(int level) {
        Problem problem = createMockProblem();
        problem.setLevel(level);
        return problem;
    }

    // ==================== TestCase ====================
    
    public static TestCase createMockTestCase() {
        TestCase testCase = new TestCase();
        testCase.setId(1);
        testCase.setInput("10 20");
        testCase.setOutput("30");
        return testCase;
    }
    
    public static List<TestCase> createMockTestCases() {
        List<TestCase> testCases = new ArrayList<>();
        
        TestCase tc1 = new TestCase();
        tc1.setId(1);
        tc1.setInput("1 2");
        tc1.setOutput("3");
        testCases.add(tc1);
        
        TestCase tc2 = new TestCase();
        tc2.setId(2);
        tc2.setInput("5 7");
        tc2.setOutput("12");
        testCases.add(tc2);
        
        return testCases;
    }

    // ==================== ProblemSet ====================
    
    public static ProblemSet createMockProblemSet() {
        ProblemSet problemSet = new ProblemSet();
        problemSet.setId(1L);
        problemSet.setTitle("테스트 문제집");
        problemSet.setSolvingTimeInMinutes(60);
        problemSet.setIsPrevious(false);
        problemSet.setUser(createMockUser());
        return problemSet;
    }

    // ==================== Solution ====================
    
    public static Solution createMockSolution() {
        Solution solution = new Solution();
        solution.setId(1L);
        solution.setUser(createMockUser());
        solution.setProblemId(1);
        solution.setCode(createValidJavaCode());
        solution.setLanguage("java");
        solution.setIsCorrect(true);
        solution.setStatus(Solution.SolutionStatus.CORRECT);
        solution.setExecutionTime(100);
        solution.setMemoryUsage(15);
        solution.setSolvingTime(60);
        return solution;
    }

    // ==================== CodeSubmissionDto ====================
    
    public static CodeSubmissionDto createValidSubmissionDto() {
        return CodeSubmissionDto.builder()
                .userId(1L)
                .problemId(1L)
                .code(createValidJavaCode())
                .language("java")
                .solvingTime(60)
                .build();
    }
    
    public static CodeSubmissionDto createTestModeSubmissionDto() {
        return CodeSubmissionDto.builder()
                .userId(1L)
                .problemId(0L)  // 테스트 모드
                .code(createValidJavaCode())
                .language("java")
                .solvingTime(0)
                .build();
    }
    
    public static SaveSolutionDto createValidSaveSolutionDto() {
        return SaveSolutionDto.builder()
                .userId(1L)
                .problemId(1)  // Integer type
                .problemSetId(1L)
                .code(createValidJavaCode())
                .language("java")
                .solvingTime(60)
                .build();
    }

    // ==================== Code Samples ====================
    
    public static String createValidJavaCode() {
        return """
            import java.util.Scanner;
            
            public class Main {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    int a = sc.nextInt();
                    int b = sc.nextInt();
                    System.out.println(a + b);
                }
            }
            """;
    }
    
    public static String createCompilationErrorJavaCode() {
        return """
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello")  // 세미콜론 누락
                }
            }
            """;
    }
    
    public static String createRuntimeErrorJavaCode() {
        return """
            public class Main {
                public static void main(String[] args) {
                    int[] arr = new int[1];
                    System.out.println(arr[10]);  // ArrayIndexOutOfBoundsException
                }
            }
            """;
    }
    
    public static String createInfiniteLoopJavaCode() {
        return """
            public class Main {
                public static void main(String[] args) {
                    while(true) { }  // 무한 루프
                }
            }
            """;
    }
    
    public static String createValidPythonCode() {
        return "a, b = map(int, input().split())\nprint(a + b)";
    }
    
    public static String createCompilationErrorPythonCode() {
        return "print('Hello'  # 괄호 누락";
    }
    
    public static String createValidCppCode() {
        return """
            #include <iostream>
            using namespace std;
            
            int main() {
                int a, b;
                cin >> a >> b;
                cout << a + b << endl;
                return 0;
            }
            """;
    }
    
    public static String createCompilationErrorCppCode() {
        return """
            #include <iostream>
            int main() {
                cout << "Hello"  // 세미콜론 누락
                return 0;
            }
            """;
    }

    // ==================== ChatRoom ====================
    
    public static ChatRoom createMockChatRoom() {
        return ChatRoom.builder()
                .roomId("test-room-id")
                .name("테스트 채팅방")
                .description("테스트용 채팅방입니다")
                .roomType(ChatRoom.RoomType.TECH_STACK)
                .createdBy("testuser")
                .createdAt(LocalDateTime.now().toString())
                .participants(new HashSet<>())
                .build();
    }
    
    public static ChatRoomDTO createMockChatRoomDTO() {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setName("테스트 채팅방");
        dto.setDescription("테스트용 채팅방입니다");
        dto.setRoomType(ChatRoom.RoomType.TECH_STACK);
        dto.setCreatedBy("testuser");
        return dto;
    }

    // ==================== Boundary Values ====================
    
    /**
     * 코드 크기 경계값 테스트용 - 최소 유효 값 (1 바이트)
     */
    public static String createMinValidCode() {
        return "a";
    }
    
    /**
     * 코드 크기 경계값 테스트용 - 최대 유효 값 (50,000 바이트)
     */
    public static String createMaxValidCode() {
        return "a".repeat(50000);
    }
    
    /**
     * 코드 크기 경계값 테스트용 - 최소 무효 값 (50,001 바이트)
     */
    public static String createMinInvalidCode() {
        return "a".repeat(50001);
    }
}
