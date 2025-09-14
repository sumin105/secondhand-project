document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("reviewForm");
    const starRatingContainer = document.querySelector('.star-rating');
    const contentTextarea = document.getElementById('content');
    const charCountSpan = document.getElementById('charCount');

    if (contentTextarea && charCountSpan) {
        contentTextarea.addEventListener('input', function () {
            const currentLength = contentTextarea.value.length;
            charCountSpan.textContent = currentLength;
        });
    }

    if (form) {
        form.addEventListener('submit', function (event) {
            const ratingChecked = document.querySelector('input[name="rating"]:checked');
            if (!ratingChecked) {
                alert("별점은 필수 항목입니다.");
                event.preventDefault();
            }
        });
    }
});