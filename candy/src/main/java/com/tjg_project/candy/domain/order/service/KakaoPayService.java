package com.tjg_project.candy.domain.order.service;

import com.tjg_project.candy.domain.order.dto.KakaoApproveResponse;
import com.tjg_project.candy.domain.order.dto.KakaoReadyResponse;
import com.tjg_project.candy.domain.order.entity.KakaoPay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class KakaoPayService {

    private static final Logger log = LoggerFactory.getLogger(KakaoPayService.class);

    @Value("${kakao.pay.host}") private String KAKAO_PAY_HOST;
    @Value("${kakao.pay.admin-key}") private String ADMIN_KEY;
    @Value("${kakao.pay.cid}") private String CID;
    @Value("${kakao.pay.ready-path}") private String READY_PATH;
    @Value("${kakao.pay.approve-path}") private String APPROVE_PATH;
    @Value("${app.backend-url}")  private String backendUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ConcurrentHashMap<String, String> tidStore = new ConcurrentHashMap<>();
    String user_id = "test";
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + ADMIN_KEY);
        return headers;
    }

    public KakaoReadyResponse ready(KakaoPay kakaoPay) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", CID);
        params.add("partner_order_id", kakaoPay.getOrderId());
        params.add("partner_user_id", user_id);
        params.add("item_name", kakaoPay.getItemName());
        params.add("quantity", String.valueOf(kakaoPay.getQty()));
        params.add("total_amount", String.valueOf(kakaoPay.getTotalAmount()));
        params.add("tax_free_amount", "0");
        params.add("approval_url",
                backendUrl + "/payment/qr/success?orderId=" + kakaoPay.getOrderId()
        );
        params.add("cancel_url",
                backendUrl + "/payment/qr/cancel?orderId=" + kakaoPay.getOrderId()
        );
        params.add("fail_url",
                backendUrl + "/payment/qr/fail?orderId=" + kakaoPay.getOrderId()
        );
        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, getHeaders());

        String url = KAKAO_PAY_HOST + "/v1" + READY_PATH;
        log.info(
                "[KAKAO_PAY][READY][KAKAO_REQUEST] orderId={}, cid={}, url={}, backendUrl={}, approvalUrl={}, cancelUrl={}, failUrl={}",
                kakaoPay.getOrderId(),
                CID,
                url,
                backendUrl,
                params.getFirst("approval_url"),
                params.getFirst("cancel_url"),
                params.getFirst("fail_url")
        );

        KakaoReadyResponse res;
        try {
            res = restTemplate.postForObject(url, body, KakaoReadyResponse.class);
        } catch (RestClientResponseException e) {
            log.error(
                    "[KAKAO_PAY][READY][KAKAO_ERROR] orderId={}, statusCode={}, responseBody={}",
                    kakaoPay.getOrderId(),
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            throw e;
        }

        if (res == null || res.getTid() == null) {
            log.error("[KAKAO_PAY][READY][KAKAO_RESPONSE_EMPTY] orderId={}, responseNull={}", kakaoPay.getOrderId(), res == null);
            throw new IllegalStateException("KakaoPay ready response does not contain tid.");
        }

        tidStore.put(kakaoPay.getOrderId(), res.getTid());
        log.info(
                "[KAKAO_PAY][READY][TID_STORED] orderId={}, tid={}, storeSize={}",
                kakaoPay.getOrderId(),
                res.getTid(),
                tidStore.size()
        );
        return res;
    }

    public KakaoApproveResponse approve(String orderId, String pgToken) {
        String tid = tidStore.get(orderId);
        log.info(
                "[KAKAO_PAY][APPROVE][REQUEST] orderId={}, tidExists={}, pgTokenExists={}, storeSize={}",
                orderId,
                tid != null,
                pgToken != null && !pgToken.isBlank(),
                tidStore.size()
        );

        if (tid == null) {
            log.error("[KAKAO_PAY][APPROVE][TID_MISSING] orderId={}, storeSize={}", orderId, tidStore.size());
            throw new IllegalStateException("KakaoPay tid is missing for orderId: " + orderId);
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", CID);
        params.add("tid", tid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", user_id);
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, getHeaders());
        String url = KAKAO_PAY_HOST + "/v1" + APPROVE_PATH;
        log.info("[KAKAO_PAY][APPROVE][KAKAO_REQUEST] orderId={}, cid={}, tid={}, url={}", orderId, CID, tid, url);

        KakaoApproveResponse response;
        try {
            response = restTemplate.postForObject(url, body, KakaoApproveResponse.class);
        } catch (RestClientResponseException e) {
            log.error(
                    "[KAKAO_PAY][APPROVE][KAKAO_ERROR] orderId={}, statusCode={}, responseBody={}",
                    orderId,
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            throw e;
        }
        log.info(
                "[KAKAO_PAY][APPROVE][KAKAO_RESPONSE] orderId={}, tid={}, status={}, approvedAt={}",
                orderId,
                response == null ? null : response.getTid(),
                response == null ? null : response.getStatus(),
                response == null ? null : response.getApprovedAt()
        );
        return response;
    }
}
