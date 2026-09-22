package org.example.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class ReqresApiTest {

    @Test
    void getSingleUserReturnsExpectedId() {
        given()
                .baseUri("https://reqres.in/api")
                .header("x-api-key", "reqres-free-v1")
        .when()
                .get("/users/2")
        .then()
                .statusCode(200)
                .body("data.id", equalTo(2))
                .body("data.email", notNullValue());
    }
}
