package com.tjg_project.candy.domain.order.controller;


import com.tjg_project.candy.domain.coupon.service.CouponService;
import com.tjg_project.candy.domain.order.dto.KakaoApproveResponse;
import com.tjg_project.candy.domain.order.entity.KakaoPay;
import com.tjg_project.candy.domain.order.dto.KakaoReadyResponse;
import com.tjg_project.candy.domain.order.service.KakaoPayService;
import com.tjg_project.candy.domain.order.service.OrderService;
import com.tjg_project.candy.domain.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(KakaoPayController.class);

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
        log.info(
                "[KAKAO_PAY][READY][REQUEST] orderId={}, userId={}, itemName={}, qty={}, totalAmount={}, couponId={}, returnType={}, cidListSize={}, productInfoSize={}",
                kakaoPay.getOrderId(),
                kakaoPay.getId(),
                kakaoPay.getItemName(),
                kakaoPay.getQty(),
                kakaoPay.getTotalAmount(),
                kakaoPay.getCouponId(),
                kakaoPay.getReturnType(),
                kakaoPay.getCidList() == null ? 0 : kakaoPay.getCidList().size(),
                kakaoPay.getProductInfo() == null ? 0 : kakaoPay.getProductInfo().size()
        );

        KakaoReadyResponse response = kakaoPayService.ready(kakaoPay);
        log.info(
                "[KAKAO_PAY][READY][RESPONSE] orderId={}, tidExists={}, pcUrlExists={}, mobileUrlExists={}, appUrlExists={}, pcUrl={}, mobileUrl={}, appUrl={}",
                kakaoPay.getOrderId(),
                response != null && response.getTid() != null,
                response != null && response.getNext_redirect_pc_url() != null,
                response != null && response.getNext_redirect_mobile_url() != null,
                response != null && response.getNext_redirect_app_url() != null,
                response == null ? null : response.getNext_redirect_pc_url(),
                response == null ? null : response.getNext_redirect_mobile_url(),
                response == null ? null : response.getNext_redirect_app_url()
        );
        return response;
    }

    @GetMapping("/qr/success")
    public ResponseEntity<String> success(@RequestParam String orderId, @RequestParam("pg_token") String pgToken) {
        log.info(
                "[KAKAO_PAY][SUCCESS][CALLBACK] orderId={}, pgTokenExists={}, payInfoExists={}, returnType={}",
                orderId,
                pgToken != null && !pgToken.isBlank(),
                payInfo != null,
                payInfo == null ? null : payInfo.getReturnType()
        );
        KakaoApproveResponse approve = kakaoPayService.approve(orderId, pgToken);
        log.info(
                "[KAKAO_PAY][APPROVE][RESPONSE] orderId={}, tid={}, status={}, method={}, approvedAt={}",
                orderId,
                approve == null ? null : approve.getTid(),
                approve == null ? null : approve.getStatus(),
                approve == null ? null : approve.getPaymentMethodType(),
                approve == null ? null : approve.getApprovedAt()
        );
        orderService.saveOrder(approve,payInfo);
        log.info("[KAKAO_PAY][ORDER][SAVED] orderId={}", orderId);

        couponService.updateCoupon(payInfo.getCouponId());
        log.info("[KAKAO_PAY][COUPON][UPDATED] orderId={}, couponId={}", orderId, payInfo.getCouponId());

        List<KakaoPay.ProductInfo> productInfo = payInfo.getProductInfo();

        productService.updateCount(productInfo);
        log.info(
                "[KAKAO_PAY][PRODUCT][COUNT_UPDATED] orderId={}, productInfoSize={}",
                orderId,
                productInfo == null ? 0 : productInfo.size()
        );

        return redirectPaymentResult(orderId, "success");
    }

    /**
     * ✅ 결제 취소 콜백
     */
    @GetMapping("/qr/cancel")
    public ResponseEntity<?> cancel(@RequestParam String orderId) {
        log.info(
                "[KAKAO_PAY][CANCEL][CALLBACK] orderId={}, payInfoExists={}, returnType={}",
                orderId,
                payInfo != null,
                payInfo == null ? null : payInfo.getReturnType()
        );
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
        log.info(
                "[KAKAO_PAY][FAIL][CALLBACK] orderId={}, payInfoExists={}, returnType={}",
                orderId,
                payInfo != null,
                payInfo == null ? null : payInfo.getReturnType()
        );
        if (isAppReturn()) {
            return redirectPaymentResult(orderId, "fail");
        }
        return ResponseEntity.ok(Map.of("status", "FAIL", "orderId", orderId));
    }

    private boolean isAppReturn() {
        boolean appReturn = payInfo != null && "app".equalsIgnoreCase(payInfo.getReturnType());
        log.info(
                "[KAKAO_PAY][RETURN_TYPE] appReturn={}, payInfoExists={}, returnType={}",
                appReturn,
                payInfo != null,
                payInfo == null ? null : payInfo.getReturnType()
        );
        return appReturn;
    }

    private ResponseEntity<String> redirectPaymentResult(String orderId, String status) {
        String appUrl = "candy://payment/result?orderId=" + orderId + "&status=" + status;

        if (isAppReturn()) {
            String intentUrl = "intent://payment/result?orderId=" + orderId + "&status=" + status
                    + "#Intent;scheme=candy;package=com.baleDev.Candy;end";

            log.info(
                    "[KAKAO_PAY][REDIRECT][APP] orderId={}, status={}, appUrl={}, intentUrl={}",
                    orderId,
                    status,
                    appUrl,
                    intentUrl
            );

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
        log.info(
                "[KAKAO_PAY][REDIRECT][WEB] orderId={}, status={}, redirect={}",
                orderId,
                status,
                redirect
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirect);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
