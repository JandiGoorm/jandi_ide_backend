# 코드 컴파일러 API 명세서

## 개요

자디(JANDI) IDE 백엔드의 코드 컴파일러 API는 사용자가 제출한 코드를 컴파일하고 실행하여 결과를 반환하는 기능을 제공합니다. 현재 Java, Python, C++ 언어를 지원합니다.

## API 엔드포인트

### 코드 제출 API

```
POST /api/compiler/submit
```

사용자가 작성한 코드를 제출하여 컴파일 및 실행 결과를 확인할 수 있습니다.

#### 요청 헤더

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| Authorization | String | O | Bearer {access_token} |
| Content-Type | String | O | application/json |

#### 요청 본문 (Request Body)

```json
{
  "userId": 1,
  "problemId": 100,
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "language": "java",
  "solvingTime": 300
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Long | O | 코드를 제출한 사용자의 ID |
| problemId | Long | O | 풀이한 문제의 ID (특별히 0을 입력하면 테스트 모드로 동작) |
| code | String | O | 제출한 코드 내용 |
| language | String | O | 프로그래밍 언어 (java, python, c++) |
| solvingTime | Integer | X | 문제 풀이에 소요된 시간 (초 단위) |

#### 응답 (Response)

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "nickname": "user1",
    "email": "user1@example.com"
  },
  "problemId": 100,
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "language": "java",
  "solvingTime": 300,
  "isCorrect": true,
  "additionalInfo": "테스트 1: CORRECT\n실행 시간: 124.5ms\n메모리 사용량: 15.2MB\n\n",
  "status": "CORRECT",
  "memoryUsage": 15,
  "executionTime": 125,
  "createdAt": "2023-08-15T14:30:45",
  "updatedAt": "2023-08-15T14:30:45"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 저장된 솔루션의 고유 ID |
| user | Object | 사용자 정보 객체 |
| problemId | Integer | 문제 ID |
| code | String | 제출한 코드 내용 |
| language | String | 프로그래밍 언어 |
| solvingTime | Integer | 문제 풀이에 소요된 시간 (초 단위) |
| isCorrect | Boolean | 정답 여부 |
| additionalInfo | String | 테스트 케이스별 실행 결과 상세 정보 |
| status | String | 솔루션 상태 (CORRECT, WRONG_ANSWER, COMPILATION_ERROR, RUNTIME_ERROR, TIMEOUT, MEMORY_LIMIT) |
| memoryUsage | Integer | 최대 메모리 사용량 (MB) |
| executionTime | Integer | 최대 실행 시간 (ms) |
| createdAt | String | 생성 일시 |
| updatedAt | String | 업데이트 일시 |

## 상태 코드

| 상태 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 500 | 서버 오류 |

## 오류 응답

```json
{
  "timestamp": "2023-08-15T14:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "지원하지 않는 언어입니다: javascript",
  "path": "/api/compiler/submit"
}
```

### 컴파일러 오류 응답 (CompilerErrorResponseDto)

컴파일 실패나 런타임 오류가 발생했을 때 다음과 같은 형식으로 상세 정보를 반환합니다:

```json
{
  "status": 400,
  "error": "Compilation Failed",
  "message": "자바 컴파일 에러가 발생했습니다",
  "timestamp": "2023-08-15T14:30:45",
  "errorType": "COMPILATION_ERROR",
  "errorDetails": "컴파일 에러 발생:\nMain.java:3: error: ';' expected\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n                                    ^\n1 error\n\n일반적인 컴파일 오류 원인:\n  - 세미콜론(;) 누락\n  - 괄호 불일치\n  - 변수 또는 메소드 이름 오타\n  - 타입 불일치",
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n  }\n}",
  "language": "java"
}
```

## 지원 언어

현재 컴파일러가 지원하는 언어 목록:

1. **Java**
   - `language` 필드에 "java" 사용
   - 클래스 이름은 반드시 "Main"이어야 함

2. **Python**
   - `language` 필드에 "python" 사용
   - Python 3 기준으로 실행됨

3. **C++**
   - `language` 필드에 "c++" 사용
   - C++11 표준 지원

## 테스트 모드 (problemId=0)

요청 시 `problemId`를 0으로 설정하면 테스트 모드가 활성화됩니다. 테스트 모드에서는:

1. 문제 존재 여부를 확인하지 않습니다.
2. 테스트 케이스를 실행하지 않습니다.
3. 코드가 컴파일되고 간단한 입력("10 20")으로 실행 가능한지만 확인합니다.
4. 결과에는 컴파일 및 실행 여부와 실행 결과가 포함됩니다.
5. 오류 발생 시 상세한 오류 메시지와 함께 문제 해결 팁을 제공합니다.

이 모드는 사용자가 코드가 기본적으로 동작하는지 빠르게 확인하고 싶을 때 유용합니다.

### 테스트 모드 요청 예제

```
POST /api/compiler/submit
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 0,
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "language": "java",
  "solvingTime": 0
}
```

### 테스트 모드 응답 예제 (성공)

```json
{
  "id": 42,
  "user": {
    "id": 1,
    "nickname": "user1",
    "email": "user1@example.com"
  },
  "problemId": 0,
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "language": "java",
  "solvingTime": 0,
  "isCorrect": true,
  "additionalInfo": "자바 코드 컴파일 시작...\n컴파일 성공. 실행 시작...\n\n실행 결과:\nHello, World!\n\n테스트 완료: 코드가 정상적으로 실행되었습니다.\n",
  "status": "CORRECT",
  "memoryUsage": 0,
  "executionTime": 0,
  "createdAt": "2023-08-15T15:45:12",
  "updatedAt": "2023-08-15T15:45:12"
}
```

### 테스트 모드 응답 예제 (컴파일 오류)

```json
{
  "status": 400,
  "error": "Compilation Failed",
  "message": "자바 컴파일 에러가 발생했습니다",
  "timestamp": "2023-08-15T14:35:12",
  "errorType": "COMPILATION_ERROR",
  "errorDetails": "자바 코드 컴파일 시작...\n컴파일 에러 발생:\nMain.java:3: error: ';' expected\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n                                    ^\n1 error\n\n일반적인 컴파일 오류 원인:\n  - 세미콜론(;) 누락\n  - 괄호 불일치\n  - 변수 또는 메소드 이름 오타\n  - 타입 불일치\n\n컴파일 에러 발생: 코드를 확인해 주세요.\n세미콜론 누락, 괄호 불일치, 메서드 이름 오타 등이 흔한 원인입니다.\n",
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n  }\n}",
  "language": "java"
}
```

## 예제

### 요청 예제 (Java)

```
POST /api/compiler/submit
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 100,
  "code": "import java.util.Scanner;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int a = sc.nextInt();\n    int b = sc.nextInt();\n    System.out.println(a + b);\n  }\n}",
  "language": "java",
  "solvingTime": 300
}
```

### 응답 예제 (성공)

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "nickname": "user1",
    "email": "user1@example.com"
  },
  "problemId": 100,
  "code": "import java.util.Scanner;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int a = sc.nextInt();\n    int b = sc.nextInt();\n    System.out.println(a + b);\n  }\n}",
  "language": "java",
  "solvingTime": 300,
  "isCorrect": true,
  "additionalInfo": "테스트 1: CORRECT\n실행 시간: 124.5ms\n메모리 사용량: 15.2MB\n\n테스트 케이스 #1 통과!",
  "status": "CORRECT",
  "memoryUsage": 15,
  "executionTime": 125,
  "createdAt": "2023-08-15T14:30:45",
  "updatedAt": "2023-08-15T14:30:45"
}
```

### 요청 예제 (Python)

```
POST /api/compiler/submit
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 100,
  "code": "a, b = map(int, input().split())\nprint(a + b)",
  "language": "python",
  "solvingTime": 120
}
```

### 응답 예제 (오답)

```json
{
  "status": 400,
  "error": "Compilation Failed",
  "message": "틀린 답안입니다",
  "timestamp": "2023-08-15T14:38:22",
  "errorType": "WRONG_ANSWER",
  "errorDetails": "테스트 케이스 1:\n- 기대 출력: 30\n- 실제 출력: 35\n\n출력 형식과 타입을 확인해 보세요. 공백이나 줄바꿈에 주의하세요.",
  "code": "a, b = map(int, input().split())\nprint(a + b + 5)",
  "language": "python"
}
```

### 응답 예제 (컴파일 오류)

```json
{
  "status": 400,
  "error": "Compilation Failed",
  "message": "컴파일 에러가 발생했습니다",
  "timestamp": "2023-08-15T14:35:12",
  "errorType": "COMPILATION_ERROR",
  "errorDetails": "컴파일 에러 발생:\nMain.java:3: error: ';' expected\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n                                    ^\n1 error\n\n일반적인 컴파일 오류 원인:\n  - 세미콜론(;) 누락\n  - 괄호 불일치\n  - 변수 또는 메소드 이름 오타\n  - 타입 불일치",
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n  }\n}",
  "language": "java"
}
```

### 응답 예제 (런타임 오류)

```json
{
  "status": 400,
  "error": "Compilation Failed",
  "message": "런타임 에러가 발생했습니다",
  "timestamp": "2023-08-15T14:39:45",
  "errorType": "RUNTIME_ERROR",
  "errorDetails": "런타임 오류 발생: java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 3\n\n배열 인덱스 범위, null 참조, 형변환 오류 등을 확인해 보세요.",
  "code": "public class Main {\n  public static void main(String[] args) {\n    int[] array = new int[3];\n    System.out.println(array[5]);\n  }\n}",
  "language": "java"
}
```

## 참고사항

1. 각 문제의 테스트 케이스는 여러 개일 수 있으며, 모든 테스트 케이스를 통과해야 정답으로 처리됩니다.
2. 실행 시간과 메모리 사용량은 테스트 케이스 중 최대값이 기록됩니다.
3. 코드 실행 환경은 다음과 같은 제한이 있습니다:
   - 시간 제한: 문제별로 설정된 제한 시간 (기본 2초)
   - 메모리 제한: 문제별로 설정된 메모리 제한 (기본 256MB)
4. 코드 실행 중 발생 가능한 상태:
   - `CORRECT`: 모든 테스트 케이스 통과
   - `WRONG_ANSWER`: 하나 이상의 테스트 케이스에서 출력 결과가 다름
   - `COMPILATION_ERROR`: 컴파일 오류 발생
   - `RUNTIME_ERROR`: 실행 중 오류 발생 (예: 배열 인덱스 범위 초과, 널 포인터 등)
   - `TIMEOUT`: 실행 시간 초과
   - `MEMORY_LIMIT`: 메모리 사용량 초과 
5. 내부 구현에서는 테스트 케이스별 결과 상태로 다음 값들이 사용됩니다:
   - `CORRECT`: 테스트 케이스 통과
   - `WRONG_ANSWER`: 테스트 케이스 실패 (출력 결과가 다른 경우)
   - `COMPILATION_ERROR`: 컴파일 오류 발생
   - `RUNTIME_ERROR`: 실행 중 오류 발생
   - `TIMEOUT`: 시간 초과 발생
   - `MEMORY_LIMIT`: 메모리 제한 초과 발생
6. 테스트 모드에서는 상세한 오류 메시지와 함께 문제 해결을 위한 팁을 제공합니다. 