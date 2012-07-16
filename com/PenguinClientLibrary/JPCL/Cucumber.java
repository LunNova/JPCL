package com.PenguinClientLibrary.JPCL;

/*
	PenguinClientLibrary - JPCL - Cucumber.java - Base Connector
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;

public class Cucumber implements Runnable {
	
	private boolean debug_enabled, notifications_enabled, store_players, is_connected = false;
	private String last_sent = "", version = "2.0";
	private ArrayList<String> recv_buffer = new ArrayList<String>();

	protected HashMap<Integer, ServerFrame> servers = new Constructors().setupServers();
	protected HashMap<Integer, ErrorFrame> errors = new Constructors().errorHandling();
	public HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	
	private int myPlayerID;
	private Thread t;
	
	// Socket and read/write stream variables
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	
	public Cucumber(){
		this.checkVersion();
		this.debug_enabled = false;
		this.notifications_enabled = true;
		this.store_players = true;
	}
	
	public Cucumber(boolean notifications, boolean debug, boolean players){
		this.checkVersion();
		this.notifications_enabled = notifications;
		this.debug_enabled = debug;
		this.store_players = players;
	}
	
	//// General Methods ////
	
	public void checkVersion(){
		String onlineVersion = "";
		try{
			onlineVersion = fileToString("http://penguinclientlibrary.com/pcl/version.php");
		}
		catch (IOException IOE){
			System.out.println("Error 1: I was not able to check for the latest version of PCL. Continuing..");
		}
		if(!onlineVersion.equals(version)){
			System.out.println("Please download the latest version of PCL");
			System.exit(1);
		}
	}
	
	private int ord(String input){
		char f = input.charAt(0);
		return ((int)f) % 2;
	}
	
	private String MD5(String md5){
		try{
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i<array.length; i++){
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).subSequence(1, 3));
			}
			return sb.toString();
		}
		catch(java.security.NoSuchAlgorithmException e){}
		return null;
	}
	
	public void sleep(int seconds){
		try{
			Thread.sleep(seconds * 1000);
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	private String fileToString(String filename) throws IOException{
		String data = "";
		URL url = new URL(filename);
        InputStream is = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
        String line = null;
        while((line = reader.readLine()) != null){
        	data = line;
        }
        reader.close();
        return data;
	}
	
	public String stribet(String input, String left, String right){
		int pos_left = input.indexOf(left) + left.length();
		int pos_right = input.indexOf(right);
		return input.substring(pos_left, pos_right);
	}
	
	//// Output Methods ////
	
	public void debug(String message){
		if(this.debug_enabled){
			System.out.println(this.now() + message);
		}
	}
	
	public void notification(String message){
		if(this.notifications_enabled){
			System.out.println(this.now() + message);
		}
	}
	
	public String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
		return "[" + sdf.format(cal.getTime()) + "] ";
	}	
	
	//// Data Stream Methods ////
	
	public void run(){
		while(this.is_connected){
			if(this.socket.isClosed() == false){
				this.recvPacket();
			}
		}
	}
	
	public void sendPacket(String data){
		if(data.equals(this.last_sent)){
			this.sleep(2);
		}
		out.println(data + (char)0);
		this.last_sent = data;
		debug("Sent: " + data);
	}
	
	public String recvPacket(){
		try{
			int dec = this.in.read();
			String data = "";

			while(dec != 0){
				data += (char)dec;
				dec = in.read();
			}
			debug("Recv: " + data);
			
			this.handlePacket(data);
			this.recv_buffer.add(data);
			if(this.recv_buffer.size() > 10){
				this.recv_buffer.remove(0);
			}
			return data;
		}
		catch(IOException e){ }
		return null;
	}
	
	public String sendAndWait(String data, String needle){
		String response = null;
		this.sendPacket(data);
		while(response == null){
			response = searchRecvBuffer(needle);
		}
		return response;
	}
	
	public String searchRecvBuffer(String needle){
		String data;
		for(int i = 0; i < this.recv_buffer.size(); i++){
			try{
				data = this.recv_buffer.get(i);
			}
			catch (IndexOutOfBoundsException e){
				break;
			}
			if(data != null){
				if(data.indexOf(needle) != -1 || data.indexOf("%e%-1%") != -1){
					this.recv_buffer.remove(data);
					return data;
				}
			}
		}
		return null;
	}
	
	public String searchRecvBuffer(String[] needles){
		String data;
		for(int i = 0; i < this.recv_buffer.size(); i++){
			try{
				data = this.recv_buffer.get(i);
			}
			catch (IndexOutOfBoundsException e){
				break;
			}
			if(data != null){
				for(String needle : needles){
					if(data.indexOf(needle) != -1 || data.indexOf("%e%-1%") != -1){
						this.recv_buffer.remove(data);
						return data;
					}
				}
			}
		}
		return null;
	}
	
	public String getLastSent(){
		return this.last_sent;
	}

	//// Server Methods ////
	
	private int getLoginPort(String username){
		int ascii = ord(username);
		if(ascii == 0)
			return 3724;
		else 
			return 6112;
	}
	
	private String getLoginIP(){
		Random r = new Random();
		int random = r.nextInt(3); 
		switch(random){
		case 0:
			return "204.75.167.218";
		case 1:
			return "204.75.167.219";
		case 2:
			return "204.75.167.176";
		case 3:
			return "204.75.167.177";
		}
		return null;
	}
	
	private String encryptPassword(String password){
		String hash = MD5(password);
		hash = hash.substring(16, 32) + hash.substring(0, 16);
		return hash;
	}
	
	private String generateKey(String password, String randKey, boolean isLogin){
		if(!isLogin) return generateKey(password, randKey);
		try{
			return fileToString("http://penguinclientlibrary.com/pcl/key.php?p=" + password + "&r=" + randKey);
		}
		catch (IOException IOE){
			System.out.println("Error 2. Unable to perform password generation. penguinclientlibrary.com seems down.");
		}
		return null;
	}
	
	private String generateKey(String password, String randKey){
		return this.encryptPassword(password + randKey) + password;
	}
	
	public boolean isConnected(){
		return this.is_connected;
	}
	
	public boolean connect(String username, String password, String server){		
		for(ServerFrame i : servers.values()){
			if(i.getName().equals(server)){
				return connect(username, password, i.getID());
			}
		}
		System.out.println("Error: Server not found");
		return false;
	}
	
	public boolean connect(String username, String password, int server){
		String loginKey, randKey, key, data, uuid;
			
		try{
			socket = new Socket(this.getLoginIP(), this.getLoginPort(username));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			
			this.is_connected = true;
			this.t = new Thread(this);
			t.start();
		
			this.sendAndWait("<msg t='sys'><body action='verChk' r='0'><ver v='153' /></body></msg>", "apiOK");
			
			data = this.sendAndWait("<msg t='sys'><body action='rndK'r='-1'></body></msg>", "rndK");
			randKey = this.stribet(data, "<k>", "</k>");
			key = generateKey(password, randKey, true);
			data = this.sendAndWait("<msg t='sys'><body action='login' r='0'><login z='w1'><nick><![CDATA[" + username + "]]></nick><pword><![CDATA[" + key + "]]></pword></login></body></msg>", "%l%-1%");
				
			socket.close();
			in.close();
			out.close();
			
			if(data.indexOf("%e%-1%") != -1){
				this.is_connected = false;
				return false;
			}
			
			this.notification("Successfully logged into " + username);
		
			String[] packet = data.split("%");
			this.myPlayerID = Integer.parseInt(packet[4]);
			uuid = packet[5];
			loginKey = packet[6];
			Player p = new Player(this.myPlayerID, username, uuid);
			players.put(this.myPlayerID, p);

			try{
				socket = new Socket(servers.get(server).getIP(), servers.get(server).getPort());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			this.sendAndWait("<msg t='sys'><body action='verChk' r='0'><ver v='153' /></body></msg>", "apiOK");
			
			data = this.sendAndWait("<msg t='sys'><body action='rndK'r='-1'></body></msg>", "rndK");
			randKey = this.stribet(data, "<k>", "</k>");
			key = generateKey(loginKey, randKey);
			data = this.sendAndWait("<msg t='sys'><body action='login' r='0'><login z='w1'><nick><![CDATA[" + username + "]]></nick><pword><![CDATA[" + key + "]]></pword></login></body></msg>", "%l%-1%");
			
			this.sendAndWait("%xt%s%j#js%-1%" + this.myPlayerID + "%" + loginKey + "%en%", "js");
			this.sendAndWait("%xt%s%i#gi%-1%", "gi");
			
			
			this.notification("Successfully connected to " + servers.get(server).getName());
			return true;
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return true;
	}

	//// Player Methods ////
	
	public int myPlayerID(){
		return this.myPlayerID;
	}
	
	public void purgePlayers(){
		Player p = this.players.get(this.myPlayerID);
		this.players.clear();
		players.put(this.myPlayerID, p);
	}
	
	public Player getPlayerByID(int user_id){
		if(this.players.containsKey(user_id)){
			return this.players.get(user_id);
		}
		return null;
	}
	
	public Player getPlayerByName(String name){
		String response;
		for(Player p : this.players.values()){
			if(p.getNickname().equals(name)){
				return p;
			}
		}
		response = this.sendAndWait("%xt%s%u#pbn%-1%" + name + "%", name);
		String[] data = response.split("%");
		return new Player(Integer.parseInt(data[5]), data[6], data[4]);
	}
		
	//// Club Penguin Packet Methods ////
	
	public void joinRoom(int room_id, int x, int y){
		if(room_id != this.players.get(this.myPlayerID).getExtRoomID() && room_id < 999){
			this.sendPacket("%xt%s%j#jr%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + room_id + "%" + x + "%" + y + "%");
		}
	}
	
	public void joinRoom(int room_id){
		if(room_id != this.players.get(this.myPlayerID).getExtRoomID() && room_id < 999){
			this.sendPacket("%xt%s%j#jr%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + room_id + "%0%0%");
		}
	}
	
	public void joinIgloo(int penguin_id){
		if(this.players.get(this.myPlayerID).getExtRoomID() != penguin_id + 2000){
			this.sendPacket("%xt%s%g#gm%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + penguin_id + "%");
			this.sendPacket("%xt%s%p#pg%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + penguin_id + "%");
			this.sendPacket("%xt%s%j#jp%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + (penguin_id + 1000) + "%");
		}
	}
	
	public void sendMessage(String message){
		this.sendPacket("%xt%s%m#sm%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + this.myPlayerID + "%" + message + "%");
	}
	
	public void sendPhraseChat(String message_id){
		this.sendPacket("%xt%s%m#sc%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + this.myPlayerID + "%" + message_id + "%");
	}
	
	// This is somewhat buggy. You might get an error if you try to handle sc packets using this.
	public String parseChatMessage(String input){
		String get;
		try{
			get = fileToString("http://phrasechat.disney.go.com/phrasechatsvc/api/1.1/pen/en/phrase/" + input);
			return stribet(get, "\"phrase\":\"", "\"}}}");
		}
		catch (IOException e){
			System.out.println("There was an error decrypting the SC packet");
			return null;
		}
	}
	
	public void sendPosition(int x, int y){
		if(x != 0 && y != 0){
			this.sendPacket("%xt%s%u#sp%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + x + "%" + y + "%");
		}
	}
	
	public void sendSafe(int message_id){
		this.sendPacket("%xt%s%u#ss%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + message_id + "%");
	}
	
	public void sendLine(int message_id){
		this.sendPacket("%xt%s%u#sl%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + message_id + "%");
	}
	
	public void sendQuick(int message_id){
		this.sendPacket("%xt%s%u#sq%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + message_id + "%");
	}
	
	public void sendGuide(int room_id){
		this.sendPacket("%xt%s%u#sg%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + room_id + "%");
	}
	
	public void sendJoke(int message_id){
		this.sendPacket("%xt%s%u#sj%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + message_id + "%");
	}
	
	public void sendEmote(int emote_id){
		this.sendPacket("%xt%s%u#se%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + emote_id + "%");
	}
	
	public void snowball(int x, int y){
		this.sendPacket("%xt%s%u#sb%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + x + "%" + y + "%");
	}
	
	public void sendAction(int id){
		this.sendPacket("%xt%s%u#sa%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void sendFrame(int id){
		this.sendPacket("%xt%s%u#sf%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void dance(){
		this.sendFrame(26);
	}
	
	public void wave(){
		this.sendAction(25);
	}
	
	public void addCoins(int amount){
		this.sendPacket("%xt%z%zo%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + amount + "%");
	}
	
	public void sendMail(int penguinID, int cardID){
		this.sendPacket("%xt%s%l#ms%" + penguinID + "%" + cardID + "%");
	}
	
	public void buddyRequest(int id){
		this.sendPacket("%xt%s%b#br%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void acceptRequest(int id){
		this.sendPacket("%xt%s%b#ba%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void removeBuddy(int id){
		this.sendPacket("%xt%s%b#rb%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id+ "%");
	}
	
	public void addIgnore(int id){
		this.sendPacket("%xt%s%n#an%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void removeIgnore(int id){
		this.sendPacket("%xt%s%n#rn%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void addItem(int id){
		this.sendPacket("%xt%s%i#ai%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void updatePlayer(String type, int item_id){
		if(this.players.get(this.myPlayerID).hasItem(item_id) || item_id == 0){
			type = type.toLowerCase();
			this.sendPacket("%xt%s%s#up" + type + "%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + item_id + "%");
		}
		else{
			// Implement buying the item if it's not bait and coin requirement met -- requires item parser
		}
	}
	
	public void openNewspaper(){
		this.sendPacket("%xt%s%t#at%"+ this.players.get(this.myPlayerID).getIntRoomID() + "%1%1%");
	}
	
	public void closeNewspaper(){
		this.sendPacket("%xt%s%t#rt%"+ this.players.get(this.myPlayerID).getIntRoomID() + "%1%");	
	}
	
	public void getPlayer(int id){
		this.sendPacket("%xt%s%u#gp%"+ this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void buyPuffle(int id, String name){
		this.sendPacket("%xt%s%p#pn%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%" + name + "%");
	}
	
	public void findBuddy(int id){
		this.sendPacket("%xt%s%u#bf%" + this.players.get(this.myPlayerID).getIntRoomID() + "%" + id + "%");
	}
	
	public void handlePacket(String packet){
		int error = 0;
		
		if(!packet.contains("%"))
			return;
		String[] packetArray = packet.split("%");
		
		if(packetArray[1].equals("xt")){
			String handler = packetArray[2];
			
			// Error handler
			if(handler.equals("e")){
				error = Integer.parseInt(packetArray[4]);
				System.out.println(this.now() + "Error: " + errors.get(error).errorDesc());
				if(errors.get(error).getShouldDie())
					System.exit(1);
			}
			
			// Load Player handler
			else if(handler.equals("lp")){
				this.players.get(this.myPlayerID).parsePlayerObject(packetArray[4]);
				this.players.get(this.myPlayerID).setCoins(Integer.parseInt(packetArray[5]));
				this.players.get(this.myPlayerID).setIsSafe(Boolean.parseBoolean(packetArray[6]));
				this.players.get(this.myPlayerID).setEggTimerRemaining(Integer.parseInt(packetArray[7]));
				this.players.get(this.myPlayerID).setServerJoinTime(Long.parseLong(packetArray[8]));
				this.players.get(this.myPlayerID).setAge(Integer.parseInt(packetArray[9]));
				this.players.get(this.myPlayerID).setBannedAge(Integer.parseInt(packetArray[10]));
				this.players.get(this.myPlayerID).setMinutesPlayed(Integer.parseInt(packetArray[11]));
				if(this.players.get(this.myPlayerID).getIsMember()){
					this.players.get(this.myPlayerID).setMembershipDaysRemaining(Integer.parseInt(packetArray[12]));
				}
				else{
					this.players.get(this.myPlayerID).setMembershipDaysRemaining(-1);
				}
				this.players.get(this.myPlayerID).setTimezoneOffset(0 - Integer.parseInt(packetArray[13]));
			}
			
			// Join Room handler
			else if(handler.equals("jr")){
				this.purgePlayers();
				this.players.get(this.myPlayerID).setRoom(Integer.parseInt(packetArray[3]), Integer.parseInt(packetArray[4]));
				int user_id;
				
				// Update players array with new player information from the room
				for(int i = 5; i <= packetArray.length - 1; i++){
					user_id = Integer.parseInt(packetArray[i].substring(0, packetArray[i].indexOf("|")));
					if(user_id == this.myPlayerID){
						this.players.get(this.myPlayerID).parsePlayerObject(packetArray[i]);
						if(this.store_players == false){
							break;
						}
					}
					else if(this.store_players == true){
						Player p = new Player();
						p.parsePlayerObject(packetArray[i]);
						this.players.put(user_id, p);
					}
				}
				this.notification(this.players.get(this.myPlayerID).getNickname() + " joined room: " + this.players.get(this.myPlayerID).getExtRoomID()); // Include room name w/ rooms hashmap
			}
			
			// Join Game handler
			else if(handler.equals("jg")){
				this.purgePlayers();
				this.players.get(this.myPlayerID).setRoom(Integer.parseInt(packetArray[3]), Integer.parseInt(packetArray[4]));
				this.notification(this.players.get(this.myPlayerID).getNickname() + " joined game: " + this.players.get(this.myPlayerID).getExtRoomID());	 // Include room name w/ rooms hashmap
			}
			
			// Add Player to room handler
			else if(handler.equals("ap")){
				Player p = new Player();
				p.parsePlayerObject(packetArray[4]);
				if(p.getID() != this.myPlayerID && this.store_players == true){
					this.players.put(p.getID(), p);
					this.notification(players.get(p.getID()).getNickname() + " has joined the room");
				}
			}
			
			// Remove Player from room handler
			else if(handler.equals("rp")){
				int user_id = Integer.parseInt(packetArray[4]);
				if(user_id != this.myPlayerID && this.store_players == true){
					this.notification(players.get(user_id).getNickname() + " has left the room");
					this.players.remove(user_id);
				}
			}
			
			// Get Items packet
			else if(handler.equals("gi")){
				int item_id;
				for(int i = 4; i < packetArray.length; i++){
					item_id = Integer.parseInt(packetArray[i]);
					this.players.get(this.myPlayerID).addItem(item_id);
				}
			}
			
			// Send Position handler
			else if(handler.equals("sp")){
				int user_id = Integer.parseInt(packetArray[4]);
				int x = Integer.parseInt(packetArray[5]);
				int y = Integer.parseInt(packetArray[6]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setPosition(x, y);
					this.notification(this.players.get(user_id).getNickname() + " moved to (" + x + ", " + y + ")");					
				}
			}
			
			// Update Color packet
			else if(handler.equals("upc")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setColor(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Head Item packet
			else if(handler.equals("uph")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setHead(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Face Item packet
			else if(handler.equals("upf")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setFace(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Neck Item packet
			else if(handler.equals("upn")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setNeck(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Body Item packet
			else if(handler.equals("upb")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setBody(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Hand Item packet
			else if(handler.equals("upa")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setHand(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Feet Item packet
			else if(handler.equals("upe")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setFeet(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Flag/Pin Item packet
			else if(handler.equals("upl")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setFlag(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Photo/Background Item packet
			else if(handler.equals("upp")){
				int user_id = Integer.parseInt(packetArray[4]);
				int item_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setPhoto(item_id);
					this.notification(this.players.get(user_id).getNickname() + " is now wearing " + item_id);
				}
			}
			
			// Update Remove Item packet
			else if(handler.equals("upr")){
				//~ CP has this packet in their listings, but I've never seen it used
				//~ If needed, incorporate with items array by getting item type and setting that type to 0
			}
			
			// Add Item packet
			else if(handler.equals("ai")){
				int item_id = Integer.parseInt(packetArray[4]);		
				int coins = Integer.parseInt(packetArray[5]);
				this.players.get(this.myPlayerID).addItem(item_id);
				this.players.get(this.myPlayerID).setCoins(coins);
				this.notification(this.players.get(this.myPlayerID).getNickname() + " now has the item " + item_id);
			}
				
			// Send Emote handler
			else if(handler.equals("se")){		
				int user_id = Integer.parseInt(packetArray[4]);
				int emote_id = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					if(emote_id == 1)
						this.notification(players.get(user_id).getNickname() + " emoted :D");
					else if(emote_id == 2)
						this.notification(players.get(user_id).getNickname() + " emoted :)");
					else if(emote_id == 3)
						this.notification(players.get(user_id).getNickname() + " emoted :|");
					else if(emote_id == 4)
						this.notification(players.get(user_id).getNickname() + " emoted :(");
					else if(emote_id == 5)
						this.notification(players.get(user_id).getNickname() + " emoted :o");
					else if(emote_id == 6)
						this.notification(players.get(user_id).getNickname() + " emoted :P");
					else if(emote_id == 7)
						this.notification(players.get(user_id).getNickname() + " emoted ;)");
					else if(emote_id == 8)
						this.notification(players.get(user_id).getNickname() + " emoted :sick:");
					else if(emote_id == 9)
						this.notification(players.get(user_id).getNickname() + " emoted >:(");
					else if(emote_id == 10)
						this.notification(players.get(user_id).getNickname() + " emoted :'(");
					else if(emote_id == 11)
						this.notification(players.get(user_id).getNickname() + " emoted :\\");
					else if(emote_id == 12)
						this.notification(players.get(user_id).getNickname() + " emoted :lightbulb:");
					else if(emote_id == 13)
						this.notification(players.get(user_id).getNickname() + " emoted :coffee:");
					else if(emote_id == 16)
						this.notification(players.get(user_id).getNickname() + " emoted :flower:");
					else if(emote_id == 17)
						this.notification(players.get(user_id).getNickname() + " emoted :clover:");
					else if(emote_id == 18)
						this.notification(players.get(user_id).getNickname() + " emoted :game:");
					else if(emote_id == 19)
						this.notification(players.get(user_id).getNickname() + " emoted :fart:");
					else if(emote_id == 20)
						this.notification(players.get(user_id).getNickname() + " emoted :coin:");
					else if(emote_id == 21)
						this.notification(players.get(user_id).getNickname() + " emoted :puffle:");
					else if(emote_id == 22)
						this.notification(players.get(user_id).getNickname() + " emoted :sun:");
					else if(emote_id == 23)
						this.notification(players.get(user_id).getNickname() + " emoted :moon:");
					else if(emote_id == 24)
						this.notification(players.get(user_id).getNickname() + " emoted :pizza:");
					else if(emote_id == 25)
						this.notification(players.get(user_id).getNickname() + " emoted :igloo:");
					else if(emote_id == 26)
						this.notification(players.get(user_id).getNickname() + " emoted :pink ice cream:");
					else if(emote_id == 27)
						this.notification(players.get(user_id).getNickname() + " emoted :brown ice cream:");
					else if(emote_id == 28)
						this.notification(players.get(user_id).getNickname() + " emoted :cake:");
					else if(emote_id == 29)
						this.notification(players.get(user_id).getNickname() + " emoted emoted :popcorn:");
					else if(emote_id == 30)
						this.notification(players.get(user_id).getNickname() + " emoted  <3");
				}
			}
			
			// Snowball handler
			else if(handler.equals("sb")){
				int user_id = Integer.parseInt(packetArray[4]);
				String x = packetArray[5];
				String y = packetArray[6];
				if(this.players.containsKey(user_id)){
					this.notification(players.get(user_id).getNickname() + " threw a snowball to (" + x + ", " + y + ")");
				}
			}
			
			// Send Message handler
			else if(handler.equals("sm")){
				int user_id = Integer.parseInt(packetArray[4]);
				String message = packetArray[5];
				if(this.players.containsKey(user_id)){
					this.notification(players.get(user_id).getNickname() + " said " + message);
				}
			}

			// Phrase Chat handler
			else if(handler.equals("sc")){
				int user_id = Integer.parseInt(packetArray[4]);
				String encMsg = packetArray[5];
				//~ parseChatMessage is buggy. Use at own risk.
				//~ if(this.players.containsKey(user_id)){
					//~ notification(players.get(user_id).getNickname() + "said " + parseChatMessage(encMsg));
				//~ }
			}	
			
			// Send Safe Message handler
			else if(handler.equals("ss")){
				int user_id = Integer.parseInt(packetArray[4]);
				String message_id = packetArray[5];
				if(this.players.containsKey(user_id)){
					this.notification(players.get(user_id).getNickname() + " safe-messaged " + message_id);
				}
			}
			
			// Send Joke handler
			else if(handler.equals("sj")){
				int user_id = Integer.parseInt(packetArray[4]);
				String joke_id = packetArray[5];
				if(this.players.containsKey(user_id))
					this.notification(players.get(user_id).getNickname() + " joked " + joke_id);
			}

			// Send Frame handler
			else if(handler.equals("sf")){
				int user_id = Integer.parseInt(packetArray[4]);
				int frame = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setFrame(frame);
					if(frame == 17)
						this.notification(players.get(user_id).getNickname() + " is sitting facing S");
					else if(frame == 18)
						this.notification(players.get(user_id).getNickname() + " is sitting facing SW");
					else if(frame == 19)
						this.notification(players.get(user_id).getNickname() + " is sitting facing W");
					else if(frame == 20)
						this.notification(players.get(user_id).getNickname() + " is sitting facing NW");
					else if(frame == 21)
						this.notification(players.get(user_id).getNickname() + " is sitting facing N");
					else if(frame == 22)
						this.notification(players.get(user_id).getNickname() + " is sitting facing NE");
					else if(frame == 23)
						this.notification(players.get(user_id).getNickname() + " is sitting facing E");
					else if(frame == 24)
						this.notification(players.get(user_id).getNickname() + " is sitting facing SE");
					else if(frame == 26)
						this.notification(players.get(user_id).getNickname() + " is dancing");
					else
						this.notification(players.get(user_id).getNickname() + " sent an unknown frame(" + frame + " )");
				}
			}
			
			// Open Newspaper handler
			else if(handler.equals("at")){
				int user_id = Integer.parseInt(packetArray[4]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setNewspaper();
				}
				this.notification(players.get(user_id).getNickname() + " is reading the newspaper");		
			}
			
			// Close Newspaper handler
			else if(handler.equals("rt")){
				int user_id = Integer.parseInt(packetArray[4]);
				if(this.players.containsKey(user_id)){
					this.players.get(user_id).setNewspaper();
				}
				this.notification(players.get(user_id).getNickname() + " has stopped reading the newspaper");	
			}
			
			// Send Action handler
			else if(handler.equals("sa")){
				int user_id = Integer.parseInt(packetArray[4]);
				int action = Integer.parseInt(packetArray[5]);
				if(this.players.containsKey(user_id)){
					if(action == 25)
						this.notification(this.players.get(user_id).getNickname() + " waved");
					else
						this.notification(this.players.get(user_id).getNickname() + " sent unknown action " + action);
				}
			}
		}
	}
	public void disconnect(){
		try{
			this.socket.close();
			this.in.close();
			this.out.close();
			this.is_connected = false;
			System.out.println(this.now() + "You are now disconnected.");
		}
		catch(IOException e){}
	}	
}
