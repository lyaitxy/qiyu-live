package org.qiyu.live.msg.dto;

import java.io.Serializable;
import java.util.Date;

public class MessageDTO implements Serializable {
    /**
     * 己方用户id（也是发送方用户id）
     */
    private Long userId;

    /**
     * 通信目标用户id
     */
    private Long objectId;

    /**
     * 消息类型
     */
    private Integer type;
    /**
     * 消息内容
     */
    private String content;
    private Date createTime;
    private Date updateTime;

    @Override
    public String toString() {
        return "MessageDTO{" +
                "content='" + content + '\'' +
                ", userId=" + userId +
                ", objectId=" + objectId +
                ", type=" + type +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
