// chatService.js

// 외부 라이브러리 (HTML에서 이미 로드되었다고 가정)
// const SockJS = window.SockJS; // 필요 시 명시적 참조
// const Stomp = window.Stomp; // 필요 시 명시적 참조

const chatService = (function() {
    let stompClient = null;
    let currentRoomId = null;
    let currentSubscription = null;
    let username = 'Guest'; // 기본 사용자 이름
    let config = { // 콜백 함수들을 저장할 설정 객체
        onConnected: () => {},
        onDisconnected: () => {},
        onMessageReceived: (message) => {},
        onError: (error) => {},
        onRoomsLoaded: (rooms) => {},
        onRoomCreated: (room) => {}
    };

    /**
     * 서비스 초기화 및 콜백 설정
     * @param {object} newConfig - UI 레이어에서 전달하는 콜백 함수들
     */
    function init(newConfig) {
        config = { ...config, ...newConfig }; // 기본 설정과 새 설정 병합
        console.log("Chat service initialized with config:", config);
    }

    /**
     * 사용자 이름 설정
     * @param {string} name - 설정할 사용자 이름
     */
    function setUsername(name) {
        if (name && name.trim()) {
            username = name.trim();
            console.log("Chat service username set to:", username);
        }
    }

    /**
     * 현재 사용자 이름 반환
     * @returns {string} 현재 사용자 이름
     */
    function getUsername() {
        return username;
    }

    /**
     * 현재 접속된 방 ID 반환
     * @returns {string | null} 현재 방 ID 또는 null
     */
    function getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * WebSocket 연결 및 STOMP 설정, 토픽 구독
     * @param {string} roomId - 접속할 채팅방 ID
     * @param {string} roomName - 접속할 채팅방 이름 (UI 콜백용)
     * @returns {boolean} 연결 시도 시작 여부
     */
    function connect(roomId, roomName) {
        if (!roomId || !username) {
            config.onError("Room ID and Username are required to connect.");
            return false;
        }
        if (stompClient && stompClient.connected) {
            // 이미 연결된 상태면 추가 연결 시도 안 함 (혹은 필요 시 기존 연결 끊고 재연결)
            console.warn("Already connected. Disconnect first to connect to a new room.");
            // 필요하다면 disconnect() 호출 후 재연결 로직 추가
            return false;
        }

        currentRoomId = roomId; // 현재 방 ID 설정
        console.log(`Attempting to connect service to room ${currentRoomId} as ${username}`);

        try {
            const socket = new SockJS('/ws/chat'); // 서버 엔드포인트
            stompClient = Stomp.over(socket);
            stompClient.debug = null; // 디버그 로그 비활성화

            stompClient.connect(
                {}, // 연결 헤더
                (frame) => { // 연결 성공
                    console.log('Chat service connected: ' + frame);
                    // 해당 방 토픽 구독
                    const topic = '/topic/chat/room/' + currentRoomId;
                    currentSubscription = stompClient.subscribe(topic, (message) => {
                        try {
                            config.onMessageReceived(JSON.parse(message.body)); // 메시지 수신 콜백 호출
                        } catch (e) {
                            config.onError("Failed to parse received message: " + e.message);
                        }
                    });
                    console.log(`Chat service subscribed to ${topic} with ID: ${currentSubscription ? currentSubscription.id : 'N/A'}`);
                    config.onConnected(roomName); // 연결 성공 콜백 호출
                    sendMessage('ENTER'); // 입장 메시지 전송
                },
                (error) => { // 연결 실패
                    console.error('Chat service STOMP Connection Error:', error);
                    const errorMessage = typeof error === 'string' ? error : 'Connection failed or closed.';
                    stompClient = null; // 클라이언트 초기화
                    currentRoomId = null;
                    config.onError('WebSocket connection failed: ' + errorMessage); // 에러 콜백 호출
                    config.onDisconnected(); // 연결 끊김 콜백 호출
                }
            );
            return true;
        } catch (e) {
            console.error("Error establishing SockJS/STOMP connection:", e);
            config.onError("Failed to initialize WebSocket connection: " + e.message);
            stompClient = null;
            currentRoomId = null;
            config.onDisconnected();
            return false;
        }
    }

    /**
     * WebSocket 연결 종료 및 리소스 정리
     */
    function disconnect() {
        if (stompClient && stompClient.connected) {
            sendMessage('LEAVE'); // 퇴장 메시지 전송 시도

            if (currentSubscription) { // 구독 취소
                try {
                    currentSubscription.unsubscribe();
                    console.log(`Chat service unsubscribed from ID: ${currentSubscription.id}`);
                } catch (e) { console.error("Error during unsubscribe:", e); }
                currentSubscription = null;
            }

            stompClient.disconnect(() => { // 연결 종료
                console.log("Chat service STOMP client disconnected.");
                stompClient = null;
                currentRoomId = null;
                config.onDisconnected(); // 연결 끊김 콜백 호출
            });
        } else {
            console.log("Chat service already disconnected or not initialized.");
            // 이미 끊긴 상태라도 콜백 호출하여 UI 일관성 유지
            stompClient = null;
            currentRoomId = null;
            currentSubscription = null;
            config.onDisconnected();
        }
    }

    /**
     * 채팅 메시지 전송
     * @param {'ENTER' | 'TALK' | 'LEAVE'} type - 메시지 타입
     * @param {string} [content=''] - 메시지 내용 ('TALK' 타입 시 사용)
     */
    function sendMessage(type, content = '') {
        if (!stompClient || !stompClient.connected || !currentRoomId) {
            console.warn("Cannot send message: Not connected or no room selected.");
            return;
        }
        if (type === 'TALK' && content.trim() === '') {
            return; // 내용 없는 TALK 메시지 보내지 않음
        }

        const chatMessage = {
            type: type,
            roomId: currentRoomId,
            sender: username,
            message: content.trim()
            // timestamp는 서버에서 설정
        };

        try {
            stompClient.send("/app/chat/message", {}, JSON.stringify(chatMessage));
        } catch (error) {
            console.error("Error sending message via STOMP:", error);
            config.onError("Failed to send message: " + error.message);
        }
    }

    /**
     * 채팅방 목록 API 호출
     */
    async function loadChatRooms() {
        try {
            const response = await fetch('/api/chat/rooms');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const rooms = await response.json();
            config.onRoomsLoaded(rooms); // 로드 성공 콜백 호출
        } catch (error) {
            console.error('Failed to load chat rooms:', error);
            config.onError("Failed to load chat rooms: " + error.message); // 에러 콜백 호출
            config.onRoomsLoaded([]); // 오류 시 빈 배열 전달 또는 별도 처리
        }
    }

    /**
     * 채팅방 생성 API 호출
     * @param {string} name - 생성할 방 이름
     * @param {string} description - 생성할 방 설명
     * @returns {Promise<object|null>} 생성된 방 정보 객체 또는 실패 시 null (Promise 반환)
     */
    async function createRoom(name, description) {
        if (!name || !username) {
            config.onError("Room name and username are required to create a room.");
            return null; // 또는 Promise.reject
        }
        try {
            const response = await fetch('/api/chat/rooms', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, description, createdBy: username })
            });

            if (response.ok) {
                const room = await response.json();
                config.onRoomCreated(room); // 방 생성 콜백 호출 (선택 사항)
                return room; // Promise resolve
            } else {
                const errorData = await response.text();
                throw new Error(`Failed to create room: ${errorData || response.statusText}`);
            }
        } catch (error) {
            console.error('Error creating room:', error);
            config.onError("Error creating room: " + error.message);
            return null; // Promise reject 또는 null 반환
        }
    }

    // 외부에 노출할 함수들 반환
    return {
        init,
        setUsername,
        getUsername,
        getCurrentRoomId,
        connect,
        disconnect,
        sendMessage,
        loadChatRooms,
        createRoom
    };
})(); // 즉시 실행 함수 표현식(IIFE)을 사용하여 모듈 스코프 생성