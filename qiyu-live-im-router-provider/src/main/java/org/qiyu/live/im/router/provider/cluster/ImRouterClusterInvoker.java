package org.qiyu.live.im.router.provider.cluster;

import io.micrometer.common.util.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;

import java.util.List;

public class ImRouterClusterInvoker<T> extends AbstractClusterInvoker<T> {

    public ImRouterClusterInvoker(Directory<T> directory){
        super(directory);
    }

    @Override
    protected Result doInvoke(Invocation invocation, List list, LoadBalance loadbalance) throws RpcException {
        checkWhetherDestroyed();
        String ip = (String)RpcContext.getContext().get("ip");
        if(StringUtils.isEmpty(ip)) {
            throw new RuntimeException("ip can not be null!");
        }
        // 获取到指定的rpc服务提供者的所有地址信息
        List<Invoker<T>> invokers = list(invocation);
        Invoker<T> matchInvoker = invokers.stream().filter(invoker -> {
            // 拿到我们服务提供者的暴露地址
            String serverIp = invoker.getUrl().getHost() + ":" + invoker.getUrl().getPort();
            return serverIp.equals(ip);
        }).findFirst().orElse(null);
        if(matchInvoker == null) {
            throw new RuntimeException("ip is invalid");
        }
        // 匹配上了，就转发到这台机器上
        return matchInvoker.invoke(invocation);
    }
}
