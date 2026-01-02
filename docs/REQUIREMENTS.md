# JANDI IDE 요구 사항 명세서

## 개요

본 문서는 JANDI IDE Backend 프로젝트의 기능 요구 사항을 정의한다. 실제 구현된 API를 기준으로 작성되었으며, 테스트 케이스 도출에 필요한 명세를 포함한다.

---

## 1. 사용자 인증 (User)

### 1.1 GitHub OAuth 로그인

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| AUTH-001 | POST | /api/users/login | GitHub OAuth code로 로그인 후 JWT 발급 |
| AUTH-002 | POST | /api/users/refresh | 리프레시 토큰으로 새 JWT 발급 |
| AUTH-003 | GET | /api/users/me | 내 정보 조회 |

**입력 (AuthRequestDTO):**

| 필드 | 타입 | 필수 | 설명 |
|-----|------|-----|------|
| code | String | O | GitHub OAuth 인가 코드 |

**출력 (AuthResponseDTO):**

| 필드 | 타입 | 설명 |
|-----|------|------|
| accessToken | String | JWT 액세스 토큰 |
| refreshToken | String | 리프레시 토큰 |
| user | UserResponseDTO | 사용자 정보 |

### 1.2 사용자 관리

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| USER-001 | GET | /api/users/{id} | 특정 사용자 정보 조회 |
| USER-002 | PUT | /api/users/{id} | 사용자 정보 수정 |
| USER-003 | DELETE | /api/users/{id} | 회원 탈퇴 |
| USER-004 | GET | /api/users/{id}/repos | GitHub 레포지토리 목록 조회 |
| USER-005 | GET | /api/users/{id}/projects | 대표 프로젝트 목록 조회 (페이징) |

---

## 2. 컴파일러 (Compiler)

### 2.1 코드 컴파일 및 실행

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| COMP-001 | POST | /api/compiler/compile | 코드 컴파일 및 실행 |
| COMP-002 | POST | /api/compiler/save-solution | 솔루션 저장 |

**입력 (CodeSubmissionDto):**

| 필드 | 타입 | 필수 | 제약 조건 |
|-----|------|-----|----------|
| userId | Long | O | 양수 (@Positive) |
| problemId | Long | O | 0 = 테스트 모드, 양수 = 문제 풀이 |
| problemSetId | Long | X | 양수 |
| code | String | O | 1 ~ 50,000 바이트 (@Size) |
| language | String | O | java, python, c++ (@Pattern) |
| solvingTime | Integer | X | 0 이상 |

**출력 상태 (SolutionStatus):**

| 상태 | 설명 |
|-----|------|
| CORRECT | 모든 테스트 케이스 통과 |
| WRONG_ANSWER | 출력 불일치 |
| COMPILATION_ERROR | 컴파일 실패 |
| RUNTIME_ERROR | 런타임 오류 |
| TIMEOUT | 시간 초과 |
| MEMORY_LIMIT | 메모리 초과 |

### 2.2 테스트 모드 (problemId=0)

- 문제 존재 여부 확인하지 않음
- 테스트 케이스 실행하지 않음
- 기본 입력 "10 20"으로 실행
- 컴파일/실행 가능 여부만 확인

---

## 3. 알고리즘 문제 (Problem)

### 3.1 문제 조회

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| PROB-001 | GET | /api/problems | 문제 목록 조회 (페이징, 정렬) |
| PROB-002 | GET | /api/problems/{id} | 문제 상세 조회 (테스트 케이스 포함) |
| PROB-003 | POST | /api/problems | 문제 추가 (STAFF 이상) |
| PROB-004 | PUT | /api/problems/{id} | 문제 수정 (STAFF 이상) |
| PROB-005 | DELETE | /api/problems/{id} | 문제 삭제 (STAFF 이상) |

**쿼리 파라미터:**

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|-----|-------|------|
| page | int | 0 | 페이지 번호 |
| size | int | 10 | 페이지 크기 |
| sort | String | - | 정렬 필드 (level) |
| direction | String | asc | 정렬 방향 (asc, desc) |

### 3.2 테스트 케이스

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| TC-001 | POST | /api/problems/{problemId}/testcases | 테스트 케이스 추가 (STAFF 이상) |
| TC-002 | PUT | /api/problems/{problemId}/testcases/{id} | 테스트 케이스 수정 (STAFF 이상) |
| TC-003 | DELETE | /api/problems/{problemId}/testcases/{id} | 테스트 케이스 삭제 (STAFF 이상) |

---

## 4. 문제집 (ProblemSet)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| PSET-001 | POST | /api/problem-set | 문제집 생성 |
| PSET-002 | GET | /api/problem-set | 문제집 목록 조회 (페이징) |
| PSET-003 | GET | /api/problem-set/{id} | 문제집 상세 조회 |
| PSET-004 | PUT | /api/problem-set/{id} | 문제집 수정 |
| PSET-005 | DELETE | /api/problem-set/{id} | 문제집 삭제 |

---

## 5. 솔루션 (Solution)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| SOL-001 | GET | /api/solutions/successful/{problemId} | 성공한 풀이 목록 조회 |
| SOL-002 | GET | /api/solutions/user/{userId}/problem-set/{problemSetId} | 사용자의 문제집 풀이 조회 |
| SOL-003 | GET | /api/solutions/user/{userId}/problem/{problemId} | 사용자의 문제 풀이 목록 조회 |

---

## 6. 채팅 (Chat)

### 6.1 채팅방 관리

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| CHAT-001 | POST | /api/chat/rooms | 채팅방 생성 (ADMIN) |
| CHAT-002 | GET | /api/chat/rooms | 모든 채팅방 조회 |
| CHAT-003 | GET | /api/chat/rooms/type/{type} | 유형별 채팅방 조회 |
| CHAT-004 | GET | /api/chat/rooms/{roomId} | 채팅방 상세 조회 |
| CHAT-005 | DELETE | /api/chat/rooms/{roomId} | 채팅방 삭제 (ADMIN) |
| CHAT-006 | POST | /api/chat/rooms/{roomId}/join | 채팅방 참여 |
| CHAT-007 | POST | /api/chat/rooms/{roomId}/leave | 채팅방 퇴장 |
| CHAT-008 | GET | /api/chat/rooms/{roomId}/participants | 참여자 목록 조회 |

**채팅방 유형 (RoomType):**

| 유형 | 설명 |
|-----|------|
| COMPANY | 기업별 채팅방 |
| TECH_STACK | 기술 스택별 채팅방 |

### 6.2 채팅 메시지

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| MSG-001 | GET | /api/chat/rooms/{roomId}/messages | 메시지 전체 조회 |
| MSG-002 | GET | /api/chat/rooms/{roomId}/messages/paged | 메시지 페이징 조회 |
| MSG-003 | GET | /api/chat/rooms/{roomId}/messages/paged/type | 타입별 메시지 조회 |
| MSG-004 | GET | /api/chat/rooms/{roomId}/messages/after | 특정 시간 이후 메시지 |
| MSG-005 | GET | /api/chat/messages/user/{sender} | 사용자별 메시지 조회 |
| MSG-006 | GET | /api/chat/messages/search | 키워드 검색 |

**메시지 타입 (MessageType):**

| 타입 | 설명 |
|-----|------|
| ENTER | 입장 메시지 |
| TALK | 일반 대화 |
| LEAVE | 퇴장 메시지 |

### 6.3 WebSocket

| Endpoint | 설명 |
|----------|------|
| /ws/chat | WebSocket 연결 (SockJS) |
| /app/chat/message | 메시지 발행 |
| /topic/chat/room/{roomId} | 메시지 구독 |

---

## 7. 프로젝트 (Project)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| PROJ-001 | POST | /api/projects | 대표 프로젝트 추가 |
| PROJ-002 | GET | /api/projects/{id} | 프로젝트 상세 조회 |
| PROJ-003 | PUT | /api/projects/{id} | 프로젝트 수정 |
| PROJ-004 | DELETE | /api/projects/{id} | 프로젝트 삭제 |

---

## 8. 기업 (Company)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| COMP-001 | GET | /api/companies | 전체 기업 목록 조회 |
| COMP-002 | GET | /api/companies/{id} | 기업 상세 조회 |
| COMP-003 | POST | /api/companies | 기업 추가 (STAFF 이상) |
| COMP-004 | PUT | /api/companies/{id} | 기업 수정 (STAFF 이상) |
| COMP-005 | DELETE | /api/companies/{id} | 기업 삭제 (STAFF 이상) |

---

## 9. 관심 기업 (Favorite Company)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| FAV-001 | GET | /api/companies/favorite | 즐겨찾는 기업 목록 조회 |
| FAV-002 | POST | /api/companies/favorite | 즐겨찾는 기업 목록 설정 |
| FAV-003 | PUT | /api/companies/favorite/{companyId} | 즐겨찾는 기업 추가 |
| FAV-004 | DELETE | /api/companies/favorite/{companyId} | 즐겨찾는 기업 삭제 |

---

## 10. 기술 스택 (Tech Stack)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| TECH-001 | GET | /api/tech-stack | 전체 기술 스택 조회 |
| TECH-002 | GET | /api/tech-stack/favorite | 즐겨찾는 기술 스택 조회 |
| TECH-003 | PUT | /api/tech-stack/favorite | 즐겨찾는 기술 스택 갱신 |

---

## 11. 채용 공고 (Job Posting)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| JOB-001 | POST | /api/companies/{companyId}/job-postings | 채용 공고 추가 (STAFF 이상) |
| JOB-002 | PUT | /api/job-postings/{id} | 채용 공고 수정 (STAFF 이상) |
| JOB-003 | DELETE | /api/job-postings/{id} | 채용 공고 삭제 (STAFF 이상) |
| JOB-004 | POST | /api/job-postings/{id}/schedule | 공고 일정 추가 (STAFF 이상) |

---

## 사용자 역할 (Role)

| 역할 | 설명 | 권한 |
|-----|------|-----|
| USER | 일반 사용자 | 기본 기능 사용 |
| STAFF | 스태프 | 문제/기업/채용공고 관리 |
| ADMIN | 관리자 | 모든 기능 + 채팅방 관리 |

---

## 에러 코드

| 코드 | 설명 |
|-----|------|
| USER_NOT_FOUND | 사용자를 찾을 수 없음 |
| PROBLEM_NOT_FOUND | 문제를 찾을 수 없음 |
| COMPANY_NOT_FOUND | 기업을 찾을 수 없음 |
| PERMISSION_DENIED | 권한 없음 |
| INVALID_TOKEN | 유효하지 않은 토큰 |
| COMPILATION_ERROR | 컴파일 오류 |
| RUNTIME_ERROR | 런타임 오류 |

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-01-02 | 1.0 | 초기 문서 작성 |
| 2026-01-02 | 1.1 | 실제 코드베이스 기반으로 재작성 |
