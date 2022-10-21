

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;

enum SCENESTATE{DEFAULT, LOGIN, ROOMSELECT,CHATTING}

public class ChatClient {
	//필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	
	static boolean isCheckLogin = false;
	Scanner scanner;
	static boolean isRun;
	static SCENESTATE curState = SCENESTATE.DEFAULT;
	static boolean isEnterChatting = false;
	String chatName;	
	String id;
	String pw;
	String name;
	String gender;
	String phone;
	String chatRoomName;
	
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
			joinMember(scanner);
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
		System.out.println("3. 정보 수정");
		System.out.println("4. 회원 탈퇴");
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
			modifyMemberInfo(scanner);
			break;
		case "4":
			withdrawalMember(scanner);
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
	

	
	
	public void passSearch()
	{
		// ID를 알고 있어야 함 Id 가 키값.
				// 검증만 "hong"으로 // 예시 ) infoDb.put("hong", "1234");

				// 직접 입력한 ID를 서번에 전달 하면
				// 리턴으로 받은 비밀번호를 표시

				Map<String, String> map = new HashMap<>();
				map.put("hong", "1234");

				try {

					connect();

					System.out.println("Id: ");
					id = scanner.nextLine();

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("ID", id);
					jsonObject.put("command", "pwSearch");
					send(jsonObject.toString());
					disconnect();

					if (map.get(id).equals("hong")) {

						// if (isRightPw() != null) {
						// TODO 가입한 회원의 비밀번호 보여주기
						// 가입된 회원의 비밀번호를 알려주기
						System.out.println("Pw : ");

						// 비밀번호 찾기 성공 시 디폴트스크린 재진입
						// 로그인 진행
						curState = SCENESTATE.DEFAULT;

					} else {
						// 비밀번호 찾기 실패 시 비밀번호찾기 화면 유지

						System.out.println("정보를 찾을 수 없습니다.");
					}

				} catch (Exception e) {
					// 에러처리
					// id를 잘못 입력한 경우
					// 비밀번호 데이터가 ""이거나 null인 경우
					System.out.println("잘못된 ID 이거나 비밀번호를 찾을 수 없습니다.");
				}
	}
	
	private void joinMember(Scanner scanner) {
		try {
			String id;
			String pw;
			String name;
			String gender;
			String phone;
			connect();

			System.out.println("Id: ");
			id = scanner.nextLine();
			System.out.println("Pw: ");
			pw = scanner.nextLine();
			System.out.println("Name: ");
			name = scanner.nextLine();
			System.out.println("gender: ");
			gender = scanner.nextLine();
			System.out.println("phone: ");
			phone = scanner.nextLine();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ID", id);
			jsonObject.put("PW", pw);
			jsonObject.put("Name", name);
			jsonObject.put("Gender", gender);
			jsonObject.put("Phone", phone);
			jsonObject.put("command", "joinMem");
			send(jsonObject.toString());

			disconnect();

			if (isJoinMember() != true) {

				// true면 회원가입 완료
				// 회원가입 성공 시 디폴트스크린 재진입
				// 로그인 진행
				curState = SCENESTATE.DEFAULT;
				// id 체크 있냐 없냐
				System.out.println("회원가입이 완료 되었습니다.");

			} else {
				// false면 중복된 아이디 있음
				// 그 아이디로 가입 안돼
				System.out.println("회원가입이 불가 합니다.");

			}
		} catch (Exception e) {
			System.out.println(e);
		}

	}
	
	public void enterRoom()
	{
		// 채팅 목록 생성
				// 목록만 보이게
				// 목록 + 채팅방 입장

				// 채팅방 별로 시퀀스가 있음
				// 대화도 채팅방 시퀀스 따라간다.

				// 0.0. 채팅목록 요청
				// 0. 서버에서 전달받은 채팅방 목록 화면에 표시

				// 채팅방 선택

				// 1. 내아이디 찾아와서
				// 2. 입장하고자하는 채팅방 시퀀스도 찾아온다.
				// 3. 이 두개를 다시 서버에 보낸다.

				try {
					connect();

					System.out.println("----- 채팅방 목록 -----");
					System.out.println("~");
					System.out.println("채팅방 선택: ");
					chatRoomName = scanner.nextLine();
//					chatRoomNum = Integer.parseInt(scanner.nextLine());

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("chatRoomName", chatRoomName);
					jsonObject.put("command", "income");
					send(jsonObject.toString());

					disconnect();

					if (isRightId() != true) {
						// 채팅방 목록 중 들어가고자 하는 채팅방 선택
						// TODO 채팅방 목록 선택
						System.out.println(chatRoomName + "방에 입장했습니다.");
					}

				} catch (Exception e) {
					System.out.println("채팅방을 다시 선택해주세요.");
				}
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
	
	
	
	private void withdrawalMember(Scanner scanner) {
		// ID를 보내고 그 해당 내용을 삭제할 수 있게
		// 회원 탈퇴기능

		// 1. 내아이디 찾아와서
		// 2. 서버로 보내고 데이터 삭제

		Map<String, String> map = new HashMap<>();
		map.put("hong", "1234");

		try {
			String id;
			connect();

			System.out.println("Id: ");
			id = scanner.nextLine();

			map.get(id).equals("hong");
			if (map.get(id).equals("hong")) {
				map.remove(id);
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ID", id);
			jsonObject.put("command", "withdrawal");
			send(jsonObject.toString());

			disconnect();

			System.out.println("탈퇴가 완료 되었습니다.");
		} catch (Exception e) {
			System.out.println("ID를 확인하세요.");
		}
	}
	
	private void modifyMemberInfo(Scanner scanner) {
		// 수정 할 내용 입력 받기
		// 서버로 보내기
		// 단발성으로 끝낼 것
		// connect disconnect;

		// id , 비번, 이름, 성별, 전화번호 (회원가입시 필요한 정보)
		// 비번, 이름, 성별, 전화번호 (회원수정 가능한 정보)

		// 1. 내아이디 찾아와서
		// 새로입력 받는 비번, 이름, 성별, 전화번호를 항상 다 서버로 보낸다

		// 변경이 없는 항목은 서비스단에서 걸러내고 나머지 업데이트

		if (isRightId() != true) {
			// TODO 아이디가 맞으면 비번, 이름, 성별, 전화번호 업데이트

			try {

				connect();

				System.out.println("Id: ");
				id = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("Pw: ");
				pw = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("Name: ");
				name = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("gender: ");
				gender = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("phone");
				phone = scanner.nextLine();

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("PW", pw);
				jsonObject.put("Name", name);
				jsonObject.put("Gender", gender);
				jsonObject.put("Phone", phone);
				jsonObject.put("command", "modify");
				send(jsonObject.toString());

				disconnect();

				System.out.println("회원 정보 수정을 완료하였습니다.");

			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			System.out.println("정보를 찾을 수 없습니다.");
		}
	}
	
	
	
	public String isRightPw() {
		try {
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			String isRightInfo = root.getString("serverResponse");
			return isRightInfo;
		} catch (Exception e) {
			System.out.println("잘못된 정보입니다.");
			return isRightPw();
		}

	}

	public boolean isRightId() {
		try {
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			boolean isRightInfo = Boolean.parseBoolean(root.getString("serverResponse"));
			return isRightInfo;

		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isJoinMember() {
		try {
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			boolean isJoin = Boolean.parseBoolean(root.getString("serverResponse"));
			return isJoin;
		} catch (Exception e) {
			return false;
		}
	}

	public String chatList() {
		try {
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			String chatList = root.getString("serverResponse");
			return chatList;
		} catch (Exception e) {

			System.out.println("리스트 목록: ");
			return chatList();
		}

	}
}