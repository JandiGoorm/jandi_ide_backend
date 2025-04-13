// 전역 변수 선언
let stompClient = null; // STOMP 클라이언트 인스턴스를 저장할 변수 (연결 전에는 null)
let username = null;    // 사용자의 이름을 저장할 변수
let roomId = null;      // 현재 접속한 채팅방 ID를 저장할 변수

/**
 * WebSocket 연결을 시작하는 함수
 */
function connect() {
    // HTML에서 사용자명과 채팅방 ID 입력 값을 가져와 앞뒤 공백 제거
    username = $("#username").val().trim();
    roomId = $("#roomId").val().trim();

    // 사용자명 또는 채팅방 ID가 비어있는지 확인
    if (username === "" || roomId === "") {
        alert("사용자명과 채팅방 ID를 입력하세요.");
        return; // 함수 실행 중단
    }

    // SockJS를 사용하여 WebSocket 엔드포인트('/ws/chat')에 연결 시도
    // '/ws/chat'는 Spring Boot 서버의 WebSocketConfig에서 설정한 엔드포인트 경로와 일치해야 함
    const socket = new SockJS('/ws/chat');

    // SockJS 연결 위에서 STOMP 클라이언트 생성
    stompClient = Stomp.over(socket);

    // STOMP 프로토콜을 사용하여 서버에 연결
    // 첫 번째 인자: 연결 헤더 (여기서는 빈 객체 {})
    // 두 번째 인자: 연결 성공 시 실행될 콜백 함수 (onConnect)
    // 세 번째 인자: 연결 실패 시 실행될 콜백 함수 (onError)
    stompClient.connect({}, function(frame) {
        // 연결 성공 로그 출력
        console.log('Connected: ' + frame);

        // 특정 채팅방의 메시지를 수신하기 위해 토픽 구독 시작
        // '/topic/chat/room/' + roomId 는 서버(WebSocketConfig, RedisSubscriber)에서 설정한 메시지 브로드캐스팅 경로와 일치해야 함
        stompClient.subscribe('/topic/chat/room/' + roomId, function(message) {
            // 메시지 수신 시 showMessage 함수 호출하여 화면에 표시
            // 메시지 본문(message.body)은 JSON 문자열 형태이므로 객체로 파싱
            showMessage(JSON.parse(message.body));
        });

        // 서버에 'ENTER'(입장) 타입의 메시지를 전송하여 입장 알림
        sendMessage('ENTER');

        // UI 상태 업데이트: 연결 버튼 비활성화, 나머지 활성화
        $("#connect").prop("disabled", true);
        $("#disconnect").prop("disabled", false);
        $("#message").prop("disabled", false);
        $("#send").prop("disabled", false);

    }, function(error) {
        // 연결 실패 로그 및 사용자 알림
        console.error('Connection error: ' + error);
        alert('연결에 실패했습니다: ' + error);
        // 필요하다면 여기서 UI 초기 상태로 복구 로직 추가 가능
    });
}

/**
 * WebSocket 연결을 종료하는 함수
 */
function disconnect() {
    // stompClient가 null이 아닌 경우(즉, 연결된 상태일 때)
    if (stompClient !== null) {
        // 서버에 'LEAVE'(퇴장) 타입의 메시지를 전송하여 퇴장 알림
        sendMessage('LEAVE');

        // STOMP 클라이언트 연결 종료
        stompClient.disconnect();
        console.log("Disconnected");
    }

    // UI 상태 업데이트: 연결 버튼 활성화, 나머지 비활성화
    $("#connect").prop("disabled", false);
    $("#disconnect").prop("disabled", true);
    $("#message").prop("disabled", true);
    $("#send").prop("disabled", true);
    // 메시지 표시 영역 클리어 등 추가 작업 가능
    // $("#message-container").html('');
}

/**
 * 서버로 메시지를 전송하는 함수
 * @param {string} type - 메시지 타입 ('ENTER', 'LEAVE', 'TALK')
 */
function sendMessage(type) {
    let messageContent = ''; // 메시지 내용을 저장할 변수

    // 메시지 타입이 'TALK'일 경우, 입력 필드(#message)의 값을 가져옴
    if (type === 'TALK') {
        messageContent = $("#message").val();
        // 'TALK' 타입인데 내용이 비어있으면 전송하지 않음
        if (messageContent === '') return;
    }

    // 서버로 전송할 ChatMessage 객체 생성
    // 서버의 ChatMessage DTO 구조와 일치해야 함
    const chatMessage = {
        type: type,                 // 메시지 타입 (ENTER, LEAVE, TALK)
        roomId: roomId,             // 현재 채팅방 ID
        sender: username,           // 현재 사용자 이름
        message: messageContent,    // 실제 메시지 내용 (ENTER/LEAVE 시 빈 문자열)
        timestamp: new Date().toISOString() // 클라이언트 측 타임스탬프 (서버에서 덮어쓸 수 있음)
    };

    // stompClient.send(destination, headers, body)
    // destination: 메시지를 처리할 서버의 경로 ('/app' + @MessageMapping 경로)
    // headers: 메시지 헤더 (여기서는 빈 객체)
    // body: 전송할 메시지 본문 (JSON 객체를 문자열로 변환하여 전송)
    stompClient.send("/app/chat/message", {}, JSON.stringify(chatMessage));

    // 메시지 타입이 'TALK'였다면, 입력 필드 초기화
    if (type === 'TALK') {
        $("#message").val('');
    }
}

/**
 * 수신된 메시지를 화면에 표시하는 함수
 * @param {object} message - 서버로부터 받은 파싱된 ChatMessage 객체
 */
function showMessage(message) {
    let msgText = ''; // 화면에 추가될 HTML 문자열을 저장할 변수

    // 메시지 타입에 따라 다른 HTML 형식 생성
    switch(message.type) {
        case 'ENTER':
            // 입장 메시지 형식
            msgText = `<div class="message"><span class="system">${message.sender}님이 입장하셨습니다.</span></div>`;
            break;
        case 'LEAVE':
            // 퇴장 메시지 형식
            msgText = `<div class="message"><span class="system">${message.sender}님이 퇴장하셨습니다.</span></div>`;
            break;
        case 'TALK':
            // 일반 대화 메시지 형식 (보낸사람, 내용, 시간 표시)
            // 서버에서 보낸 timestamp를 사용하며, 보기 좋은 로컬 시간 형식으로 변환
            msgText = `<div class="message">
                <span class="sender">${message.sender}: </span>
                <span class="content">${message.message}</span>
                <span class="timestamp">${new Date(message.timestamp).toLocaleTimeString()}</span>
            </div>`;
            break;
        default:
            // 알 수 없는 타입의 메시지 처리 (옵션)
            console.warn("Unknown message type received:", message);
            return; // 화면에 표시하지 않음
    }

    // 생성된 HTML 문자열을 메시지 컨테이너(#message-container)의 맨 뒤에 추가
    $("#message-container").append(msgText);

    // 메시지 컨테이너의 스크롤을 항상 가장 아래로 이동시켜 최신 메시지가 보이도록 함
    $("#message-container").scrollTop($("#message-container")[0].scrollHeight);
}

// === 페이지 로드 완료 후 실행되는 코드 ===
// jQuery의 $(function() { ... })는 $(document).ready(function() { ... })의 축약형
$(function () {
    // '연결' 버튼 클릭 시 connect 함수 실행
    $("#connect").click(connect);

    // '연결 끊기' 버튼 클릭 시 disconnect 함수 실행
    $("#disconnect").click(disconnect);

    // '전송' 버튼 클릭 시 sendMessage('TALK') 함수 실행
    $("#send").click(function() { sendMessage('TALK'); });

    // 메시지 입력 필드(#message)에서 키보드를 눌렀을 때 이벤트 처리
    $("#message").keypress(function(e) {
        // 눌린 키가 Enter 키(keyCode 13)이고, Shift 키가 동시에 눌리지 않았을 경우
        if (e.which === 13 && !e.shiftKey) {
            sendMessage('TALK'); // 'TALK' 메시지 전송
            e.preventDefault(); // Enter 키의 기본 동작(줄바꿈 등)을 막음
        }
    });
});