document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    function deleteReview(reviewId) {
        if (!confirm("정말로 후기를 삭제하시겠습니까?")) {
            return;
        }

        fetch(`/reviews/${reviewId}`, {
            method: 'DELETE',
            headers: {
                [csrfHeader]: csrfToken
            }
        })
            .then(response => {
                if (response.ok) {
                    // 성공적으로 삭제되면 페이지 새로고침
                    alert("후기가 삭제되었습니다.");
                    location.reload();
                } else {
                    // 실패 시 서버에서 받은 메시지 표시
                    response.text().then(text => alert(`삭제 실패: ${text}`));
                }
            })
            .catch(error => {
                console.error("삭제 요청 중 오류 발생:", error);
                alert("삭제 중 오류가 발생했습니다.");
            });
    }

    // 모든 삭제 버튼에 이벤트 리스너를 붙이는 함수
    function attachDeleteEventListeners() {
        const deleteButtons = document.querySelectorAll('.delete-review-btn');
        deleteButtons.forEach(button => {
            // 이미 리스너가 할당된 버튼은 건너뛰기
            if (button.dataset.listenerAttached) return;

            button.addEventListener('click', function () {
                const reviewId = this.dataset.reviewId;
                deleteReview(reviewId);
            });
            // 리스너가 할당되었음을 표시
            button.dataset.listenerAttached = 'true';
        });
    }

    // 페이지 초기 로드 시 존재하는 삭제 버튼에 이벤트 리스너 할당
    attachDeleteEventListeners();

});