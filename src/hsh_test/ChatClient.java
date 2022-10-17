package hsh_test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;

public class ChatClient {
	//필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;	
	static boolean isCheckLogin = false;
	//메소드: 서버 연결
	public  void connect() throws IOException {
		socket = new Socket("localhost", 50001);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[클라이언트] 서버에 연결됨");		
	}	
	//메소드: JSON 받기
	public void receive() {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					String json = dis.readUTF();
					JSONObject root = new JSONObject(json);
					String clientIp = root.getString("clientIp");
					String chatName = root.getString("chatName");
					String message = root.getString("message");
					System.out.println("<" + chatName + "@" + clientIp + "> " + message);
				}
			} catch(Exception e1) {
				System.out.println("[클라이언트] 서버 연결 끊김");
				System.exit(0);
			}
		});
		thread.start();
	}	
	
	public void login(Scanner scanner)
	{
		try
		{
			String id;
			String password;
			
			
			System.out.println("로그인");
			System.out.print("아이디: ");
			id = scanner.nextLine();
			System.out.print("비밀번호: ");
			password = scanner.nextLine();
			
			connect();
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "checkLogin");
			jsonObject.put("id", id);
			jsonObject.put("password", password);
			
			send(jsonObject.toString());
			
			loginResponse();
			
			disconnect();
			
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public void loginResponse()
	{
		try
		{
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			boolean isRightInfo = root.getBoolean("isRightInfo");
			
			if(isRightInfo)
			{
				isCheckLogin = true;
				System.out.println("로그인에 성공했습니다");
			}
			else
			{
				isCheckLogin =false;
				System.out.println("올바른 ID/Password가 아닙니다");
			}
			
		} catch (IOException e)
		{
			// TODO: handle exception
		}
		
		
	}
	
	public void registerMember(Scanner scanner)
	{
		
	}
	
	public void passSearch()
	{
		
	}
	
	public void enterRoom()
	{
		
	}
	//메소드: JSON 보내기
	public void send(String json) throws IOException {
		dos.writeUTF(json);
		dos.flush();
	}	
	//메소드: 서버 연결 종료
	public void disconnect() throws IOException {
		socket.close();
	}	
	//메소드: 메인
	public static void main(String[] args) {		
		try {			
			ChatClient chatClient = new ChatClient();
			boolean stop = false;
			
			while(false == stop) {
				System.out.println();
				System.out.println("1. 로그인");
				System.out.println("2. 회원가입");
				System.out.println("3. 비밀번호 찾기");
				System.out.println("4. 채팅방 입장");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				switch(menuNum) {
				case "1":
					chatClient.login(scanner);
					break;
				case "2":
					chatClient.registerMember(scanner);
					break;
				case "3":
					chatClient.passSearch();
					break;
				case "4":
					if(isCheckLogin)
						chatClient.enterRoom();
					else
						System.out.println("로그인을 먼저 해주세요");
					break;
				case "Q", "q":
					scanner.close();
					stop = true;
					System.out.println("프로그램 종료됨");
					break;
				}
			}
			
//		
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}
}