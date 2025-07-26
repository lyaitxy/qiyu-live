package org.qiyu.live.user.interfaces.dto;

import java.io.Serializable;
import java.util.Date;

public class UserTagDTO implements Serializable {
    private long UserId;
    private Long tagInfo01;
    private Long tagInfo02;
    private Long tagInfo03;
    private Date createTime;
    private Date updateTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getTagInfo01() {
        return tagInfo01;
    }

    public void setTagInfo01(Long tagInfo01) {
        this.tagInfo01 = tagInfo01;
    }

    public Long getTagInfo02() {
        return tagInfo02;
    }

    public void setTagInfo02(Long tagInfo02) {
        this.tagInfo02 = tagInfo02;
    }

    public Long getTagInfo03() {
        return tagInfo03;
    }

    public void setTagInfo03(Long tagInfo03) {
        this.tagInfo03 = tagInfo03;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public long getUserId() {
        return UserId;
    }

    public void setUserId(long userId) {
        UserId = userId;
    }

    @Override
    public String toString() {
        return "UserTagDTO{" +
                "createTime=" + createTime +
                ", UserId=" + UserId +
                ", tagInfo01=" + tagInfo01 +
                ", tagInfo02=" + tagInfo02 +
                ", tagInfo03=" + tagInfo03 +
                ", updateTime=" + updateTime +
                '}';
    }
}
