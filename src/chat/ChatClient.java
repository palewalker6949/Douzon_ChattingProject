package chat;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;

enum SCENESTATE {
	DEFAULT, LOGIN, ROOMSELECT, CHATTING, FILETRANSFER
}

public class ChatClient {
	// 필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;

	static boolean isCheckLogin = false;
	Scanner scanner;
	static boolean isRun;
	static SCENESTATE curState = SCENESTATE.DEFAULT;
	static boolean isEnterChatting = false;
	static Member userMemberInfo = null;
	String curChatRoomName;
	String uid;
	String pwd;
	String name;
	String gender;
	String phone;
	String email;

	static List<String> chatRooms = new ArrayList<>();

	// 메소드: 서버 연결
	public void connect() throws IOException {
		socket = new Socket("localhost", 50001);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[클라이언트] 서버에 연결됨");
	}

	// 메소드: JSON 받기
	public void receive() {
		Thread thread = new Thread(() -> {
			try {
				while (true) {
					String json = dis.readUTF();
					JSONObject root = new JSONObject(json);

					String clientIp = root.getString("clientIp");
					String chatName = root.getString("chatName");
					String message = root.getString("message");
					System.out.println("<" + chatName + "@" + clientIp + "> " + message);
				}
			} catch (Exception e1) {
				System.out.println("[클라이언트] 서버 연결 끊김");
				System.exit(0);
			}
		});
		thread.start();
	}

	// 메소드: JSON 보내기
	public void send(String json) throws IOException {
		dos.writeUTF(json);
		dos.flush();
	}

	// 메소드: 서버 연결 종료
	public void disconnect() throws IOException {
		socket.close();
	}

	// 메소드: 메인
	public static void main(String[] args) {

		try {
			ChatClient chatClient = new ChatClient();
			chatClient.mainMenu();

//		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}

	public void sceneDisplay(Scanner scanner) throws IOException {
		System.out.println("");
		switch (curState) {

		case DEFAULT: {
			System.out.println("1. 로그인");
			System.out.println("2. 회원가입");
			System.out.println("3. 비밀번호 찾기");
			System.out.println("q. 프로그램 종료");
		}
			break;
		case LOGIN: {
			System.out.println("1. 방 리스트 보기");
			System.out.println("2. 채팅 방 생성");
			System.out.println("3. 정보 수정");
			System.out.println("4. 회원 탈퇴");
			System.out.println("5. 파일 전송");
			System.out.println("q. 프로그램 종료");
		}
			break;
		case FILETRANSFER: {
			System.out.println("1. 파일 목록 보기");
			System.out.println("2. 파일 다운로드");
			System.out.println("3. 파일 업로드");
			System.out.println("q. 메인 화면으로 돌아가기");
		}
			break;
		case CHATTING: {
			if (isEnterChatting == false) {
				isEnterChatting = true;
				System.out.println("======================");
				System.out.println("채팅방 입장");
				System.out.println("======================");
			}
		}
			break;
		case ROOMSELECT: {
			System.out.println("입장할 채팅방 이름을 입력해주세요");
		}
			break;

		}

	}

	public void mainMenu() throws IOException {
		System.out.println();
		boolean isStop = false;
		scanner = new Scanner(System.in);
		String message;
		String menuNum;
		while (isStop == false) {
			sceneDisplay(scanner);
			switch (curState) {
			case DEFAULT: {
				menuNum = scanner.nextLine();
				System.out.print("메뉴 선택 => ");
				switch (menuNum) {
				case "1":
					login(scanner);
					break;
				case "2":
					joinMember(scanner);
					break;
				case "3":
					passSearch();
					break;
				case "Q", "q":
					// scanner.close();
					isRun = true;
					System.out.println("프로그램 종료됨");
					//break;
					System.exit(0);
				}

			}
				break;
			case LOGIN: {
				System.out.print("메뉴 선택 => ");
				menuNum = scanner.nextLine();
				switch (menuNum) {
				case "1":
					// 채팅방 리스트
					setSceneState(SCENESTATE.ROOMSELECT);
					requestChatRoomList(scanner);
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
				case "5":
					setSceneState(SCENESTATE.FILETRANSFER);
					break;
				case "Q", "q":
					scanner.close();
					isRun = true;
					System.out.println("프로그램 종료됨");
					break;
				}
				
			}
				break;
			case FILETRANSFER: {
				System.out.print("메뉴 선택 => ");
				menuNum = scanner.nextLine();
				switch (menuNum) {
				case "1":
					// 목록
					requestFileList();
					break;
				case "2":
					// 다운로드
					fileDownload(scanner);
					break;
				case "3":
					// 업로드
					fileUpload(scanner);
					break;
				case "Q", "q":
					setSceneState(SCENESTATE.LOGIN);
					break;
				}

			}
				break;
			case ROOMSELECT: {
				String name = scanner.nextLine();

			}
				break;

			}
		}
	}

	public void fileUpload(Scanner scanner) throws IOException {
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

			disconnect();

		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}

	}

	public void fileDownload(Scanner scanner) throws IOException {
		System.out.println("서버 준비완료");
		try {

			System.out.println("다운로드 할 파일의 주소를 입력하세요 ");
			String fileName = scanner.next();

			JSONObject jsonObject = new JSONObject();

//            jsonObject.put("statusCode", "0");
			jsonObject.put("command", "fileDownload");
			jsonObject.put("fileName", fileName);

			String json = jsonObject.toString();
			connect();
			send(json);
			fileDownloadResponse(fileName);

		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (Exception ie) {
			System.out.println(ie.getMessage());
		}

	}

	private void fileDownloadResponse(String fileName) throws Exception {
		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);

		System.out.println("===================================");
		System.out.println(root.toString());
		System.out.println("===================================");
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");

		if (statusCode.equals("0")) {
			byte[] data = Base64.getDecoder().decode(root.getString("content").getBytes());

			// File file = new File("/Users/kimyoungwook/Desktop/server/client/");
			File file = new File(EnvClient.getWorkPath());
			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
				bos.write(data);
				bos.close();
				if (fileName.contains("jpg") || fileName.contains("png") || fileName.contains("jpeg")) {
					Desktop.getDesktop().open(new File(EnvClient.getWorkPath() + fileName));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("파일 다운로드 완료");
		} else {
			System.out.println(message);
		}
	}

	public void requestFileList() throws IOException {
		connect();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "requestFileList");

		send(jsonObject.toString());

		String json = dis.readUTF();
		System.out.println(json);
		disconnect();
	}

	public void login(Scanner scanner) {
		try {
			System.out.println("로그인");
			System.out.print("아이디: ");
			uid = scanner.nextLine();
			System.out.print("비밀번호: ");
			pwd = scanner.nextLine();

			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "checkLogin");
			jsonObject.put("Uid", uid);
			jsonObject.put("Pwd", pwd);

			send(jsonObject.toString());

			loginResponse();

			disconnect();

		} catch (Exception e) {

		}
	}

	public void loginResponse() {
		try {
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			boolean isRightInfo = root.getBoolean("isRightInfo");

			if (isRightInfo) {
				isCheckLogin = true;
				curState = SCENESTATE.LOGIN;
				System.out.println("로그인에 성공했습니다");
			} else {
				isCheckLogin = false;
				System.out.println("올바른 ID/Password가 아닙니다");
			}

		} catch (IOException e) {
			// TODO: handle exception
		}

	}

	public void passSearch() {
		// ID를 알고 있어야 함 Id 가 키값.
		// 검증만 "hong"으로 // 예시 ) infoDb.put("hong", "1234");

		// 직접 입력한 ID를 서번에 전달 하면
		// 리턴으로 받은 비밀번호를 표시

		Map<String, String> map = new HashMap<>();
		map.put("hong", "1234");

		try {

			connect();

			System.out.println("Uid: ");
			String uid = scanner.nextLine();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("UiD", uid);
			jsonObject.put("command", "pwSearch");
			send(jsonObject.toString());
			disconnect();

			if (map.get(uid).equals("hong")) {

				// if (isRightPw() != null) {
				// TODO 가입한 회원의 비밀번호 보여주기
				// 가입된 회원의 비밀번호를 알려주기
				System.out.println("Pwd : ");

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

	public void createRoom(Scanner scanner) {
		try {
			System.out.println("방 만들기");
			System.out.print("방제: ");
			curChatRoomName = scanner.nextLine();

			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "createRoom");
			jsonObject.put("name", uid);
			jsonObject.put("roomName", curChatRoomName);

			send(jsonObject.toString());

			createRoomResponse();

			disconnect();

			enterRoom(scanner);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void leaveRoom() throws IOException {
		curState = SCENESTATE.LOGIN;

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "leaveRoom");
		send(jsonObject.toString());

		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);
		String roomName = root.getString("roomName");
		curChatRoomName = " ";

		disconnect();
		System.out.println("방나감");

	}

	public void createRoomResponse() throws IOException {
		String json = dis.readUTF();

		curState = SCENESTATE.CHATTING;

	}

	private void enterRoom(Scanner scanner) {
		try {

			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "incoming");
			jsonObject.put("name", uid);
			jsonObject.put("roomName", curChatRoomName);

			send(jsonObject.toString());

			receive();

			inputChatMessage(scanner);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void inputChatMessage(Scanner scanner) throws IOException {
		System.out.println(curChatRoomName + "에 입장하셨습니다");
		System.out.println("'/w id 채팅내용'을 입력하시면 귓속말을 보낼 수 있습니다 ");
		System.out.println("q키를 누르면 퇴장합니다");
		while (true) {
			String message = scanner.nextLine();
			if (message.toLowerCase().equals("q")) {
				leaveRoom();
				break;
			} else {
				messageCheck(message);
			}
		}
	}

	public void messageCheck(String message) throws IOException {
		JSONObject jsonObject = new JSONObject();
		if (message.startsWith("/w")) {
			String edit = message.substring("/w ".length());
			String[] array = edit.split(" ");
			// 귓속말인 경우
			jsonObject.put("command", "whisper");
			jsonObject.put("name", uid);
			jsonObject.put("roomName", curChatRoomName);
			jsonObject.put("targetName", array[0]);
			jsonObject.put("data", array[1]);
			send(jsonObject.toString());
		} else {
			jsonObject.put("command", "message");
			jsonObject.put("name", uid);
			jsonObject.put("roomName", curChatRoomName);
			jsonObject.put("data", message);
			send(jsonObject.toString());
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

			connect();

			System.out.println("Uid: ");
			uid = scanner.nextLine();

			map.get(uid).equals("hong");
			if (map.get(uid).equals("hong")) {
				map.remove(uid);
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("Uid", uid);
			jsonObject.put("command", "withdrawal");
			send(jsonObject.toString());

			disconnect();

			System.out.println("탈퇴가 완료 되었습니다.");
		} catch (Exception e) {
			System.out.println("ID를 확인하세요.");
		}
	}

	private void joinMember(Scanner scanner) {
		try {

			connect();

			System.out.println("Uid: ");
			uid = scanner.nextLine();
			System.out.println("Pwd: ");
			pwd = scanner.nextLine();
			System.out.println("Name: ");
			name = scanner.nextLine();
			System.out.println("gender: ");
			gender = scanner.nextLine();
			System.out.println("phone: ");
			phone = scanner.nextLine();
			System.out.println("email: ");
			email = scanner.nextLine();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("Uid", uid);
			jsonObject.put("Pwd", pwd);
			jsonObject.put("Name", name);
			jsonObject.put("Gender", gender);
			jsonObject.put("Phone", phone);
			jsonObject.put("Email", email);
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

				System.out.println("Uid: ");
				uid = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("Pwd: ");
				pwd = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("Name: ");
				name = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("gender: ");
				gender = scanner.nextLine();
				scanner = new Scanner(System.in);
				System.out.println("phone");
				phone = scanner.nextLine();
				System.out.println("email: ");
				email = scanner.nextLine();

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Pwd", pwd);
				jsonObject.put("Name", name);
				jsonObject.put("Gender", gender);
				jsonObject.put("Phone", phone);
				jsonObject.put("Email", email);
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

	public void requestChatRoomList(Scanner scanner) throws IOException {

		connect();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "requestRoomList");

		send(jsonObject.toString());

		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);
		chatRooms.clear();
		root.getJSONArray("chatRooms").forEach(s -> chatRooms.add((String) s));
		disconnect();
		displayChattingRoomList();
	}

	private void displayChattingRoomList() {
		int idx = 1;
		System.out.println("----------------");
		System.out.println("* 채팅방 목록 *");
		for (String chatRoom : chatRooms) {
			System.out.println(idx + ". " + chatRoom);
			idx++;
		}
		if (0 == chatRooms.size()) {
			System.out.println("* 입장 가능한 채팅방이 없습니다. 채팅방 생성을 먼저 생성하세요 *");
			setSceneState(curState.LOGIN);
		}
	}

	private void setSceneState(SCENESTATE state) {
		curState = state;
	}
}