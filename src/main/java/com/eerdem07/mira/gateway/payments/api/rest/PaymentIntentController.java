package com.eerdem07.mira.gateway.payments.api.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PaymentIntentController {

    @PostMapping("/payment-intents")
    public String createPaymentIntent(){
        return "";
    }
}
