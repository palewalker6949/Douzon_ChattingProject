package chat;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatServer {
	//필드
	ServerSocket serverSocket;
	ExecutorService threadPool = Executors.newFixedThreadPool(100);
	Map<String, SocketClient> clients = Collections.synchronizedMap(new HashMap<>());
	Map<String, Map<String, SocketClient>> roomList = Collections.synchronizedMap(new HashMap<>());
//	MemberRepository memberRepository = new MemberRepository();

	//메소드: 서버 시작
	public void start() throws IOException {
		serverSocket = new ServerSocket(50001);	
		System.out.println( "[서버] 시작됨");
		
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					Socket socket = serverSocket.accept();
					SocketClient sc = new SocketClient(this, socket);
				}
			} catch(IOException e) {
			}
		});
		thread.start();
	}
	//메소드: 클라이언트 연결시 SocketClient 생성 및 추가
	public void addSocketClient(SocketClient socketClient) {
		String key = socketClient.getChatName() + "@" + socketClient.clientIp;
		clients.put(key, socketClient);
		System.out.println("입장: " + key);
		System.out.println("현재 채팅자 수: " + clients.size() + "\n");
	}

	//메소드: 클라이언트 연결 종료시 SocketClient 제거
	public void removeSocketClient(SocketClient socketClient) {
		String key = socketClient.getChatName() + "@" + socketClient.clientIp;
		clients.remove(key);
		System.out.println("나감: " + key);
		System.out.println("현재 채팅자 수: " + clients.size() + "\n");
	}		
	//메소드: 모든 클라이언트에게 메시지 보냄
	public void sendToAll(SocketClient sender, String message) {
		JSONObject root = new JSONObject();
		root.put("clientIp", sender.clientIp);
		root.put("chatName", sender.getChatName());
		root.put("message", message);
		String json = root.toString();
		System.out.println("채팅");
		//스트림 통해서 전달
		Map<String, SocketClient> chatRoom = roomList.get(sender.getRoomName());
		//chatRoom으로는 왜 전송이 안되는지?
		clients.values().stream()
		.forEach(socketClient->socketClient.send(json));
	}	
	
	public void whisper(SocketClient sender, String targetName,String message)
	{
		JSONObject root = new JSONObject();
		root.put("clientIp", sender.clientIp);
		root.put("chatName", sender.getChatName() + "님의 귓속말");
		root.put("message", message);
		String json = root.toString();
		System.out.println("귓속말");
		Map<String, SocketClient> chatRoom = roomList.get(sender.getRoomName());
		chatRoom.get(targetName).send(json);
		
		//스트림 통해서 전달
//		clients.values().stream()
//		.filter()
//		.forEach(socketClient->socketClient.send(json));
		
	}
	
	public void checkIdPass(SocketClient sender,String id, String password)
	{
		//id key pass value json 파일에서 검색
		boolean isRightInfo = false;
		HashMap<String,String> infoDb = new HashMap<>();
		infoDb.put("hong", "1234");
		if(password.equals(infoDb.get(id))) 
			isRightInfo = true;
		
		JSONObject root = new JSONObject();
		root.put("isRightInfo",isRightInfo);
		String json = root.toString();
		
		sender.send(json);
		
	}
	
	public synchronized void registerMember(Member member)
	{
//		try {
//			memberRepository.insertMember(member);
//		} catch (Member.ExistMember e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	//메소드: 서버 종료
	public void stop() {
		try {
			serverSocket.close();
			threadPool.shutdownNow();
			clients.values().stream().forEach(sc -> sc.close());
			System.out.println( "[서버] 종료됨 ");
		} catch (IOException e1) {}
	}		
	//메소드: 메인
	public static void main(String[] args) {	
		try {
			ChatServer chatServer = new ChatServer();
			chatServer.start();
			
			System.out.println("----------------------------------------------------");
			System.out.println("서버를 종료하려면 q를 입력하고 Enter.");
			System.out.println("----------------------------------------------------");
			
			Scanner scanner = new Scanner(System.in);
			while(true) {
				String key = scanner.nextLine();
				if(key.equals("q")) 	
					break;
			}
			scanner.close();
			chatServer.stop();
		} catch(IOException e) {
			System.out.println("[서버] " + e.getMessage());
		}
	}
	
	public void enterRoom(SocketClient socketClient)
	{
		String key = socketClient.getChatName() + "@" + socketClient.clientIp;
		roomList.get(socketClient.getRoomName()).put(key,clients.get(key));
	}
	
	public void leaveRoom(SocketClient sender)
	{
		String key = sender.getChatName() + "@" + sender.clientIp;
		roomList.get(sender.getRoomName()).remove(key);
		
		if(roomList.get(sender.getRoomName()).size() <=0)
		{
			deleteRoom(sender.getRoomName());
		}
		
	}
	
	public void createRoom(SocketClient sender)
	{
		
		roomList.put(sender.getRoomName(), new HashMap<>());
		enterRoom(sender);
		
		JSONObject root = new JSONObject();
		root.put("roomName",sender.getRoomName());
		System.out.println("닉네임: " + sender.getChatName()+", 방이름 : "+sender.getRoomName() + " 방 입장");
		for(String room : roomList.keySet())
			System.out.println("현재 만들어져있는  방들:" +room);
		sender.send(root.toString());
	}
	
	public List<String> getChatRoomList() {
        return roomList.keySet().stream().toList();
    }
	
	private void deleteRoom(String roomName)
	{
		roomList.remove(roomName);
	}
	
}