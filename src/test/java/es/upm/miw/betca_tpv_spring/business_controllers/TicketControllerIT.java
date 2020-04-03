package es.upm.miw.betca_tpv_spring.business_controllers;

import es.upm.miw.betca_tpv_spring.TestConfig;
import es.upm.miw.betca_tpv_spring.dtos.CashierClosureInputDto;
import es.upm.miw.betca_tpv_spring.dtos.ShoppingDto;
import es.upm.miw.betca_tpv_spring.dtos.TicketCreationInputDto;
import es.upm.miw.betca_tpv_spring.dtos.TicketSearchDto;
import es.upm.miw.betca_tpv_spring.repositories.ArticleRepository;
import es.upm.miw.betca_tpv_spring.repositories.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestConfig
public class TicketControllerIT {

    @Autowired
    private TicketController ticketController;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CashierClosureController cashierClosureController;

    @Test
    void testUpdateStockWhenCreateTicket() {
        int stock = this.articleRepository.findById("1").get().getStock();
        StepVerifier
                .create(this.cashierClosureController.createCashierClosureOpened()).expectComplete().verify();
        ShoppingDto shoppingDto =
                new ShoppingDto("1", "prueba", BigDecimal.TEN, 1, BigDecimal.ZERO,
                        BigDecimal.TEN, true);
        TicketCreationInputDto ticketCreationInputDto = new TicketCreationInputDto(null, BigDecimal.TEN
                , BigDecimal.ZERO, BigDecimal.ZERO, Collections.singletonList(shoppingDto), "Nota del ticket...");
        StepVerifier
                .create(this.ticketController.createTicketAndPdf(ticketCreationInputDto))
                .expectNextCount(1)
                .expectComplete()
                .verify();
        assertEquals(new Integer(stock - 1), this.articleRepository.findById("1").get().getStock());
        shoppingDto.setAmount(-1);
        StepVerifier
                .create(this.ticketController.createTicketAndPdf(ticketCreationInputDto))
                .expectNextCount(1)
                .expectComplete()
                .verify();
        assertEquals(new Integer(stock), this.articleRepository.findById("1").get().getStock());
        StepVerifier
                .create(this.cashierClosureController.close(new CashierClosureInputDto())).expectComplete().verify();
    }

    @Test
    void testSearchOnlyByMobile() {
        String mobile = "666666004";
        TicketSearchDto ticketSearchDto = new TicketSearchDto(mobile, null, null);
        StepVerifier
                .create(this.ticketController.searchByMobileDateOrAmount(ticketSearchDto))
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    void testSearchOnlyByDate() {
        LocalDateTime date = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        TicketSearchDto ticketSearchDto = new TicketSearchDto(null, date, null);

        int expected = this.ticketRepository.findByCreationDateBetween(date, date.plusDays(1)).size();

        StepVerifier
                .create(this.ticketController.searchByMobileDateOrAmount(ticketSearchDto))
                .expectNextCount(expected)
                .expectComplete()
                .verify();
    }

    @Test
    void testSearchOnlyByAmount() {
        Integer amount = 6;
        TicketSearchDto ticketSearchDto = new TicketSearchDto(null, null, amount);
        StepVerifier
                .create(this.ticketController.searchByMobileDateOrAmount(ticketSearchDto))
                .expectNextCount(4)
                .expectComplete()
                .verify();
    }

    @Test
    void testSearchByMobileDateAndAmount() {
        String mobile = "666666004";
        LocalDateTime date = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Integer amount = 6;
        TicketSearchDto ticketSearchDto = new TicketSearchDto(mobile, date, amount);
        StepVerifier
                .create(this.ticketController.searchByMobileDateOrAmount(ticketSearchDto))
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    void testGetNotCommittedByArticle() {
        String articleId = "8400000000024";
        StepVerifier
                .create(this.ticketController.searchNotCommittedByArticle(articleId))
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }
}
