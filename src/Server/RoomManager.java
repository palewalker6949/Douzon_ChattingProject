package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomManager
{
	Map<String, Room> rooms;
	MainServer mainServer;
	
	public RoomManager(MainServer mainServer)
	{
		super();
		rooms = Collections.synchronizedMap(new HashMap<>());
		this.mainServer = mainServer;
	}
	
	
	public List<String> showRoomList()
	{
		return new ArrayList<>(rooms.keySet());
	}
	
	public void createRoom(String roomName)
	{
		rooms.put(roomName, new Room(this,roomName));
	}
	
	public void deleteRoom(String roomName)
	{
		rooms.remove(roomName);
	}
	
	public Room enterRoom(String roomName, ClientSocket clientSocket)
	{
		rooms.get(roomName).enterRoom(clientSocket);
		
		return rooms.get(roomName);
	}
	
	public void leaveRoom(String roomName, ClientSocket clientSocket)
	{
		rooms.get(roomName).leaveRoom(clientSocket);
	}
	
	public boolean isExistRoom(String roomName)
	{
		if(rooms.get(roomName) == null)
			return false;
		else 
			return true;
	}
	
	
}
