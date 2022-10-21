package hsh_test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

enum SCENESTATE{DEFAULT, LOGIN, ROOMSELECT,CHATTING}

public class ChatClient {
	//필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;	
	static boolean isCheckLogin = false;
	Scanner scanner;
	static boolean isRun;
	static SCENESTATE curState = SCENESTATE.DEFAULT;
	static boolean isEnterChatting = false;
	
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
			Scanner scanner = new Scanner(System.in);
			while(false == isRun) {
				switch (curState)
					{
						case DEFAULT :
						{
							chatClient.defaultScene(scanner);
						}break;
						case LOGIN :
						{
							chatClient.loginScene(scanner);
						}break;
						case CHATTING :
						{
							chatClient.chattingScene(scanner);
						}
					}
			}
			
//		
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}
	
	public void defaultScene(Scanner scanner)
	{
		
		System.out.println();
		System.out.println();
		System.out.println("1. 로그인");
		System.out.println("2. 회원가입");
		System.out.println("3. 비밀번호 찾기");
		System.out.println("4. 채팅방 입장");
		System.out.println("q. 프로그램 종료");
		System.out.print("메뉴 선택 => ");
		 scanner = new Scanner(System.in);
		String menuNum = scanner.nextLine();
		switch(menuNum) {
		case "1":
			login(scanner);
			break;
		case "2":
			registerMember(scanner);
			break;
		case "3":
			passSearch();
			break;
		case "4":
			if(isCheckLogin)
				enterRoom();
			else
				System.out.println("로그인을 먼저 해주세요");
			break;
		case "Q", "q":
			scanner.close();
			isRun = true;
			System.out.println("프로그램 종료됨");
			break;
		}
	}
	
	public void loginScene(Scanner scanner)
	{
		System.out.println();
		System.out.println();
		System.out.println("1. 방 리스트 보기");
		System.out.println("2. 채팅 방 생성");
		System.out.println("3. 회원 탈퇴");
		System.out.println("4. 정보 수정");
		System.out.println("q. 프로그램 종료");
		System.out.print("메뉴 선택 => ");
		 scanner = new Scanner(System.in);
		String menuNum = scanner.nextLine();
		switch(menuNum) {
		case "1":
			login(scanner);
			break;
		case "2":
			createRoom(scanner);
			break;
		case "3":
			passSearch();
			break;
		case "4":
			if(isCheckLogin)
				enterRoom();
			else
				System.out.println("로그인을 먼저 해주세요");
			break;
		case "Q", "q":
			scanner.close();
			isRun = true;
			System.out.println("프로그램 종료됨");
			break;
		}
	}
	
	public void chattingScene(Scanner scanner)
	{
		String message;
		
		if(isEnterChatting == false)
		{
			receive();
			isEnterChatting = true;
			System.out.println("======================");
			System.out.println("채팅방 입장");
			System.out.println("======================");
		}
		
		
		message = scanner.nextLine();
		messageCheck(message);
		
		try
		{
			
		} catch (Exception e)
		{
			// TODO: handle exception
		}
		
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
				curState = SCENESTATE.LOGIN;
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
	
	
	
	public void createRoom(Scanner scanner)
	{
		try
		{
			String roomName;
			String chatName;
			
			
			System.out.println("방 만들기");
			System.out.print("방제: ");
			roomName = scanner.nextLine();
			System.out.print("채팅 닉네임: ");
			chatName = scanner.nextLine();
			
			connect();
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "createRoom");
			jsonObject.put("roomName", roomName);
			jsonObject.put("chatName", chatName);
			
			send(jsonObject.toString());
			
			createRoomResponse();
			
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public void createRoomResponse()
	{
		try
		{
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			String roomName = root.getString("roomName");
			
			System.out.println(roomName + "에 입장하셨습니다");
			System.out.println("q키를 누르면 퇴장합니다");
			
			curState = SCENESTATE.CHATTING;
		} catch (IOException e)
		{
			// TODO: handle exception
		}
		
	}
	
	public void messageCheck(String message)
	{
		try
		{
			if(message.indexOf("/") != -1)
			{
				String[] messages = message.split(" ");
				JSONObject jsonObject = new JSONObject();
				jsonObject = new JSONObject();
				if(messages[0].equals("/w"))//귓속말
				{
					jsonObject.put("command", "whisper");
					jsonObject.put("target", messages[1]);
					jsonObject.put("data", messages[2]);
				}
				//파일목록,파일업로드,파일다운로드 키워드 체크
				
				send(jsonObject.toString());
			}
			
			else 
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject = new JSONObject();
				jsonObject.put("command", "message");
				jsonObject.put("data", message);
				
				send(jsonObject.toString());
			}
			
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	public ChatClient()
	{
		// TODO Auto-generated constructor stub
	}public void fileUpload(Scanner scanner) throws JSONException {
		try {
			System.out.println("파일주소를 입력하세요:");
			String fileName = scanner.next();

			JSONObject jsonObject = new JSONObject();
			File file = new File(fileName);
			if (!file.exists()) {
				System.out.println("파일이 존재 하지 않습니다");
				return;
			}
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] data = new byte[(int) file.length()];
			in.read(data);
			in.close();
			jsonObject.put("command", "fileUpload");
			jsonObject.put("fileName", file.getName());
			jsonObject.put("content", new String(Base64.getEncoder().encode(data)));

			String json = jsonObject.toString();
			connect();
			send(json);

			System.out.println("파일전송 완료");
		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}
	}
	 public void fileDownload() throws IOException {
	    	ServerSocket serverSocket = new ServerSocket(7000);
	    	
	        System.out.println("서버 준비완료");
	       
	        Socket sock = serverSocket.accept();
	    	
	    	DataInputStream dis = new DataInputStream(sock.getInputStream());
	    	String fileName = dis.readUTF();
	    	
	    	File f = new File("/Users/kimyoungwook/Desktop/test/"+fileName);
	    	DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
	    	dos.writeUTF(f.getName());
	    	dos.flush();
	    	
	    	FileInputStream fis = new FileInputStream("/Users/kimyoungwook/Desktop/"+fileName);
	    	if (fileName.contains("jpg")||fileName.contains("png")||fileName.contains("jpeg")) 
	    	{Runtime.getRuntime().exec("/Users/kimyoungwook/Desktop/"+fileName);}
	    	
	    	int n = 0;
	    	byte b[] = new byte[1024];
	    	while ((n = fis.read(b)) != -1) {
	    		dos.write(b, 0, n);
	    	}

	    	dos.close();
	    	fis.close();
	    	System.out.println("파일전송 완료");
	    	sock.close();
	    	
	    		}
//목록 조회 
	public List<String> getFileList( Scanner scanner ) {
		System.out.println("조회 하실 폴더 명을 입력하세요.");
		String folderName = scanner.next();
		String path = String.format("/Users/kimyoungwook/Desktop/"+ folderName); // 경로만들기

		List<String> fileList = new LinkedList<>(); // 파일리스트들 만들기

		File dir = new File(path);// 경로를 담은 파일 객체 만들기
		File[] files = dir.listFiles(); // 디렉토리 경로에 있는 파일들을 배열에 담기

		for (File f : files) { // 배열에서 파일 찾기
			if (f.isFile()) {
				fileList.add(f.getName());
				// 있으면 보내준다. f.getName() 이름을 담아서 리스트에 넣어주기
			} else if (!f.isFile()) {
				break;
				// 없으면 나가기.
			}
		}
		System.out.println(fileList);
		
		return fileList; // 리스트 리턴해주기
	}
	
}