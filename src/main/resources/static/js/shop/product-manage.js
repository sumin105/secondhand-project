document.addEventListener("DOMContentLoaded", function () {

    const deleteBtn = document.querySelectorAll('.delete-btn');

    deleteBtn.forEach(button => {
        button.addEventListener('click', function (event) {

            if (!confirm('정말로 삭제하시겠습니까?')) {
                return;
            }

            const productId = this.dataset.productId;
            const url = `/products/${productId}`;

            fetch(url, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfHeader ? csrfToken : undefined
                }
            })
                .then(response => {
                    if (response.ok) {
                        alert("상품이 삭제되었습니다.");
                        window.location.reload();
                    } else {
                        alert("삭제에 실패했습니다.");
                    }
                })
                .catch(error => {
                    console.error("삭제 요청 중 에러 발생: ", error);
                    alert("삭제 중 오류가 발생했습니다.");
                });

        });
    });
});