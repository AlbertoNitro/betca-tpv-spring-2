package es.upm.miw.betca_tpv_spring.api_rest_controllers;

import es.upm.miw.betca_tpv_spring.documents.User;
import es.upm.miw.betca_tpv_spring.dtos.MessagesDto;
import es.upm.miw.betca_tpv_spring.dtos.UserCredentialDto;
import es.upm.miw.betca_tpv_spring.dtos.UserDto;
import es.upm.miw.betca_tpv_spring.dtos.UserMinimumDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDateTime;

import static es.upm.miw.betca_tpv_spring.api_rest_controllers.UserResource.MESSAGES;
import static es.upm.miw.betca_tpv_spring.api_rest_controllers.UserResource.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@ApiTestConfig
class UserResourceIT {

    @Autowired
    private RestService restService;

    @Autowired
    private WebTestClient webTestClient;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Test
    void testLogin() {
        this.restService.loginAdmin(this.webTestClient);
        assertTrue(this.restService.getTokenDto().getToken().length() > 10);
    }

    @Test
    void testLoginAdminPassNull() {
        webTestClient
                .mutate().filter(basicAuthentication(restService.getAdminMobile(), "kk")).build()
                .post().uri(contextPath + USERS + UserResource.TOKEN)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginNonMobile() {
        webTestClient
                .mutate().filter(basicAuthentication("1", "kk")).build()
                .post().uri(contextPath + USERS + UserResource.TOKEN)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testReadAdminWithAdminRole() {
        this.restService.loginAdmin(this.webTestClient)
                .get().uri(contextPath + USERS + UserResource.MOBILE_ID, this.restService.getAdminMobile())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(Assertions::assertNotNull)
                .value(user -> assertEquals(this.restService.getAdminMobile(), user.getMobile()));
    }

    @Test
    void testReadOperatorWithManagerRole() {
        this.restService.loginManager(this.webTestClient)
                .get().uri(contextPath + USERS + UserResource.MOBILE_ID, "666666002")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(Assertions::assertNotNull)
                .value(user -> assertEquals("666666002", user.getMobile()));

    }

    @Test
    void testReadCustomerWithRoleOperator() {
        this.restService.loginManager(this.webTestClient)
                .get().uri(contextPath + USERS + UserResource.MOBILE_ID, "666666004")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(Assertions::assertNotNull)
                .value(user -> assertEquals("666666004", user.getMobile()));
    }

    @Test
    void testReadOperatorWithRoleOperator() {
        this.restService.loginOperator(this.webTestClient)
                .get().uri(contextPath + USERS + UserResource.MOBILE_ID, "666666003")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testReadAll() {
        this.restService.loginAdmin(this.webTestClient)
                .get().uri(contextPath + USERS)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserMinimumDto.class)
                .value(Assertions::assertNotNull)
                .value(list -> assertTrue(list.size() > 1));
    }

    @Test
    void testCreateUserAlreadyExists() {
        this.restService.loginAdmin(this.webTestClient)
                .post().uri(contextPath + USERS)
                .body(BodyInserters.fromObject(
                        new UserDto(User.builder().mobile("666666000").username("all-roles").build())))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testCreateUserBadNumber() {
        this.restService.loginAdmin(this.webTestClient)
                .post().uri(contextPath + USERS)
                .body(BodyInserters.fromObject(
                        new UserDto(User.builder().mobile("7").username("m001").build())))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testCreateUser() {
        this.restService.loginAdmin(this.webTestClient)
                .post().uri(contextPath + USERS)
                .body(BodyInserters.fromObject(
                        new UserDto(User.builder().mobile("616117255").username("m001").dni("51714988V").address("C/M, 14").email("m001@gmail.com").build()))
                ).exchange().expectStatus().isOk().expectBody(UserDto.class)
                .value(Assertions::assertNotNull);
    }

    @Test
    void testUpdateUserNonExist() {
        this.restService.loginAdmin(this.webTestClient)
                .put().uri(contextPath + USERS + UserResource.MOBILE_ID, "999999999")
                .body(BodyInserters.fromObject(
                        new UserDto(User.builder().mobile("666666007").build())
                )).exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateUserBadNumber() {
        this.restService.loginAdmin(this.webTestClient)
                .put().uri(contextPath + USERS + UserResource.MOBILE_ID, "666666002")
                .body(BodyInserters.fromObject(
                        new UserDto(User.builder().mobile("7").build())
                )).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateUserToMobileAlreadyExists() {
        this.restService.loginAdmin(this.webTestClient)
                .put().uri(contextPath + USERS + UserResource.MOBILE_ID, "666666002")
                .body(BodyInserters.fromObject(
                        new UserDto(User.builder().mobile("666666003").build())
                )).exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testUpdateUser() {
        this.restService.loginAdmin(this.webTestClient)
                .put().uri(contextPath + USERS + UserResource.MOBILE_ID, "666666006")
                .body(BodyInserters.fromObject(
                        new UserDto(User.builder().mobile("666666006").username("u006").password("p006").dni("66666606W").address("C/TPV, 6").email("u006@gmail.com").build())
                )).exchange().expectStatus().isOk().expectBody(UserDto.class)
                .value(Assertions::assertNotNull);
    }

    @Test
    void testSendMessageToUser() {
        LocalDateTime ldt = LocalDateTime.now();
        MessagesDto messagesDto = new MessagesDto("666666006", "666666007", "FROM 6 to 7", ldt, null);
        this.restService.loginAdmin(this.webTestClient)
                .put().uri(contextPath + USERS + MESSAGES)
                .body(BodyInserters.fromObject(messagesDto)).exchange().expectStatus().isOk()
                .expectBody(MessagesDto.class)
                .value(Assertions::assertNotNull);
    }

    @Test
    void testSendMessageToUserWihNoOtherMessages() {
        LocalDateTime ldt = LocalDateTime.now();
        MessagesDto messagesDto = new MessagesDto("666666002", "666666003", "FROM 2 to 3", ldt, null);
        this.restService.loginAdmin(this.webTestClient)
                .put().uri(contextPath + USERS + MESSAGES)
                .body(BodyInserters.fromObject(messagesDto)).exchange().expectStatus().isOk().expectBody(MessagesDto.class)
                .value(Assertions::assertNotNull);
    }

    @Test
    void updatePassword() {
        this.restService.loginAdmin(this.webTestClient)
                .patch().uri(contextPath + USERS + "/password" + UserResource.MOBILE_ID, "6")
                .body(BodyInserters.fromObject(
                        new UserCredentialDto("6", "5")
                )).exchange().expectStatus().isOk().expectBody(UserDto.class)
                .value(Assertions::assertNotNull);
    }

    @Test
    void updatePasswordMobileBadRequest() {
        this.restService.loginAdmin(this.webTestClient)
                .patch().uri(contextPath + USERS + "/password" + UserResource.MOBILE_ID, "")
                .body(BodyInserters.fromObject(
                        new UserCredentialDto("6", "5")
                )).exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updatePasswordMobileNotFound() {
        this.restService.loginAdmin(this.webTestClient)
                .patch().uri(contextPath + USERS + "/password" + UserResource.MOBILE_ID, "asda")
                .body(BodyInserters.fromObject(
                        new UserCredentialDto("6", "5")
                )).exchange().expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }
}
