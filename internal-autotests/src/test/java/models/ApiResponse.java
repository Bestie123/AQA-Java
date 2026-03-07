package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse {
    
    @JsonProperty("result")
    private String result;
    
    @JsonProperty("message")
    private String message;
    
    public ApiResponse() {}
    
    public ApiResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "result='" + result + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
