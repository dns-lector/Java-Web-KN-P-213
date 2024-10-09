package itstep.learning.rest;

import java.util.Date;
import java.util.Map;

public class RestMetaData {
    private String uri;
    private String method;
    private String name;              // Self-descriptive messages
    private Date serverTime;
    private String[] allowedMethods;  // Resource manipulation through representations
    private Map<String, Object> usedParameters;

    public String getUri() {
        return uri;
    }

    public RestMetaData setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RestMetaData setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getName() {
        return name;
    }

    public RestMetaData setName(String name) {
        this.name = name;
        return this;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public RestMetaData setServerTime(Date serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public String[] getAllowedMethods() {
        return allowedMethods;
    }

    public RestMetaData setAllowedMethods(String[] allowedMethods) {
        this.allowedMethods = allowedMethods;
        return this;
    }

    public Map<String, Object> getUsedParameters() {
        return usedParameters;
    }

    public RestMetaData setUsedParameters(Map<String, Object> usedParameters) {
        this.usedParameters = usedParameters;
        return this;
    }
}

/*
{
    uri: "/shop/product",
    method: "GET",
    name: "'MyStore' Shop Price API",
    serverTime: 1265465132,
    allowedMethods: [ "GET", "PUT", "POST" ],
    usedParameters: {
        productId: "165166-6151-....",
        locale: "uk-UA"
    }
}
 */