package me.veeraya.Reactor_HSM;

import java.time.Duration;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import io.netty.channel.ChannelHandler;
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
import me.veeraya.Reactor_HSM.model.ServerModel;
import me.veeraya.Reactor_HSM.service.RegistryService;


//ใช้ enum singleton
public enum Client{
	INSTANCE;

	private final int maxConnections;
	private final Bootstrap b;
	private int max;

    final PayShieldClientHandler handler = new PayShieldClientHandler();
	
    // คิวของ server ที่สามารถใช้ได้ 
    private final Deque<Channel> queue = new LinkedList<>();
    // คิวของ server ที่ไม่สามารถใช้ได้
	private final Deque<ServerModel> disconnect = new LinkedList<>();
	
	private int currentConnections = 0;
	
	@Autowired
	private RegistryService registryService;
	//private Integer position = 0;
	
	// เก็บ server ที่ลงทะเบียนไว้ สามารถเพิ่มลดได้จาก registryservice
	private List<ServerModel> server=registryService.getServers();
	private Client() {
	    this.maxConnections = server.size();
	    this.max = 0;
	    // set server ในรอบแรกของการรัน
        setDisconnect();
        
        b = new Bootstrap();
        b.group(new NioEventLoopGroup())
         .channel(NioSocketChannel.class)
         .option(ChannelOption.TCP_NODELAY, true)
         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,500)
         .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new StringDecoder(CharsetUtil.ISO_8859_1));
                    ch.pipeline().addLast(new StringEncoder(CharsetUtil.ISO_8859_1));
                    ch.pipeline().addLast(handler);
                }
            }) ;
        
    }
	// set queue server ที่ disconnect เอาไว้ทำรอบแรกของการรัน
	public void setDisconnect() {
		for (ServerModel serverModel : server) {
			disconnect.add(serverModel);
			System.out.println(disconnect);
		}
		System.out.println("\n");
	}

	// set channel
    private Channel acquireChannel()  {
        synchronized(queue) {
            do {   	
            	max = server.size();
            	System.out.println("\nqueueConnect "+queue.size()+" maxConnect "+max);
            	
            	//น้อยกว่า จำนวน server ทั้งหมดสร้าง queue ใหม่
            	while (queue.size()<max) {
            		System.out.println("\nqueueConnectInloop"+queue.size()+" maxConnect "+max);
                  
            		if (currentConnections < maxConnections) { 
                    	ServerModel target =null;
                    	//รับ Target ตัวแรกจาก server ที่คิว disconnect 
                    	target=disconnect.removeFirst();
            			System.out.println("disconnect first: "+target);
                    	
                    	ChannelFuture future = null;
						
                    	try {
							// create channel
							future = b.connect(target.getIP(), target.getPort()).sync();
							System.out.println("Can connect "+target);
						} catch (Exception e) {
							System.out.println("Can Not Connect "+target);				
							//ลดจำนวน sever ที่ไม่ใช้ออก
							--max;
							disconnect.add(target);
							System.out.println("new max queue = "+max+"||disconnect = "+disconnect);
							continue;
							//e.printStackTrace();
						}		
             	        Channel newChan = future.channel();
             	        System.out.println("make channel");
             	        System.out.println(target.getIP()+"  "+target.getPort());
             	       
             	       // System.out.println("Host = "+host+" Port = "+port);
             	        
                        currentConnections++;
                        //นำ channel ที่สร้างใหม่ลง queue
                        queue.add(newChan); 
                        System.out.println(queue);
                        System.out.println("add Channel in queue");
                        
                    } else {
							try {
								queue.wait();
								System.out.println("........wait.......................................");
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    }
                }
            	
                 System.out.println("out loop make chanel");
                 System.out.println("Queue size "+queue.size());
                 System.out.println("all queue = "+queue);
                  
                Channel toUse = queue.removeFirst();
                System.err.println(toUse);
                
                // check ว่า channel นี้ทำงานได้จริงไหม
                if (toUse.isOpen()) {
                	// ถ้าใช้ได้ให้ return ออกจาก loop เลย
                    System.out.println("Channel active"+toUse);
                    returnChan(toUse);
                    return toUse;
                }       
                
                System.out.println("ch not active");
                max--;
                
                //เอา channel ที่เชื่อมไม่ได้ลงคิว disconnect
                String ch =toUse.toString();
                ServerModel target2=ChannelToTarget(ch);
                //System.out.println("disconnnnn "+target2);
                disconnect.add(target2);
                
                currentConnections--;
                System.out.println(currentConnections);
            } while (true);
        }
    }

    //	ตัดสติงแปลงเป็น servermodel เนื่องจากหาแค่การดึงเอา IP/port ใน channel ไม่ได้
    private ServerModel ChannelToTarget(String input) {
    	ServerModel serverModel = null;
    
    	String pattern = ".*?R:/\\s*(.*)";
        String output  = "["+input.replaceAll(pattern, "$1");
        String trimmedInput = output.substring(1, output.length() - 1); // Remove the brackets
        int colonIndex = trimmedInput.indexOf(":"); // Find the index of the colon
        String ip = trimmedInput.substring(0, colonIndex); // Get the substring before the colon
        String port = trimmedInput.substring(colonIndex + 1); // Get the substring after the colon
        int port2= Integer.parseInt(port);
        serverModel = new ServerModel(ip,port2);
    	return serverModel;
    }
    
    // เอากลับไปใน Queue
    private void returnChan(Channel chan) {
        synchronized(queue) {
        	System.out.println("return channel");
            queue.addLast(chan);
            queue.notifyAll();
        }
    }

    public String sendCommand(String string) throws Exception{
     
        //หา Channel3 
        Channel ch = acquireChannel();
        try {
        	 System.out.println("Sending command: " + new String(string));
        	 ch.writeAndFlush(Unpooled.copiedBuffer(string.toString() + "\n", CharsetUtil.ISO_8859_1));
        	 ch.closeFuture().await(500, TimeUnit.MILLISECONDS);
        	 System.out.println("Waiting2 command...");
 	         return handler.getResponse();  
        
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Send Message error");


            //เอา channel ที่เชื่อมไม่ได้ลงคิว disconnect
            String ch2 =ch.toString();
            ServerModel target2=ChannelToTarget(ch2);
            disconnect.add(target2);
            
            throw new Exception("Error connecting to server: " + e.getMessage());
        } finally {
            //returnChan(ch);
            System.out.println("finally");
        }
       
    }
    
    
    @ChannelHandler.Sharable //ให้ส่วน hander แชร์กันได้
    public  class PayShieldClientHandler extends SimpleChannelInboundHandler<String> {
	    private final StringBuffer response = new StringBuffer();
	    private ChannelHandlerContext context;

	    public synchronized String getResponse() {	
	        //System.out.println("Received response: " + response.toString());
	    	System.out.println("Get Response");
	        return response.toString();
	    }

	    //รับ response 
	    @Override
	    public synchronized void channelRead0(ChannelHandlerContext ctx, String msg) {
	    	//เคลียร์ response
	    	response.setLength(0);
	    	System.out.println("channelRead0...");
	        System.out.println("Received response: " + msg.toString());
	        response.append(msg);
	        System.out.println("ChannelRead");
	    }

	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	        context = ctx;
	    }

	    @Override
	    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	        context.executor().execute(() -> {
	            try {
	                //ctx.close();
	                sendCommandComplete(response.toString());
	            } catch (Exception e) {
	            }
	        });
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        //ctx.close();
	        response.setLength(0); // Clear the response buffer
	    }

	    public void sendCommandComplete(String response) {
	        // Do something with the response after it has been fully received
	    }
	    
	}
}