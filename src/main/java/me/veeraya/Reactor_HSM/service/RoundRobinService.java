package me.veeraya.Reactor_HSM.service;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.servers.Server;
import me.veeraya.Reactor_HSM.model.ServerModel;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

@Service
public class RoundRobinService {
	
	
	@Autowired
	private  static  RegistryService registryService;
	private  static Integer position = 0;
	
	public  static ServerModel RoundRobin ()
	 { 
		System.out.println("RoundRobin");
		List<ServerModel>server=registryService.getServers();
		System.out.println("ddddd"+server);
		 ServerModel target = null;
	        synchronized (position) {
	            if (position > server.size() - 1) {
	                position = 0;
	            }
	            target = server.get(position);
	            position++;
	        }
	        System.out.println(target);
	        return target;
    }
	
	public static  ServerModel TargetFromLoadBalance() {
		ServerModel target =RoundRobin();
    	String status = checkConnection(target.getIP(),target.getPort());
    	
    	while(status!="connect") {
    			 ServerModel Newtarget = RoundRobin();
    			 status = checkConnection(Newtarget.getIP(),Newtarget.getPort());
    			 if(status=="connect")
    	    		{
    	    		      return Newtarget;
    	    		}
    	};
		return target;
	}
	
	public static String checkConnection(String Ip,int port) {
		String status;
		try (Socket socket = new Socket(Ip, port)){ 
        	System.out.println("Can connect server");
        	status = "connect";
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
           status = "ServerNotFound";
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            status = "I/O-Error";
        }
		
		return status;
	}
	
	
	
	
}
