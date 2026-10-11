package ru.ivan.yandextest;

import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.ivan.yandextest.api.YandexDiskApi;

import java.io.File;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class YandexDiskTests {


    static YandexDiskApi API;
    static YandexDiskApi APIInvalidToken;
    static YandexDiskApi APIEmptyToken;
    static String TEST_FOLDER;
    static File TEST_FILE;
    static ArrayList<String> createdResources = new ArrayList<>();


    @BeforeAll
    static void setup() {
        ClassLoader classLoader = YandexDiskTests.class.getClassLoader();

        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("YANDEX_DISK_OAUTH_TOKEN");

        API = new YandexDiskApi(token);
        APIInvalidToken = new YandexDiskApi("invalid_token");
        APIEmptyToken = new YandexDiskApi("");
        TEST_FOLDER = "/TEST_FOLDER";
        TEST_FILE = new File(classLoader.getResource("test_file.txt").getFile());
        API.DELETEResource(TEST_FOLDER);
        API.PUTcreateFolder(TEST_FOLDER);

    }

    @Test
    @Order(1)
    @DisplayName("получение инфы о диске с валидным токеном")
    void testGetDiskInfoValidToken() {
        var response = API.GETDiskInformation()
                .statusCode(200)
                .contentType("application/json")
                .body("total_space", notNullValue())
                .body("used_space", notNullValue())
                .extract();
    }

    @Test
    @Order(2)
    @DisplayName("получение инфы о диске с невалидным токеном")
    void testGetDiskInfoInvalidToken() {
        var response = APIInvalidToken.GETDiskInformation()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"));
    }

    @Test
    @Order(3)
    @DisplayName("получение инфы о диске с пустым полем токена")
    void testGetDiskInfoEmptyToken() {
        var response = APIEmptyToken.GETDiskInformation()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"));
    }

    @Order(4)
    @DisplayName("создание пустой папки")
    @ParameterizedTest
    @CsvSource({
            "/VALID_FOLDER_NAME_LATIN",
            "/ВАЛИДНОЕ_НАЗВАНИЕ_КИРИЛЛИЦА",
            "/VALID NAME WITH SPACES",
    })
    void testCreateFolder(String path) {
        API.PUTcreateFolder(path)
                .statusCode(201);
        createdResources.add(path);
    }

    @Order(5)
    @DisplayName("попытка создания существующей папки")
    @ParameterizedTest
    @ValueSource(strings = {"/VALID_FOLDER_NAME_LATIN"})
    void testCreateExistedFolder(String path) {
        API.PUTcreateFolder(path)
                .statusCode(409);
        createdResources.add(path);
    }

    @Order(6)
    @DisplayName("загрузка валидного файла")
    @Test
    void testPostFile() {
        String href = API.GETUploadUrl(TEST_FOLDER + "/" + TEST_FILE.getName())
                .then()
                .contentType("application/json")
                .extract()
                .body()
                .path("href");
        var response = API.PUTUploadFile(href, TEST_FILE);
        if (response.extract().statusCode() == 202) {
            String operationHref = response.extract().body().path("href");

            boolean completed = false;

            while (!completed) {
                String status = API.GETOperationStatus(operationHref)
                        .extract()
                        .body()
                        .path("status");
                if (status.equals("success")) {
                    completed = true;
                } else if (status.equals("failed")) {
                    Assertions.fail(status);
                }
            }
        } else {
            response.statusCode(201);
        }
        createdResources.add(TEST_FOLDER);
        createdResources.add(href);
    }

    @Order(7)
    @DisplayName("повторная загрузка добавленного файла")
    @Test
    void testPostExistedFile() {
        API.GETUploadUrl(TEST_FOLDER + "/" + TEST_FILE.getName())
                .then()
                .statusCode(409);
    }

    @Test
    @Order(8)
    @DisplayName("получение инфы о сущ. файле")
    void testGetValidFileInfo() {
        API.GETResourceInfo(TEST_FOLDER + "/" + TEST_FILE.getName())
                .statusCode(200)
                .contentType("application/json")
                .body("path", notNullValue())
                .body("type", notNullValue())
                .body("name", notNullValue())
                .extract();
    }

    @Test
    @Order(9)
    @DisplayName("Копированиие сущ. файла")
    void testCopyExistedFile() {
//        API.POSTCopyResourse(TEST_FOLDER + "/" + TEST_FILE.getName())
//                .statusCode(201);
        var response = API.POSTCopyResourse(TEST_FOLDER + "/" + TEST_FILE.getName(), TEST_FOLDER + "/" + TEST_FILE.getName() +  "_copied");
        if (response.extract().statusCode() == 202) {
            String operationHref = response.extract().body().path("href");

            boolean completed = false;

            while (!completed) {
                String status = API.GETOperationStatus(operationHref)
                        .extract()
                        .body()
                        .path("status");
                if (status.equals("success")) {
                    completed = true;
                } else if (status.equals("failed")) {
                    Assertions.fail(status);
                }
            }
        } else {
            response.statusCode(201);
        }
        createdResources.add(TEST_FOLDER + "/" + TEST_FILE.getName() +  "_copied");
    }

    @Test
    @Order(10)
    @DisplayName("копирование несущ. ресурса")
    void testCopyUnexistedFile() {
        API.POSTCopyResourse(TEST_FOLDER + "/" + "invalid_file_name.txt", TEST_FOLDER + "/" + "invalid_file_name.txt" +  "_copied")
                .statusCode(404);
    }

    @Test
    @Order(11)
    @DisplayName("получение инфы о не сущ файле")
    void testGetInvalidFileInfo() {
        API.GETResourceInfo(TEST_FOLDER + "/" + "invalid_file_path.txt")
                .statusCode(404);
    }

    @Test
    @Order(12)
    @DisplayName("перемещение сущ. ресурса")
    void testMoveExistedFile() {
        var response = API.POSTMoveREsourse(TEST_FOLDER + "/" + TEST_FILE.getName(), "/" + TEST_FILE.getName());
        if (response.extract().statusCode() == 202) {
            String operationHref = response.extract().body().path("href");


        } else {
            response.statusCode(201);
        }
        createdResources.add("/" + TEST_FILE.getName());
    }

    @Test
    @Order(13)
    void testMoveUnexistedFile() {
        API.POSTMoveREsourse(TEST_FOLDER + "/" + "invalid_file_name.txt", "/" + "invalid_file_name.txt")
                .statusCode(404);
    }

    @Test
    @Order(14)
    @DisplayName("удаление существующего ресурса")
    void testDeleteExistedFile() {
        testPostFile();
        API.DELETEResource(TEST_FOLDER + "/" + TEST_FILE.getName())
                .statusCode(204);
    }

    @Test
    @Order(15)
    @DisplayName("удаление несуществующего ресурса")
    void testDeleteDeletedFile() {
        API.DELETEResource(TEST_FOLDER + "/" + "invalid_file_path.txt")
                .statusCode(404);
    }

    @AfterAll
    static void clear() {
        for (String path: createdResources){
            try {
                API.DELETEResource(path);
            } catch (Exception ignored) {}
        }
    }

//    static String waitForStatus(String operationHref) {
//        boolean completed = false;
//
//        while (!completed) {
//            String status = API.GETOperationStatus(operationHref)
//                    .extract()
//                    .body()
//                    .path("status");
//            if (status.equals("success")) {
//                completed = true;
//                return status;
//            } else if (status.equals("failed")) {
//                return status;
//            }
//        }
}

