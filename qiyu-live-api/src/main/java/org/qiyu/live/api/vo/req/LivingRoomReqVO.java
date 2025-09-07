package org.qiyu.live.api.vo.req;

public class LivingRoomReqVO {

    private Integer type;
    private int page;
    private int pageSize;
    private Integer roomId;
    private String redPacketConfigCode;

    @Override
    public String toString() {
        return "LivingRoomReqVO{" +
                "page=" + page +
                ", type=" + type +
                ", pageSize=" + pageSize +
                ", roomId=" + roomId +
                ", redPacketConfigCode='" + redPacketConfigCode + '\'' +
                '}';
    }

    public String getRedPacketConfigCode() {
        return redPacketConfigCode;
    }

    public void setRedPacketConfigCode(String redPacketConfigCode) {
        this.redPacketConfigCode = redPacketConfigCode;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
