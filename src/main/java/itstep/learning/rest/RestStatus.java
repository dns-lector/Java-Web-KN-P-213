package itstep.learning.rest;

public class RestStatus {
    private boolean isSuccessful;
    private int code;
    private String phrase;

    public RestStatus() {
    }

    public RestStatus(int code) {
        this.setCode( code );
        switch( code ) {
            case 200: setSuccessful(true); setPhrase( "OK" ); break;
            case 201: setSuccessful(true); setPhrase( "Created" ); break;
            case 202: setSuccessful(true); setPhrase( "Accepted" ); break;
            case 204: setSuccessful(true); setPhrase( "No Content" ); break;
            case 401: setSuccessful(false); setPhrase( "Unauthorized" ); break;
            case 402: setSuccessful(false); setPhrase( "Payment Required" ); break;
            case 403: setSuccessful(false); setPhrase( "Forbidden" ); break;
            case 404: setSuccessful(false); setPhrase( "Not Found" ); break;
            case 500: setSuccessful(false); setPhrase( "Internal Server Error" ); break;
            case 503: setSuccessful(false); setPhrase( "Service Unavailable" ); break;
            default: setSuccessful( code < 300 ); setPhrase( "Code: " + code );
        }
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public RestStatus setSuccessful(boolean successful) {
        isSuccessful = successful;
        return this;
    }

    public int getCode() {
        return code;
    }

    public RestStatus setCode(int code) {
        this.code = code;
        return this;
    }

    public String getPhrase() {
        return phrase;
    }

    public RestStatus setPhrase(String phrase) {
        this.phrase = phrase;
        return this;
    }
}
/*
Статуси і коди
є два етапи оброблення запиту
- HTTP
- REST
Наприклад, запит на товар /shop/product/no-one
- за НТТР обробляється успішно - знаходить URL та передає йому управління
   (формуються метадані)
- за REST маємо помилку 404 не знайдено
= метадані є, даних немає

Наприклад /shop/no-one
- за НТТР 404, оброблення немає, метаданих немає

 */