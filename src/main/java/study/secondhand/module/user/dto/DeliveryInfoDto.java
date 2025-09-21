package study.secondhand.module.user.dto;

import lombok.Getter;

@Getter
public class DeliveryInfoDto {
    private String name;
    private String phoneNumber;
    private String address;
    private String detailAddress;
    private String postCode;
    private String storeName;
    private String storeAddress;

    public DeliveryInfoDto() {
    }

    public DeliveryInfoDto(String name, String phoneNumber, String address, String detailAddress, String postCode
            , String storeName, String storeAddress) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.detailAddress = detailAddress;
        this.postCode = postCode;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
    }
}
