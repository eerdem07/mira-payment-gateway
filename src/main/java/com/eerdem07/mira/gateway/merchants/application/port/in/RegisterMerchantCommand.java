package com.eerdem07.mira.gateway.merchants.application.port.in;

public record RegisterMerchantCommand(String legalName, String email, String password,
                                      String paswordConfirm) {
}
