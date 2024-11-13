package tests;

import models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static specs.CreateSpec.createUserRequestSpec;
import static specs.CreateSpec.createUserResponseSpec;
import static specs.RegisterSpec.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiTests extends TestBase {

    @Test
    @DisplayName("Успешное создание нового пользователя")
    void successfulCreateUserTest() {

        CreateUserModel userData = new CreateUserModel();
        userData.setName("bond");
        userData.setJob("qa");

        CreateUserModel response = step("Запрос на создание нового пользователя", ()->
                given(createUserRequestSpec)
                        .body(userData)

                        .when()
                        .post("/users")

                        .then()
                        .spec(createUserResponseSpec)
                        .extract().as(CreateUserModel.class));

        step("Проверка имени нового пользователя", ()->
                assertEquals("bond",response.getName()));

        step("Проверка профессии нового пользователя", ()->
                assertEquals("qa", response.getJob()));

        step("Проверка присвоения id пользователю", ()->
                assertNotNull(response.getId()));

        step("Проверка записи времени создания пользователя", ()->
                assertNotNull(response.getCreatedAt()));
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void successfulRegisterUserTest() {

        RegistrationBodyModel authData = new RegistrationBodyModel();
        authData.setEmail("eve.holt@reqres.in");
        authData.setPassword("pistol");

        RegistrationResponseModel response = step("Запрос на регистрацию существующего пользователя", ()->
                given(registerRequestSpec)
                        .body(authData)

                        .when()
                        .post("/register")

                        .then()
                        .spec(responseSpec200)
                        .extract().as(RegistrationResponseModel.class));

        step("Проверка Id", ()->
                assertEquals("4", response.getId()));

        step("Проверка token", ()->
                assertThat(response.getToken()).isNotEmpty());
    }

    @Test
    @DisplayName("Отправка на регистрацию с незаполненными email/password")
    void emptyAuthDataTest() {

        CreateUserModel response = step("Передача запроса на регистрацию с незаполненными email/password", ()->
                given(registerRequestSpec)

                        .when()
                        .post("/register")

                        .then()
                        .spec(errorResponseSpec400)
                        .extract().as(CreateUserModel.class));

        step("Проверка ответа", ()->
                assertEquals("Missing email or username", response.getError()));
    }

    @Test
    @DisplayName("Отправка на регистрацию с незаполненным password")
    void userMissingPassRegisterTest() {
        RegistrationBodyModel authData = new RegistrationBodyModel();
        authData.setEmail("eve.holt@reqres.in");

        CreateUserModel response = step("Запрос на регистрацию без ввода пароля", ()->
                given(registerRequestSpec)
                        .body(authData)

                        .when()
                        .post("/register")

                        .then()
                        .spec(errorResponseSpec400)
                        .extract().as(CreateUserModel.class));

        step("Проверка ответа", ()->
                assertEquals("Missing password", response.getError()));
    }

    @Test
    @DisplayName("Отправка на регистрацию пользователя с несуществующим email")
    void notDefindedEmailRegisterTest() {

        RegistrationBodyModel authData = new RegistrationBodyModel();
        authData.setEmail("123test@bk.ru");
        authData.setPassword("pistol");

        CreateUserModel response = step("Запрос на регистрацию пользователя с несуществующим email", ()->
                given(registerRequestSpec)
                        .body(authData)

                        .when()
                        .post("/register")

                        .then()
                        .spec(errorResponseSpec400)
                        .extract().as(CreateUserModel.class));

        step("Проверка ответа", ()->
                assertEquals("Note: Only defined users succeed registration", response.getError()));
    }
}