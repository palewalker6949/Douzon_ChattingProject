

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;

public class ChatClient 
{
	//필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;	
	static boolean isCheckLogin = false;	
	//메소드: 서버 연결
	public  void connect() throws IOException 
	{
		socket = new Socket("localhost", 50001);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[클라이언트] 서버에 연결됨");		
	}	
	//메소드: JSON 받기
	public void receive() 
	{
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
	
	public void loginReg(Scanner scanner,int commandNum)
	{
		try
		{
			String id;
			String password;
			
			System.out.print("아이디: ");
			id = scanner.nextLine();
			System.out.print("비밀번호: ");
			password = scanner.nextLine();
			
			connect();
			
			JSONObject jsonObject = new JSONObject();
			if(commandNum==1)
				jsonObject.put("command", "checkLogin");
			else if(commandNum ==2)
				jsonObject.put("command", "registerMember");
			else
				return;
			
			jsonObject.put("id", id);
			jsonObject.put("password", password);
		
			send(jsonObject.toString());
			
			boolean isRightInfo = Boolean.parseBoolean(Response());
			
			if(commandNum==1)
			{	//로그인
				if(isRightInfo)
				{
					System.out.println("로그인 되었습니다");
					isCheckLogin = true;
				}
				else
				{
					System.out.println("올바른 id/password가 아닙니다");
					isCheckLogin = false;
				}
			}
			else if(commandNum ==2)
			{	//회원가입
				if(isRightInfo)
				{
					System.out.println("회원가입 되었습니다");
					isCheckLogin = true;
				}
				else
				{
					System.out.println("중복된 id가 존재합니다");
					isCheckLogin = false;
				}
			}
			disconnect();
			
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public String Response()
	{
		try
		{
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			String isRightInfo = root.getString("serverResponse");
			return isRightInfo;
			
		} catch (IOException e)
		{
			// TODO: handle exception
			return "false";
		}
		
		
	}
	
	public void passSearch(Scanner scanner)
	{
		try
		{
			System.out.println("아이디를 입력해주세요: ");
			String id = scanner.nextLine();
			
			connect();
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "registerMember");
			jsonObject.put("id", id);
		
			send(jsonObject.toString());
			
			
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	
	public void enterRoom()
	{
		
	}
	//메소드: JSON 보내기
	public void send(String json) throws IOException 
	{
		dos.writeUTF(json);
		dos.flush();
	}	
	//메소드: 서버 연결 종료
	public void disconnect() throws IOException 
	{
		socket.close();
	}	
	//메소드: 메인
	public static void main(String[] args) 
	{		
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
					chatClient.loginReg(scanner, Integer.parseInt(menuNum));
					break;
				case "2":
					chatClient.loginReg(scanner, Integer.parseInt(menuNum));
					break;
				case "3":
					chatClient.passSearch(scanner);
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