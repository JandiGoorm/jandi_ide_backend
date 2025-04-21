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
  };
  
  stompClient.publish({
    destination: '/app/chat/message',
    headers: { 'Authorization': 'Bearer your-jwt-token-here' },
    body: JSON.stringify(chatMessage)
  });
}
```

## WebSocket 메시징 명세

### 1. 연결 엔드포인트

- **URL**: `/ws/chat`
- **Protocols**: WebSocket, SockJS(폴백)
- **Headers**: 
  - `Authorization: Bearer {jwt-token}`

### 2. 메시지 발행

- **Destination**: `/app/chat/message`
- **Headers**: 
  - `Authorization: Bearer {jwt-token}`
- **Body**:
  ```json
  {
    "type": "TALK", // "ENTER", "TALK", "LEAVE" 중 하나
    "roomId": "채팅방ID",
    "message": "메시지 내용",
    "sender": "발신자 닉네임"
  }
  ```

### 3. 메시지 구독

- **Destination**: `/topic/chat/room/{roomId}`
- **Received Message Format**:
  ```json
  {
    "type": "TALK", // "ENTER", "TALK", "LEAVE" 중 하나
    "roomId": "채팅방ID",
    "message": "메시지 내용",
    "sender": "발신자 닉네임",
    "timestamp": "2023-08-01T12:01:00"
  }
  ```

## 클라이언트 구현 예제 (React)

```jsx
import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

function ChatComponent({ roomId, token, username }) {
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const stompClientRef = useRef(null);

  useEffect(() => {
    // 컴포넌트 마운트 시 WebSocket 연결
    connect();

    // 컴포넌트 언마운트 시 연결 해제
    return () => {
      disconnect();
    };
  }, [roomId]);

  const connect = () => {
    const client = new Client({
      webSocketFactory: () => new SockJS('https://ide-be.yeonjae.kr/ws/chat'),
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      onConnect: () => {
        console.log('WebSocket 연결 성공');
        setConnected(true);
        
        // 해당 채팅방 구독
        client.subscribe(`/topic/chat/room/${roomId}`, onMessageReceived);
        
        // 입장 메시지 전송
        sendMessage('ENTER', '입장했습니다.');
        
        // 기존 메시지 로드
        loadMessages();
      },
      onStompError: (frame) => {
        console.error('STOMP 에러:', frame);
      }
    });

    client.activate();
    stompClientRef.current = client;
  };

  const disconnect = () => {
    if (stompClientRef.current && stompClientRef.current.connected) {
      // 퇴장 메시지 전송
      sendMessage('LEAVE', '퇴장했습니다.');
      stompClientRef.current.deactivate();
      setConnected(false);
    }
  };

  const onMessageReceived = (payload) => {
    const message = JSON.parse(payload.body);
    setMessages((prevMessages) => [...prevMessages, message]);
  };

  const sendMessage = (type, text) => {
    if (stompClientRef.current && stompClientRef.current.connected) {
      const chatMessage = {
        type: type,
        roomId: roomId,
        sender: username,
        message: text
      };
      
      stompClientRef.current.publish({
        destination: '/app/chat/message',
        headers: { 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(chatMessage)
      });
    }
  };

  const handleSend = () => {
    if (newMessage.trim() && connected) {
      sendMessage('TALK', newMessage);
      setNewMessage('');
    }
  };

  const loadMessages = async () => {
    try {
      const response = await fetch(`https://ide-be.yeonjae.kr/api/chat/rooms/${roomId}/messages`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setMessages(data);
      }
    } catch (error) {
      console.error('메시지 로드 오류:', error);
    }
  };

  return (
    <div className="chat-container">
      <h2>채팅방: {roomId}</h2>
      <div className="connection-status">
        상태: {connected ? '연결됨' : '연결 중...'}
      </div>
      
      <div className="messages-container">
        {messages.map((msg, index) => (
          <div key={index} className={`message ${msg.sender === username ? 'mine' : 'other'}`}>
            <div className="message-sender">{msg.sender}</div>
            <div className="message-content">{msg.message}</div>
            <div className="message-time">{new Date(msg.timestamp).toLocaleTimeString()}</div>
          </div>
        ))}
      </div>
      
      <div className="message-input">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="메시지 입력..."
          disabled={!connected}
          onKeyPress={(e) => e.key === 'Enter' && handleSend()}
        />
        <button onClick={handleSend} disabled={!connected}>전송</button>
      </div>
    </div>
  );
}

export default ChatComponent;
```

## 주의사항 및 팁

1. **인증 처리**: 모든 WebSocket 연결 및 메시지 전송 시 JWT 토큰을 헤더에 포함해야 합니다.
2. **에러 처리**: WebSocket 연결 실패, 메시지 전송 실패 등의 상황에 대한 에러 처리를 구현하세요.
3. **재연결 로직**: 네트워크 문제로 연결이 끊어졌을 때 자동으로 재연결을 시도하는 로직을 구현하세요.
4. **메시지 캐싱**: 오프라인 상태에서도 메시지를 잃지 않도록 캐싱 처리를 고려하세요.
5. **타임스탬프 처리**: 서버와 클라이언트 간의 시간 동기화 문제를 고려하여 타임스탬프를 처리하세요.
