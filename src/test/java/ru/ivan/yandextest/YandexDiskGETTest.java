package ru.ivan.yandextest;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class YandexDiskGETTest {
}

class YandexDiskGetTest {

    private static final String BASE_URL = "https://cloud-api.yandex.net";

    static Dotenv dotenv = Dotenv.load();
    private static String TOKEN = dotenv.get("YANDEX_DISK_OAUTH_TOKEN");

    @BeforeAll
    static void PrintToken() {
        System.out.println(TOKEN);
    }

    @Test
    void GetDiskInformation() {
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
        .when()
            .get("/v1/disk")
        .then()
            .statusCode(200)
            .contentType("application/json")
            .body("total_space", greaterThanOrEqualTo(0L))
            .body("used_space", greaterThanOrEqualTo(0))
            .body("trash_size", greaterThanOrEqualTo(0));
    }
}