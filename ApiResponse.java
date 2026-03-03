package com.example.aqa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the JSON response returned by the application endpoint.
 *
 * <p>Successful response: {@code {"result": "OK"}}
 * <p>Error response: {@code {"result": "ERROR", "message": "reason"}}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {

    @JsonProperty("result")
    private String result;

    @JsonProperty("message")
    private String message;

    public ApiResponse() {}

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOk() {
        return "OK".equals(result);
    }

    public boolean isError() {
        return "ERROR".equals(result);
    }

    @Override
    public String toString() {
        return "ApiResponse{result='" + result + "', message='" + message + "'}";
    }
}
