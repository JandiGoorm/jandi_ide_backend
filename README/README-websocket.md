# 채팅 WebSocket API 명세서

## WebSocket 개요

이 프로젝트는 실시간 채팅을 위해 WebSocket을 사용합니다. WebSocket은 HTTP와 달리 클라이언트와 서버 간에 지속적인 양방향 통신을 제공하는 프로토콜입니다.

### 주요 기술

- **WebSocket**: 브라우저와 서버 간의 실시간 양방향 통신을 제공하는 프로토콜
- **STOMP**: Simple Text Oriented Messaging Protocol의 약자로, WebSocket 위에서 동작하는 메시징 프로토콜로 메시지 라우팅, 헤더 기반 인증 등의 기능 제공
- **SockJS**: WebSocket을 지원하지 않는 브라우저에서도 WebSocket과 유사한 기능을 제공하는 라이브러리

## 클라이언트 설정 방법

### 1. 필요한 라이브러리 설치

```bash
npm install sockjs-client @stomp/stompjs
```

### 2. STOMP 클라이언트 설정 예제

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// STOMP 클라이언트 객체 생성
const stompClient = new Client({
  // SockJS를 통한 WebSocket 연결 설정
  webSocketFactory: () => new SockJS('https://ide-be.yeonjae.kr/ws/chat'),
  
  // 연결 성공 시 콜백
  onConnect: (frame) => {
    console.log('WebSocket 연결 성공:', frame);
    
    // 특정 채팅방 구독 예시
    stompClient.subscribe('/topic/chat/room/room-id-here', (message) => {
      // 메시지 수신 시 처리 로직
      const receivedMessage = JSON.parse(message.body);
      console.log('메시지 수신:', receivedMessage);
      // UI 업데이트 로직 추가
    });
    
    // 연결 성공 후 입장 메시지 전송 예시
    sendMessage('ENTER', '안녕하세요! 입장했습니다.');
  },
  
  // 연결 오류 시 콜백
  onStompError: (frame) => {
    console.error('STOMP 에러:', frame.headers, frame.body);
  },
  
  // 재연결 설정
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000
});

// JWT 토큰을 포함한 연결 설정
stompClient.connectHeaders = {
  'Authorization': 'Bearer your-jwt-token-here'
};

// WebSocket 연결 시작
function connect() {
  stompClient.activate();
}

// WebSocket 연결 종료
function disconnect() {
  stompClient.deactivate();
}

// 메시지 전송 함수
function sendMessage(type, messageText, roomId) {
  if (!stompClient.connected) {
    console.error('WebSocket 연결이 되어있지 않습니다.');
    return;
  }
  
  const chatMessage = {
    type: type, // 'ENTER', 'TALK', 'LEAVE' 중 하나
    roomId: roomId,
    message: messageText,
    sender: '사용자 닉네임' // 실제 앱에서는 로그인한 사용자 정보 사용
    // 프로필 이미지는 서버에서 자동으로 추가됩니다
  };
  
  stompClient.publish({
    destination: '/app/chat/message',
    headers: { 'Authorization': 'Bearer your-jwt-token-here' },
    body: JSON.stringify(chatMessage)
  });
}
```