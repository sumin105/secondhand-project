document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("reviewForm");

    form.addEventListener("submit", function (event) {
        const ratingChecked = document.querySelector('input[name="rating"]:checked');
        if (!ratingChecked) {
            alert("별점을 선택해주세요.");
            event.preventDefault(); // 폼 제출 막기
        }
    });
});