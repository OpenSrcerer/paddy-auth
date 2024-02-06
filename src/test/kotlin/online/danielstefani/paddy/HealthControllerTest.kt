package online.danielstefani.paddy

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class HealthControllerTest {

    @Test
    fun testHealthEndpoint() {
        given()
          .`when`().get("/health")
          .then()
             .statusCode(200)
             .body(`is`(":)"))
    }

}