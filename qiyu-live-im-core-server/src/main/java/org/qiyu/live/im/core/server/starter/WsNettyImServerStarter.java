package org.qiyu.live.im.core.server.starter;

import io.micrometer.common.util.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.Resource;
import org.qiyu.live.im.core.server.common.ChannelHandlerContextCache;
import org.qiyu.live.im.core.server.common.WebsocketEncoder;
import org.qiyu.live.im.core.server.ws.WsImServerCoreHandler;
import org.qiyu.live.im.core.server.ws.WsSharkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RefreshScope
public class WsNettyImServerStarter implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsNettyImServerStarter.class);

    // 指定监听的端口
    @Value("${qiyu.im.ws.port}")
    private int port;
    @Resource
    private Environment environment;

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                startApplication();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "qiyu-live-im-server-ws").start();
    }
    // 基于netty去启动一个java进程，绑定监听的端口
    private void startApplication() throws InterruptedException {
        // boss处理连接事件
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // worker处理读写事件
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        // netty初始化相关的handler
        serverBootstrap.childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                // 打印日志，方便观察
                LOGGER.info("初始化连接通道");
                // 因为是基于http协议，使用http的编码和解码器
                channel.pipeline().addLast(new HttpServerCodec());
                // 是以块的方式写，添加处理器
                channel.pipeline().addLast(new ChunkedWriteHandler());
                // http数据在传输过程中是分段 就是可以将多个段聚合 这就是为什么当浏览器发生大量数据时 就会发生多次http请求
                channel.pipeline().addLast(new HttpObjectAggregator(8192));
                // 负责将出战（服务器 -> 浏览器）的WebSocket消息编码为二进制帧
                channel.pipeline().addLast(new WebsocketEncoder());
                channel.pipeline().addLast(new WsSharkHandler());
                channel.pipeline().addLast(new WsImServerCoreHandler());
            }
        });
        //基于JVM的钩子函数去实现优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));
        //获取im的服务注册ip和暴露端口
        String registryIp = environment.getProperty("DUBBO_IP_TO_REGISTRY");
        String registryPort = environment.getProperty("DUBBO_PORT_TO_REGISTRY");
        if (StringUtils.isEmpty(registryPort) || StringUtils.isEmpty(registryIp)) {
            throw new IllegalArgumentException("启动参数中的注册端口和注册ip不能为空");
        }
        ChannelHandlerContextCache.setServerIpAddress(registryIp + ":" + registryPort);
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        LOGGER.info("服务启动成功，监听端口为{}", port);
        //这里会阻塞掉主线程，实现服务长期开启的效果
        channelFuture.channel().closeFuture().sync();
    }
}
