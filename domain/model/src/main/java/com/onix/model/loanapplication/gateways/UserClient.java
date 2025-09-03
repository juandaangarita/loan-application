package com.onix.model.loanapplication.gateways;

import com.onix.model.loanapplication.dto.UserDTO;
import reactor.core.publisher.Mono;

public interface UserClient {
    Mono<UserDTO> validateUserRegistered(String email, String documentNumber, String token);
}
