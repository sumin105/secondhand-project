document.addEventListener('DOMContentLoaded', function () {
    const dealMethodSelect = document.getElementById('dealMethod');
    const deliveryFeesDiv = document.getElementById('deliveryFees');
    const imageInput = document.getElementById('image');
    const imagePreview = document.getElementById('imagePreview');
    const descInput = document.getElementById('description');
    const descCount = document.getElementById('descCount');
    let selectedFiles = [];

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
        const files = Array.from(this.files); // input에서 선택한 파일을 배열로 변환
        // 누적된 파일과 새 파일을 합친 후 중복 제거
        selectedFiles = [...selectedFiles, ...files];

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
})