package me.veeraya.Reactor_HSM;


import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class PayShieldClient {

	private String host;
	private int port;

	public PayShieldClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public byte[] sendCommand(String command) throws Exception {
	    final PayShieldClientHandler handler = new PayShieldClientHandler();
	  
	    //สร้าง bootstrap 	
	    Bootstrap b = new Bootstrap();
	    //สร้าง group channel
	    b.group(new NioEventLoopGroup())
	    .channel(NioSocketChannel.class)
	    .option(ChannelOption.TCP_NODELAY, true)
	           .handler(new ChannelInitializer<SocketChannel>() {
	                @Override
	                public void initChannel(SocketChannel ch) throws Exception {
	                    ch.pipeline().addLast(new StringDecoder(CharsetUtil.ISO_8859_1));
	                    ch.pipeline().addLast(new StringEncoder(CharsetUtil.ISO_8859_1));
	                    ch.pipeline().addLast(handler);
	                }
	            });
	    // connect channel ในอนาคตขึ้นมา
	    try {
	        ChannelFuture future = b.connect(host, port).sync();
	        
	        Channel ch = future.channel();
	        
	        System.out.println("Sending command: " + new String(command));
	        ch.writeAndFlush(Unpooled.copiedBuffer(command + "\n", CharsetUtil.ISO_8859_1));
	       
	        System.out.println("Waiting1 command...");
	        ch.closeFuture().await(8000, TimeUnit.MILLISECONDS);
	       
	        //ส่ง response กลับมาให้ server
	        System.out.println("Waiting2 command...");
	        return handler.getResponse().getBytes();  
	        
	    } catch (Exception e) {
	        // Handle other exceptions
	    	e.printStackTrace();
	    	throw new Exception("Error connecting to server: " + e.getMessage());
	    }
	}

	public static class PayShieldClientHandler extends SimpleChannelInboundHandler<String> {
	    private final StringBuffer response = new StringBuffer();
	    private ChannelHandlerContext context;

	    public synchronized String getResponse() {
	    	
	        //System.out.println("Received response: " + response.toString());
	        return response.toString();
	    }

	    //รับ response 
	    @Override
	    public synchronized void channelRead0(ChannelHandlerContext ctx, String msg) {
	        System.out.println("channelRead0...");
	        System.out.println("Received response: " + msg.toString());
	        response.append(msg);
	    }

	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	        context = ctx;
	    }

	    @Override
	    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	        context.executor().execute(() -> {
	            try {
	                ctx.close();
	                sendCommandComplete(response.toString());
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        });
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	        response.setLength(0); // Clear the response buffer
	    }

	    public void sendCommandComplete(String response) {
	        // Do something with the response after it has been fully received
	    }
	    
	}
}