package cl.pagoseguro.accounts.infrastructure.adapter.in.web;

import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.port.in.CloseAccountUseCase;
import cl.pagoseguro.accounts.domain.port.in.GetAccountUseCase;
import cl.pagoseguro.accounts.domain.port.in.OpenAccountUseCase;
import cl.pagoseguro.accounts.domain.port.in.OpenAccountUseCase.OpenAccountCommand;
import cl.pagoseguro.accounts.domain.port.in.UpdateAccountUseCase;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.AccountResponse;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.ChangeHolderNameRequest;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.OpenAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de entrada: traduce HTTP a casos de uso. No contiene reglas de negocio.
 * Depende de las INTERFACES (puertos), no de AccountService directamente.
 */
@RestController
@RequestMapping("/api/v1/accounts")
class AccountController {

    private final OpenAccountUseCase openAccount;
    private final GetAccountUseCase getAccount;
    private final UpdateAccountUseCase updateAccount;
    private final CloseAccountUseCase closeAccount;

    AccountController(OpenAccountUseCase openAccount, GetAccountUseCase getAccount,
                      UpdateAccountUseCase updateAccount, CloseAccountUseCase closeAccount) {
        this.openAccount = openAccount;
        this.getAccount = getAccount;
        this.updateAccount = updateAccount;
        this.closeAccount = closeAccount;
    }

    @PostMapping
    ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        Account account = openAccount.open(new OpenAccountCommand(request.holderRut(), request.holderName()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(account.getId().value()).toUri();
        return ResponseEntity.created(location).body(AccountResponse.from(account));
    }

    @GetMapping
    List<AccountResponse> list() {
        return getAccount.listAll().stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/{id}")
    AccountResponse get(@PathVariable UUID id) {
        return AccountResponse.from(getAccount.getById(new AccountId(id)));
    }

    @PatchMapping("/{id}")
    AccountResponse changeHolderName(@PathVariable UUID id, @Valid @RequestBody ChangeHolderNameRequest request) {
        return AccountResponse.from(updateAccount.changeHolderName(new AccountId(id), request.holderName()));
    }

    @PostMapping("/{id}/close")
    AccountResponse close(@PathVariable UUID id) {
        return AccountResponse.from(closeAccount.close(new AccountId(id)));
    }
}
