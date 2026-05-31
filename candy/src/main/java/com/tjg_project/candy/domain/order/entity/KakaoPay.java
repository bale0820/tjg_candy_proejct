package com.tjg_project.candy.domain.order.entity;

import lombok.Data;

import java.util.List;

@Data
public class KakaoPay {
    private String orderId;
    private Long id;
    private String itemName;
    private String qty;
    private String totalAmount;
    private Receiver receiver;
    private PaymentInfo paymentInfo;
    private List<Long> cidList;
    private Long couponId;
    private List<ProductInfo> productInfo;
    private String returnType;

    @Data
    public static class Receiver {
        private String name;
        private String phone;
        private String zipcode;
        private String address1;
        private String address2;
        private String memo;
    }//Receiver

    @Data
    public static class PaymentInfo {
        private int shippingFee;
        private int discountAmount;
        private int totalAmount;
    }

    @Data
    public static class ProductInfo {
        private Long pid;
        private Long qty;
    }
}//KakaoPay dto

