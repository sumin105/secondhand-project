package study.secondhand.module.user.dto;

import lombok.Getter;

@Getter
public class DeliveryInfoDto {
    private String name;
    private String phoneNumber;
    private String address;
    private String detailAddress;
    private String postCode;

    public DeliveryInfoDto() {}

    public DeliveryInfoDto(String name, String phoneNumber, String address, String detailAddress, String postCode) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.detailAddress = detailAddress;
        this.postCode = postCode;
    }
}
