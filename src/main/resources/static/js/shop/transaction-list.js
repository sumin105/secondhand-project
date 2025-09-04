document.addEventListener("DOMContentLoaded", function () {
    // 카드 클릭 시 페이지 이동
    const clickableCards = document.querySelectorAll('.clickable-card');

    clickableCards.forEach(card => {
        card.addEventListener('click', function () {
            const url = this.dataset.url;
            if (url) {
                window.location.href = url;
            }
        });
    });
});