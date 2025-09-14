document.addEventListener("DOMContentLoaded", function () {
    const dataElement = document.getElementById('product-view-data');

    if (!dataElement || !dataElement.dataset.productJson) {
        console.error("Product data element or data attribute not found!");
        return;
    }

    const productJsonString = dataElement.dataset.productJson;
    const productData = JSON.parse(productJsonString);

    const images = productData.imageUrls || [];

    let currentIndex = 0;
    let selectedDeliveryType = null;
    let selectedFinalAmount = 0;
    let selectedDeliveryInfo = null;
    let selectedPostCode = "";

    // Kakao 지도 관련 변수
    let map, markers = [], infowindow, ps;

    function updateImage() {
        const mainImage = document.getElementById("mainImage");
        const prevBtn = document.getElementById("prevBtn");
        const nextBtn = document.getElementById("nextBtn");

        if (!mainImage || !prevBtn || !nextBtn) return;

        if (images.length > 0 && images[currentIndex]) {
            mainImage.src = images[currentIndex];
            if (images.length <= 1) {
                prevBtn.classList.add('d-none');
                nextBtn.classList.add('d-none');
            } else {
                prevBtn.classList.toggle('d-none', currentIndex === 0);
                nextBtn.classList.toggle('d-none', currentIndex === images.length - 1);
            }
        } else if (images.length === 0) {
            prevBtn.classList.add('d-none');
            nextBtn.classList.add('d-none');
        }
    }

    function showPrevImage() {
        if (images.length === 0) return;
        currentIndex = (currentIndex - 1 + images.length) % images.length;
        updateImage();
    }

    function showNextImage() {
        if (images.length === 0) return;
        currentIndex = (currentIndex + 1) % images.length;
        updateImage();
    }

    async function toggleFavorite() {
        const favoriteBtn = document.getElementById("favoriteBtn");
        if (!favoriteBtn) return;

        if (favoriteBtn.getAttribute("data-logged-in") !== 'true') {
            if (typeof openLoginModal === 'function') {
                openLoginModal();
            } else {
                alert('로그인이 필요합니다.');
            }
            return;
        }

        const productId = favoriteBtn.dataset.productId;
        const isCurrentlyFavorite = favoriteBtn.innerText.includes("찜 취소 💔");
        const httpMethod = isCurrentlyFavorite ? 'DELETE' : 'POST';

        const countSpan = document.querySelector(".favorite-count span");
        let currentCount = parseInt(countSpan.innerText, 10);

        try {
            const response = await fetchWithRefresh(`/api/products/${productId}/favorites`, {
                method: httpMethod,
                headers: {[csrfHeader]: csrfToken},
                credentials: 'include'
            });

            // if (response.status === 401) {
            //    if (typeof openLoginModal === 'function') openLoginModal();
            //    else alert('로그인이 필요합니다.');
            //   return;
            // }

            if (!response.ok) {
                throw new Error('찜하기 처리 실패');
            }
            if (isCurrentlyFavorite) {
                favoriteBtn.innerText = "찜 ❤️";
                countSpan.innerText = currentCount - 1;
            } else {
                favoriteBtn.innerText = "찜 취소 💔"
                countSpan.innerText = currentCount + 1;
            }

        } catch (err) {
            console.error("찜 요청 실패: ", err);
            alert("요청 처리 중 오류가 발생했습니다.");
        }
    }

    function openWishlistUsersModal() {
        const wishlistUsers = productData.wishlistUsers;
        const modalBody = document.getElementById('wishlistUsersList');
        modalBody.innerHTML = '';

        if (!wishlistUsers || wishlistUsers.length === 0) {
            modalBody.innerHTML = '<p>찜한 유저가 없습니다.</p>';
        } else {
            const ul = document.createElement('ul');
            ul.classList.add('list-unstyled');
            wishlistUsers.forEach(user => {
                const li = document.createElement('li');
                const link = document.createElement('a');
                link.href = `/shop/${user.id}`;
                link.textContent = user.nickname && user.nickname.trim() !== '' ? user.nickname : `ID ${user.id}`;
                link.classList.add('shop-link');
                li.appendChild(link);
                ul.appendChild(li);
            });
            modalBody.appendChild(ul);
        }
        const wishlistModal = new bootstrap.Modal(document.getElementById('wishlistUsersModal'));
        wishlistModal.show();
    }

    function openDeliveryInfoModal() {
        const infoModalEl = document.getElementById("deliveryInfoModal");
        let infoModal = bootstrap.Modal.getInstance(infoModalEl);
        if (!infoModal) {
            infoModal = new bootstrap.Modal(infoModalEl);
        }
        const detailModal = bootstrap.Modal.getInstance(document.getElementById("deliveryDetailModal"));
        if (detailModal) detailModal.hide();

        if (selectedDeliveryInfo) {
            document.getElementById("name").value = selectedDeliveryInfo.name || "";
            document.getElementById("phone").value = selectedDeliveryInfo.phoneNumber || "";
            document.getElementById("addressLine1").value = selectedDeliveryInfo.address || "";
            document.getElementById("addressLine2").value = selectedDeliveryInfo.detailAddress || "";
        }
        infoModal.show();
    }

    function openCVSInfoModal() {
        const infoModalEl = document.getElementById("cvsInfoModal");
        let infoModal = bootstrap.Modal.getInstance(infoModalEl);
        if (!infoModal) {
            infoModal = new bootstrap.Modal(infoModalEl);
        }

        const detailModal = bootstrap.Modal.getInstance(document.getElementById("deliveryDetailModal"));
        if (detailModal) detailModal.hide();

        if (selectedDeliveryInfo) {
            document.getElementById("cvsName").value = selectedDeliveryInfo.name || "";
            document.getElementById("cvsPhone").value = selectedDeliveryInfo.phoneNumber || "";
            document.getElementById("cvsStoreNameInput").value = selectedDeliveryInfo.storeName || "";
            document.getElementById("cvsStoreAddressInput").value = selectedDeliveryInfo.storeAddress || "";
        }
        infoModal.show();
    }

    function selectDeliveryFee(type) {
        selectedDeliveryType = type;
        selectedFinalAmount = type === 'normal' ? productData.price + productData.normalDeliveryFee : productData.price + productData.cheapDeliveryFee;

        document.getElementById('selectedDeliveryMethod').textContent = type === 'normal' ? '일반택배' : '반값・알뜰택배';
        document.getElementById('finalAmountText').textContent = selectedFinalAmount.toLocaleString() + '원';

        const optionModalEl = document.getElementById('deliveryOptionModal');
        const optionModal = bootstrap.Modal.getInstance(optionModalEl);

        const handler = () => {
            loadDeliveryInfoAndShowDetailModal(type);
            optionModalEl.removeEventListener('hidden.bs.modal', handler);
        };
        optionModalEl.addEventListener('hidden.bs.modal', handler);
        optionModal.hide();
    }

    function backToOptionModal() {
        const detailModal = bootstrap.Modal.getInstance(document.getElementById("deliveryDetailModal"));
        const optionModal = new bootstrap.Modal(document.getElementById("deliveryOptionModal"));
        if (detailModal) detailModal.hide();
        optionModal.show();
    }

    async function loadDeliveryInfoAndShowDetailModal(type) {
        const addressInfoDiv = document.getElementById('addressInfo');
        const cheapDeliveryInfoDiv = document.getElementById('cheapDeliveryInfo');
        if (!addressInfoDiv || !cheapDeliveryInfoDiv) return;

        try {
            const response = await fetch('/api/user/delivery-info');
            if (!response.ok) throw new Error('Failed to fetch delivery info');
            const data = await response.json();

            if (data && data.name) {
                selectedPostCode = data.postcode;
                selectedDeliveryInfo = data;

                document.getElementById('recipientName').textContent = data.name;
                document.getElementById('recipientPhone').textContent = data.phoneNumber;
                document.getElementById('recipientAddress').textContent = data.address;
                document.getElementById('recipientDetailAddress').textContent = data.detailAddress;
                document.getElementById('cvsRecipientName').textContent = data.name;
                document.getElementById('cvsRecipientPhone').textContent = data.phoneNumber;
                document.getElementById('cvsStoreName').textContent = data.storeName || "편의점 미지정";
                document.getElementById('cvsStoreAddress').textContent = data.storeAddress || "";

                if (type === 'normal') {
                    addressInfoDiv.classList.remove('d-none');
                    cheapDeliveryInfoDiv.classList.add('d-none');
                } else {
                    addressInfoDiv.classList.add('d-none');
                    cheapDeliveryInfoDiv.classList.remove('d-none');
                }
                const detailModal = new bootstrap.Modal(document.getElementById('deliveryDetailModal'));
                detailModal.show();
            } else {
                if (type === 'normal') openDeliveryInfoModal();
                else openCVSInfoModal();
            }
        } catch (error) {
            console.error("배송지 정보를 불러오는 데 실패했습니다:", error);
            if (type === 'normal') openDeliveryInfoModal();
            else openCVSInfoModal();
        }
    }

    function handlePaymentAndCloseModal() {
        const request = document.getElementById("deliveryRequest").value.trim();
        if (selectedDeliveryInfo) {
            selectedDeliveryInfo.request = request;
        }

        const {name, phoneNumber, address, storeName, storeAddress} = selectedDeliveryInfo || {};

        if (selectedDeliveryType === 'normal') {
            if (!name || !phoneNumber || !address) {
                alert("배송 필수 정보를 입력해주세요.");
                openDeliveryInfoModal();
                return;
            }
        } else {
            if (!name || !phoneNumber || !storeName || !storeAddress) {
                alert("배송 필수 정보를 입력해주세요.");
                openCVSInfoModal();
                return;
            }
        }

        requestPayment(selectedFinalAmount, selectedDeliveryInfo);
        const detailModal = bootstrap.Modal.getInstance(document.getElementById("deliveryDetailModal"));
        if (detailModal) detailModal.hide();
    }

    async function requestPayment(finalAmount, deliveryInfo) {
        let customer;
        let effectiveDeliveryType = selectedDeliveryType;

        if (effectiveDeliveryType === 'normal') {
            customer = {
                fullName: deliveryInfo?.name,
                phoneNumber: deliveryInfo?.phoneNumber,
                address: {
                    addressLine1: deliveryInfo?.address,
                    addressLine2: deliveryInfo?.detailAddress ?? "",
                },
                zipcode: deliveryInfo?.postCode,
            };
        } else if (effectiveDeliveryType === 'cheap') {
            customer = {
                fullName: deliveryInfo?.name,
                phoneNumber: deliveryInfo?.phoneNumber,
                address: {
                    addressLine1: deliveryInfo?.storeAddress ?? "",
                    addressLine2: deliveryInfo?.storeName ?? "",
                },
            };
        } else {
            customer = {
                fullName: "",
                address: {
                    addressLine1: "",
                    addressLine2: ""
                },
            };
            effectiveDeliveryType = "direct";
        }

        try {
            const response = await PortOne.requestPayment({
                storeId: "store-143bbbb4-e906-4abb-814e-2568e1597352",
                channelKey: "channel-key-5dc5b2ce-2f5f-49f7-b18c-b51e576572c5",
                paymentId: `payment-${crypto.randomUUID()}`,
                orderName: productData.title,
                totalAmount: finalAmount,
                currency: "KRW",
                payMethod: "CARD",
                customData: {
                    product: productData.id
                },
                customer: customer,
            });

            if (response.code) return alert(`결제 실패: ${response.message}`);

            const notified = await fetch('/api/payments', {
                method: "POST",
                headers: {
                    [csrfHeader]: csrfToken,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    paymentId: response.paymentId,
                    productId: productData.id,
                    name: customer.fullName,
                    phone: customer.phoneNumber,
                    address: customer.address.addressLine1,
                    detailAddress: customer.address.addressLine2,
                    postCode: customer.zipcode,
                    storeName: deliveryInfo?.storeName ?? "",
                    storeAddress: deliveryInfo?.storeAddress ?? "",
                    requestMessage: deliveryInfo?.request ?? "",
                    deliveryType: effectiveDeliveryType
                }),
            });


            if (notified.ok) {
                const orderId = await notified.json();
                alert("결제가 성공적으로 처리되었습니다.");
                window.location.href = `/orders/${orderId}`;
            } else {
                alert("서버에 결제 정보 저장 실패!");
            }
        } catch (error) {
            console.error("결제 프로세스 실패:", error);
            alert("결제가 실패했습니다. 다시 시도해 주세요.");
        }
    }

    // --- Kakao Map 관련 함수 --- //
    function searchPlaces() {
        const keyword = document.getElementById('keyword')?.value.trim();
        if (!keyword) {
            alert('키워드를 입력해주세요!');
            return;
        }
        if (ps) ps.keywordSearch(keyword, placesSearchCB);
    }

    function placesSearchCB(data, status, pagination) {
        if (status === kakao.maps.services.Status.OK) {
            displayPlaces(data);
            displayPagination(pagination);
        } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
            alert('검색 결과가 존재하지 않습니다.');
        } else if (status === kakao.maps.services.Status.ERROR) {
            alert('검색 중 오류가 발생했습니다.');
        }
    }

    function displayPlaces(places) {
        const listEl = document.getElementById('placesList');
        const menuEl = document.getElementById('menu_wrap');
        if (!listEl || !menuEl) return;

        const fragment = document.createDocumentFragment();
        const bounds = new kakao.maps.LatLngBounds();

        removeAllChildNodes(listEl);
        removeMarker();

        places.forEach((place, i) => {
            const position = new kakao.maps.LatLng(place.y, place.x);
            const marker = addMarker(position, i);
            const itemEl = getListItem(i, place);

            bounds.extend(position);

            (function (marker, title, address) {
                kakao.maps.event.addListener(marker, 'mouseover', () => displayInfowindow(marker, title));
                kakao.maps.event.addListener(marker, 'mouseout', () => infowindow.close());

                itemEl.onmouseover = () => displayInfowindow(marker, title);
                itemEl.onmouseout = () => infowindow.close();

                itemEl.onclick = () => {
                    document.getElementById('cvsStoreNameInput').value = title;
                    document.getElementById('cvsStoreAddressInput').value = address;
                    if (infowindow) infowindow.close();

                    const modal = bootstrap.Modal.getInstance(document.getElementById('placeSearchModal'));
                    if (modal) modal.hide();
                };
            })(marker, place.place_name, place.road_address_name || place.address_name);

            fragment.appendChild(itemEl);
        });

        listEl.appendChild(fragment);
        menuEl.scrollTop = 0;
        if (map) map.setBounds(bounds);
    }

    function getListItem(index, place) {
        const el = document.createElement('li');
        el.className = 'item';
        let itemStr = `<span class="markerbg marker_${index + 1}"></span><div class="info"><h5>${place.place_name}</h5>`;

        if (place.road_address_name) {
            itemStr += `<span>${place.road_address_name}</span><span class="jibun gray">${place.address_name}</span>`;
        } else {
            itemStr += `<span>${place.address_name}</span>`;
        }

        itemStr += `<span class="tel">${place.phone}</span></div>`;
        el.innerHTML = itemStr;
        return el;
    }

    function addMarker(position, idx) {
        const imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_number_blue.png',
            imageSize = new kakao.maps.Size(36, 37),
            imgOptions = {
                spriteSize: new kakao.maps.Size(36, 691),
                spriteOrigin: new kakao.maps.Point(0, (idx * 46) + 10),
                offset: new kakao.maps.Point(13, 37)
            },
            markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imgOptions),
            marker = new kakao.maps.Marker({position: position, image: markerImage});

        if (map) marker.setMap(map);
        markers.push(marker);
        return marker;
    }

    function removeMarker() {
        markers.forEach(marker => marker.setMap(null));
        markers = [];
    }

    function displayPagination(pagination) {
        const paginationEl = document.getElementById('pagination');
        if (!paginationEl) return;
        while (paginationEl.hasChildNodes()) {
            paginationEl.removeChild(paginationEl.lastChild);
        }

        for (let i = 1; i <= pagination.last; i++) {
            const el = document.createElement('a');
            el.href = "#";
            el.innerHTML = i;

            if (i === pagination.current) {
                el.className = 'on';
            } else {
                el.onclick = (p => () => p.gotoPage(i))(pagination);
            }
            paginationEl.appendChild(el);
        }
    }

    function displayInfowindow(marker, title) {
        const content = `<div class="map-infowindow-content">${title}</div>`;
        if (infowindow) {
            infowindow.setContent(content);
            infowindow.open(map, marker);
        }
    }

    function removeAllChildNodes(el) {
        while (el.hasChildNodes()) {
            el.removeChild(el.lastChild);
        }
    }


    // --- 3. 이벤트 리스너 등록 --- //

    // 이미지 슬라이더
    document.getElementById("prevBtn")?.addEventListener("click", showPrevImage);
    document.getElementById("nextBtn")?.addEventListener("click", showNextImage);

    // 상품 액션 버튼
    document.getElementById("wishlistUsersBtn")?.addEventListener("click", openWishlistUsersModal);
    document.getElementById("favoriteBtn")?.addEventListener("click", toggleFavorite);
    document.getElementById("selectNormalDelivery")?.addEventListener("click", () => selectDeliveryFee('normal'));
    document.getElementById("selectCheapDelivery")?.addEventListener("click", () => selectDeliveryFee('cheap'));
    document.getElementById("backToOptionBtn")?.addEventListener("click", backToOptionModal);
    document.getElementById("editAddressBtn")?.addEventListener("click", openDeliveryInfoModal);
    document.getElementById("editCvsBtn")?.addEventListener("click", openCVSInfoModal);
    document.getElementById("payNowBtn")?.addEventListener("click", handlePaymentAndCloseModal);

    document.getElementById("chatBtn")?.addEventListener("click", function () {
        if (this.getAttribute("data-logged-in") !== 'true') {
            if (typeof openLoginModal === 'function') openLoginModal();
            else alert('로그인이 필요합니다.');
            return;
        }
        const chatUrl = this.dataset.chatUrl;
        if (chatUrl) window.location.href = chatUrl;
    });

    document.getElementById("paymentBtn")?.addEventListener("click", async () => {
        if (document.getElementById("paymentBtn").getAttribute("data-logged-in") !== 'true') {
            if (typeof openLoginModal === 'function') openLoginModal();
            else alert('로그인이 필요합니다.');
            return;
        }

        if (productData.dealMethod === '택배거래') {
            const modal = new bootstrap.Modal(document.getElementById('deliveryOptionModal'));
            modal.show();
        } else {
            await requestPayment(productData.price, null);
        }
    });

    // 주소 및 편의점 검색 관련
    document.getElementById("addressLine1")?.addEventListener("click", function () {
        new daum.Postcode({
            oncomplete: function (data) {
                document.getElementById("addressLine1").value = data.roadAddress;
                selectedPostCode = data.zonecode;
                document.getElementById("addressLine2")?.focus();
            }
        }).open();
    });

    document.getElementById("searchForm")?.addEventListener("submit", function (e) {
        e.preventDefault();
        searchPlaces();
    });

    document.getElementById("cvsStoreNameInput")?.addEventListener("click", function () {
        const placeSearchModalEl = document.getElementById('placeSearchModal');
        const placeSearchModal = new bootstrap.Modal(placeSearchModalEl);

        // 'shown.bs.modal' 이벤트: 모달이 완전히 화면에 표시된 후 콜백 함수를 실행합니다.
        // { once: true } 옵션으로 이벤트가 한 번만 실행되도록 합니다.
        placeSearchModalEl.addEventListener('shown.bs.modal', function () {
            kakao.maps.load(function () {
                const container = document.getElementById("map");
                if (!container) return;

                // 지도가 표시될 div가 보이지 않는 상태에서 지도 생성을 막기 위해,
                // innerHTML을 비워 이전 지도 인스턴스를 제거합니다.
                container.innerHTML = '';

                const option = {
                    center: new kakao.maps.LatLng(37.566826, 126.9786567),
                    level: 3
                };

                map = new kakao.maps.Map(container, option);
                ps = new kakao.maps.services.Places();
                infowindow = new kakao.maps.InfoWindow({zIndex: 1});

                // 모달 크기 변경이나 표시 상태 변경 후 지도가 깨지는 것을 방지합니다.
                map.relayout();
            });
        }, { once: true });

        placeSearchModal.show();
    });

    // 폼 제출 관련
    document.getElementById("deliveryForm")?.addEventListener("submit", function (event) {
        event.preventDefault();
        const name = document.getElementById("name").value.trim();
        const phone = document.getElementById("phone").value.trim();
        const addressLine1 = document.getElementById("addressLine1").value.trim();
        const addressLine2 = document.getElementById("addressLine2").value.trim();

        if (!name || !phone || !addressLine1) {
            alert("모든 필수 정보를 입력해 주세요.");
            return;
        }

        selectedDeliveryInfo = {
            name,
            phoneNumber: phone,
            address: addressLine1,
            detailAddress: addressLine2,
            postcode: selectedPostCode
        };

        const infoModal = bootstrap.Modal.getInstance(document.getElementById("deliveryInfoModal"));
        if (infoModal) infoModal.hide();

        document.getElementById('recipientName').textContent = selectedDeliveryInfo.name;
        document.getElementById('recipientPhone').textContent = selectedDeliveryInfo.phoneNumber;
        document.getElementById('recipientAddress').textContent = selectedDeliveryInfo.address;
        document.getElementById('recipientDetailAddress').textContent = selectedDeliveryInfo.detailAddress;

        const detailModal = new bootstrap.Modal(document.getElementById('deliveryDetailModal'));
        detailModal.show();
    });

    document.getElementById("cvsForm")?.addEventListener("submit", function (event) {
        event.preventDefault();
        const name = document.getElementById("cvsName").value.trim();
        const phone = document.getElementById("cvsPhone").value.trim();
        const storeName = document.getElementById("cvsStoreNameInput").value.trim();
        const storeAddress = document.getElementById("cvsStoreAddressInput").value.trim();

        if (!name || !phone || !storeName || !storeAddress) {
            alert("모든 필수 정보를 입력해 주세요.");
            return;
        }
        const lowerStoreName = storeName.toLowerCase();
        if (!lowerStoreName.includes("cu") && !lowerStoreName.includes("gs")) {
            alert("CU 또는 GS 편의점만 선택 가능합니다. 다시 입력해 주세요.");
            document.getElementById("cvsStoreNameInput").focus();
            return;
        }

        selectedDeliveryInfo = {...selectedDeliveryInfo, name, phoneNumber: phone, storeName, storeAddress};

        const cvsModal = bootstrap.Modal.getInstance(document.getElementById("cvsInfoModal"));
        if (cvsModal) cvsModal.hide();

        const detailModal = new bootstrap.Modal(document.getElementById("deliveryDetailModal"));
        detailModal.show();

        document.getElementById('cvsRecipientName').textContent = name;
        document.getElementById('cvsRecipientPhone').textContent = phone;
        document.getElementById('cvsStoreName').textContent = storeName;
        document.getElementById('cvsStoreAddress').textContent = storeAddress;
    });


    // --- 4. 초기 실행 코드 --- //
    updateImage();

    const normalFeeText = document.getElementById("normalFeeText");
    const cheapFeeText = document.getElementById("cheapFeeText");
    if (normalFeeText) normalFeeText.textContent = productData.normalDeliveryFee.toLocaleString() + "원";
    if (cheapFeeText) cheapFeeText.textContent = productData.cheapDeliveryFee.toLocaleString() + "원";

    const reportModal = document.getElementById('reportProductModal');
    if (reportModal) {
        const reasonSelect = document.getElementById('reportReason');
        const descriptionTextarea = document.getElementById('reportDescription');
        const descCountSpan = document.getElementById('reportDescCount');
        const submitBtn = document.getElementById('reportSubmitBtn');

        function toggleReportSubmitButton() {
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

        reasonSelect.addEventListener('change', toggleReportSubmitButton);
        descriptionTextarea.addEventListener('input', updateDescriptionCount);

        reportModal.addEventListener('show.bs.modal', function () {
            reportModal.querySelector('form').reset();
            toggleReportSubmitButton();
            updateDescriptionCount();
        });
    }
});