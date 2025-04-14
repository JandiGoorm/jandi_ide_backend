// chat.js
let stompClient = null;
let username = localStorage.getItem('chat-username') || '테스터'; // Load username on start
let currentRoomId = null; // Use this instead of reading from a non-existent input
let currentSubscription = null; // To store the subscription object/ID

// updateChatUI 함수는 그대로 두거나, currentRoomId = null 부분을
// 정말로 연결이 끊어졌을 때만 호출되도록 disconnect(true) 내부 등으로 옮기는 것을 고려
function updateChatUI(isConnected, roomName = '') {
    $('#current-room-name').text(isConnected ? `Current Room: ${roomName}` : 'No room selected');
    $("#disconnect").prop("disabled", !isConnected);
    $("#message").prop("disabled", !isConnected);
    $("#send").prop("disabled", !isConnected);
    $("#username").prop("disabled", isConnected);

    if (!isConnected) {
        $('#message-container').empty(); // Clear messages on disconnect/room change
        // !!! 중요: 이 부분을 여기서 실행하는 것이 맞는지 재고 필요 !!!
        // 방을 바꾸는 중에도 isConnected가 false가 되어 currentRoomId가 null이 될 수 있음
        // currentRoomId = null; // 일단 주석 처리하거나, disconnect(true)가 호출될 때만 실행하도록 변경 고려
        currentSubscription = null; // 구독 해제는 disconnect에서 처리하므로 여기서도 필수 아님
    }
}

// 채팅방 목록 조회 및 표시
// chat.js
async function loadChatRooms() {
    try {
        console.log("[loadChatRooms] Attempting to load chat rooms..."); // 시작 로그
        const response = await fetch('/api/chat/rooms');
        console.log("[loadChatRooms] Fetch response status:", response.status); // 상태 코드 로그

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const rooms = await response.json();
        console.log("[loadChatRooms] Fetched rooms data:", JSON.stringify(rooms)); // 수신된 데이터 로그 (객체 내용을 보려면 stringify)

        const roomList = $('#room-list');
        roomList.empty(); // 이전 목록 지우기

        // 데이터가 배열인지, 길이는 얼마인지 확인
        if (!Array.isArray(rooms)) {
            console.error("[loadChatRooms] Fetched data is not an array:", rooms);
            roomList.html('<p>Error: Invalid room data format.</p>');
            return;
        }
        console.log(`[loadChatRooms] Found ${rooms.length} rooms.`);

        if (rooms.length === 0) {
            console.log("[loadChatRooms] No chat rooms available to display.");
            roomList.html('<p>No chat rooms available.</p>'); // 빈 경우 메시지 표시
        } else {
            console.log(`[loadChatRooms] Processing ${rooms.length} rooms for display...`);
            rooms.forEach((room, index) => {
                // 각 방 객체 내용 및 필수 속성 확인
                console.log(`[loadChatRooms] Processing room ${index}:`, JSON.stringify(room));
                if (!room || typeof room.roomId === 'undefined' || typeof room.name === 'undefined') {
                    console.warn(`[loadChatRooms] Skipping invalid room object at index ${index}:`, room);
                    return; // 필수 정보 없으면 건너뛰기 (continue 대신 return 사용)
                }

                // 날짜 형식 확인 및 안전하게 변환
                let createdAtString = 'N/A';
                if (room.createdAt) {
                    try {
                        // ISO 8601 형식이 아닐 경우 new Date()가 Invalid Date를 반환할 수 있음
                        const dateObj = new Date(room.createdAt);
                        if (!isNaN(dateObj.getTime())) { // 유효한 날짜인지 확인
                            createdAtString = dateObj.toLocaleString();
                        } else {
                            console.warn(`[loadChatRooms] Invalid date format detected for room ${room.roomId}: ${room.createdAt}`);
                            createdAtString = room.createdAt; // 원본 문자열 표시 또는 'Invalid Date'
                        }
                    } catch (e) {
                        console.warn(`[loadChatRooms] Error parsing date for room ${room.roomId}: ${room.createdAt}`, e);
                        createdAtString = room.createdAt; // 오류 시 원본 표시
                    }
                }

                // HTML 생성 및 데이터 바인딩
                const roomElement = $(`
                     <div class="room-item">
                         <div class="room-name">${room.name || 'Unnamed Room'}</div>
                         <div class="room-description">${room.description || 'No description'}</div>
                         <div class="room-info">
                             Created by: ${room.createdBy || 'Unknown'}
                             (${createdAtString})
                         </div>
                     </div>
                 `);
                roomElement.data('roomId', room.roomId);
                roomElement.data('roomName', room.name);
                roomElement.click(function() {
                    selectRoom($(this).data('roomId'), $(this).data('roomName'));
                });

                // DOM에 추가
                roomList.append(roomElement);
                // console.log(`[loadChatRooms] Appended room ${room.roomId} to list.`); // 너무 많은 로그 방지 위해 주석 처리 가능
            });
            console.log("[loadChatRooms] Finished appending rooms to list.");
        }

    } catch (error) {
        // 기존 오류 처리 유지
        console.error('[loadChatRooms] 채팅방 목록 로딩 실패:', error);
        $('#room-list').html('<p>Error loading rooms.</p>');
    }
}

// 채팅방 생성
// chat.js
async function createRoom() {
    const name = $('#newRoomName').val().trim();
    const description = $('#newRoomDescription').val().trim();
    const creatorUsername = $('#username').val().trim(); // 사용자 이름 가져오기

    if (!name) {
        alert('채팅방 이름을 입력하세요.');
        return;
    }
    if (!creatorUsername) {
        alert('사용자 이름을 입력하세요.');
        return;
    }

    try {
        const response = await fetch('/api/chat/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
                // 'X-User-Name' 헤더 제거
            },
            // body에 사용자 이름 포함
            body: JSON.stringify({
                name: name,
                description: description,
                createdBy: creatorUsername // 필드 추가
            })
        });

        if (response.ok) {
            const room = await response.json();
            await loadChatRooms();
            $('#createRoomModal').hide();
            $('#newRoomName').val('');
            $('#newRoomDescription').val('');
            selectRoom(room.roomId, room.name);
        } else {
            const errorData = await response.text();
            console.error('채팅방 생성 실패:', response.status, errorData);
            alert(`채팅방 생성에 실패했습니다: ${errorData || response.statusText}`);
        }
    } catch (error) {
        console.error('채팅방 생성 중 네트워크 오류:', error); // 여기에 에러가 잡혔음
        alert('채팅방 생성 중 오류가 발생했습니다.'); // 사용자에게 알림
    }
}

// 채팅방 선택
function selectRoom(selectedRoomId, selectedRoomName) {
    console.log(`Selecting room: ${selectedRoomId} (${selectedRoomName})`);
    if (currentRoomId === selectedRoomId) {
        console.log("Already in this room.");
        return; // Already in this room
    }

    // Disconnect from the previous room if connected
    if (stompClient && stompClient.connected) {
        disconnect(false); // Call disconnect without UI update for switching rooms
    }

    currentRoomId = selectedRoomId; // Set the new room ID
    username = $("#username").val().trim(); // Ensure username is up-to-date

    if (!username) {
        alert("Please enter a username before joining a room.");
        currentRoomId = null; // Reset room ID if username is missing
        // UI를 업데이트하여 사용자에게 피드백 제공 (선택 사항)
        updateChatUI(false);
        return; // Exit if username is missing
    }

    $('#message-container').empty(); // Clear messages for the new room

    // !!! 아래 줄 제거: connect 호출 전에 UI를 false로 설정하고 currentRoomId를 null로 만드는 문제 수정 !!!
    // updateChatUI(false);

    // Attempt to connect to the new room
    connect(selectedRoomName);
}

function connect(roomName) {
    // selectRoom에서 이미 검사했으므로 아래 검사 제거 또는 유지 (선택 사항)
    // 이 검사들이 여전히 실패한다면 다른 비동기 문제나 변수 스코프 문제가 있을 수 있음
    /*
    if (!currentRoomId) {
        console.error("Cannot connect without a selected room ID.");
        return;
    }
     if (!username) {
        console.error("Cannot connect without a username.");
        return;
    }
    */

    // username 유효성 검사는 selectRoom에서 이미 수행됨
    // currentRoomId는 connect 호출 시점에 유효해야 함
    console.log(`Attempting to connect to room ${currentRoomId} as ${username}`); // 연결 시도 로깅 추가

    const socket = new SockJS('/ws/chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect(
        {},
        function(frame) { // Success callback
            console.log('Connected: ' + frame);
            // 연결 성공 시 UI 업데이트
            updateChatUI(true, roomName);

            const topic = '/topic/chat/room/' + currentRoomId;
            currentSubscription = stompClient.subscribe(topic, function(message) {
                showMessage(JSON.parse(message.body));
            });
            console.log(`Subscribed to ${topic} with ID: ${currentSubscription.id}`);

            sendMessage('ENTER');
        },
        function(error) { // Error callback
            console.error('STOMP Connection Error: ' + error);
            let errorMessage = error;
            if (typeof error === 'object' && error.headers && error.headers.message) {
                errorMessage = error.headers.message;
            } else if (typeof error === 'object'){
                errorMessage = 'Connection closed or failed.';
            }
            alert('WebSocket connection failed: ' + errorMessage);
            // 연결 실패 시 UI 업데이트
            updateChatUI(false);
            stompClient = null;
        }
    );
}

// Disconnect function, accepts flag to prevent recursive UI updates when switching rooms
function disconnect(updateUI = true) {
    if (stompClient !== null && stompClient.connected) {
        // Send LEAVE message before unsubscribing/disconnecting
        sendMessage('LEAVE');

        // Unsubscribe if we have a subscription ID
        if (currentSubscription) {
            try{
                currentSubscription.unsubscribe();
                console.log(`Unsubscribed from ID: ${currentSubscription.id}`);
            } catch (e) {
                console.error("Error during unsubscribe:", e);
            }
            currentSubscription = null;
        } else {
            console.warn("No active subscription to unsubscribe from.");
        }

        // Disconnect STOMP client
        stompClient.disconnect(function() {
            console.log("STOMP client disconnected");
        });
    } else {
        console.log("STOMP client already disconnected or not initialized.");
    }

    stompClient = null; // Ensure client is reset

    // Update UI only if explicitly requested (i.e., not during room switch)
    if (updateUI) {
        // 명시적으로 연결을 끊을 때만 currentRoomId 초기화
        currentRoomId = null;
        updateChatUI(false);
        console.log("Disconnected and UI updated.");
    } else {
        console.log("Disconnected internally (switching rooms).");
    }
}


function sendMessage(type) {
    // Ensure connection and room selection before sending
    if (!stompClient || !stompClient.connected || !currentRoomId) {
        console.warn("Cannot send message: Not connected or no room selected.");
        // alert("You must be connected to a room to send messages."); // Optional user feedback
        return;
    }

    const messageContent = (type === 'TALK') ? $("#message").val().trim() : ''; // Trim whitespace

    // Don't send empty TALK messages
    if (type === 'TALK' && messageContent === '') {
        return;
    }

    const chatMessage = {
        type: type,
        roomId: currentRoomId,
        sender: username,
        message: messageContent
        // timestamp is set by the server in your ChatController
    };

    try {
        stompClient.send("/app/chat/message", {}, JSON.stringify(chatMessage)); // Send to backend controller
        // console.log("Message sent:", chatMessage); // Optional log

        if (type === 'TALK') {
            $("#message").val(''); // Clear input field after sending
            $("#message").focus(); // Keep focus on input
        }
    } catch (error) {
        console.error("Error sending message:", error);
        alert("Failed to send message."); // Inform user
    }
}

function showMessage(message) {
    const messageContainer = $("#message-container");
    let msgHtml = '';
    const isMyMessage = message.sender === username; // Check if the message is from the current user

    // Format timestamp from server (assuming ISO string)
    const formattedTime = message.timestamp ? new Date(message.timestamp).toLocaleTimeString() : new Date().toLocaleTimeString();

    switch(message.type) {
        case 'ENTER':
            msgHtml = `<div class="message system"><span>${message.sender}님이 입장하셨습니다. (${formattedTime})</span></div>`;
            break;
        case 'LEAVE':
            msgHtml = `<div class="message system"><span>${message.sender}님이 퇴장하셨습니다. (${formattedTime})</span></div>`;
            break;
        case 'TALK':
            // Add a class for self-messages if needed for styling
            const messageClass = isMyMessage ? 'message talk self' : 'message talk other';
            msgHtml = `<div class="${messageClass}">
                            <span class="sender">${message.sender}: </span>
                            <span class="content">${message.message}</span>
                            <span class="timestamp">${formattedTime}</span>
                       </div>`;
            break;
        default:
            console.warn("Unknown message type received:", message.type);
            return; // Don't display unknown types
    }

    messageContainer.append(msgHtml);
    // Scroll to the bottom
    messageContainer.scrollTop(messageContainer[0].scrollHeight);
}

// Initialize after DOM is ready
$(function () {
    $('#username').val(username); // Set initial username in input

    // Event Handlers
    $('#username').change(function() {
        const newUsername = $(this).val().trim();
        if (newUsername) {
            username = newUsername;
            localStorage.setItem('chat-username', username);
            console.log("Username updated:", username);
        } else {
            $(this).val(username); // Revert if empty
            alert("Username cannot be empty.");
        }
    });

    $('#createRoomBtn').click(function() {
        // Ensure username is set before allowing room creation
        if (!$('#username').val().trim()) {
            alert("Please enter a username before creating a room.");
            return;
        }
        $('#createRoomModal').show();
        $('#newRoomName').focus();
    });

    // Modal close button
    $('#createRoomModal .modal-buttons button:contains("취소")').click(function() {
        $('#createRoomModal').hide();
        $('#newRoomName').val('');
        $('#newRoomDescription').val('');
    });
    // Modal create button (delegated from HTML onclick)
    // Ensure the createRoom function is globally accessible or attach it here
    // window.createRoom = createRoom; // If createRoom is defined outside $(function(){})

    // Modal external click closes it
    $(window).click(function(event) {
        if ($(event.target).is('#createRoomModal')) {
            $('#createRoomModal').hide();
        }
    });

    // Chat controls
    $('#disconnect').click(function() { disconnect(true); }); // Explicitly disconnect and update UI
    $('#send').click(function() { sendMessage('TALK'); });

    $('#message').keypress(function(e) {
        // Check if Enter key was pressed without Shift key
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault(); // Prevent default newline behavior
            sendMessage('TALK');
        }
    });

    // Initial setup
    updateChatUI(false); // Start in disconnected state
    loadChatRooms(); // Load rooms on page load

    // Optional: Set interval for reloading rooms (consider alternatives like SSE or manual refresh)
    // setInterval(loadChatRooms, 30000); // Reload every 30 seconds
});

// Ensure createRoom is accessible for the inline onclick handler in the modal HTML
// If chat.js is loaded correctly, this function should be global.
// Alternatively, attach the handler using jQuery inside $(function(){})
// $('#createRoomModal .modal-buttons button:contains("만들기")').click(createRoom);