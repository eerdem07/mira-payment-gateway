package com.eerdem07.mira.gateway.auth.application.port.in;

import java.util.UUID;

public record GenerateAccessTokenCommand (UUID merchantId){
}
