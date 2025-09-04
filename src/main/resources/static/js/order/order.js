document.addEventListener("DOMContentLoaded", function () {
    const closeSuccess = document.getElementById("closeSuccess");
    const closeError = document.getElementById("closeError");

    if (closeSuccess) {
        closeSuccess.addEventListener("click", () => {
            const box = document.getElementById("successMessageBox");
            if (box) {
                box.classList.add("d-none");
            }
        });
    }

    if (closeError) {
        closeError.addEventListener("click", () => {
            const box = document.getElementById("errorMessageBox");
            if (box) {
                box.classList.add("d-none");
            }
        });
    }
});