document.addEventListener("DOMContentLoaded", function () {
    const nicknameInput = document.getElementById("nickname");
    const introInput = document.getElementById("intro");
    const saveButton = document.getElementById("saveButton");
    const form = document.querySelector('form[data-has-withdraw-error]');
    const nicknameError = document.getElementById("nicknameError");

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

    function validateNickname() {
        const nickname = nicknameInput.value;

        // '상점..호' 패턴 정규식
        const reservedPattern = /^상점\s*\d+호$/;
        // 기존 형식 유효성 검사 정규식
        const formatPattern = /^(?=.*[a-zA-Z가-힣])[a-zA-Z0-9가-힣]{2,12}$/;

        if (reservedPattern.test(nickname)) {
            nicknameInput.classList.add("is-invalid");
            nicknameError.textContent = "'상점...호' 형식의 닉네임은 사용할 수 없습니다.";
            return false;
        } else if (!formatPattern.test(nickname)) {
            nicknameInput.classList.add("is-invalid");
            nicknameError.textContent = "2~12자의 한글/영문 또는 숫자 조합이어야 하며, 숫자만으로는 만들 수 없습니다.";
            return false;
        } else {
            nicknameInput.classList.remove("is-invalid");
            nicknameError.textContent = "";
            return true;
        }
    }

    function toggleSaveButton() {
        const currentNickname = nicknameInput.value.trim();
        const currentIntro = introInput.value.trim();

        const isEmpty = (currentNickname === "" && currentIntro === "");
        const isUnchanged = (currentNickname === originalNickname && currentIntro === originalIntro);

        const isNicknameValid = validateNickname();

        // 둘다 비어있거나 변경사항 없거나, 닉네임이 유효하지 않으면 비활성화
        saveButton.disabled = isEmpty || isUnchanged || !isNicknameValid;
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
    if (introInput) {
        updateIntroCharCount();
        toggleSaveButton();
    }

    const reportUserModal = document.getElementById('reportUserModal');
    if (reportUserModal) {
        const reasonSelect = document.getElementById('reportReason');
        const descriptionTextarea = document.getElementById('reportDescription');
        const descCountSpan = document.getElementById('reportDescCount');
        const submitBtn = document.getElementById('reportSubmitBtn');

        function toggleUserReportSubmitButton() {
            if (reasonSelect.value) {
                submitBtn.disabled = false;
            } else {
                submitBtn.disabled = true;
            }
        }

        function updateDescriptionCount() {
            if (descCountSpan) {
                descCountSpan.textContent = descriptionTextarea.value.length;
            }
        }

        reasonSelect.addEventListener('change', toggleUserReportSubmitButton);
        descriptionTextarea.addEventListener('input', updateDescriptionCount);

        reportUserModal.addEventListener('show.bs.modal', function () {
            reportUserModal.querySelector('form').reset();
            toggleUserReportSubmitButton();
            updateDescriptionCount();
        });
    }
});
