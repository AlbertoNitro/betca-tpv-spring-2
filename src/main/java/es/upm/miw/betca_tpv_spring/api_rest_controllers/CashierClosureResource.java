package es.upm.miw.betca_tpv_spring.api_rest_controllers;

import es.upm.miw.betca_tpv_spring.business_controllers.CashierClosureController;
import es.upm.miw.betca_tpv_spring.dtos.CashMovementInputDto;
import es.upm.miw.betca_tpv_spring.dtos.CashierClosureInputDto;
import es.upm.miw.betca_tpv_spring.dtos.CashierLastOutputDto;
import es.upm.miw.betca_tpv_spring.dtos.CashierStateOutputDto;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('OPERATOR')")
@RestController
@RequestMapping(CashierClosureResource.CASHIER_CLOSURES)
public class CashierClosureResource {

    public static final String CASHIER_CLOSURES = "/cashier-closures";
    public static final String LAST = "/last";
    public static final String STATE = "/state";
    public static final String DEPOSIT = "/deposit";
    public static final String WITHDRAWAL = "/withdrawal";

    private CashierClosureController cashierClosureController;

    @Autowired
    public CashierClosureResource(CashierClosureController cashierClosureController) {
        this.cashierClosureController = cashierClosureController;
    }

    @PostMapping
    public Mono<Void> createCashierClosureOpened() {
        return cashierClosureController.createCashierClosureOpened()
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @GetMapping(value = LAST)
    public Mono<CashierLastOutputDto> findCashierClosureLast() {
        return cashierClosureController.findCashierClosureLast()
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @GetMapping(value = LAST + STATE)
    public Mono<CashierStateOutputDto> readStateFromLast() {
        return this.cashierClosureController.readTotalsFromLast()
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @PatchMapping(value = LAST)
    public Mono<Void> closeCashierClosure(@Valid @RequestBody CashierClosureInputDto cashierClosureInputDto) {
        return cashierClosureController.close(cashierClosureInputDto)
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @PatchMapping(value = LAST + DEPOSIT)
    public Mono<Void> cashierDeposit(@Valid @RequestBody CashMovementInputDto cashMovementInputDto){
        return cashierClosureController.deposit(cashMovementInputDto)
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @PatchMapping(value = LAST + WITHDRAWAL)
    public Mono<Void> cashierWithdrawal(@Valid @RequestBody CashMovementInputDto cashMovementInputDto){
        return cashierClosureController.withdrawal(cashMovementInputDto)
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

}
