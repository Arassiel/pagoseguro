package cl.pagoseguro.accounts.infrastructure.adapter.in.web;

import cl.pagoseguro.accounts.application.service.AccountService;
import cl.pagoseguro.accounts.domain.exception.AccountNotFoundException;
import cl.pagoseguro.accounts.domain.exception.DomainException;
import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Rut;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test del adaptador web: levanta solo la capa MVC, con el caso de uso mockeado. */
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private final Account account = Account.open("0011111111", new Rut("12.345.678-5"), "Franco Vera",
            Instant.parse("2026-11-04T12:00:00Z"));

    @Test
    void postCreaLaCuentaYDevuelve201() throws Exception {
        when(accountService.open(any())).thenReturn(account);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"holderRut": "12.345.678-5", "holderName": "Franco Vera"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/accounts/" + account.getId().value()))
                .andExpect(jsonPath("$.accountNumber").value("0011111111"))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void postConCamposVaciosDevuelve400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"holderRut": "", "holderName": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.holderRut").exists())
                .andExpect(jsonPath("$.errors.holderName").exists());

        verifyNoInteractions(accountService);
    }

    @Test
    void reglaDeNegocioIncumplidaDevuelve422() throws Exception {
        when(accountService.open(any())).thenThrow(new DomainException("Dígito verificador inválido"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"holderRut": "12.345.678-9", "holderName": "Franco Vera"}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Dígito verificador inválido"));
    }

    @Test
    void getDeCuentaInexistenteDevuelve404() throws Exception {
        AccountId id = AccountId.newId();
        when(accountService.getById(id)).thenThrow(new AccountNotFoundException(id));

        mockMvc.perform(get("/api/v1/accounts/" + id.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cuenta no encontrada"));
    }
}
