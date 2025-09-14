document.addEventListener('DOMContentLoaded', () => {
    const dealMethod = document.getElementById('dealMethod');
    const deliveryFees = document.getElementById('deliveryFees');
    const imageInput = document.getElementById('image');
    const imagePreview = document.getElementById('imagePreview');
    const descInput = document.getElementById('description');
    const descCount = document.getElementById('descCount');
    const productEditForm = document.getElementById('productEditForm');
    let selectedFiles = [];

    const titleInput = document.getElementById('title');
    const priceInput = document.getElementById('price');
    const normalFeeInput = document.getElementById('normalDeliveryFee');
    const cheapFeeInput = document.getElementById('cheapDeliveryFee');

    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    function toggleDeliveryFeeFields() {
        if (dealMethod.value === 'DELIVERY') {
            deliveryFees.classList.remove('d-none');
        } else {
            deliveryFees.classList.add('d-none');
        }
    }

    dealMethod.addEventListener('change', toggleDeliveryFeeFields);
    toggleDeliveryFeeFields();

    imagePreview.addEventListener('click', function (event) {
        const btn = event.target.closest('.btn-remove-existing') // 기존 이미지 제거
        if (btn) {
            const imageId = btn.getAttribute('data-image-id');
            const imgDiv = document.getElementById(`existing-image-${imageId}`);

            // 이미 추가된 hidden input이 있는지 확인
            let existingInput = productEditForm.querySelector(`input[name="deletedImageIds"][value="${imageId}"]`);
            if (!existingInput) {
                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'deletedImageIds';
                hiddenInput.value = imageId;
                productEditForm.appendChild(hiddenInput);
            }
            if (imgDiv) {
                imgDiv.remove();
            }
        }

        const removeNewBtn = event.target.closest('.btn-remove-new'); // 새 이미지 제거
        if (removeNewBtn) {
            const index = parseInt(removeNewBtn.dataset.index, 10);
            if (!isNaN(index)) {
                selectedFiles.splice(index, 1);
                renderNewImages();
            }
        }
    });

    function renderNewImages() {
        // 새로 추가되니 이미지를 표시하는 영역만 찾아서 비움
        imagePreview.querySelectorAll('.new-image-wrapper').forEach(wrapper => wrapper.remove());

        selectedFiles.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = function (e) {
                const wrapper = document.createElement('div');
                wrapper.className = 'new-image-wrapper';

                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'preview-image';

                const btn = document.createElement('button');
                btn.innerText = 'x';
                btn.type = 'button';
                btn.className = 'btn btn-sm btn-danger btn-remove-new';
                btn.dataset.index = index;

                wrapper.appendChild(img);
                wrapper.appendChild(btn);
                imagePreview.appendChild(wrapper);
            };
            reader.readAsDataURL(file);
        });

        // input 업데이트
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(file => dataTransfer.items.add(file));
        imageInput.files = dataTransfer.files;
    }

    imageInput.addEventListener('change', function () {
        const newFiles = Array.from(this.files); // input에서 선택한 파일을 배열로 변환
        const existingCount = imagePreview.querySelectorAll('.existing-image').length;

        const validFiles = [];
        // 새로 선택한 파일들의 용량 먼저 검사
        for (const file of newFiles) {
            if (file.size > MAX_FILE_SIZE) {
                alert(`'${file.name}' 파일의 용량이 너무 큽니다. (최대 10MB)`);
                continue; // 용량이 큰 파일은 건너뜀
            }
            validFiles.push(file);
        }

        // 합쳐서 중복 제거
        selectedFiles = [...selectedFiles, ...validFiles];

        selectedFiles = selectedFiles.filter((file, index, self) =>
            index === self.findIndex(f => f.name === file.name && f.size === file.size)
        );

        // 이미지 최대 5장 제한
        const totalNewCount = selectedFiles.length;
        if (existingCount + totalNewCount > 5) {
            alert("이미지는 최대 5장까지만 업로드할 수 있습니다.");
            const allowedNewCount = 5 - existingCount;
            selectedFiles = selectedFiles.slice(0, allowedNewCount); // 무조건 5장까지만 유지
        }

        renderNewImages();
    });

    function validateInput(input) {
        if (!input) return;
        if (input.checkValidity()) {
            input.classList.remove('is-invalid');
        } else {
            input.classList.add('is-invalid');
        }
    }

    titleInput.addEventListener('input', () => validateInput(titleInput));
    priceInput.addEventListener('input', () => validateInput(priceInput));
    normalFeeInput.addEventListener('input', () => validateInput(normalFeeInput));
    cheapFeeInput.addEventListener('input', () => validateInput(cheapFeeInput));

    if (descInput && descCount) {
        descCount.textContent = descInput.value.length;
        descInput.addEventListener('input', () => {
            descCount.textContent = descInput.value.length;
        });
    }

    productEditForm.addEventListener('submit', function (e) {
        // 이미지 개수 검사
        const existingImagesCount = imagePreview.querySelectorAll('.existing-image').length;
        const totalImageCount = existingImagesCount + selectedFiles.length;
        if (totalImageCount === 0 || totalImageCount > 5) {
            e.preventDefault();
            alert(totalImageCount === 0 ? "이미지는 최소 1장 이상 등록해야 합니다." : "이미지는 최대 5장까지만 업로드할 수 있습니다.");
            return;
        }

        // 폼 전체 유효성 검사
        if (!productEditForm.checkValidity()) {
            e.preventDefault();
            e.stopPropagation();

            Array.from(productEditForm.elements).forEach(element => {
                validateInput(element);
            });

            alert("입력 내용을 다시 확인해주세요.");
        }
    });
});
