package me.veeraya.Reactor_HSM.model;

import lombok.Data;
import lombok.ToString;

@ToString
public class ServerModel {

	private String IP;
	private int port;
	
	
	public ServerModel(String iP, int port) {
		super();
		IP = iP;
		this.port = port;
	}
	public ServerModel() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public String getIP() {
		return IP;
	}
	public void setIP(String iP) {
		IP = iP;
	}
	
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	
}
