package com.jeremyliao.plugin.extension;

/**
 * Created by liaohailiang on 2019/1/25.
 */
public class DemoExtension {

    private boolean enable;
    private String message;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("enable=").append(enable).append(";");
        sb.append("message=").append(message).append(";");
        return sb.toString();
    }
}
