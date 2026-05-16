package com.eerdem07.mira.gateway.payments.pos;

import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidStatus;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MockBankPosPaymentProcessorAdapterTest {

    @Test
    void capture_shouldPostCaptureRequestToMockPos() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/v1/pos/capture", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                    {
                      "status": "CAPTURED",
                      "transactionType": "CAPTURE",
                      "approved": true,
                      "responseCode": "00",
                      "responseMessage": "Capture approved",
                      "transactionId": "capture_123",
                      "originalTransactionId": "attempt_123",
                      "posCaptureId": "pos_cap_123",
                      "originalPosTransactionId": "pos_txn_123",
                      "hostReferenceNumber": "HST-CAP-123",
                      "amount": "1250.50",
                      "currency": "TRY",
                      "capturedAt": "2026-05-13T08:30:00Z"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            MockBankPosPaymentProcessorAdapter adapter = new MockBankPosPaymentProcessorAdapter(
                    "http://localhost:" + port,
                    "mrc_mock_001",
                    "term_mock_001"
            );

            PaymentCaptureResult result = adapter.capture(new PaymentCaptureRequest(
                    UUID.randomUUID(),
                    UUID.fromString("00000000-0000-0000-0000-000000000123"),
                    UUID.fromString("00000000-0000-0000-0000-000000000456"),
                    UUID.randomUUID(),
                    new BigDecimal("1250.50"),
                    "TRY",
                    "order-123",
                    "pos_txn_123",
                    "AUTH123",
                    "HST-AUTH-123"
            ));

            assertThat(result.status()).isEqualTo(PaymentCaptureStatus.SUCCEEDED);
            assertThat(result.posCaptureId()).isEqualTo("pos_cap_123");
            assertThat(result.posReference()).isEqualTo("HST-CAP-123");
            assertThat(result.responseCode()).isEqualTo("00");

            assertThat(requestBody.get()).contains("\"merchantId\":\"mrc_mock_001\"");
            assertThat(requestBody.get()).contains("\"terminalId\":\"term_mock_001\"");
            assertThat(requestBody.get()).contains("\"orderId\":\"order-123\"");
            assertThat(requestBody.get()).contains("\"transactionId\":\"00000000-0000-0000-0000-000000000123\"");
            assertThat(requestBody.get()).contains("\"originalTransactionId\":\"00000000-0000-0000-0000-000000000456\"");
            assertThat(requestBody.get()).contains("\"originalPosTransactionId\":\"pos_txn_123\"");
            assertThat(requestBody.get()).contains("\"authCode\":\"AUTH123\"");
            assertThat(requestBody.get()).contains("\"hostReferenceNumber\":\"HST-AUTH-123\"");
            assertThat(requestBody.get()).contains("\"amount\":\"1250.50\"");
            assertThat(requestBody.get()).contains("\"currency\":\"TRY\"");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void voidAuthorization_shouldPostVoidRequestToMockPos() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/v1/pos/void", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                    {
                      "status": "VOIDED",
                      "transactionType": "VOID",
                      "approved": true,
                      "responseCode": "00",
                      "responseMessage": "Void approved",
                      "transactionId": "void_123",
                      "originalTransactionId": "attempt_123",
                      "posVoidId": "pos_void_123",
                      "originalPosTransactionId": "pos_txn_123",
                      "hostReferenceNumber": "HST-VOID-123",
                      "amount": "1250.50",
                      "currency": "TRY",
                      "voidedAt": "2026-05-13T08:45:00Z"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            MockBankPosPaymentProcessorAdapter adapter = new MockBankPosPaymentProcessorAdapter(
                    "http://localhost:" + port,
                    "mrc_mock_001",
                    "term_mock_001"
            );

            PaymentVoidResult result = adapter.voidAuthorization(new PaymentVoidRequest(
                    UUID.randomUUID(),
                    UUID.fromString("00000000-0000-0000-0000-000000000123"),
                    UUID.fromString("00000000-0000-0000-0000-000000000456"),
                    UUID.randomUUID(),
                    new BigDecimal("1250.50"),
                    "TRY",
                    "order-123",
                    "pos_txn_123",
                    "AUTH123",
                    "HST-AUTH-123"
            ));

            assertThat(result.status()).isEqualTo(PaymentVoidStatus.SUCCEEDED);
            assertThat(result.posVoidId()).isEqualTo("pos_void_123");
            assertThat(result.posReference()).isEqualTo("HST-VOID-123");
            assertThat(result.responseCode()).isEqualTo("00");

            assertThat(requestBody.get()).contains("\"merchantId\":\"mrc_mock_001\"");
            assertThat(requestBody.get()).contains("\"terminalId\":\"term_mock_001\"");
            assertThat(requestBody.get()).contains("\"orderId\":\"order-123\"");
            assertThat(requestBody.get()).contains("\"transactionId\":\"00000000-0000-0000-0000-000000000123\"");
            assertThat(requestBody.get()).contains("\"originalTransactionId\":\"00000000-0000-0000-0000-000000000456\"");
            assertThat(requestBody.get()).contains("\"originalPosTransactionId\":\"pos_txn_123\"");
            assertThat(requestBody.get()).contains("\"authCode\":\"AUTH123\"");
            assertThat(requestBody.get()).contains("\"hostReferenceNumber\":\"HST-AUTH-123\"");
            assertThat(requestBody.get()).contains("\"amount\":\"1250.50\"");
            assertThat(requestBody.get()).contains("\"currency\":\"TRY\"");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void refund_shouldPostRefundRequestToMockPos() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/v1/pos/refund", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                    {
                      "status": "REFUNDED",
                      "transactionType": "REFUND",
                      "approved": true,
                      "responseCode": "00",
                      "responseMessage": "Refund approved",
                      "transactionId": "refund_123",
                      "originalTransactionId": "attempt_123",
                      "posRefundId": "pos_ref_123",
                      "originalPosTransactionId": "pos_txn_123",
                      "hostReferenceNumber": "HST-REF-123",
                      "amount": "1250.50",
                      "currency": "TRY",
                      "refundedAt": "2026-05-13T09:00:00Z"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            MockBankPosPaymentProcessorAdapter adapter = new MockBankPosPaymentProcessorAdapter(
                    "http://localhost:" + port,
                    "mrc_mock_001",
                    "term_mock_001"
            );

            PaymentRefundResult result = adapter.refund(new PaymentRefundRequest(
                    UUID.randomUUID(),
                    UUID.fromString("00000000-0000-0000-0000-000000000123"),
                    UUID.fromString("00000000-0000-0000-0000-000000000456"),
                    UUID.randomUUID(),
                    new BigDecimal("1250.50"),
                    "TRY",
                    "order-123",
                    "pos_txn_123",
                    "AUTH123",
                    "HST-AUTH-123"
            ));

            assertThat(result.status()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
            assertThat(result.posRefundId()).isEqualTo("pos_ref_123");
            assertThat(result.posReference()).isEqualTo("HST-REF-123");
            assertThat(result.responseCode()).isEqualTo("00");

            assertThat(requestBody.get()).contains("\"merchantId\":\"mrc_mock_001\"");
            assertThat(requestBody.get()).contains("\"terminalId\":\"term_mock_001\"");
            assertThat(requestBody.get()).contains("\"orderId\":\"order-123\"");
            assertThat(requestBody.get()).contains("\"transactionId\":\"00000000-0000-0000-0000-000000000123\"");
            assertThat(requestBody.get()).contains("\"originalTransactionId\":\"00000000-0000-0000-0000-000000000456\"");
            assertThat(requestBody.get()).contains("\"originalPosTransactionId\":\"pos_txn_123\"");
            assertThat(requestBody.get()).contains("\"authCode\":\"AUTH123\"");
            assertThat(requestBody.get()).contains("\"hostReferenceNumber\":\"HST-AUTH-123\"");
            assertThat(requestBody.get()).contains("\"amount\":\"1250.50\"");
            assertThat(requestBody.get()).contains("\"currency\":\"TRY\"");
        } finally {
            server.stop(0);
        }
    }
}
