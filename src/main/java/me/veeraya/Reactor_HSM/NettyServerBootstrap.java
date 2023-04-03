package me.veeraya.Reactor_HSM;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

class NettyServerBootstrap {

	// create TCPserver
    void start(int port) throws InterruptedException {
        Log.log("Starting server at: " + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1000);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1000);
       
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
            		.childOption(ChannelOption.TCP_NODELAY,true)
            		.option(ChannelOption.SO_BACKLOG, 1024)
            		.childHandler(new ChannelInitializer<SocketChannel>() {
            			@Override
            				public void initChannel(SocketChannel socketChannel) throws Exception {
            			        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.ISO_8859_1));
            			        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.ISO_8859_1));
            			        socketChannel.pipeline().addLast(new TCPChannelHandler());		
            			}
            			});

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();
            if(f.isSuccess()) 
            	Log.log("Server started successfully"); 
            f.channel().closeFuture().sync();
        } finally {
            Log.log("Stopping server");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
