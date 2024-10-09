package itstep.learning.rest;

public class RestResponse {
    private RestStatus status;
    private RestMetaData meta;
    private Object data;

    public RestStatus getStatus() {
        return status;
    }

    public RestResponse setStatus(RestStatus status) {
        this.status = status;
        return this;
    }

    public Object getData() {
        return data;
    }

    public RestResponse setData(Object data) {
        this.data = data;
        return this;
    }

    public RestMetaData getMeta() {
        return meta;
    }

    public RestResponse setMeta(RestMetaData meta) {
        this.meta = meta;
        return this;
    }
}
