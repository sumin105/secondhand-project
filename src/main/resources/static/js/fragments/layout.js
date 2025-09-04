document.addEventListener('DOMContentLoaded', function () {
    // 로그인된 사용자를 위한 UI 초기화
    function initializeUserUI() {
        console.log("[DEBUG] initializeUserUI: 함수 실행 시작");

        // 로그아웃 버튼
        function handleLogout(event) {
            event.preventDefault();
            console.log("-> [DEBUG] 로그아웃 버튼 클릭됨!");

            console.log("-> [DEBUG] fetch /logout 요청 전송 시도...");
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
                    console.error("-> [DEBUG] 로그아웃 fetch 요청 실패:", error);
                    alert("로그아웃 중 오류가 발생했습니다.");
                    window.location.href = '/'; // 오류 발생시 홈으로
                });
        }

        const logoutBtn = document.getElementById('logoutBtn');

        if (logoutBtn) {
            console.log("[DEBUG] initializeUserUI: 일반 로그아웃 버튼에 이벤트 리스너 추가");
            logoutBtn.addEventListener('click', handleLogout);
        }

        // 관리자 로그아웃
        const adminLogoutBtn = document.getElementById('adminLogoutBtn');
        if (adminLogoutBtn) {
            console.log("[DEBUG] initializeUserUI: 관리자 로그아웃 버튼에 이벤트 리스너 추가");
            adminLogoutBtn.addEventListener('click', handleLogout);
        }
        // 안 읽은 채팅 수 갱신
        console.log("[DEBUG] initializeUserUI: updateNavUnreadCount 호출");
        updateNavUnreadCount();
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
                console.log("Silent refresh successful. Reloading page to apply UI changes.");
                window.location.reload();
            } else if (response.ok) {
                console.log("Login status confirmed. Initializing user UI.");
                initializeUserUI();
            } else {
                console.log("Login status check failed. Initializing guest UI.");
                initializeGuestUI();
            }
        } catch (error) {
            console.error("Error during login status check:", error);
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
            console.log(`[API 요청] 주소: ${url}, 상태: ${res.status}`);

            if (res.ok) return res;

            if (res.status === 401) {
                console.log("-> 401 에러 감지! 토큰 재발급을 시도합니다.");

                // accessToken 만료 시 refresh 시도
                return fetch('/api/token/refresh', {
                    method: 'POST',
                    credentials: 'include', // 쿠키 기반 인증 시 필수
                    headers: {
                        [csrfHeader]: csrfToken
                    }
                }).then(refreshRes => {
                    console.log("-> 재발급 API(/api/token/refresh) 응답 상태:", refreshRes.status);

                    if (!refreshRes.ok) {
                        throw new Error("토큰 재발급 실패");
                    }
                    // 토큰 재발급 성공 -> 원래 요청 재시도
                    console.log("-> 토큰 재발급 성공! 원래 요청을 재시도합니다.");
                    return fetch(url, customOptions);
                });
            }
            throw new Error("요청 실패: " + res.status);
        });
}

function updateNavUnreadCount() {
    fetchWithRefresh('/api/chat/unread-count')
        .then(res => res.json())
        .then(count => {
            const navBadge = document.getElementById("nav-unread-count");
            if (!navBadge) return;
            if (count > 0) {
                navBadge.textContent = count > 99 ? "99+" : count;
                navBadge.style.display = "inline-block";
            } else {
                navBadge.style.display = "none";
            }
        })
        .catch(err => console.error("네비바 안 읽은 메시지 수 갱신 실패:", err));
}
