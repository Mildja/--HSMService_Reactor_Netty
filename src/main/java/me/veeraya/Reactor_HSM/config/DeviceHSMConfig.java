package me.veeraya.Reactor_HSM.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.veeraya.Reactor_HSM.model.ServerModel;
import me.veeraya.Reactor_HSM.service.RegistryService;

@Configuration
public class DeviceHSMConfig {
	 
	// config HSM vm ที่มีอยู่ทั้งหมด
	
	 @Autowired
	 private RegistryService registryService;

	 private int MaxConnections = 4;
	 private static  List<ServerModel> serverModelList = new ArrayList<ServerModel>();
	 //static ServerModel serverModel = new ServerModel("fff",1);
	   
	 static {
	    	//serverModelList.add(serverModel);
	    	serverModelList.add(new ServerModel("192.168.245.138",9998));
	    	serverModelList.add(new ServerModel("192.168.245.138",9988));
	    	serverModelList.add(new ServerModel("192.168.245.138",9978));
	    	serverModelList.add(new ServerModel("192.168.245.138",9968));
	    }
	    
	@Bean
	public void SetToRegistryService ()
	  {
		  System.out.println(serverModelList);
		  registryService.setServers(serverModelList);
	  }

	public int getMaxConnections() {
		return MaxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		MaxConnections = maxConnections;
	}
		
		
	  
	   
	    
}
