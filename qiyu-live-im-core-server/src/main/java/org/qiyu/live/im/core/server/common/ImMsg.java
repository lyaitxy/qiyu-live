package org.qiyu.live.im.core.server.common;

import org.qiyu.live.im.constants.ImConstants;

import java.io.Serializable;
import java.util.Arrays;

/**
 * netty的消息体
 */
public class ImMsg implements Serializable {

    // 魔数: 用于做基本校验
    private short magic;

    // 用于做记录body的长度
    private int len;

    // 用于标识当前消息的左右，后序交给不同的handler去处理
    private int code;

    // 存储消息体的内容，一般会按照字节数组的方式去存放
    private byte[] body;

    public static ImMsg build(int code, String data) {
        ImMsg imMsg = new ImMsg();
        imMsg.setCode(code);
        imMsg.setMagic(ImConstants.DEFAULT_MAGIC);
        imMsg.setBody(data.getBytes());
        imMsg.setLen(imMsg.getBody().length);
        return imMsg;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    @Override
    public String toString() {
        return "ImMsg{" +
                "body=" + Arrays.toString(body) +
                ", magic=" + magic +
                ", len=" + len +
                ", code=" + code +
                '}';
    }
}
