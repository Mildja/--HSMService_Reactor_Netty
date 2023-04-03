package me.veeraya.Reactor_HSM;

import java.awt.AWTEvent;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Async;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.netty.util.Constant;
import me.veeraya.Reactor_HSM.model.ServerModel;
import me.veeraya.Reactor_HSM.service.RoundRobinService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;


@SpringBootApplication
public class ReactorHsmApplication implements CommandLineRunner {
	
	private static Connection connection;
	private static RoundRobinService connect;
	
	@Value("${server.config}")
	private String serverConfig;
	
	//บันทึกเหตุการณ์
//	1.ถ้า ใน ฟังก์ชัน disConnect ใช้ connection.onDispose().block(); 
//	  ใน client ใช้ connection.onDispose().block(); 
//	  : ถ้าเรียกปกติ จะสามารถ return ค่าและรับมาแสดงได้ เนื่องจากเราสั่งให้ ใน client block();จนกว่าจะมีการ disponse
//	  : ถ้าเรียกใน server จะไม่ได้ เนื่องจาก server เป็นแบบ non-blocking ไม่สามารถ thread bocking ได้ 
//		จะข้ามการทำงานใน doOnNext ไปเลย เนื่องจากเกิด event error แล้วจึงค่อยกลับไปทำใน client
//	  
//	2.ถ้าใน ฟังชัน ใช้ connection.onDispose().block();
//	  ใน client ใช้ connection.onDisposre();
//	  : ถ้าเรียกปกติ reactor จะทำการทำคำสั่งที่ไม่ต้องรอจาก I/O ก่อน ซึ้งนั้นแหละปัญหา ถ้าไม่ block ไว้น้องจะ return 
//	    ออกไปก่อนที่จะไปทำการเชื่อม client ซึ่งมันก็จะเป็น null แล้วหลังจากนั้นจึงค่อยไปทำ คำสั่ง connect
//	  : เรียกใน server จะเหมือนเดิมกับ ตอนทำข้อ 1
//
//  3.ถ้าใน ฟังชัน ใช้ connection.disposeNow();
//	  ใน client ใช้ connection.onDispose().block(); 
//    : ถ้าเรียกปกติ เหมือนข้อที่ 1 ต่างตรงที่ จะมีการแสดง doOnError ออกมาก่อนส่งให้ server 
//    : เรียกใน server เหมือนเดิมกับข้อ 1
//  4.ถ้าใน ฟังชัน ใช้ connection.onDispose().block();
//	  ใน client ใช้ connection.onDispose().block(); 	
//    : เหมือนข้อ 2
// สรุปถ้าอยากให้รับข้อมูลให้เสร็จก่อน เราต้อง block เพื่อที่จะบังคับให้น้องทำให้เสร็จ
	 
	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(ReactorHsmApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		serverConfig=serverConfig.toLowerCase();
		System.out.println("\nYou select "+serverConfig+" server");
		
		if(Objects.equals(serverConfig,"bootstarp"))
		{
			System.out.println("Server HSM service is bootstarp Server\n");
			System.out.println("If you wanna change server run this command");
			System.out.println("mvn spring-boot:run -Dspring-boot.run.arguments=\"--server.config=reactor\"\n");
			try {
	            NettyServerBootstrap nettyServerBootstrap = new NettyServerBootstrap();
	            nettyServerBootstrap.start(6565);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		}
		else if(Objects.equals(serverConfig,"reactor"))
		{
			System.out.println("Server HSM service is Reacter Server\n");
			System.out.println("If you wanna change server run this command");
			System.out.println("mvn spring-boot:run -Dspring-boot.run.arguments=\"--server.config=bootstarp\"\n");
			TCPServer2 tcpServer2 = new TCPServer2();
			tcpServer2.create();
		}
		else
		{
			System.out.println("Error this project have Two server");
			System.out.println("1.bootstap");
			System.out.println("2.reactor");
			System.out.println("Error this project have Two server");
			System.out.println("This Project haven't "+serverConfig.toString()+" server");
		}
			
	}
}
