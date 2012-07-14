package com.PenguinClientLibrary.JPCL;

public class ErrorFrame {
	
	private int errorID;
	private String errorName;
	private String errorDesc;
	private boolean shouldDie;
	
	public ErrorFrame(int errorID, String errorName, String errorDesc, boolean shouldDie){
		this.errorID = errorID;
		this.errorName = errorName;
		this.errorDesc = errorDesc;
		this.shouldDie = shouldDie;
	}
	
	public int getErrorID(){
		return errorID;
	}
	
	public String getErrorName(){
		return errorName;
	}
	
	public String errorDesc(){
		return errorDesc;
	}
	
	public boolean getShouldDie(){
		return shouldDie;
	}
}
