package es.upm.miw.betca_tpv_spring.business_controllers;

import es.upm.miw.betca_tpv_spring.documents.CashierClosure;
import es.upm.miw.betca_tpv_spring.dtos.CashMovementInputDto;
import es.upm.miw.betca_tpv_spring.dtos.CashierClosureInputDto;
import es.upm.miw.betca_tpv_spring.dtos.CashierLastOutputDto;
import es.upm.miw.betca_tpv_spring.dtos.CashierStateOutputDto;
import es.upm.miw.betca_tpv_spring.exceptions.BadRequestException;
import es.upm.miw.betca_tpv_spring.repositories.CashierClosureReactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Controller
public class CashierClosureController {

    private CashierClosureReactRepository cashierClosureReactRepository;

    @Autowired
    public CashierClosureController(CashierClosureReactRepository cashierClosureReactRepository) {
        this.cashierClosureReactRepository = cashierClosureReactRepository;
    }

    private Mono<CashierClosure> lastCashierClosureStateAssure(boolean opened) {
        return this.cashierClosureReactRepository.findFirstByOrderByOpeningDateDesc()
                .handle((last, sink) -> {
                    if (last.isClosed() ^ opened) {
                        sink.next(last);
                    } else {
                        String msg = opened ? "Open cashier was expected: " : "Close cashier was expected: ";
                        sink.error(new BadRequestException(msg + last.getId()));
                    }
                });
    }

    public Mono<Void> createCashierClosureOpened() {
        Mono<CashierClosure> cashierClosure = this.lastCashierClosureStateAssure(false)
                .map(cashier -> new CashierClosure(cashier.getFinalCash()));
        return this.cashierClosureReactRepository.saveAll(cashierClosure).then();
    }

    public Mono<CashierLastOutputDto> findCashierClosureLast() {
        return this.cashierClosureReactRepository.findFirstByOrderByOpeningDateDesc()
                .map(CashierLastOutputDto::new);
    }

    public Mono<CashierStateOutputDto> readTotalsFromLast() {
        return this.lastCashierClosureStateAssure(true).map(
                last -> {
                    BigDecimal salesTotal = last.getSalesCard().add(last.getSalesCash())
                            .add(last.getUsedVouchers());
                    BigDecimal finalCash = last.getInitialCash().add(last.getSalesCash())
                            .add(last.getDeposit()).subtract(last.getWithdrawal());
                    return new CashierStateOutputDto(salesTotal, last.getSalesCard(), finalCash, last.getUsedVouchers());
                }
        );
    }

    public Mono<Void> close(CashierClosureInputDto cashierClosureInputDto) {
        Mono<CashierClosure> cashierClosure = this.lastCashierClosureStateAssure(true)
                .map(last -> {
                    last.close(cashierClosureInputDto.getFinalCard(), cashierClosureInputDto.getFinalCash(),
                            cashierClosureInputDto.getComment());
                    return last;
                });
        return this.cashierClosureReactRepository.saveAll(cashierClosure).then();
    }

    public Mono<Void> deposit(CashMovementInputDto cashMovementInputDto) {
        Mono<CashierClosure> cashierClosure = this.lastCashierClosureStateAssure(true)
                .map(last -> {
                    last.deposit(cashMovementInputDto.getCashMovement(), cashMovementInputDto.getComment());
                    return last;
                });
        return this.cashierClosureReactRepository.saveAll(cashierClosure).then();
    }

    public Mono<Void> withdrawal(CashMovementInputDto cashMovementInputDto) {
        Mono<CashierClosure> cashierClosureMono = this.lastCashierClosureStateAssure(true)
                .handle((last, sink) -> {
                    BigDecimal finalCash = last.getInitialCash().add(last.getSalesCash())
                            .add(last.getDeposit()).subtract(last.getWithdrawal());
                    if (cashMovementInputDto.getCashMovement().compareTo(finalCash) < 1) {
                        last.withdrawal(cashMovementInputDto.getCashMovement(), cashMovementInputDto.getComment());
                        sink.next(last);
                    } else {
                        String msg = "Not enough cash, you can only withdraw " + finalCash + "€";
                        sink.error(new BadRequestException(msg));
                    }
                });
        return this.cashierClosureReactRepository.saveAll(cashierClosureMono).then();
    }

}
