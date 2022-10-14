import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import hsh_test.MainServer;

public class RoomManager
{
	Map<String,SocketClient> clients = Collections.synchronizedMap(new HashMap<>());
	MainServer mainServer;
	
	
	
	public void addSocketClient(SocketClient socketClient) 
	{
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		clients.put(key, socketClient);
		System.out.println("입장: " + key);
		System.out.println("현재 채팅자 수: " + clients.size() + "\n");
	}

	//메소드: 클라이언트 연결 종료시 SocketClient 제거
	public void removeSocketClient(SocketClient socketClient)
	{
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		clients.remove(key);
		System.out.println("나감: " + key);
		System.out.println("현재 채팅자 수: " + clients.size() + "\n");
		
		if(clients.size() <1)
		{
			//방 삭제
		}
	}		
	
	public void sendToAll(SocketClient sender, String message)
	{
		
	}
	
	public int getClientsCount()
	{
		return clients.size();
	}
	
	
	
}
