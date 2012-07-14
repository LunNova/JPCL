package com.PenguinClientLibrary.JPCL;

/*
	PenguinClientLibrary - JPCL - Player.java - Player Class
	Copyright (C) 2012 PenguinClientLibrary.com 
	View license in LICENSE file.

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;

public class Player {
	
	private ArrayList<Integer> items = new ArrayList<Integer>();
	
	private int id, color, head, face, neck, body, hand, feet, flag, photo, x, y, frame, total_membership_days, coins, egg_timer_remaining, age, banned_age, minutes_played, membership_days_remaining, timezone_offset, intRoomID, extRoomID;
	
	private long server_join_time;
	
	// username is what is used to login, nickname is what your name shows as in the game
	private String username, nickname, language_bitmask, uuid;
	
	private boolean is_member, is_safe, has_newspaper;
	
	public Player(){
		this.has_newspaper = false;
	}
	
	public Player(int id, String username, String uuid){
		this.id = id;
		this.username = username;
		this.uuid = uuid;
	}
	
	public void parsePlayerObject(String packet){
		
		// Exits if packet is empty
		if(packet.equals("")){
			return;
		}
		
		String[] info = packet.split("\\|");
	
		this.id = Integer.parseInt(info[0]);
		this.nickname = info[1];
		this.language_bitmask = info[2];
		this.color = Integer.parseInt(info[3]);
		this.head = Integer.parseInt(info[4]);
		this.face = Integer.parseInt(info[5]);
		this.neck = Integer.parseInt(info[6]);
		this.body = Integer.parseInt(info[7]);
		this.hand = Integer.parseInt(info[8]);
		this.feet = Integer.parseInt(info[9]);
		this.flag = Integer.parseInt(info[10]);
		this.photo = Integer.parseInt(info[11]);
		this.x = Integer.parseInt(info[12]);
		this.y = Integer.parseInt(info[13]);
		this.frame = Integer.parseInt(info[14]);
		this.is_member = Boolean.parseBoolean(info[15]);
		this.total_membership_days = Integer.parseInt(info[16]);
	}
	
	public int getID(){
		return id;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getNickname(){
		return nickname;
	}
	
	public String getLanguage_Bitmask(){
		return language_bitmask;
	}
	
	public int getColor(){
		return color;
	}
	
	public int getHead(){
		return head;
	}
	
	public int getFace(){
		return face;
	}
	
	public int getNeck(){
		return neck;
	}
	
	public int getBody(){
		return body;
	}
	
	public int getHand(){
		return hand;
	}
	
	public int getFeet(){
		return feet;
	}
	
	public int getFlag(){
		return flag;
	}
	
	public int getPhoto(){
		return photo;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getFrame(){
		return frame;
	}
	
	public boolean getIsMember(){
		return is_member;
	}
	
	public int getTotal_Membership_Days(){
		return total_membership_days;
	}
	
	public int getIntRoomID(){
		return intRoomID;
	}
	
	public int getExtRoomID(){
		return extRoomID;
	}
	
	public String getUUID(){
		return uuid;
	}
	
	public int getCoins(){
		return coins;
	}
	
	public int getEggTimerRemaining(){
		return egg_timer_remaining;
	}
	
	public long getServerJoinTime(){
		return server_join_time;
	}
	
	public int getAge(){
		return age;
	}
	
	public int getBannedAge(){
		return banned_age;
	}
	
	public int getMinutesPlayed(){
		return minutes_played;
	}
	
	public int getMembershipDaysRemaining(){
		return membership_days_remaining;
	}
	
	public int getTimezoneOffset(){
		return timezone_offset;
	}
	
	public boolean getIsSafe(){
		return is_safe;
	}
	
	public boolean hasNewspaper(){
		return has_newspaper;
	}
	
	public boolean hasItem(int item_id){
		if(this.items.contains(item_id)){
			return true;
		}
		return false;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void addItem(int item_id){
		this.items.add(item_id);
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	public void setHead(int head){
		this.head = head;
	}
	
	public void setFace(int face){
		this.face = face;
	}
	
	public void setNeck(int neck){
		this.neck = neck;
	}
	
	public void setBody(int body){
		this.body = body;
	}
	
	public void setHand(int hand){
		this.hand = hand;
	}
	
	public void setFeet(int feet){
		this.feet = feet;
	}
	
	public void setFlag(int flag){
		this.flag = flag;
	}
	
	public void setPhoto(int photo){
		this.photo = photo;
	}
	
	public void setPosition(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void setRoom(int intRoomID, int extRoomID){
		this.intRoomID = intRoomID;
		this.extRoomID = extRoomID;
	}
	
	public void setFrame(int frame){
		this.frame = frame;
	}
	
	public void setCoins(int coins){
		this.coins = coins;
	}
	
	public void setEggTimerRemaining(int time){
		this.egg_timer_remaining = time;
	}
	
	public void setServerJoinTime(long time){
		this.server_join_time = time;
	}
	
	public void setAge(int age){
		this.age = age;
	}
	
	public void setBannedAge(int age){
		this.banned_age = age;
	}
	
	public void setMinutesPlayed(int minutes){
		this.minutes_played = minutes;
	}
	
	public void setMembershipDaysRemaining(int days){
		this.membership_days_remaining = days;
	}
	
	public void setTimezoneOffset(int timezone){
		this.timezone_offset = timezone;
	}
	
	public void setIsSafe(boolean bool){
		this.is_safe = bool;
	}
	
	public void setNewspaper(){
		this.has_newspaper = !this.has_newspaper;
	}

	public void setUUID(String uuid){
		this.uuid = uuid;
	}
}
