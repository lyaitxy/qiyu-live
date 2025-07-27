package org.qiyu.live.im.core.server.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ImMsgEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        ImMsg imMsg = (ImMsg) msg;
        // 按照ImMsg属性的类型顺序
        out.writeShort(imMsg.getMagic());
        out.writeInt(imMsg.getCode());
        out.writeInt(imMsg.getLen());
        out.writeBytes(imMsg.getBody());
//        channelHandlerContext.writeAndFlush(out);
    }
}
