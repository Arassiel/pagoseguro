package cl.pagoseguro.accounts.infrastructure.adapter.in.web;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Transfer;
import cl.pagoseguro.accounts.domain.port.in.TransferMoneyUseCase;
import cl.pagoseguro.accounts.domain.port.in.TransferMoneyUseCase.TransferCommand;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.TransferRequest;
import cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto.TransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
class TransferController {

    private final TransferMoneyUseCase transferMoney;

    TransferController(TransferMoneyUseCase transferMoney) {
        this.transferMoney = transferMoney;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        Transfer transfer = transferMoney.transfer(new TransferCommand(
                new AccountId(request.sourceAccountId()),
                new AccountId(request.targetAccountId()),
                new Money(request.amount()),
                request.description()));
        return TransferResponse.from(transfer);
    }
}
