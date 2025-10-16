package org.qiyu.live.im.core.server.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.qiyu.live.im.constants.ImConstants;

import java.util.List;

public class ImMsgDecoder extends ByteToMessageDecoder {

    //ImMag的最低基本字节数
    private final int BASE_LEN = 2 + 4 + 4;

    /**
     *
     * @param channelHandlerContext 当前channel的上下文
     * @param byteBuf 缓冲区对象
     * @param out 解码之后要放入这个列表，传递给下一个处理器
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        // 进行byteBuf内容的基本校验，长度校验 和 magic值校验
        if(byteBuf.readableBytes() >= BASE_LEN) {
            byteBuf.markReaderIndex(); // 标记当前读的位置
            if(byteBuf.readShort() != ImConstants.DEFAULT_MAGIC) {
                channelHandlerContext.close();
                return;
            }
            int code = byteBuf.readInt();
            int len = byteBuf.readInt();
            //byte数组的字节数小于len，说明消息不完整
            if(byteBuf.readableBytes() < len) {
                // 回滚到标记位置，等待更多的数据
                byteBuf.resetReaderIndex();
                // channelHandlerContext.close();
                return;
            }
            byte[] body = new byte[len];
            byteBuf.readBytes(body);
            //将byteBuf转换为ImMsg对象
            ImMsg imMsg = new ImMsg();
            imMsg.setCode(code);
            imMsg.setLen(len);
            imMsg.setBody(body);
            imMsg.setMagic(ImConstants.DEFAULT_MAGIC);
            out.add(imMsg);
        }
    }
}
