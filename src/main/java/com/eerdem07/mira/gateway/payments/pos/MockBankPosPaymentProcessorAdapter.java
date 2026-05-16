package com.eerdem07.mira.gateway.payments.pos;

import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidResult;
import com.eerdem07.mira.gateway.payments.application.port.out.ThreeDsCompleteRequest;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPos3DsCompleteResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosAuthorizeResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosCaptureResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosRefundResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosVoidResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MockBankPosPaymentProcessorAdapter implements PaymentProcessorPort {

    private static final String POS_PROVIDER = "MOCK_BANK_POS";
    private static final String AUTHORIZE_PATH = "/api/v1/pos/authorize";
    private static final String THREE_DS_COMPLETE_PATH = "/api/v1/pos/3ds/complete";
    private static final String CAPTURE_PATH = "/api/v1/pos/capture";
    private static final String VOID_PATH = "/api/v1/pos/void";
    private static final String REFUND_PATH = "/api/v1/pos/refund";

    private final RestClient restClient;
    private final MockPosRequestMapper requestMapper;
    private final MockPosResultMapper resultMapper;

    public MockBankPosPaymentProcessorAdapter(
            @Value("${mira.mock-pos.base-url:http://localhost:5102}") String baseUrl,
            @Value("${mira.mock-pos.merchant-id:mrc_mock_001}") String merchantId,
            @Value("${mira.mock-pos.terminal-id:term_mock_001}") String terminalId
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.requestMapper = new MockPosRequestMapper(merchantId, terminalId);
        this.resultMapper = new MockPosResultMapper(POS_PROVIDER);
    }

    @Override
    public PaymentAuthorizationResult authorize(PaymentAuthorizationRequest request) {
        try {
            MockPosAuthorizeResponse response = restClient.post()
                    .uri(AUTHORIZE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestMapper.toAuthorizeRequest(request))
                    .retrieve()
                    .body(MockPosAuthorizeResponse.class);

            return resultMapper.toAuthorizationResult(response);
        } catch (RestClientResponseException ex) {
            return resultMapper.authorizationError("30", "Format error");
        } catch (RestClientException ex) {
            return resultMapper.authorizationError("TIMEOUT", "Bank POS timeout");
        }
    }

    @Override
    public PaymentAuthorizationResult complete3ds(ThreeDsCompleteRequest request) {
        try {
            MockPos3DsCompleteResponse response = restClient.post()
                    .uri(THREE_DS_COMPLETE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestMapper.to3DsCompleteRequest(request))
                    .retrieve()
                    .body(MockPos3DsCompleteResponse.class);

            return resultMapper.to3DsCompleteResult(response);
        } catch (RestClientResponseException ex) {
            return resultMapper.authorizationError("30", "Format error");
        } catch (RestClientException ex) {
            return resultMapper.authorizationError("TIMEOUT", "Bank POS timeout");
        }
    }

    @Override
    public PaymentCaptureResult capture(PaymentCaptureRequest request) {
        try {
            MockPosCaptureResponse response = restClient.post()
                    .uri(CAPTURE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestMapper.toCaptureRequest(request))
                    .retrieve()
                    .body(MockPosCaptureResponse.class);

            return resultMapper.toCaptureResult(response);
        } catch (RestClientResponseException ex) {
            return resultMapper.captureError("30", "Format error");
        } catch (RestClientException ex) {
            return resultMapper.captureError("TIMEOUT", "Bank POS timeout");
        }
    }

    @Override
    public PaymentVoidResult voidAuthorization(PaymentVoidRequest request) {
        try {
            MockPosVoidResponse response = restClient.post()
                    .uri(VOID_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestMapper.toVoidRequest(request))
                    .retrieve()
                    .body(MockPosVoidResponse.class);

            return resultMapper.toVoidResult(response);
        } catch (RestClientResponseException ex) {
            return resultMapper.voidError("30", "Format error");
        } catch (RestClientException ex) {
            return resultMapper.voidError("TIMEOUT", "Bank POS timeout");
        }
    }

    @Override
    public PaymentRefundResult refund(PaymentRefundRequest request) {
        try {
            MockPosRefundResponse response = restClient.post()
                    .uri(REFUND_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestMapper.toRefundRequest(request))
                    .retrieve()
                    .body(MockPosRefundResponse.class);

            return resultMapper.toRefundResult(response);
        } catch (RestClientResponseException ex) {
            return resultMapper.refundError("30", "Format error");
        } catch (RestClientException ex) {
            return resultMapper.refundError("TIMEOUT", "Bank POS timeout");
        }
    }
}
