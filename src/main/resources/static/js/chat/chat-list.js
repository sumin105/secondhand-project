let stompClient = null;
const roomId = window.roomId || 0;
const loginUserId = window.loginUserId || 0;

function updateChatRoomUI(update) {
    const roomItem = document.querySelector(`.chat-room-item[data-room-id='${update.roomId}']`);
    if (!roomItem) return;

    const lastMessageSpan = roomItem.querySelector(".last-message");
    if (lastMessageSpan) lastMessageSpan.textContent = update.lastMessage;

    const timeSpan = roomItem.querySelector(".message-time");
    if (timeSpan) timeSpan.textContent = update.formattedTime;

    let badgeSpan = roomItem.querySelector(".badge");
    if (update.unreadCount > 0) {
        if (!badgeSpan) {
            badgeSpan = document.createElement("span");
            badgeSpan.className = "badge bg-danger ms-2";
            roomItem.querySelector(".room-title").appendChild(badgeSpan);
        }
        badgeSpan.textContent = update.unreadCount;
    } else {
        if (badgeSpan) badgeSpan.remove();
    }

    updateNavUnreadCount();
}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = StompJs.Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log('웹소켓 연결 완료');

        // 로그인 유저에게 보내는 채팅방 업데이트 수신
        stompClient.subscribe('/topic/chat-room/' + loginUserId, function (message) {
            const update = JSON.parse(message.body);
            console.log("수신된 chat-room 메시지:", update);
            updateChatRoomUI(update);
        });
    }, function (error) {
        console.error('웹소켓 연결 에러:', error);
    });
}

function initializeChatList() {
    document.querySelectorAll('.chat-room-item').forEach(item => {
        item.addEventListener('click', function () {
            const userId = this.dataset.userId;
            if (userId) {
                window.location.href = `/chat/user/${userId}`;
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", function () {
    connectWebSocket();
    initializeChatList();
});
