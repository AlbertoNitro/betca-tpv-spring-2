package es.upm.miw.betca_tpv_spring.api_rest_controllers;

import es.upm.miw.betca_tpv_spring.business_controllers.CustomerDiscountController;
import es.upm.miw.betca_tpv_spring.dtos.CustomerDiscountDto;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;


@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('OPERATOR')")
@RestController
@RequestMapping(CustomerDiscountResource.CUSTOMER_DISCOUNTS)
public class CustomerDiscountResource {

    public static final String CUSTOMER_DISCOUNTS = "/customer-discounts";

    private CustomerDiscountController customerDiscountController;

    @Autowired
    CustomerDiscountResource(CustomerDiscountController customerDiscountController) {
        this.customerDiscountController = customerDiscountController;
    }

    @GetMapping
    public Flux<CustomerDiscountDto> readAll() {
        return this.customerDiscountController.readAll()
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @PostMapping
    public Mono<CustomerDiscountDto> createCustomerDiscount(@Valid @RequestBody CustomerDiscountDto customerDiscountDto) {
        return this.customerDiscountController.createCustomerDiscount(customerDiscountDto)
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }
}
