document.addEventListener('DOMContentLoaded', function () {
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    // 정지/해제 폼
    document.querySelectorAll('form.confirm-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            const message = form.getAttribute('data-confirm');
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    // 탈퇴 버튼
    document.querySelectorAll('.delete-user-btn').forEach(button => {
        button.addEventListener('click', function () {
            if (!confirm('정말로 이 사용자를 탈퇴시키겠습니까?')) {
                return;
            }

            const userId = this.dataset.userId;

            fetch(`/admin/users/${userId}`, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    if (response.ok) {
                        alert("사용자가 탈퇴 처리되었습니다.");
                        window.location.reload();
                    } else {
                        response.text().then(text => {
                            alert(`탈퇴 처리에 실패했습니다: ${text}`);
                        });
                    }
                })
                .catch(error => {
                    console.error("탈퇴 요청 중 에러 발생: ", error);
                    alert("탈퇴 처리 중 오류가 발생했습니다.");
                });
        });
    });
});
