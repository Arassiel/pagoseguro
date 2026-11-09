package cl.pagoseguro.accounts.infrastructure.adapter.in.web;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.port.in.AccountStatementUseCase;
import cl.pagoseguro.accounts.domain.port.in.DepositMoneyUseCase;
import cl.pagoseguro.accounts.domain.port.in.DepositMoneyUseCase.DepositCommand;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.AmountRequest;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.MonthlySummaryResponse;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.MovementResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Operaciones sobre el dinero de una cuenta: depósitos, cartola y resumen mensual. */
@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
class AccountMovementsController {

    private final DepositMoneyUseCase depositMoney;
    private final AccountStatementUseCase statement;

    AccountMovementsController(DepositMoneyUseCase depositMoney, AccountStatementUseCase statement) {
        this.depositMoney = depositMoney;
        this.statement = statement;
    }

    @PostMapping("/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    MovementResponse deposit(@PathVariable UUID accountId, @Valid @RequestBody AmountRequest request) {
        return MovementResponse.from(depositMoney.deposit(
                new DepositCommand(new AccountId(accountId), new Money(request.amount()), request.description())));
    }

    @GetMapping("/movements")
    List<MovementResponse> movements(@PathVariable UUID accountId,
                                     @RequestParam(defaultValue = "50") int limit) {
        return statement.latestMovements(new AccountId(accountId), limit).stream()
                .map(MovementResponse::from)
                .toList();
    }

    /** Ejemplo: GET /api/v1/accounts/{id}/summary?period=2026-11 */
    @GetMapping("/summary")
    MonthlySummaryResponse summary(@PathVariable UUID accountId,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        return MonthlySummaryResponse.from(statement.monthlySummary(new AccountId(accountId), period));
    }
}
