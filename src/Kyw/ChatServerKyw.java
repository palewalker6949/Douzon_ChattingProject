package Kyw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;





public class ChatServerKyw {
	
	ServerSocket serverSocket;
	ExecutorService threadPool;
	Map<String, SocketClientKyw> chatRoom;
	MemberRepository memberRepository;
	
	public ChatServerKyw() {
	    threadPool = Executors.newFixedThreadPool(EnvServer.getThreadPoolSize());
	    chatRoom = Collections.synchronizedMap(new HashMap<>());
	    memberRepository = new MemberRepository();
        try {
            serverSocket = new ServerSocket(EnvServer.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        } 
	}

	public void start() throws IOException {

		memberRepository.loadMember();

		//properties로 .. 
		//serverSocket = new ServerSocket(50001);
	
		
		Thread thread = new Thread(() -> {
			try {
				while (true) {
					Socket socket = serverSocket.accept();
					SocketClientKyw sc = new SocketClientKyw(this, socket);
					
				}
			} catch (Exception e) {
			}
		});
		thread.start();
	}



//public void removeSocketClient(SocketClient socketClient) {
//	String key = socketClient.chatName + "@" + socketClient.clientIp;
//	chatRoom.remove(key);
//	System.out.println("나감: " + key);
//	System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
//}
//
//
//public void addSocketClient(SocketClient socketClient) {
//	String key = socketClient.chatName + "@" + socketClient.clientIp;
//	chatRoom.put(key, socketClient);
//	System.out.println("입장: " + key);
//	System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
//}
//public void sendToAll(SocketClient sender, String message) {
//	JSONObject root = new JSONObject();
//	root.put("clientIp", sender.clientIp);
//	root.put("chatName", sender.chatName);
//	root.put("message", message);
//	String json = root.toString();
//
//	// 귓속말 조건
//	if (message.indexOf("/") == 0) {
//		int pos = message.indexOf(" ");
//		String key = message.substring(1, pos); // chatName 타겟 키
//		message = "(귓)" + message.substring(pos + 1);
//
//		// 타겟 닉네임
//		// 소켓 클라이언트 변수에 채팅방 values를 대입
//		for (SocketClient socketClient : chatRoom.values()) {
//			// 소켓 클라이언트에 멤버변수인 chatName에 key값(해당이름)이 있을 경우
//			if (socketClient.chatName.equals(key)) {
//				socketClient.send(json);
//			}
//		}
//	}
//
//	chatRoom.values().stream().filter(socketClient -> socketClient != sender)
//			.forEach(socketClient -> socketClient.send(json));
//}
public static void main(String[] args) {
	try {
		ChatServerKyw chatServer = new ChatServerKyw();
		chatServer.start();
		System.out.println("서버가 시작되었습니다.");

		// 클라이언트와의 연결 대기 루프
		while (true) {
			System.out.println("새로운 Client의 연결요청을 기다립니다.");
			System.out.println("----------------------------------------------------");
			System.out.println("서버를 종료하려면 q를 입력하고 Enter.");
			System.out.println("----------------------------------------------------");
			// 연결되면 통신용 소켓 생성
			Scanner scanner = new Scanner(System.in);
			
			while (true) {
				String key = scanner.nextLine();
				
				if (key.equals("q"))
					break;
				
			}
			
			System.out.println("클라이언트와 연결되었습니다.");
			System.out.println();
			
			// 파일 수신용 클래스 생성 및 시작
//			Receiver receiver = new Receiver(socket);
//			receiver.start();
			scanner.close();
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
}
}

