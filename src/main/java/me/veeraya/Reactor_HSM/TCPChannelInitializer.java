package me.veeraya.Reactor_HSM;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class TCPChannelInitializer extends ChannelInitializer<SocketChannel> {

	//แปลง string -> byte
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.ISO_8859_1));
        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.ISO_8859_1));
        socketChannel.pipeline().addLast(new TCPChannelHandler());
    }
}
