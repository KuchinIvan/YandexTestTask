package ru.ivan.yandextest.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

import java.io.File;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;


public class YandexDiskApi {

    private final String BASE_URL = "https://cloud-api.yandex.net";
    private final String TOKEN;

    public YandexDiskApi(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    // получить инфу о диске
    public ValidatableResponse  GETDiskInformation() {
        return
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
        .when()
            .get("/v1/disk")
        .then();
    }

    // создать папку
    public ValidatableResponse PUTcreateFolder(String path) {
        return
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
            .queryParam("path", path)
        .when()
            .put("/v1/disk/resources")
        .then();
    }

    // получить ссылку на загрузку файла
    public Response GETUploadUrl(String path) {
        return
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
            .queryParam("path", path)
        .when()
            .get("/v1/disk/resources/upload")
        .then()
            .extract()
            .response();
    }

    // отправка самого файла
    public ValidatableResponse PUTUploadFile(String href, File file) {
        return
        given()
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
            .body(file)
        .when()
            .put(href)
        .then();
    }

    public ValidatableResponse GETResourceInfo(String path) {
        return
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
            .queryParam("path", path)
        .when()
            .get("/v1/disk/resources")
        .then();
    }

    // удалить ресурс
    public ValidatableResponse DELETEResource(String path) {
        return
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
            .queryParam("path", path)
        .when()
            .delete("/v1/disk/resources")
        .then();
    }

    // копировать ресурс
    public ValidatableResponse POSTCopyResourse(String from, String path){
        return
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
            .queryParam("from", from)
            .queryParam("path", path)
        .when()
            .post("/v1/disk/resources/copy")
        .then();
    }

    public ValidatableResponse POSTMoveREsourse(String from, String path){
        return
        given()
            .baseUri(BASE_URL)
            .header("Authorization", "OAuth " + TOKEN)
            .header("Accept", "application/json")
            .queryParam("from", from)
            .queryParam("path", path)
        .when()
            .post("/v1/disk/resources/move")
        .then();
    }

    // проверка статуса операции
    public ValidatableResponse GETOperationStatus(String href) {
        return
        given()
                .baseUri(BASE_URL)
                .header("Authorization", "OAuth " + TOKEN)
                .header("Accept", "application/json")
        .when()
                .get(href)
        .then();
    }

}