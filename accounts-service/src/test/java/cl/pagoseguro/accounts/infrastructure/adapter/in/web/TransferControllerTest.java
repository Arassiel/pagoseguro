package cl.pagoseguro.accounts.infrastructure.adapter.in.web;

import cl.pagoseguro.accounts.application.service.MoneyMovementService;
import cl.pagoseguro.accounts.domain.exception.InsufficientFundsException;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Transfer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MoneyMovementService moneyMovementService;

    private final UUID source = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void transferenciaExitosaDevuelve201() throws Exception {
        Transfer transfer = Transfer.create(new AccountId(source), new AccountId(target), Money.of("15000"),
                "Arriendo", Instant.parse("2026-11-09T12:00:00Z"));
        when(moneyMovementService.transfer(any())).thenReturn(transfer);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("15000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(15000.00))
                .andExpect(jsonPath("$.currency").value("CLP"))
                .andExpect(jsonPath("$.description").value("Arriendo"));
    }

    @Test
    void montoConMasDeDosDecimalesDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("100.001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());

        verifyNoInteractions(moneyMovementService);
    }

    @Test
    void saldoInsuficienteDevuelve422() throws Exception {
        when(moneyMovementService.transfer(any()))
                .thenThrow(new InsufficientFundsException("0011111111", Money.of("10"), Money.of("15000")));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("15000")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Saldo insuficiente")));
    }

    @Test
    void modificacionConcurrenteDevuelve409() throws Exception {
        when(moneyMovementService.transfer(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("AccountJpaEntity", source));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("15000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"));
    }

    private String body(String amount) {
        return """
                {"sourceAccountId": "%s", "targetAccountId": "%s", "amount": %s, "description": "Arriendo"}
                """.formatted(source, target, amount);
    }
}
