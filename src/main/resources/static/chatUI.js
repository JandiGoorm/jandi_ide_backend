// chatUI.js - chat.html 페이지의 UI 상호작용 담당

// chatService.js가 먼저 로드되었다고 가정
// const chatService = window.chatService; // 필요 시 명시적 참조

$(function () { // DOM 로드 완료 후 실행

    let localUsername = localStorage.getItem('chat-username') || '테스터'; // 로컬 스토리지에서 사용자 이름 로드

    // --- UI 요소 참조 ---
    const $usernameInput = $('#username');
    const $roomList = $('#room-list');
    const $messageContainer = $('#message-container');
    const $messageInput = $('#message');
    const $sendButton = $('#send');
    const $disconnectButton = $('#disconnect');
    const $createRoomBtn = $('#createRoomBtn');
    const $createRoomModal = $('#createRoomModal');
    const $newRoomNameInput = $('#newRoomName');
    const $newRoomDescriptionInput = $('#newRoomDescription');
    const $currentRoomNameHeader = $('#current-room-name');

    // --- chatService 콜백 함수 정의 ---

    /** chatService 연결 성공 시 호출될 콜백 */
    function handleConnected(roomName) {
        console.log("UI: Connected to room -", roomName);
        updateChatUI(true, roomName); // UI 상태 업데이트
    }

    /** chatService 연결 종료 시 호출될 콜백 */
    function handleDisconnected() {
        console.log("UI: Disconnected");
        updateChatUI(false); // UI 상태 업데이트
    }

    /** chatService 에서 메시지 수신 시 호출될 콜백 */
    function handleMessageReceived(message) {
        // console.log("UI: Message received", message);
        showMessage(message); // 메시지 화면에 표시
    }

    /** chatService 에서 오류 발생 시 호출될 콜백 */
    function handleError(error) {
        console.error("UI: Error received -", error);
        alert("Error: " + error); // 간단히 alert 표시
    }

    /** chatService 에서 채팅방 목록 로드 완료 시 호출될 콜백 */
    function handleRoomsLoaded(rooms) {
        console.log("UI: Rooms loaded -", rooms);
        renderRoomList(rooms); // 채팅방 목록 렌더링 함수 호출
    }

    /** chatService 에서 채팅방 생성 완료 시 호출될 콜백 (선택 사항) */
    function handleRoomCreated(room) {
        console.log("UI: Room created callback -", room);
        // 필요 시 추가 작업 (예: 생성 성공 알림)
    }


    // --- UI 업데이트 및 렌더링 함수 ---

    /**
     * 채팅방 목록을 받아와 화면(#room-list)에 렌더링합니다.
     * @param {Array<object>} rooms - 채팅방 정보 객체 배열
     */
    function renderRoomList(rooms) {
        $roomList.empty(); // 기존 목록 비우기
        if (!Array.isArray(rooms)) {
            console.error("UI: Invalid room data format for rendering.");
            $roomList.html('<p>Error: Could not load rooms.</p>');
            return;
        }
        if (rooms.length === 0) {
            $roomList.html('<p>No chat rooms available.</p>');
        } else {
            rooms.forEach((room) => {
                if (!room || !room.roomId || !room.name) {
                    console.warn("UI: Skipping invalid room data during render:", room);
                    return;
                }
                let createdAtString = 'N/A';
                if (room.createdAt) {
                    try {
                        const dateObj = new Date(room.createdAt);
                        createdAtString = !isNaN(dateObj.getTime()) ? dateObj.toLocaleString() : room.createdAt;
                    } catch (e) { createdAtString = room.createdAt; }
                }
                const roomElement = $(`
                     <div class="room-item">
                         <div class="room-name">${room.name}</div>
                         <div class="room-description">${room.description || 'No description'}</div>
                         <div class="room-info">Created by: ${room.createdBy || 'Unknown'} (${createdAtString})</div>
                     </div>
                 `);
                roomElement.data('roomId', room.roomId);
                roomElement.data('roomName', room.name);
                // 클릭 시 selectRoom 함수 호출 (chatUI.js 내의 함수)
                roomElement.click(() => selectRoomUIAction($(roomElement).data('roomId'), $(roomElement).data('roomName')));
                $roomList.append(roomElement);
            });
        }
    }

    /**
     * 수신된 메시지를 HTML로 변환하여 메시지 목록(#message-container)에 추가합니다.
     * @param {object} message - 수신된 메시지 객체 (ChatMessageDTO)
     */
    function showMessage(message) {
        let msgHtml = '';
        const currentUsername = chatService.getUsername(); // 현재 사용자 이름 가져오기
        const isMyMessage = message.sender === currentUsername;
        const formattedTime = message.timestamp ? new Date(message.timestamp).toLocaleTimeString() : new Date().toLocaleTimeString();

        switch(message.type) {
            case 'ENTER':
                msgHtml = `<div class="message system"><span>${message.sender}님이 입장하셨습니다. (${formattedTime})</span></div>`;
                break;
            case 'LEAVE':
                msgHtml = `<div class="message system"><span>${message.sender}님이 퇴장하셨습니다. (${formattedTime})</span></div>`;
                break;
            case 'TALK':
                const messageClass = isMyMessage ? 'message talk self' : 'message talk other';
                // HTML 인코딩 필요 시 처리 추가 (예: jQuery의 .text() 사용 또는 라이브러리 사용)
                const safeMessage = $('<div>').text(message.message).html(); // 간단한 HTML 인코딩
                msgHtml = `<div class="${messageClass}">
                                <span class="sender">${message.sender}: </span>
                                <span class="content">${safeMessage}</span>
                                <span class="timestamp">${formattedTime}</span>
                           </div>`;
                break;
            default:
                console.warn("UI: Unknown message type received:", message.type);
                return;
        }
        $messageContainer.append(msgHtml);
        $messageContainer.scrollTop($messageContainer[0].scrollHeight); // 자동 스크롤
    }

    /**
     * 채팅 UI 요소들의 상태를 업데이트합니다.
     * @param {boolean} isConnected - 연결 상태
     * @param {string} [roomName=''] - 현재 방 이름
     */
    function updateChatUI(isConnected, roomName = '') {
        $currentRoomNameHeader.text(isConnected ? `Current Room: ${roomName}` : 'No room selected');
        $disconnectButton.prop("disabled", !isConnected);
        $messageInput.prop("disabled", !isConnected);
        $sendButton.prop("disabled", !isConnected);
        $usernameInput.prop("disabled", isConnected);
        if (!isConnected) {
            $messageContainer.empty();
        }
    }

    // --- UI 액션 함수 ---

    /** 사용자가 UI에서 채팅방을 선택했을 때 실행되는 액션 */
    function selectRoomUIAction(roomId, roomName) {
        const currentRoomId = chatService.getCurrentRoomId();
        if (currentRoomId === roomId) {
            console.log("UI: Already in this room.");
            return;
        }
        console.log(`UI: Selecting room action - ${roomId} (${roomName})`);
        // chatService의 connect 함수 호출 (disconnect는 connect 내부 또는 여기서 명시적 호출)
        // 참고: chatService.connect는 내부적으로 이전 연결 처리 로직을 가질 수 있음 (현재 구현은 X)
        // 필요 시 chatService.disconnect() 먼저 호출
        chatService.connect(roomId, roomName);
    }

    /** 사용자가 UI에서 '새 채팅방 만들기' 모달을 제출했을 때 실행되는 액션 */
    async function createRoomUIAction() {
        const name = $newRoomNameInput.val().trim();
        const description = $newRoomDescriptionInput.val().trim();

        if (!name) { alert('채팅방 이름을 입력하세요.'); return; }
        if (!chatService.getUsername()) { alert('사용자 이름을 입력하세요.'); return; }

        // chatService의 createRoom 호출
        const createdRoom = await chatService.createRoom(name, description);

        if (createdRoom) { // 생성 성공 시
            await chatService.loadChatRooms(); // 목록 새로고침
            $createRoomModal.hide();
            $newRoomNameInput.val('');
            $newRoomDescriptionInput.val('');
            // 생성된 방으로 자동 접속 (selectRoomUIAction 호출)
            selectRoomUIAction(createdRoom.roomId, createdRoom.name);
        } else {
            // 생성 실패 시 chatService 내부에서 onError 콜백이 호출되어 alert가 뜰 것임
            console.log("UI: Room creation failed (handled by chatService onError).");
        }
    }


    // --- 초기화 및 이벤트 리스너 설정 ---

    // 1. chatService 초기화 (콜백 함수들 전달)
    chatService.init({
        onConnected: handleConnected,
        onDisconnected: handleDisconnected,
        onMessageReceived: handleMessageReceived,
        onError: handleError,
        onRoomsLoaded: handleRoomsLoaded,
        onRoomCreated: handleRoomCreated
    });

    // 2. 초기 사용자 이름 설정
    $usernameInput.val(localUsername);
    chatService.setUsername(localUsername); // 서비스에도 사용자 이름 설정

    // 3. 이벤트 핸들러 등록
    $usernameInput.change(function() { // 사용자 이름 변경
        const newUsername = $(this).val().trim();
        if (newUsername) {
            localUsername = newUsername;
            localStorage.setItem('chat-username', localUsername); // localStorage 업데이트
            chatService.setUsername(localUsername); // 서비스에도 업데이트
            console.log("UI: Username updated:", localUsername);
        } else {
            $(this).val(localUsername);
            alert("Username cannot be empty.");
        }
    });

    $createRoomBtn.click(function() { // '새 채팅방 만들기' 버튼
        if (!chatService.getUsername()) { // 사용자 이름 입력 확인
            alert("Please enter a username before creating a room.");
            return;
        }
        $createRoomModal.show();
        $newRoomNameInput.focus();
    });

    $('#createRoomModal .modal-buttons button:contains("취소")').click(function() { // 모달 '취소' 버튼
        $createRoomModal.hide();
        $newRoomNameInput.val('');
        $newRoomDescriptionInput.val('');
    });

    // 모달 '만들기' 버튼 (HTML onclick 대신 여기서 핸들러 등록)
    $('#createRoomModal .modal-buttons button:contains("Create")').click(createRoomUIAction);


    $(window).click(function(event) { // 모달 외부 클릭 시 닫기
        if ($(event.target).is($createRoomModal)) {
            $createRoomModal.hide();
        }
    });

    $disconnectButton.click(function() { // 'Leave Room' 버튼
        chatService.disconnect();
    });

    $sendButton.click(function() { // 'Send' 버튼
        chatService.sendMessage('TALK', $messageInput.val());
        $messageInput.val(''); // 메시지 전송 후 입력 필드 비우기
        $messageInput.focus();
    });

    $messageInput.keypress(function(e) { // 메시지 입력 중 Enter 키
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            chatService.sendMessage('TALK', $messageInput.val());
            $messageInput.val('');
            $messageInput.focus();
        }
    });

    // 4. 초기 UI 상태 설정 및 채팅방 목록 로드
    updateChatUI(false);
    chatService.loadChatRooms();

}); // End of DOM ready