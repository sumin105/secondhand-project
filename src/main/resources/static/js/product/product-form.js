document.addEventListener('DOMContentLoaded', function () {
    const dealMethodSelect = document.getElementById('dealMethod');
    const deliveryFeesDiv = document.getElementById('deliveryFees');
    const imageInput = document.getElementById('image');
    const imagePreview = document.getElementById('imagePreview');
    const descInput = document.getElementById('description');
    const descCount = document.getElementById('descCount');
    let selectedFiles = [];

    const titleInput = document.getElementById('title');
    const priceInput = document.getElementById('price');
    const form = document.querySelector('form');
    const normalFeeInput = document.getElementById('normalDeliveryFee');
    const cheapFeeInput = document.getElementById('cheapDeliveryFee');

    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB



    function updateDeliveryFees() {
        if (dealMethodSelect.value === 'DELIVERY') {
            deliveryFeesDiv.classList.remove('d-none');
        } else {
            deliveryFeesDiv.classList.add('d-none');
        }
    }

    if (dealMethodSelect) {
        dealMethodSelect.addEventListener('change', updateDeliveryFees);
        updateDeliveryFees();
    }

    function renderPreview() {
        imagePreview.innerHTML = '';

        selectedFiles.forEach((file, index) => {
            if (!file.type.startsWith('image/')) return;

            const reader = new FileReader();
            reader.onload = function (e) {
                const wrapper = document.createElement('div');
                wrapper.className = 'image-wrapper';

                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'preview-image';

                const btn = document.createElement('button');
                btn.innerText = '×';
                btn.type = 'button';
                btn.className = 'btn btn-sm btn-danger remove-preview-btn';
                btn.addEventListener('click', () => {
                    selectedFiles.splice(index, 1);
                    renderPreview();
                });

                wrapper.appendChild(img);
                wrapper.appendChild(btn);
                imagePreview.appendChild(wrapper);
            };
            reader.readAsDataURL(file);
        });

        // input의 파일 목록 동기화
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(file => dataTransfer.items.add(file));
        imageInput.files = dataTransfer.files;
    }

    imageInput.addEventListener('change', function () {
        const newFiles = Array.from(this.files); // input에서 선택한 파일을 배열로 변환
        const validFiles = [];

        // 새로 선택된 파일들 용량 먼저 검사
        for (const file of newFiles) {
            if (file.size > MAX_FILE_SIZE) {
                alert(`'${file.name}' 파일의 용량이 너무 큽니다. (최대 10MB)`);
                continue; // 용량이 큰 파일 건너뜀
            }
            validFiles.push(file);
        }

        // 누적된 파일과 새 파일을 합친 후 중복 제거
        selectedFiles = [...selectedFiles, ...validFiles];

        // 중복 파일 제거 (파일 이름 기준)
        selectedFiles = selectedFiles.filter((file, index, self) =>
            index === self.findIndex(f => f.name === file.name && f.size === file.size)
        );

        // 이미지 최대 5장 제한
        if (selectedFiles.length > 5) {
            alert("이미지는 최대 5장까지만 업로드할 수 있습니다.");
            selectedFiles = selectedFiles.slice(0, 5); // 무조건 5장까지만 유지
        }

        renderPreview();
    });

    descInput.addEventListener('input', () => {
        descCount.textContent = descInput.value.length;
    });

    // 숫자 입력창 자동 교정
    function sanitizeNumberInput(event) {
        const input = event.target;
        // 입력값을 정수로 변환하여 앞에 있는 0 등을 제거
        const sanitizedValue = parseInt(input.value, 10);

        // 변환된 값이 유효한 숫자인지 확인 후, 다시 입력창에 넣어줌
        if (!isNaN(sanitizedValue)) {
            input.value = sanitizedValue;
        }
    }

    priceInput?.addEventListener('blur', sanitizeNumberInput);
    normalFeeInput?.addEventListener('blur', sanitizeNumberInput);
    cheapFeeInput?.addEventListener('blur', sanitizeNumberInput);

    function handleFeeInputFocus(event) {
        if (event.target.value === '0') {
            event.target.value = '';
        }
    }

    function handleFeeInputBlur(event) {
        if (event.target.value === '') {
            event.target.value = '0'
        }
    }

    if (normalFeeInput) {
        normalFeeInput.addEventListener('focus', handleFeeInputFocus);
        normalFeeInput.addEventListener('blur', handleFeeInputBlur);
    }
    if (cheapFeeInput) {
        cheapFeeInput.addEventListener('focus', handleFeeInputFocus);
        cheapFeeInput.addEventListener('blur', handleFeeInputBlur);
    }

    function validateInput(input) {
        if (input.checkValidity()) {
            input.classList.remove('is-invalid');
        } else {
            input.classList.add('is-invalid');
        }
    }

    titleInput.addEventListener('input', () => validateInput(titleInput));
    priceInput.addEventListener('input', () => validateInput(priceInput));
    descInput.addEventListener('input', () => {
        validateInput(descInput);
        descCount.textContent = descInput.value.length;
    });
    normalFeeInput.addEventListener('input', () => validateInput(normalFeeInput));
    cheapFeeInput.addEventListener('input', () => validateInput(cheapFeeInput));

    form.addEventListener('submit', function(event) {
        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();

            Array.from(form.elements).forEach(element => {
                validateInput(element);
            });

            alert("입력 내용을 다시 확인해주세요.");
        }
    });
});