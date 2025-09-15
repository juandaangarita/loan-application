package com.onix.config;

import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.model.loanapplication.gateways.UserClient;
import com.onix.model.loanstatus.gateways.LoanStatusRepository;
import com.onix.model.loantype.gateways.LoanTypeRepository;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }

        @Bean
        public LoanRepository loanRepository() {
            return mock(LoanRepository.class);
        }

        @Bean
        public UserClient userClient() {
            return mock(UserClient.class);
        }

        @Bean
        public LoanStatusRepository loanStatusRepository() {
            return mock(LoanStatusRepository.class);
        }

        @Bean
        public LoanTypeRepository loanTypeRepository() {
            return mock(LoanTypeRepository.class);
        }

        @Bean
        public LoanValidator loanValidator() {
            return mock(LoanValidator.class);
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}