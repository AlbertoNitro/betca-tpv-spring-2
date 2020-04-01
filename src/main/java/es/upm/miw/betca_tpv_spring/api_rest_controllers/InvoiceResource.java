package es.upm.miw.betca_tpv_spring.api_rest_controllers;

import es.upm.miw.betca_tpv_spring.business_controllers.InvoiceController;
import es.upm.miw.betca_tpv_spring.dtos.InvoiceNegativeCreationInputDto;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('OPERATOR')")
@RestController
@RequestMapping(InvoiceResource.INVOICES)
public class InvoiceResource {

    public static final String INVOICES = "/invoices";
    public static final String INVOICE_ID = "/{id}";
    public static final String PRINT = "/print";
    public static final String NEGATIVE = "/negative";
    private InvoiceController invoiceController;

    @Autowired
    public InvoiceResource(InvoiceController invoiceController) {
        this.invoiceController = invoiceController;
    }

    @PostMapping
    public Mono<byte[]> create() {
        return this.invoiceController.createAndPdf()
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @PatchMapping(value = INVOICE_ID + PRINT)
    public Mono<byte[]> generate(@PathVariable String id){
        return this.invoiceController.updateAndPdf(id)
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }

    @PostMapping(value = NEGATIVE)
    public Mono<byte[]> generateNegative(@RequestBody @Valid InvoiceNegativeCreationInputDto invoiceNegativeCreationInputDto){
        return this.invoiceController.createNegativeAndPdf(invoiceNegativeCreationInputDto)
                .doOnNext(log -> LogManager.getLogger(this.getClass()).debug(log));
    }
}
