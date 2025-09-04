document.addEventListener('DOMContentLoaded', function () {
    const contentDiv = document.getElementById('div[layout\\:fragment="content"]');
    const errorMessage = contentDiv ? contentDiv.dataset.errorMessage : null;

    if (errorMessage) {
        const modalEl = document.getElementById('editUserModal');
        if (modalEl) {
            const modal = new bootstrap.Modal(modalEl);
            modal.show();

            const nicknameInput = document.getElementById('nickname');
            if (nicknameInput) {
                modalEl.addEventListener('shown.bs.modal', function () {
                    nicknameInput.focus();
                }, { once: true });
            }
        }
    }
});