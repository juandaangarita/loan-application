package com.onix.model.loanapplication.gateways;

import com.onix.model.loanapplication.dto.UserDTO;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface UserClient {
    Mono<UserDTO> validateUserRegistered(String email, String documentNumber, String token);
    Mono<Map<String, UserDTO>> getUsersByEmails(Set<String> emails, String token);
}
