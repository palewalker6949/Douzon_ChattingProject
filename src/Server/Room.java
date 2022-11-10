package Server;

import java.lang.ref.Cleaner.Cleanable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Room
{
	Map<String, ClientSocket> chatClients;
	RoomManager roomManager;
	String roomName;
	
	public Room(RoomManager roomManager,String roomName)
	{
		this.roomManager = roomManager;
		chatClients = Collections.synchronizedMap(new HashMap<>());
		this.roomName = roomName;
	}
	
	public void enterRoom(ClientSocket clientSocket)
	{
		chatClients.put(clientSocket.getKey(),clientSocket);
	}
	
	public void leaveRoom(ClientSocket clientSocket)
	{
		chatClients.remove(clientSocket.getKey());
		if(chatClients.size() <=0 )
			roomManager.deleteRoom(roomName);
		
	}
	
	public void sendToAll(ClientSocket clientSocket, String message)
	{
		System.out.println(chatClients.keySet());
		JSONObject root = new JSONObject();
		root.put("clientIp",clientSocket.clientIp);
		root.put("chatName",clientSocket.uid);
		root.put("message",message);
		
		chatClients.values().stream()
        .forEach(socketClient -> socketClient.send(root.toString()));
	}
	
	public String getRoomName()
	{
		return roomName;
	}
	
}
