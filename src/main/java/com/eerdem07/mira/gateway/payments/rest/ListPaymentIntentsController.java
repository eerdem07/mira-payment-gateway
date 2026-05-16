package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.ListPaymentIntentsQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.ListPaymentIntentsResult;
import com.eerdem07.mira.gateway.payments.application.port.in.ListPaymentIntentsUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.GetPaymentIntentResponse;
import com.eerdem07.mira.gateway.payments.rest.dto.ListPaymentIntentsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payment-intents")
public class ListPaymentIntentsController {

    private final ListPaymentIntentsUseCase listPaymentIntentsUseCase;

    public ListPaymentIntentsController(ListPaymentIntentsUseCase listPaymentIntentsUseCase) {
        this.listPaymentIntentsUseCase = listPaymentIntentsUseCase;
    }

    @GetMapping
    public ResponseEntity<ListPaymentIntentsResponse> list(
            @AuthenticationPrincipal String merchantIdString) {

        UUID merchantId = UUID.fromString(merchantIdString);
        ListPaymentIntentsResult result = listPaymentIntentsUseCase.execute(
                new ListPaymentIntentsQuery(merchantId)
        );

        List<GetPaymentIntentResponse> items = result.items().stream()
                .map(item -> new GetPaymentIntentResponse(
                        item.id(),
                        item.status(),
                        item.amount(),
                        item.currency(),
                        item.captureMethod(),
                        item.merchantReference(),
                        item.description(),
                        item.expiresAt(),
                        item.createdAt()
                ))
                .toList();

        return ResponseEntity.ok(new ListPaymentIntentsResponse(items, items.size()));
    }
}
