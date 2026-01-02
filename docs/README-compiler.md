# 코드 컴파일러 API 명세서

## 개요

자디(JANDI) IDE 백엔드의 코드 컴파일러 API는 사용자가 제출한 코드를 컴파일하고 실행하여 결과를 반환하는 기능을 제공합니다. 현재 Java, Python, C++ 언어를 지원합니다.

## API 엔드포인트

### 1. 코드 컴파일 및 실행 API

```
POST /api/compiler/compile
```

사용자가 작성한 코드를 제출하여 컴파일 및 실행 결과를 확인할 수 있습니다. 이 API는 솔루션을 데이터베이스에 저장하지 않고 실행 결과만 반환합니다.

#### 요청 헤더

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| Authorization | String | O | Bearer {access_token} |
| Content-Type | String | O | application/json |

#### 요청 본문 (Request Body)

```json
{
  "userId": 1,
  "problemId": 0,
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "language": "java",
  "solvingTime": 0
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Long | O | 코드를 제출한 사용자의 ID |
| problemId | Long | O | 풀이한 문제의 ID (특별히 0을 입력하면 테스트 모드로 동작) |
| problemSetId | Long | X | 문제가 속한 문제집 ID |
| code | String | O | 제출한 코드 내용 |
| language | String | O | 프로그래밍 언어 (java, python, c++) |
| solvingTime | Integer | X | 문제 풀이에 소요된 시간 (초 단위) |

#### 응답 (정상적으로 실행된 경우)

```json
{
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "language": "java",
  "result": "Hello, World!",
  "compilationOutput": "자바 코드 컴파일 시작...\n컴파일 성공. 실행 시작...\n\n실행 결과:\nHello, World!\n\n테스트 완료: 코드가 정상적으로 실행되었습니다.",
  "executionTime": 125,
  "memoryUsage": 15,
  "status": "CORRECT"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| code | String | 제출한 코드 내용 |
| language | String | 프로그래밍 언어 |
| result | String | 코드 실행 결과 (출력 내용) |
| compilationOutput | String | 컴파일 및 실행 과정의 상세 출력 |
| executionTime | Integer | 실행 시간 (ms) |
| memoryUsage | Integer | 메모리 사용량 (MB) |
| status | String | 실행 결과 상태 (CORRECT, WRONG_ANSWER, COMPILATION_ERROR, RUNTIME_ERROR, TIMEOUT, MEMORY_LIMIT) |

### 2. 솔루션 저장 API

```
POST /api/compiler/save-solution
```

사용자가 작성한 코드와 실행 결과를 솔루션으로 데이터베이스에 저장합니다.

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
  "problemSetId": 1,
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "language": "java",
  "solvingTime": 300
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Long | O | 코드를 제출한 사용자의 ID |
| problemId | Long | O | 풀이한 문제의 ID |
| problemSetId | Long | X | 문제가 속한 문제집 ID |
| code | String | O | 제출한 코드 내용 |
| language | String | O | 프로그래밍 언어 (java, python, c++) |
| solvingTime | Integer | X | 문제 풀이에 소요된 시간 (초 단위) |

#### 응답 (정상적으로 저장된 경우)

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "nickname": "user1",
    "email": "user1@example.com"
  },
  "problemId": 100,
  "problemSetId": 1,
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
| problemSetId | Integer | 문제집 ID |
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

오류가 발생했을 때는 다음 형식으로 응답이 반환됩니다:

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

| 필드 | 타입 | 설명 |
|------|------|------|
| status | Integer | HTTP 상태 코드 |
| error | String | 오류 종류 |
| message | String | 오류 메시지 |
| timestamp | String | 오류 발생 시간 |
| errorType | String | 오류 유형 (COMPILATION_ERROR, RUNTIME_ERROR, TIMEOUT, MEMORY_LIMIT, WRONG_ANSWER 등) |
| errorDetails | String | 오류에 대한 상세 정보 |
| code | String | 제출된 코드 |
| language | String | 프로그래밍 언어 |

## 실행 결과 상태

컴파일러 API는 다음과 같은 실행 결과 상태를 반환합니다:

| 상태 | 설명 |
|------|------|
| SUBMITTED | 제출됨 (테스트 케이스 실행 전) |
| EVALUATING | 테스트 케이스 평가 중 |
| CORRECT | 테스트 케이스 통과 |
| WRONG_ANSWER | 테스트 케이스 실패 (결과가 기대 출력과 다름) |
| RUNTIME_ERROR | 런타임 에러 발생 |
| COMPILATION_ERROR | 컴파일 에러 발생 |
| TIMEOUT | 시간 초과 발생 |
| MEMORY_LIMIT | 메모리 사용 제한 초과 |

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

## 예제

### 예제 1: 테스트 모드로 Java 코드 컴파일 (성공)

#### 요청

```
POST /api/compiler/compile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 0,
  "code": "import java.util.Scanner;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int a = sc.nextInt();\n    int b = sc.nextInt();\n    System.out.println(a + b);\n  }\n}",
  "language": "java",
  "solvingTime": 0
}
```

#### 응답

```json
{
  "code": "import java.util.Scanner;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int a = sc.nextInt();\n    int b = sc.nextInt();\n    System.out.println(a + b);\n  }\n}",
  "language": "java",
  "result": "30",
  "compilationOutput": "자바 코드 컴파일 시작...\n컴파일 성공. 실행 시작...\n\n실행 결과:\n30\n\n테스트 완료: 코드가 정상적으로 실행되었습니다.",
  "executionTime": 125,
  "memoryUsage": 15,
  "status": "CORRECT"
}
```

### 예제 2: 컴파일 오류가 있는 Java 코드

#### 요청

```
POST /api/compiler/compile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 0,
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n  }\n}",
  "language": "java",
  "solvingTime": 0
}
```

#### 응답

```json
{
  "status": 400,
  "error": "Compilation Failed",
  "message": "자바 컴파일 에러가 발생했습니다",
  "timestamp": "2023-08-15T14:35:12",
  "errorType": "COMPILATION_ERROR",
  "errorDetails": "자바 코드 컴파일 시작...\n컴파일 에러 발생:\nMain.java:3: error: ';' expected\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n                                    ^\n1 error\n\n일반적인 컴파일 오류 원인:\n  - 세미콜론(;) 누락\n  - 괄호 불일치\n  - 변수 또는 메소드 이름 오타\n  - 타입 불일치\n\n컴파일 에러 발생: 코드를 확인해 주세요.\n세미콜론 누락, 괄호 불일치, 메서드 이름 오타 등이 흔한 원인입니다.",
  "code": "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\")  // 세미콜론 누락\n  }\n}",
  "language": "java"
}
```

### 예제 3: 문제 ID와 함께 솔루션 저장

#### 요청

```
POST /api/compiler/save-solution
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 100,
  "problemSetId": 1,
  "code": "import java.util.Scanner;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int a = sc.nextInt();\n    int b = sc.nextInt();\n    System.out.println(a + b);\n  }\n}",
  "language": "java",
  "solvingTime": 300
}
```

#### 응답 (성공)

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "nickname": "user1",
    "email": "user1@example.com"
  },
  "problemId": 100,
  "problemSetId": 1,
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

### 예제 4: Python 코드 컴파일

#### 요청

```
POST /api/compiler/compile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 0,
  "code": "a, b = map(int, input().split())\nprint(a + b)",
  "language": "python",
  "solvingTime": 0
}
```

#### 응답

```json
{
  "code": "a, b = map(int, input().split())\nprint(a + b)",
  "language": "python",
  "result": "30",
  "compilationOutput": "파이썬 코드 실행 시작...\n\n실행 결과:\n30\n\n테스트 완료: 코드가 정상적으로 실행되었습니다.",
  "executionTime": 72,
  "memoryUsage": 8,
  "status": "CORRECT"
}
```

### 예제 5: C++ 코드 컴파일

#### 요청

```
POST /api/compiler/compile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "userId": 1,
  "problemId": 0,
  "code": "#include <iostream>\nusing namespace std;\n\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << a + b << endl;\n    return 0;\n}",
  "language": "c++",
  "solvingTime": 0
}
```

#### 응답

```json
{
  "code": "#include <iostream>\nusing namespace std;\n\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << a + b << endl;\n    return 0;\n}",
  "language": "c++",
  "result": "30",
  "compilationOutput": "C++ 코드 컴파일 시작...\n컴파일 성공. 실행 시작...\n\n실행 결과:\n30\n\n테스트 완료: 코드가 정상적으로 실행되었습니다.",
  "executionTime": 62,
  "memoryUsage": 5,
  "status": "CORRECT"
}
```

## 참고사항

1. 각 문제의 테스트 케이스는 여러 개일 수 있으며, 모든 테스트 케이스를 통과해야 정답으로 처리됩니다.
2. 실행 시간과 메모리 사용량은 테스트 케이스 중 최대값이 기록됩니다.
3. 코드 실행 환경은 다음과 같은 제한이 있습니다:
   - 시간 제한: 문제별로 설정된 제한 시간 (기본 2초)
   - 메모리 제한: 문제별로 설정된 메모리 제한 (기본 256MB)
4. 테스트 모드에서는 상세한 오류 메시지와 함께 문제 해결을 위한 팁을 제공합니다.
5. 컴파일 및 실행 과정에서 발생할 수 있는 보안 문제를 방지하기 위해 코드 실행은 격리된 환경에서 이루어집니다.
6. 모든 API 요청에는 유효한 JWT 토큰이 필요합니다. 