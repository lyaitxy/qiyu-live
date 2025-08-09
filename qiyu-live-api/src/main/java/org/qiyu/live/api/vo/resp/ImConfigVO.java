package org.qiyu.live.api.vo.resp;

public class ImConfigVO {

    // 前端请求建立连接的url ws://wsServerIp:port/token=???&&userId=???
    private String token;
    private String wsImServerAddress;
    private String tcpImServerAddress;

    @Override
    public String toString() {
        return "ImConfigVO{" +
                "tcpImServerAddress='" + tcpImServerAddress + '\'' +
                ", token='" + token + '\'' +
                ", wsImServerAddress='" + wsImServerAddress + '\'' +
                '}';
    }

    public String getTcpImServerAddress() {
        return tcpImServerAddress;
    }

    public void setTcpImServerAddress(String tcpImServerAddress) {
        this.tcpImServerAddress = tcpImServerAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWsImServerAddress() {
        return wsImServerAddress;
    }

    public void setWsImServerAddress(String wsImServerAddress) {
        this.wsImServerAddress = wsImServerAddress;
    }
}
