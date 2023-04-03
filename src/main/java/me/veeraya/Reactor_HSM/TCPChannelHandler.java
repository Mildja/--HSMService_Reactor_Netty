package me.veeraya.Reactor_HSM;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import reactor.core.publisher.Flux;


public class TCPChannelHandler extends SimpleChannelInboundHandler<String> {

	 private final StringBuffer response = new StringBuffer();
	 private ChannelHandlerContext context;
	    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //Log.log(ctx.channel().remoteAddress(), "Channel Active");
    	context = ctx;
    }

    @Override
    public synchronized void channelRead0(ChannelHandlerContext ctx, String s){
       // Log.log(ctx.channel().remoteAddress(), s);
    	response.setLength(0);
    	
        System.out.println("Receivers from client ");
		System.out.println("Received data from client: "+ s.toString());
		response.append(s);
		System.out.println("ddd:"+response.toString());
		System.out.println("Waiting1: ");
		String responsewithHSM = null;
		String incoming = null;
		Client client = Client.INSTANCE; 
		
		try {
			System.out.println("Waiting2: ");
			//responsewithHSM = client.sendCommand(response);
			System.out.println("Server :"+new String(responsewithHSM));
			System.out.println("Waiting3: ");
			System.out.println("\n");
		} catch (Exception e) {
			System.out.println("Can not set massage from server to hsm");
			e.printStackTrace();
		}
		
		if (response != null) {
			incoming = responsewithHSM;
			System.out.println("Server response not null:"+new String(incoming));
		    ctx.channel().writeAndFlush(Unpooled.copiedBuffer(incoming + "\n", CharsetUtil.ISO_8859_1));
		    System.out.println("send");
		} else {
			incoming = "";
			System.out.println("Server response null");
			ctx.channel().writeAndFlush(Unpooled.copiedBuffer(incoming + "\n", CharsetUtil.ISO_8859_1));
		}
		    
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
       // Log.log(ctx.channel().remoteAddress(), "Channel no Active");
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        context.executor().execute(() -> {
            try {
               // ctx.close();
                sendCommandComplete(response.toString());
            } catch (Exception e) {
            }
        });
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
       // ctx.close();
        response.setLength(0); // Clear the response buffer
    }

    public void sendCommandComplete(String response) {
        // Do something with the response after it has been fully received
    }
}
