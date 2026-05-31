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
import org.springframework.http.MediaType;
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
    public ResponseEntity<String> success(@RequestParam String orderId, @RequestParam("pg_token") String pgToken) {
        KakaoApproveResponse approve = kakaoPayService.approve(orderId, pgToken);
        orderService.saveOrder(approve,payInfo);
        couponService.updateCoupon(payInfo.getCouponId());

        List<KakaoPay.ProductInfo> productInfo = payInfo.getProductInfo();

        productService.updateCount(productInfo);

        return redirectPaymentResult(orderId, "success!");
    }

    /**
     * ✅ 결제 취소 콜백
     */
    @GetMapping("/qr/cancel")
    public ResponseEntity<?> cancel(@RequestParam String orderId) {
        if (isAppReturn()) {
            return redirectPaymentResult(orderId, "cancel");
        }
        return ResponseEntity.ok(Map.of("status", "CANCEL", "orderId", orderId));
    }

    /**
     * ✅ 결제 실패 콜백
     */
    @GetMapping("/qr/fail")
    public ResponseEntity<?> fail(@RequestParam String orderId) {
        if (isAppReturn()) {
            return redirectPaymentResult(orderId, "fail");
        }
        return ResponseEntity.ok(Map.of("status", "FAIL", "orderId", orderId));
    }

    private boolean isAppReturn() {
        return payInfo != null && "app".equalsIgnoreCase(payInfo.getReturnType());
    }

    private ResponseEntity<String> redirectPaymentResult(String orderId, String status) {
        String appUrl = "candy://payment/result?orderId=" + orderId + "&status=" + status;

        if (isAppReturn()) {
            String intentUrl = "intent://payment/result?orderId=" + orderId + "&status=" + status
                    + "#Intent;scheme=candy;package=com.baleDev.Candy;end";

            String html = """
                    <!doctype html>
                    <html lang="ko">
                    <head>
                      <meta charset="utf-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1">
                      <title>결제 완료</title>
                    </head>
                    <body>
                      <p>결제가 처리되었습니다. 앱으로 이동 중입니다.</p>
                      <script>
                        const appUrl = "%s";
                        const intentUrl = "%s";
                        window.location.replace(appUrl);
                        setTimeout(function () {
                          window.location.replace(intentUrl);
                        }, 700);
                      </script>
                      <a href="%s">앱으로 돌아가기</a>
                    </body>
                    </html>
                    """.formatted(appUrl, intentUrl, appUrl);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        }

        URI redirect = URI.create(
                frontendUrl + "/payResult?orderId=" + orderId + "&status=" + status
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirect);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
