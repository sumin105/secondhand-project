document.addEventListener('DOMContentLoaded', function () {

    document.querySelectorAll('.delete-product-btn').forEach(button => {
        button.addEventListener('click', function () {
            if (!confirm('정말로 이 상품을 삭제하시겠습니까?')) {
                return;
            }

            const productId = this.dataset.productId;

            fetch(`/admin/products/${productId}`, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    if (response.ok) {
                        alert("상품이 삭제되었습니다.");
                        window.location.reload();
                    } else {
                        alert("삭제에 실패했습니다. 다시 시도해주세요.");
                    }
                })
                .catch(error => {
                    console.error("삭제 요청 중 에러 발생: ", error);
                    alert("삭제 중 오류가 발생했습니다.");
                });
        });
    });
});