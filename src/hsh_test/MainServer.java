package hsh_test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;



public class MainServer
{
	public ExecutorService threadPool = Executors.newFixedThreadPool(100);
	public ServerSocket serverSocket;
	Map<String, SocketClient> clients = Collections.synchronizedMap(new HashMap<>());
	Map<String,RoomManager> roomList = Collections.synchronizedMap(new HashMap<>());
	
	public static void main(String[] args) {	
		try {
			MainServer mainServer = new MainServer();
			mainServer.start();
			
			System.out.println("----------------------------------------------------");
			System.out.println("서버를 종료하려면 q를 입력하고 Enter.");
			System.out.println("----------------------------------------------------");
			
			Scanner scanner = new Scanner(System.in);
			while(true) {
				String key = scanner.nextLine();
				if(key.equals("q")) 	break;
			}
			scanner.close();
			mainServer.stop();
		} catch(IOException e) {
			System.out.println("[서버] " + e.getMessage());
		}
	}
	
	
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
	
	public void stop() {
		try {
			serverSocket.close();
			threadPool.shutdownNow();
			clients.values().stream().forEach(sc -> sc.close());
			System.out.println( "[서버] 종료됨 ");
		} catch (IOException e1) {}
	}		
	
	
	public void createRoom(String roomName,SocketClient socketClient)
	{
		//클라이언트 측에서 요청 받아 방 개설
		//방 만든뒤 map에 추가,
		roomList.put(roomName,new RoomManager(this,socketClient,roomName));
	}
	public void deleteRoom(String roomName)
	{
		roomList.remove(roomName);
	}
	
	public void checkLogin(SocketClient sender,String id, String password )
	{
		//로그인 체크
		String isRightInfo = "false";
		HashMap<String,String> infoDb = new HashMap<>();
		infoDb.put("hong", "1234");
		if(password.equals(infoDb.get(id))) 
			isRightInfo = "true";
		
		JSONObject root = new JSONObject();
		root.put("serverResponse",isRightInfo);
		sender.send(root.toString());
	}
	
	public void registerMember(SocketClient sender,String id, String password)
	{
		//회원 가입
		String canRegister = "false";
		//db에서 아이디 비밀번호 검색, 중복 아이디 체크
		HashMap<String,String> infoDb = new HashMap<>();
		infoDb.put("hong", "1234");
		if(infoDb.containsKey(id))
		{
			//중복 id 존재하는 경우
			canRegister  ="false";
		}
		else
		{
			//중복 id 없는 경우, 회원 정보 데이터에 해당 id,비밀번호 삽입
			canRegister = "true";
			infoDb.put(id,password);
		}
		JSONObject root = new JSONObject();
		root.put("serverResponse", canRegister);
		sender.send(root.toString());
	}
	
	public void editProfile()
	{
		//개인 정보 수정
	}
	
	public void withdrawal()
	{
		//회원 탈퇴
	}
	
	public void findPassword()
	{
		
	}
	
	
}
