package com.jeremyliao.plugin.extension;

import java.util.List;

/**
 * Created by liaohailiang on 2019/1/25.
 */
public class DemoExtension {

    private boolean enable;
    private String message;
    private List<String> strings;

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

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("enable=").append(enable).append(";");
        sb.append("message=").append(message).append(";");
        sb.append("strings=").append(strings).append(";");
        return sb.toString();
    }
}
