package com.tjg_project.candy.domain.order.controller;


import com.tjg_project.candy.domain.coupon.service.CouponService;
import com.tjg_project.candy.domain.order.dto.KakaoApproveResponse;
import com.tjg_project.candy.domain.order.entity.KakaoPay;
import com.tjg_project.candy.domain.order.dto.KakaoReadyResponse;
import com.tjg_project.candy.domain.order.service.KakaoPayService;
import com.tjg_project.candy.domain.order.service.OrderService;
import com.tjg_project.candy.domain.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ProductService productService;
    private KakaoPay payInfo = null; //kakaoPay DTO 클래스를 전역으로 선언

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired
    public KakaoPayController(KakaoPayService kakaoPayService, OrderService orderService, CouponService couponService, ProductService productService) {
        this.kakaoPayService = kakaoPayService;
        this.orderService = orderService;
        this.couponService = couponService;
        this.productService = productService;
    }

    @PostMapping("/kakao/ready")
    public KakaoReadyResponse ready(@RequestBody KakaoPay kakaoPay) {
        kakaoPay.setOrderId(UUID.randomUUID().toString());
        payInfo = kakaoPay;
        return kakaoPayService.ready(kakaoPay);
    }

    @GetMapping("/qr/success")
    public ResponseEntity<Void> success(@RequestParam String orderId, @RequestParam("pg_token") String pgToken) {
        KakaoApproveResponse approve = kakaoPayService.approve(orderId, pgToken);
        orderService.saveOrder(approve,payInfo);
        couponService.updateCoupon(payInfo.getCouponId());

        List<KakaoPay.ProductInfo> productInfo = payInfo.getProductInfo();

        productService.updateCount(productInfo);

        String redirectUrl = "app".equalsIgnoreCase(payInfo.getReturnType())
                ? "candy://payment/result?orderId=" + orderId + "&status=success"
                : frontendUrl + "/payResult?orderId=" + orderId + "&status=success";

        URI redirect = URI.create(
                redirectUrl
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirect);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * ✅ 결제 취소 콜백
     */
    @GetMapping("/qr/cancel")
    public ResponseEntity<?> cancel(@RequestParam String orderId) {
        return ResponseEntity.ok(Map.of("status", "CANCEL", "orderId", orderId));
    }

    /**
     * ✅ 결제 실패 콜백
     */
    @GetMapping("/qr/fail")
    public ResponseEntity<?> fail(@RequestParam String orderId) {
        return ResponseEntity.ok(Map.of("status", "FAIL", "orderId", orderId));
    }
}
