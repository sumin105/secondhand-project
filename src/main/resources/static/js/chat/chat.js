let stompClient = null;
let selectedImageFile = null;
const roomId = parseInt(document.body.dataset.roomId);
const senderId = parseInt(document.body.dataset.senderId);

const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

function isSameDay(date1, date2) {
    return date1.getFullYear() === date2.getFullYear() &&
        date1.getMonth() === date2.getMonth() &&
        date1.getDate() === date2.getDate();
}

function formatDateLabel(date) {
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
}

function formatTime(isoString) {
    const date = new Date(isoString);
    return date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
}

// 새로 추가한 부분
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

function appendMessage(msg) {
    console.log("실시간 수신된 메시지:", msg);

    const chatBox = document.getElementById("chat-box");
    const imMe = msg.senderId === senderId;
    const className = imMe ? 'me' : 'other';

    const wrapper = document.createElement("div");
    wrapper.classList.add("message-wrapper", className);
    if (msg.id !== undefined && msg.id !== null) {
        wrapper.setAttribute("data-message-id", msg.id);
    }

    if (msg.messageType === "ORDER") {
        const card = document.createElement("div");
        card.classList.add("order-card");

        // 이미지
        const img = document.createElement("img");
        img.src = msg.imageUrl;
        img.classList.add("order-image");
        img.onclick = () => location.href = `/products/${msg.productId}`;

        // 주문 상태
        const status = document.createElement("div");
        status.textContent = msg.orderStatus;
        status.classList.add("order-status");

        // 버튼
        const btn = document.createElement("button");
        btn.textContent = msg.buttonText;

        btn.className = "btn btn-sm btn-outline-primary order-button";

        if (msg.userRole === "BUYER") {
            btn.onclick = () => location.href = `/orders/${msg.orderId}`;
        } else if (msg.userRole === "SELLER") {
            btn.onclick = () => location.href = `/orders/${msg.orderId}`;
        }

        card.appendChild(img);
        card.appendChild(status);
        card.appendChild(btn);

        if (msg.trackingUrl) {
            const trackingBtn = document.createElement("button");
            trackingBtn.textContent = "배송조회";
            trackingBtn.className = "btn btn-sm btn-outline-primary order-button";
            trackingBtn.onclick = () => window.open(msg.trackingUrl, '_blank');
            card.appendChild(trackingBtn);
        }

        wrapper.appendChild(card);

        const timeDiv = document.createElement("div");
        timeDiv.classList.add("timestamp-system");
        timeDiv.textContent = formatTime(msg.sentAt || new Date());
        wrapper.appendChild(timeDiv);
    } else if (msg.messageType === "REVIEW") {
        const card = document.createElement("div");
        card.classList.add("review-card");

        const topText = document.createElement("div");
        topText.classList.add("review-top-text");
        const centerText = document.createElement("div");
        centerText.classList.add("review-center-text");
        const btn = document.createElement("button");
        btn.classList.add("btn", "btn-primary", "review-btn");
        btn.textContent = "후기 확인하기";
        btn.onclick = () => {
            location.href = `/shop/${msg.targetId}/reviews`;
        };

        if (msg.writerId === senderId) {
            topText.textContent = "후기를 남겼어요.";
            centerText.textContent = "작성한 후기는 다른 거래에 큰 도움이 됩니다!";
        } else if (msg.targetId === senderId) {
            topText.textContent = "후기가 도착했어요.";
            centerText.textContent = `${msg.targetId}님이 후기를 남겼어요!`;
        }
        card.appendChild(topText);
        card.appendChild(centerText);
        card.appendChild(btn);

        wrapper.appendChild(card);

        const timeDiv = document.createElement("div");
        timeDiv.classList.add("timestamp-system");
        timeDiv.textContent = formatTime(msg.sentAt || new Date());
        wrapper.appendChild(timeDiv);
    } else if (msg.messageType === "IMAGE") {
        const imageContainer = document.createElement("div");
        imageContainer.classList.add("chat-image-container");

        const image = document.createElement("img");
        image.src = msg.chatImageUrl;
        image.classList.add("chat-image");
        image.alt = "전송된 이미지";
        image.onclick = () => {
            // 큰 이미지 보기 등ㅇ 확대 기능 추가시 여기에 구현
            window.open(msg.chatImageUrl, '_blank');
        };

        imageContainer.appendChild(image);
        wrapper.appendChild(imageContainer);

        const timeDiv = document.createElement("div");
        timeDiv.classList.add("timestamp");
        timeDiv.textContent = formatTime(msg.sentAt || new Date());

        if (imMe && msg.read === false) {
            const unreadSpan = document.createElement("span");
            unreadSpan.classList.add("unread-mark");
            unreadSpan.textContent = " (안읽음)";
            timeDiv.appendChild(unreadSpan);
        }
        wrapper.appendChild(timeDiv);
    } else {
        // 일반 텍스트 메시지, 삭제된 후기 메시지
        const messageDiv = document.createElement("div");
        messageDiv.classList.add("message", className);
        messageDiv.textContent = msg.message;

        if (msg.messageType === 'DELETED_REVIEW') {
            messageDiv.classList.add("deleted-review-message");
        }

        const timeDiv = document.createElement("div");
        timeDiv.classList.add("timestamp");
        timeDiv.textContent = formatTime(msg.sentAt || new Date());

        // "안읽음"을 timestamp 옆에 붙이기
        if (imMe && msg.read === false) {
            const unreadSpan = document.createElement("span");
            unreadSpan.classList.add("unread-mark");
            unreadSpan.textContent = " (안읽음)"; // 괄호로 묶어도 되고 안 묶어도 됨

            timeDiv.appendChild(unreadSpan);
        }
        wrapper.setAttribute('data-message-id', msg.id);
        wrapper.appendChild(messageDiv);
        wrapper.appendChild(timeDiv);
    }
    chatBox.appendChild(wrapper);
    chatBox.scrollTop = chatBox.scrollHeight;

    updateNavUnreadCount();
}

// 이전 메시지 로드
function loadPreviousMessages() {
    return fetch(`/api/chat/${roomId}/messages`)
        .then(response => {
            if (!response.ok) throw new Error("이전 메시지를 불러오지 못했습니다.");
            return response.json();
        })
        .then(messages => {
            const chatBox = document.getElementById("chat-box");
            chatBox.innerHTML = ''; // 초기화

            let lastDate = null;

            messages.forEach(msg => {
                const sentDate = new Date(msg.sentAt);

                // 날짜가 달라지면 날짜 라벨 삽입
                if (!lastDate || !isSameDay(sentDate, lastDate)) {
                    const dateLabel = document.createElement("div");
                    dateLabel.classList.add("date-label");
                    dateLabel.textContent = formatDateLabel(sentDate);
                    chatBox.appendChild(dateLabel);
                    lastDate = sentDate;
                }
                appendMessage(msg);
            });
        })
        .catch(error => {
            console.error("이전 메시지 로딩 실패:", error);
        });
}

function connect() {
    const socket = new SockJS('/ws'); // 백엔드 WebSocketConfig와 일치
    stompClient = StompJs.Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('connected: ' + frame);

        // 채팅방 메시지 수신
        stompClient.subscribe('/topic/chat/' + roomId, function (message) {
            const msg = JSON.parse(message.body);

            appendMessage(msg);

            // 현재 이 채팅방을 열어둔 상태이므로, 실시간으로 읽음 처리
            fetch(`/api/chat/${roomId}/messages/read`, {
                method: "POST",
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(res => {
                    if (!res.ok) throw new Error("실시간 읽음 처리 실패");

                    // 뱃지가 다시 생겼다면 제거
                    const badgeSpan = document.querySelector(`.chat-room-item[onclick*='${roomId}'] .badge`);
                    if (badgeSpan) badgeSpan.remove();

                    updateNavUnreadCount();

                    console.log("실시간 읽음 처리 완료");
                })
                .catch(err => {
                    console.error("실시간 읽음 처리 오류:", err);
                });
        });

        // 채팅방 목록 UI 업데이트 수신
        stompClient.subscribe('/topic/chat-room/' + senderId, function (message) {
            const update = JSON.parse(message.body);
            updateChatRoomUI(update);
        });

        // 읽음 알림 수신 처리
        stompClient.subscribe('/topic/chat-read/' + senderId, function (message) {
            console.log("상대방이 내 메시지를 읽음 (전체 '안읽음' 제거)");

            // 내가 보낸 메시지 중 unread-mark가 있는 것들 제거
            document.querySelectorAll('.message-wrapper.me .unread-mark').forEach(unreadSpan => {
                unreadSpan.remove();
            });
        });

    }, function (error) {
        console.error('stomp 연결 에러:', error);
        alert('웹소켓 연결에 실패했습니다. 페이지를 새로고침 해주세요.');
    });
}

function sendMessage() {
    const content = document.getElementById("messageInput").value.trim();

    if (!stompClient || !stompClient.connected) {
        alert("웹소켓 연결이 완료되지 않았습니다. 잠시 후 다시 시도해 주세요.");
        return;
    }

    // 1. 이미지 전송하는 경우
    if (selectedImageFile) {
        const formData = new FormData();
        formData.append("image", selectedImageFile);
        formData.append("roomId", roomId);
        formData.append("senderId", senderId);

        fetch(`/api/chat/${roomId}/images`, {
            method: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            body: formData
        })
            .then(res => {
                if (!res.ok) throw new Error("이미지 업로드 실패");
                return res.json();
            })
            .then(response => {
                const imageUrl = response.imageUrl;
                // 서버에서 imageUrl을 반환
                stompClient.send("/app/chat/" + roomId, {}, JSON.stringify({
                    roomId: roomId,
                    senderId: senderId,
                    imageUrl: imageUrl,
                    messageType: "IMAGE"
                }));

                document.querySelector('.image-preview-wrapper')?.remove();
                selectedImageFile = null;
            })
            .catch(err => {
                alert("이미지 전송 중 오류 발생");
                console.error(err);
            });

        return; // 이미지 전송 시 일반 텍스트는 무시
    }

    // 2. 일반 텍스트 메시지 전송
    if (content === '') return;

    stompClient.send("/app/chat/" + roomId, {}, JSON.stringify({
        roomId: roomId,
        senderId: senderId,
        content: content,
        messageType: "TEXT"
    }));
    console.log("메시지 전송 시도:", roomId, senderId, content);
    document.getElementById("messageInput").value = '';
}

// 이미지 전송
function sendImageMessage() {
    const fileInput = document.getElementById('imageInput');
    const file = fileInput.files[0];
    if (!file) return;

    selectedImageFile = file;

    const reader = new FileReader();
    reader.onload = function (e) {
        const previewUrl = e.target.result;

        const chatBox = document.getElementById("chat-box");

        const existingPreview = document.querySelector('.image-preview-wrapper');
        if (existingPreview) existingPreview.remove();

        const wrapper = document.createElement("div");
        wrapper.classList.add("message-wrapper", "me", "image-preview-wrapper");

        const imagePreviewContainer = document.createElement("div");
        imagePreviewContainer.classList.add("chat-image-preview-container");

        const imageElement = document.createElement("img");
        imageElement.src = previewUrl;
        imageElement.classList.add("chat-image-preview"); // 미리보기용 스타일

        const removeBtn = document.createElement("button");
        removeBtn.textContent = "x";
        removeBtn.classList.add("remove-preview-btn");
        removeBtn.onclick = () => {
            wrapper.remove();
            selectedImageFile = null;
        }

        imagePreviewContainer.appendChild(imageElement);
        imagePreviewContainer.appendChild(removeBtn);
        wrapper.appendChild(imagePreviewContainer);
        chatBox.appendChild(wrapper);
        chatBox.scrollTop = chatBox.scrollHeight;

        // ▶️ 실제 업로드 로직은 여기에서 추가로 구현 필요
        // 예: formData로 서버 전송 후 imageUrl 받아와 WebSocket 전송 등
    };
    reader.readAsDataURL(file);
    fileInput.value = ''; // 초기화
}

// 초기화: 이전 메시지 불러온 후 읽음 처리, 그 다음 웹소켓 연결
loadPreviousMessages()
    .then(() => {
        return fetch(`/api/chat/${roomId}/messages/read`, {
            method: "POST",
            headers: {
                [csrfHeader]: csrfToken
            }
        });
    })
    .then(res => {
        if (!res.ok) throw new Error("읽음 처리 실패");

        const badgeSpan = document.querySelector(`.chat-room-item[onclick*='${roomId}'] .badge`);
        if (badgeSpan) badgeSpan.remove();

        console.log("읽음 처리 완료 + UI 뱃지 제거");
    })
    .catch(err => {
        console.error("읽음 처리 오류:", err);
    })
    .finally(() => {
        connect(); // 무조건 connect는 실행되게
    });

document.addEventListener("DOMContentLoaded", () => {
    const chatRoomItems = document.querySelectorAll(".chat-room-item");
    chatRoomItems.forEach(item => {
        item.addEventListener("click", () => {
            const userId = item.dataset.userId;
            if (userId) {
                location.href = `${userId}`;
            }
        });
    });

    const sendBtn = document.getElementById("sendMessageBtn");
    sendBtn?.addEventListener("click", sendMessage);

    const imageInput = document.getElementById("imageInput");
    imageInput?.addEventListener("change", sendImageMessage);
});