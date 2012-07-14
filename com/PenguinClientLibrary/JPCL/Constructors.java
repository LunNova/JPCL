package com.PenguinClientLibrary.JPCL;

/*
 	PenguinClientLibrary - JPCL - Constructors.java - Error and Server Table Creator
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

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class Constructors {

	public HashMap<Integer, ServerFrame> setupServers(){
		int ID, port;
		String name, ip;
		boolean safe;
		
		HashMap<Integer, ServerFrame> temp = new HashMap<Integer, ServerFrame>();
		
		try{
			URL u = new Constructors().getClass().getResource("servers.ini");
			INIParser i = new INIParser(u.getFile());
			Iterator<String> in = i.getSections();
			
			while(in.hasNext()){
				String cur = in.next().toString();
				ID = Integer.parseInt(cur);
				port = Integer.parseInt(i.getString(cur, "port").replaceAll(" ", ""));
				name = i.getString(cur, "name").toString().replaceFirst(" ", "");
				ip = i.getString(cur, "ip").toString().replaceAll(" ", "");
				safe = Boolean.parseBoolean(i.getString(cur, "safe").replaceAll(" ", ""));
				temp.put(ID, new ServerFrame(ID, name, ip, port, safe));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return temp;
	}
	
	public HashMap<Integer, ErrorFrame> errorHandling(){
		
		int ID;
		String name, desc;
		boolean disconnect;
		
		HashMap<Integer, ErrorFrame> temp = new HashMap<Integer, ErrorFrame>();
		
		try{
			URL u = new Constructors().getClass().getResource("errors.ini");
			INIParser i = new INIParser(u.getFile());
			Iterator<String> in = i.getSections();
			
			while(in.hasNext()){
				String cur = in.next().toString();
				ID = Integer.parseInt(cur);
				name = i.getString(cur, "name").toString().replaceFirst(" ", "");
				desc = i.getString(cur, "desc").toString().replaceFirst(" ", "");
				disconnect = Boolean.parseBoolean(i.getString(cur, "dis"));
				temp.put(ID, new ErrorFrame(ID, name, desc, disconnect));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return temp;
	}
}
