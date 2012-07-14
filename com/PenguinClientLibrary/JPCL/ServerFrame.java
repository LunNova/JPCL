package com.PenguinClientLibrary.JPCL;

public class ServerFrame {
	private int ID;
	private String name;
	private String ip;
	private int port;
	private boolean safe;
	
	public ServerFrame(int ID, String name, String ip, int port, boolean safe){
		this.ID = ID;
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.safe = safe;
	}
	
	public int getID(){
		return ID;
	}
	
	public String getName(){
		return name;
	}
	
	public String getIP(){
		return ip;
	}
	
	public int getPort(){
		return port;
	}
	
	public boolean getSafe(){
		return safe;
	}
}
