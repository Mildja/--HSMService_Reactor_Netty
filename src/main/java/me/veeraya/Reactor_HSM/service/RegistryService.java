package me.veeraya.Reactor_HSM.service;
import org.springframework.stereotype.Service;

import me.veeraya.Reactor_HSM.model.ServerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistryService {
	// Service สำหรับลงทะเบียน IP/Port ของ device
	
	//สร้าง object ที่จะมาเป็น server ที่ใช้
    private static List<ServerModel> servers;
    
    //ฟังก์ชันรับค่า server
    public void setServers(List<ServerModel> servers) {
       this.servers = servers;
       System.out.println(servers);
    }

    //ฟังก์ชั่นบส่งค่า server ที่ใช้
    //ทำให้ดึงข้อมูลจากดาต้าเบส แล้วมีfilter
    //แต่ตอนนี้ดึงข้อมูลทั้งหมดจาก ไฟล์ config มาก่อน
    public static List<ServerModel> getServers() {
    	System.out.println(" get a registeted server ");
    	//filter เงื่อนไข server แล้วส่งออกไป
        return servers.stream().collect(Collectors.toList());
    }
   
}
