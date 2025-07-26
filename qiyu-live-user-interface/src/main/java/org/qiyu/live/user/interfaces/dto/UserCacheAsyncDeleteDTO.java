package org.qiyu.live.user.interfaces.dto;

import java.io.Serializable;

public class UserCacheAsyncDeleteDTO implements Serializable {
    /**
     * 不同的业务场景的code，区别不同的延迟消息
     */
    private int code;
    private String json;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "UserCacheAsyncDeleteDTO{" +
                "code=" + code +
                ", json='" + json + '\'' +
                '}';
    }
}
