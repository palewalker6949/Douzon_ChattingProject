package hsh_test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer
{
	public ExecutorService threadPool = Executors.newFixedThreadPool(100);
	public ServerSocket serverSocket;
	Map<String, Map<String,SocketClient>> RoomList = Collections.synchronizedMap(new HashMap<>());
	
	public void start() throws IOException
	{
		serverSocket = new ServerSocket(50001);
		System.out.println("[서버] 시작");
		
		Thread thread = new Thread(()->{
			try
			{
				while(true)
				{
					Socket socket = serverSocket.accept();
					SocketClient socketClient = new SocketClient(this,socket);
				}
			} catch (IOException e)
			{
				// TODO: handle exception
			}
		});
	}
	
	public void createRoom()
	{
		
	}
	
}
