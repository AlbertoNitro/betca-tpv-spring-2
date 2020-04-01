package es.upm.miw.betca_tpv_spring.repositories;

import es.upm.miw.betca_tpv_spring.documents.CustomerDiscount;
import es.upm.miw.betca_tpv_spring.documents.User;
import es.upm.miw.betca_tpv_spring.dtos.CustomerDiscountDto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerDiscountReactRepository extends ReactiveSortingRepository<CustomerDiscount, String> {
    Mono<CustomerDiscount> findByUser(Mono<User> user);

    @Query(value = "{}", fields = "{ '_id': 0, 'mobile' : 1, 'discount' : 1}")
    Flux<CustomerDiscountDto> findAllCustomerDiscounts();
}