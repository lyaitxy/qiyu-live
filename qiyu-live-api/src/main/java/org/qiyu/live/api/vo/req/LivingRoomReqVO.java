package org.qiyu.live.api.vo.req;

public class LivingRoomReqVO {

    private Integer type;
    private int page;
    private int pageSize;

    @Override
    public String toString() {
        return "LivingRoomReqVO{" +
                "page=" + page +
                ", type=" + type +
                ", pageSize=" + pageSize +
                '}';
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
