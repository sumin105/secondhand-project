document.addEventListener('DOMContentLoaded', () => {
    const dealMethod = document.getElementById('dealMethod');
    const deliveryFees = document.getElementById('deliveryFees');
    const imageInput = document.getElementById('image');
    const imagePreview = document.getElementById('imagePreview');
    const descInput = document.getElementById('description');
    const descCount = document.getElementById('descCount');
    const productEditForm = document.getElementById('productEditForm');

    let selectedFiles = [];

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

            let existingInput = productEditForm.querySelector(`input[name="deletedImageIds"][value="${imageId}"]`);

            if (!existingInput) {
                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'deletedImageIds';
                hiddenInput.value = imageId;

                productEditForm.appendChild(hiddenInput);
            }

            const imgDiv = document.getElementById(`existing-image-${imageId}`);
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
        // 기존 이미지 제외하고 새 이미지만 다시 렌더링
        const existingImages = imagePreview.querySelectorAll('.existing-image');
        imagePreview.innerHTML = '';
        existingImages.forEach(img => imagePreview.appendChild(img));

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
        const files = Array.from(this.files); // input에서 선택한 파일을 배열로 변환
        const existingCount = imagePreview.querySelectorAll('.existing-image').length;
        const availableSlots = 5 - existingCount;

        // 합쳐서 중복 제거
        selectedFiles = [...selectedFiles, ...files];
        selectedFiles = selectedFiles.filter((file, index, self) =>
            index === self.findIndex(f => f.name === file.name && f.size === file.size)
        );

        // 이미지 최대 5장 제한
        if (selectedFiles.length > availableSlots) {
            alert("이미지는 최대 5장까지만 업로드할 수 있습니다.");
            selectedFiles = selectedFiles.slice(0, availableSlots); // 무조건 5장까지만 유지
        }

        renderNewImages();
    });

    if (descInput && descCount) {
        descCount.textContent = descInput.value.length;
        descInput.addEventListener('input', () => {
            descCount.textContent = descInput.value.length;
        });
    }

    document.querySelector('#productEditForm').addEventListener('submit', function (e) {
        const existingImagesCount = imagePreview.querySelectorAll('.existing-image').length;
        const totalImageCount = existingImagesCount + selectedFiles.length;

        if (totalImageCount === 0) {
            e.preventDefault();
            alert("이미지는 최소 1장 이상 등록해야 합니다.");
        } else if (totalImageCount > 5) {
            e.preventDefault();
            alert("이미지는 최대 5장까지만 업로드할 수 있습니다.");
        }
    });
});
