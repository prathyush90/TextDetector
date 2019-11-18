package com.example.noob.textdetector.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseClass {

@SerializedName("success")
@Expose
private Boolean success;
@SerializedName("data")
@Expose
private List<JsonObject> data = null;

public String getErr() {
        return err;

 }
 public void setErr(String err) {
        this.err = err;
 }
 @SerializedName("err")
@Expose

private String err = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @SerializedName("message")
    @Expose

    private String message = null;

public Boolean getSuccess() {
return success;
}

public void setSuccess(Boolean success) {
this.success = success;
}

public List<JsonObject> getData() {
return data;
}

public void setData(List<JsonObject> data) {
this.data = data;
}

}