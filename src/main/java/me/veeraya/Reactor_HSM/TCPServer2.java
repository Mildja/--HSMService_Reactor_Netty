package me.veeraya.Reactor_HSM;

import java.time.Duration;

import org.apache.commons.codec.binary.Hex;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import me.veeraya.Reactor_HSM.model.ServerModel;
import me.veeraya.Reactor_HSM.service.RoundRobinService;
import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpServer;

public class TCPServer2 {

	public void create() {

		
		DisposableServer server = TcpServer.create()
				.host("localhost")
				.port(6565)
				.option(ChannelOption.SO_BACKLOG, 1024) 
				.doOnConnection(s->{System.out.println("canconnect server");})
				.doOnConnection(conn -> conn.channel().config().setAllocator(PooledByteBufAllocator.DEFAULT))
				.handle((inbound, outbound) -> { 
					
				 inbound.receive().asByteArray().flatMap(data -> {

					System.out.println("Receivers ");
					System.out.println("Received data: " + Hex.encodeHexString(data));
					System.out.println("Received data: " + new String(data));
					System.out.println("Waiting1: ");

					byte[] response = null;
					Flux<byte[]> incoming = null;
					//ServerModel target = RoundRobinService.TargetFromLoadBalance();
					//ServerModel target = RoundRobinService.RoundRobin();
					//System.out.println(target.getIP() + " " + target.getPort());
					 Client client = Client.INSTANCE; 
					//PayShieldClient client = new PayShieldClient(target.getIP(), target.getPort());
					try {
						System.out.println("Waiting2: ");
						response = client.sendCommand(new String(data)).getBytes();
						System.out.println("Server :"+new String(response));
						System.out.println("Waiting3: ");
						System.out.println("\n");
					} catch (Exception e) {
						System.out.println("Can not set massage from server to hsm");
						e.printStackTrace();
					}
					
					if (response != null) {
						incoming = Flux.just(response);
						return outbound.sendByteArray(incoming);
					} else {
						incoming = Flux.just("".getBytes());
						return outbound.sendByteArray(incoming);
					}
				})
                    .then()
                    .subscribe();
            return outbound.neverComplete();
        })
		.doOnUnbound(c->System.out.println("vv"+c))
        .wiretap(true)
        .bind()
        .block();
		
		// Wait for the server to 3shutdown
		server.onDispose().block();
		System.out.println("Waiting4: ");
	}
}