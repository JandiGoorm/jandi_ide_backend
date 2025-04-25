# 채팅방 REST API 명세서

## 개요

이 문서는 채팅방 관리를 위한 REST API 엔드포인트를 설명합니다. 모든 API는 인증이 필요하며, JWT 토큰을 Authorization 헤더에 포함해야 합니다.

기본 URL: `https://ide-be.yeonjae.kr/api/chat/rooms`

## 채팅방 유형

채팅방은 다음과 같은 유형으로 분류됩니다:

- `COMPANY`: 기업 관련 채팅방
- `TECH_STACK`: 기술 스택 관련 채팅방

## 인증 요구사항

모든 API 호출에는 다음 헤더가 필요합니다:
```
Authorization: Bearer {jwt-token}
```

## 채팅방 관리 API

### 1. 채팅방 생성

새로운 채팅방을 생성합니다. ADMIN 권한이 필요합니다.

- **URL**: `/`
- **Method**: `POST`
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
  - `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "name": "채팅방 이름",
    "description": "채팅방 설명 (선택 사항)",
    "roomType": "COMPANY" // 또는 "TECH_STACK"
  }
  ```
- **Response**: 생성된 채팅방 정보
  ```json
  {
    "roomId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "개발팀 채팅방",
    "description": "개발팀 일반 대화를 위한 채팅방입니다",
    "createdBy": "홍길동",
    "createdAt": "2023-06-01T12:00:00",
    "roomType": "COMPANY",
    "participants": []
  }
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음 (ADMIN만 가능)

### 2. 모든 채팅방 목록 조회

모든 채팅방 목록을 조회합니다.

- **URL**: `/`
- **Method**: `GET`
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 채팅방 목록
  ```json
  [
    {
      "roomId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "개발팀 채팅방",
      "description": "개발팀 일반 대화를 위한 채팅방입니다",
      "createdBy": "홍길동",
      "createdAt": "2023-06-01T12:00:00",
      "roomType": "COMPANY",
      "participants": ["user1", "user2"]
    },
    ...
  ]
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `401 Unauthorized`: 인증 실패

### 3. 채팅방 유형별 목록 조회

특정 유형(COMPANY 또는 TECH_STACK)에 해당하는 채팅방 목록을 조회합니다.

- **URL**: `/type/{roomType}`
- **Method**: `GET`
- **Path Parameters**:
  - `roomType`: 채팅방 유형 (`COMPANY` 또는 `TECH_STACK`)
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 필터링된 채팅방 목록
  ```json
  [
    {
      "roomId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "개발팀 채팅방",
      "description": "개발팀 일반 대화를 위한 채팅방입니다",
      "createdBy": "홍길동",
      "createdAt": "2023-06-01T12:00:00",
      "roomType": "COMPANY",
      "participants": ["user1", "user2"]
    },
    ...
  ]
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `400 Bad Request`: 잘못된 채팅방 유형
  - `401 Unauthorized`: 인증 실패

### 4. 특정 채팅방 조회

특정 ID의 채팅방을 조회합니다.

- **URL**: `/{roomId}`
- **Method**: `GET`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 채팅방 정보
  ```json
  {
    "roomId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "개발팀 채팅방",
    "description": "개발팀 일반 대화를 위한 채팅방입니다",
    "createdBy": "홍길동",
    "createdAt": "2023-06-01T12:00:00",
    "roomType": "COMPANY",
    "participants": ["user1", "user2"]
  }
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 채팅방 없음

### 5. 채팅방 참여자 목록 조회

특정 채팅방의 참여자 목록을 조회합니다.

- **URL**: `/{roomId}/participants`
- **Method**: `GET`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 참여자 목록
  ```json
  ["홍길동", "김철수", "이영희"]
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 채팅방 없음

### 6. 채팅방 삭제

특정 ID의 채팅방을 삭제합니다. ADMIN 권한이 필요합니다.

- **URL**: `/{roomId}`
- **Method**: `DELETE`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Status Codes**:
  - `200 OK`: 성공
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음 (ADMIN만 가능)
  - `404 Not Found`: 채팅방 없음

### 7. 채팅방 참여

채팅방에 참여합니다.

- **URL**: `/{roomId}/join`
- **Method**: `POST`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 업데이트된 채팅방 정보
  ```json
  {
    "roomId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "개발팀 채팅방",
    "description": "개발팀 일반 대화를 위한 채팅방입니다",
    "createdBy": "홍길동",
    "createdAt": "2023-06-01T12:00:00",
    "roomType": "COMPANY",
    "participants": ["user1", "user2", "신규참여자"]
  }
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 채팅방 없음

### 8. 채팅방 나가기

채팅방에서 나갑니다.

- **URL**: `/{roomId}/leave`
- **Method**: `POST`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 업데이트된 채팅방 정보
  ```json
  {
    "roomId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "개발팀 채팅방",
    "description": "개발팀 일반 대화를 위한 채팅방입니다",
    "createdBy": "홍길동",
    "createdAt": "2023-06-01T12:00:00",
    "roomType": "COMPANY",
    "participants": ["user1"]
  }
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 채팅방 없음

## 채팅 메시지 관리 API

### 1. 채팅방 메시지 목록 조회

특정 채팅방의 모든 메시지를 조회합니다.

- **URL**: `/rooms/{roomId}/messages`
- **Method**: `GET`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 메시지 목록
  ```json
  [
    {
      "type": "TALK",
      "roomId": "550e8400-e29b-41d4-a716-446655440000",
      "message": "안녕하세요!",
      "sender": "홍길동",
      "timestamp": "2023-08-01T12:00:00"
    },
    ...
  ]
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `404 Not Found`: 채팅방 없음

### 2. 채팅방 메시지 페이징 조회

채팅방 메시지를 페이징 처리하여 조회합니다.

- **URL**: `/rooms/{roomId}/messages/paged`
- **Method**: `GET`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Query Parameters**:
  - `page`: 페이지 번호 (0부터 시작, 기본값: 0)
  - `size`: 페이지 크기 (기본값: 20)
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 페이징된 메시지 목록
  ```json
  {
    "content": [
      {
        "type": "TALK",
        "roomId": "550e8400-e29b-41d4-a716-446655440000",
        "message": "안녕하세요!",
        "sender": "홍길동",
        "timestamp": "2023-08-01T12:00:00"
      },
      ...
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 5,
    "totalElements": 100,
    "last": false,
    "numberOfElements": 20,
    "first": true,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "size": 20,
    "number": 0,
    "empty": false
  }
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `404 Not Found`: 채팅방 없음

### 3. 특정 시간 이후 채팅 메시지 조회

채팅방 ID와 기준 시간을 입력받아 해당 시간 이후의 메시지를 조회합니다.

- **URL**: `/rooms/{roomId}/messages/after`
- **Method**: `GET`
- **Path Parameters**:
  - `roomId`: 채팅방 ID
- **Query Parameters**:
  - `timestamp`: 기준 시간 (ISO-8601 형식, e.g. "2023-08-01T12:00:00")
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 메시지 목록
  ```json
  [
    {
      "type": "TALK",
      "roomId": "550e8400-e29b-41d4-a716-446655440000",
      "message": "안녕하세요!",
      "sender": "홍길동",
      "timestamp": "2023-08-01T12:01:00"
    },
    ...
  ]
  ```
- **Status Codes**:
  - `200 OK`: 성공
  - `400 Bad Request`: 잘못된 요청 (시간 형식 오류 등)
  - `404 Not Found`: 채팅방 없음

### 4. 사용자별 메시지 조회

특정 사용자가 보낸 모든 메시지를 조회합니다.

- **URL**: `/messages/user/{sender}`
- **Method**: `GET`
- **Path Parameters**:
  - `sender`: 메시지 발신자
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 메시지 목록
  ```json
  [
    {
      "type": "TALK",
      "roomId": "550e8400-e29b-41d4-a716-446655440000",
      "message": "안녕하세요!",
      "sender": "홍길동",
      "timestamp": "2023-08-01T12:00:00"
    },
    ...
  ]
  ```
- **Status Codes**:
  - `200 OK`: 성공

### 5. 메시지 키워드 검색

메시지 내용에 특정 키워드가 포함된 메시지를 검색합니다.

- **URL**: `/messages/search`
- **Method**: `GET`
- **Query Parameters**:
  - `keyword`: 검색 키워드
- **Headers**:
  - `Authorization: Bearer {jwt-token}`
- **Response**: 메시지 목록
  ```json
  [
    {
      "type": "TALK",
      "roomId": "550e8400-e29b-41d4-a716-446655440000",
      "message": "안녕하세요 여러분!",
      "sender": "홍길동",
      "timestamp": "2023-08-01T12:00:00"
    },
    ...
  ]
  ```
- **Status Codes**:
  - `200 OK`: 성공

## 클라이언트 구현 예제 (React)

다음은 React에서 채팅방 목록을 표시하고 채팅방을 선택하는 간단한 예제입니다.

```jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function ChatRoomList({ token, onSelectRoom }) {
  const [rooms, setRooms] = useState([]);
  const [selectedType, setSelectedType] = useState('');
  
  // 모든 채팅방 로드
  const loadAllRooms = async () => {
    try {
      const response = await axios.get('https://ide-be.yeonjae.kr/api/chat/rooms', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setRooms(response.data);
    } catch (error) {
      console.error('채팅방 목록 로드 실패:', error);
    }
  };
  
  // 유형별 채팅방 로드
  const loadRoomsByType = async (type) => {
    try {
      const response = await axios.get(`https://ide-be.yeonjae.kr/api/chat/rooms/type/${type}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setRooms(response.data);
      setSelectedType(type);
    } catch (error) {
      console.error(`${type} 채팅방 목록 로드 실패:`, error);
    }
  };
  
  // 채팅방 참여
  const joinRoom = async (roomId) => {
    try {
      const response = await axios.post(`https://ide-be.yeonjae.kr/api/chat/rooms/${roomId}/join`, {}, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      // 채팅방 선택 및 WebSocket 연결 처리를 위해 부모 컴포넌트에 알림
      onSelectRoom(roomId);
    } catch (error) {
      console.error('채팅방 참여 실패:', error);
    }
  };
  
  useEffect(() => {
    loadAllRooms();
  }, [token]);
  
  return (
    <div className="chatroom-list">
      <h2>채팅방 목록</h2>
      
      <div className="room-type-filter">
        <button onClick={() => loadAllRooms()}>전체 채팅방</button>
        <button onClick={() => loadRoomsByType('COMPANY')}>기업 채팅방</button>
        <button onClick={() => loadRoomsByType('TECH_STACK')}>기술 스택 채팅방</button>
      </div>
      
      <div className="rooms">
        {rooms.map(room => (
          <div key={room.roomId} className="room-card">
            <h3>{room.name}</h3>
            <p>{room.description}</p>
            <div className="room-info">
              <span>유형: {room.roomType === 'COMPANY' ? '기업' : '기술 스택'}</span>
              <span>생성자: {room.createdBy}</span>
              <span>참여자: {room.participants.length}명</span>
            </div>
            <button onClick={() => joinRoom(room.roomId)}>
              참여하기
            </button>
          </div>
        ))}
        
        {rooms.length === 0 && (
          <p>사용 가능한 채팅방이 없습니다.</p>
        )}
      </div>
    </div>
  );
}

export default ChatRoomList;
``` 