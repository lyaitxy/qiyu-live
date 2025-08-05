package org.qiyu.live.living.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 直播记录表，比直播表多了个endTime
 */
@TableName("t_living_room_record")
public class LivingRoomRecordPO {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 主播id
     */
    private Long anchorId;
    /**
     * 直播间类型（1普通直播间，2 pk直播间）
     */
    private Integer type;
    /**
     * 直播间名字
     */
    private String roomName;
    /**
     * 直播间封面
     */
    private String covertImg;
    /**
     * 状态（0无效， 1有效）
     */
    private Integer status;
    /**
     * 观看数量
     */
    private Integer watchNum;
    /**
     * 点赞数量
     */
    private Integer goodNum;
    /**
     * 开播时间
     */
    private Date startTime;
    /**
     * 关播时间
     */
    private Date endTime;
    /**
     * 记录更新时间
     */
    private Date updateTime;

    @Override
    public String toString() {
        return "LivingRoomRecordPO{" +
                "anchorId=" + anchorId +
                ", id=" + id +
                ", type=" + type +
                ", roomName='" + roomName + '\'' +
                ", covertImg='" + covertImg + '\'' +
                ", status=" + status +
                ", watchNum=" + watchNum +
                ", goodNum=" + goodNum +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public Long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(Long anchorId) {
        this.anchorId = anchorId;
    }

    public String getCovertImg() {
        return covertImg;
    }

    public void setCovertImg(String covertImg) {
        this.covertImg = covertImg;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getGoodNum() {
        return goodNum;
    }

    public void setGoodNum(Integer goodNum) {
        this.goodNum = goodNum;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Integer getWatchNum() {
        return watchNum;
    }

    public void setWatchNum(Integer watchNum) {
        this.watchNum = watchNum;
    }
}
