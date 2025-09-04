document.addEventListener("DOMContentLoaded", function () {
    const nicknameInput = document.getElementById("nickname");
    const introInput = document.getElementById("intro");
    const saveButton = document.getElementById("saveButton");
    const form = document.querySelector('form[data-has-withdraw-error]');

    const hasWithdrawError = form?.dataset.hasWithdrawError === "true";
    if (hasWithdrawError) {
        const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
        deleteModal.show();
    }

    let originalNickname = nicknameInput.value.trim();
    let originalIntro = introInput.value.trim();

    function updateIntroCharCount() {
        const countSpan = document.getElementById("introCharCount");
        countSpan.textContent = introInput.value.length;
    }

    function toggleSaveButton() {
        const currentNickname = nicknameInput.value.trim();
        const currentIntro = introInput.value.trim();

        const isEmpty = (currentNickname === "" && currentIntro === "");
        const isUnchanged = (currentNickname === originalNickname && currentIntro === originalIntro);

        // 둘다 비어있거나 변경사항 없으면 비활성화
        saveButton.disabled = isEmpty || isUnchanged;
    }

    nicknameInput.addEventListener("input", function () {
        toggleSaveButton();
        updateIntroCharCount();
    });

    introInput.addEventListener("input", function () {
        toggleSaveButton();
        updateIntroCharCount();
    });

    const confirmDeleteBtn = document.getElementById('confirm-delete-btn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function () {
            const deleteModalButton = document.querySelector('button[data-bs-target="#deleteConfirmModal"]');
            const csrfToken = deleteModalButton.dataset.csrfToken;
            const csrfHeader = deleteModalButton.dataset.csrfHeader;

            const errorDiv = document.getElementById('withdrawErrorMessage');
            errorDiv.innerText = '';
            errorDiv.classList.add('d-none');

            fetch('/user', {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    if (response.ok) {
                        alert("회원 탈퇴가 완료되었습니다.");
                        window.location.href = '/logout'; // 로그아웃 처리
                    } else {
                        // 400 Bad Request 등
                        return response.text().then(errorMessage => {
                            console.log("서버로부터 받은 에러 메시지:", errorMessage);
                            console.log("에러를 표시할 DIV 요소:", errorDiv);

                            if (errorDiv) {
                                errorDiv.innerText = errorMessage;
                                errorDiv.classList.remove('d-none');
                            }
                        });
                    }
                })
                .catch(error => {
                    console.error("탈퇴 요청 중 에러 발생: ", error);
                    if (errorDiv) {
                        errorDiv.innerText = "요청 처리 중 문제가 발생했습니다. 네트워크 연결을 확인해주세요.";
                        errorDiv.classList.remove('d-none');
                    }
                    alert("탈퇴 처리 중 오류가 발생했습니다.");
                })
        })
    }

    // 모달이 열릴 때 현재 값 기준으로 버튼 상태 갱신
    const editModal = document.getElementById('editUserModal');
    editModal.addEventListener('shown.bs.modal', function () {
        originalNickname = nicknameInput.value.trim();
        originalIntro = introInput.value.trim();

        updateIntroCharCount(); // 현재 글자 수 표시
        toggleSaveButton(); // 저장 버튼 상태
    });

    // 최초 상태 초기화
    updateIntroCharCount();
    toggleSaveButton();


});
