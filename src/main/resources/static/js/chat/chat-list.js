const roomId = window.roomId || 0;
const loginUserId = document.body.dataset.userId;

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
}

function subscribeToChatListTopics() {

    // 로그인 유저에게 오는 모든 채팅방 업데이트 수신
    stompClient.subscribe('/topic/chat-room/' + loginUserId, function (message) {
        const update = JSON.parse(message.body);
        updateChatRoomUI(update);
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
    // layout.js의 stompClient 연결을 기다린 후 구독 시작
    function waitForSocketConnection(callback) {
        setTimeout(function () {
            // layout.js에 선언된 전역 stompClient를 확인
            if (typeof stompClient !== 'undefined' && stompClient && stompClient.connected) {
                callback();
            } else {
                waitForSocketConnection(callback);
            }
        }, 100);
    }

    waitForSocketConnection(subscribeToChatListTopics);
    initializeChatList();
});
