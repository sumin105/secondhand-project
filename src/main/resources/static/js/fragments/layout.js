let stompClient = null;

document.addEventListener('DOMContentLoaded', function () {
    // 로그인된 사용자를 위한 UI 초기화
    function initializeUserUI() {

        // 로그아웃 버튼
        function handleLogout(event) {
            event.preventDefault();

            fetch('/logout', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    window.location.href = '/';
                })
                .catch(error => {
                    alert("로그아웃 중 오류가 발생했습니다.");
                    window.location.href = '/'; // 오류 발생시 홈으로
                });
        }

        const logoutBtn = document.getElementById('logoutBtn');

        if (logoutBtn) {
            logoutBtn.addEventListener('click', handleLogout);
        }

        // 관리자 로그아웃
        const adminLogoutBtn = document.getElementById('adminLogoutBtn');
        if (adminLogoutBtn) {
            adminLogoutBtn.addEventListener('click', handleLogout);
        }

        connectWebSocket();
    }

    // 로그인 안 된 사용자용 UI 초기화
    function initializeGuestUI() {
        // 로그인 모달 열기 버튼들
        const loginTriggers = [
            'loginShopBtn', 'loginUploadBtn', 'loginChatBtn', 'loginBtn', 'loginProductBtn'
        ];
        loginTriggers.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener('click', function (e) {
                    e.preventDefault();
                    openLoginModal();
                });
            }
        });
    }

    // 페이지 로드 시 로그인 상태 확인을 시도하여 UI 결정
    (async function checkLoginStatus() {
        try {
            const response = await fetch('/api/auth/status', {
                method: 'GET',
                credentials: 'include',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });

            if (response.status === 201) {
                window.location.reload();
            } else if (response.ok) {
                initializeUserUI();
            } else {
                initializeGuestUI();
            }
        } catch (error) {
            initializeGuestUI();
        }
    })();

    // 탈퇴한 계정 에러 메시지 처리는 재발급과 별개로 진행
    const urlParams = new URLSearchParams(window.location.search);
    const errorType = urlParams.get('error');
    if (errorType === 'disabledAccount') {
        alert("이미 탈퇴한 계정입니다. 새로운 이메일로 회원가입해주세요");
        history.replaceState({}, document.title, "/");
    }
});

function openLoginModal() {
    const modal = new bootstrap.Modal(document.getElementById('loginModal'));
    modal.show();
}

const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

// JWT 자동 재발급을 포함한 fetch wrapper
function fetchWithRefresh(url, options = {}) {
    const customOptions = {
        ...options,
        headers: {
            ...options.headers,
            [csrfHeader]: csrfToken
        }
    };

    return fetch(url, customOptions)
        .then(res => {
            if (res.ok) return res;

            if (res.status === 401) {

                // accessToken 만료 시 refresh 시도
                return fetch('/api/token/refresh', {
                    method: 'POST',
                    credentials: 'include', // 쿠키 기반 인증 시 필수
                    headers: {
                        [csrfHeader]: csrfToken
                    }
                }).then(refreshRes => {
                    if (!refreshRes.ok) {
                        throw new Error("토큰 재발급 실패");
                    }
                    // 토큰 재발급 성공 -> 원래 요청 재시도
                    return fetch(url, customOptions);
                });
            }
            throw new Error("요청 실패: " + res.status);
        });
}

// 웹소켓 연결 및 구독
function connectWebSocket() {
    const loginUserId = document.body.dataset.userId;
    if (loginUserId) {
        const socket = new SockJS('/ws');
        // 전역 변수 stompClient에 할당
        stompClient = StompJs.Stomp.over(socket);
        stompClient.debug = () => {};

        stompClient.connect({}, function (frame) {

            // 전체 안 읽은 개수 토픽 구독
            stompClient.subscribe('/topic/unread-count/' + loginUserId, function (message) {
                const count = JSON.parse(message.body);
                displayNavUnreadCount(count); // UI 업데이트
            });

            // 개인 에러 메시지 토빅 구독
            stompClient.subscribe('/user/queue/error', function (message) {
                const errorMessage = message.body;
                console.error("WebSocket Error Received:", errorMessage);
                alert(errorMessage);
            });

            // 페이지 로드 시, 웹소켓 연결 성공 직후 최신 안 읽은 개수 가져오기
            updateNavUnreadCount();
        });
    } else {
        // 비로그인 사용자는 웹소켓 연결 없이 API로만 개수 가져오기
        updateNavUnreadCount();
    }

}

function displayNavUnreadCount(count) {
    const navBadge = document.getElementById("nav-unread-count");
    if (!navBadge) return;
    if (count > 0) {
        navBadge.textContent = count > 99 ? "99+" : count;
        navBadge.style.display = "inline-block";
    } else {
        navBadge.style.display = "none";
    }
}

function updateNavUnreadCount() {
    fetchWithRefresh('/api/chat/unread-count')
        .then(res => res.json())
        .then(count => {
            displayNavUnreadCount(count);
        })
        .catch(err => console.error("네비바 안 읽은 메시지 수 갱신 실패:", err));
}
