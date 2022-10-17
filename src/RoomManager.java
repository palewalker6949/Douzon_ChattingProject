

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;


public class RoomManager
{
	Map<String,SocketClient> roomClients = Collections.synchronizedMap(new HashMap<>());
	MainServer mainServer;
	String roomName;
	
	public RoomManager(MainServer mainServer, SocketClient socketClient,String roomName)
	{
		this.mainServer = mainServer;
		this.roomName = roomName;
		enterRoom(socketClient);
	}

	
	public void enterRoom(SocketClient socketClient) 
	{
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		roomClients.put(key, socketClient);
		System.out.println("입장: " + key);
		System.out.println("현재 채팅자 수: " + roomClients.size() + "\n");
		
	}

	//메소드: 클라이언트 연결 종료시 SocketClient 제거
	public void leaveRoom(SocketClient socketClient)
	{
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		roomClients.remove(key);
		System.out.println("나감: " + key);
		System.out.println("현재 채팅자 수: " + roomClients.size() + "\n");
		
		if(roomClients.size() <1)
		{
			mainServer.deleteRoom(roomName);
		}
	}		
	
	public void sendToAll(SocketClient sender, String message)
	{
		JSONObject root = new JSONObject();
		root.put("clientIp", sender.clientIp);
		root.put("chatName", sender.chatName);
		root.put("message", message);
		//스트림 통해서 전달
		
		roomClients.values().stream().forEach(socketClient->socketClient.send(root.toString()));
		
	}
	
	public void whisper(SocketClient sender, String targetName,String message)
	{
		JSONObject root = new JSONObject();
		root.put("senderName", sender.chatName);
		root.put("message",message);
		
		roomClients.values().stream()
		.filter(socketClient -> socketClient.chatName.equals(targetName) || socketClient == sender)
		.forEach(socketClient -> socketClient.send(root.toString()));
	}
	
	public int getClientsCount()
	{
		return roomClients.size();
	}

	public void requestFileList()
	{
		
	}
	
	public void uploadFile()
	{
		
	}
	
	public void donwloadFile()
	{
		
	}
	
	
}
