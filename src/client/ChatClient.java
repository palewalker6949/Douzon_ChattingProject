package client;

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
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

import Server.Room;

enum MenuStatus
{
	START,
	LOGIN,
	FILETRANSFER,
	CHATTING,
	
}
public class ChatClient
{
	
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String roomName;
	Scanner scanner;
	MenuStatus curMenuState = MenuStatus.START;
	
	public static void main(String[] args)
	{
		ChatClient chatClient = new ChatClient();
		chatClient.mainMenu();
	}
	
	private void chattingResponse()
	{
		
		Thread thread = new Thread(()->{
			while(true) {
				JSONObject jsonObject = getServerMessage();
				String clientIp = jsonObject.getString("clientIp");
				String chatName = jsonObject.getString("chatName");
				String message = jsonObject.getString("message");
				System.out.println("<" + chatName + "@" + clientIp + "> " + message);
			}
		});
		
		thread.start();
	}
	
	private void mainMenu()
	{
		scanner = new Scanner(System.in);
		String menuNum;
		boolean isRun = true;
		while(isRun)
		{
			menuDisplay();
			System.out.print("메뉴를 입력하세요 : ");
			menuNum = scanner.nextLine();
			switch (curMenuState)
				{
				//최초 메뉴
				case START:
				{
					switch (menuNum)	
					{
						case "1":
						{
							//로그인
							login();
						}break;
						case "2":
						{
							//회원가입
							registerMember();
						}break;
						case "3":
						{
							//비밀번호 찾기
							searchPassword();
						}break;
						case "q","Q":
						{
							//프로그램 종료
							scanner.close();
							isRun = false;
						}break;
						}
				}break;
				//로그인 이후 메뉴
				case LOGIN:
				{
					switch (menuNum)
						{
						case "1":
						{
							getRoomList();
						}break;
						case "2":
						{
							createRoom();
						}break;
						case "3":
						{
							updateMember();
						}break;
						case "4":
						{
							withdrawMember();
						}break;
						case "5":
						{
							SetMenuStatus(MenuStatus.FILETRANSFER);
						}break;
						case "q","Q":
						{
							logOut();
						}break;
						}
				}break;
				//파일 전송 메뉴
				case FILETRANSFER:
				{
					switch(menuNum) {
					case "1":
						//목록
						try {
							requestFileList();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
						break;
					case "2":
						//다운로드
						fileDownload();
						break;
					case "3":
						//업로드
						fileUpload();
						break;
					case "Q", "q":
						SetMenuStatus(MenuStatus.LOGIN);
						break;
					}
				}break;
				
				}
		}
	}
	
	private void menuDisplay()
	{
		System.out.println();
		switch (curMenuState) {
			
		case START : 
		{
			System.out.println("1. 로그인");
			System.out.println("2. 회원가입");
			System.out.println("3. 비밀번호 찾기");
			System.out.println("q. 프로그램 종료");
		}break;
		case LOGIN : 
		{
			System.out.println("1. 방 리스트 보기");
			System.out.println("2. 채팅 방 생성");
			System.out.println("3. 정보 수정");
			System.out.println("4. 회원 탈퇴");
			System.out.println("5. 파일 전송");
			System.out.println("q. 로그아웃");
		}break;
		case FILETRANSFER:
		{
			System.out.println("1. 파일 목록 보기");
			System.out.println("2. 파일 다운로드");
			System.out.println("3. 파일 업로드");
			System.out.println("q. 메인 화면으로 돌아가기");
		}break;
	
		}
	}
	
	private void SetMenuStatus(MenuStatus status)
	{
		curMenuState = status;
	}
//region member method

	private void login()
	{
		System.out.print("아이디 : ");
		String uid = scanner.nextLine();
		System.out.print("비밀번호 : ");
		String pwd = scanner.nextLine();
		
		connect();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "login");
		jsonObject.put("uid", uid);
		jsonObject.put("pwd", pwd);
		
		send(jsonObject.toString());
		
		String isSuccess = getServerMessage().getString("isSuccess");
		System.out.println(isSuccess);
		disconnect();
		
		if("success".equals(isSuccess))
		{
			System.out.println("로그인에 성공하셨습니다");
			SetMenuStatus(MenuStatus.LOGIN);
		}
		else
		{
			System.out.println("로그인에 실패했습니다");
		}
	}
	
	private void registerMember()
	{
		System.out.print("아이디 : ");
		String uid = scanner.nextLine();
		System.out.print("비밀번호 : ");
		String pwd = scanner.nextLine();
		System.out.print("이름 : ");
		String name = scanner.nextLine();
		System.out.print("성별 : ");
		String sex = scanner.nextLine();
		System.out.print("주소 : ");
		String address = scanner.nextLine();
		System.out.print("전화번호 : ");
		String phone = scanner.nextLine();
		
		connect();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "registerMember");
		jsonObject.put("uid", uid);
		jsonObject.put("pwd", pwd);
		jsonObject.put("name", name);
		jsonObject.put("sex", sex);
		jsonObject.put("address", address);
		jsonObject.put("phone", phone);
		
		send(jsonObject.toString());
		
		String isExist = getServerMessage().getString("isSuccess");
		
		disconnect();
		
		if(isExist.equals("failed"))
		{
			System.out.println("이미 존재하는 id입니다");
			return;
		}
		
		System.out.println("회원가입이 성공적으로 되었습니다");
		SetMenuStatus(MenuStatus.LOGIN);
	}
	
	private void searchPassword()
	{
		String uid;
		System.out.print("찾으려는 비밀번호의 id를 입력해주세요 : ");
		uid = scanner.nextLine();
		
		connect();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command","searchPwd");
		jsonObject.put("uid", uid);
		
		send(jsonObject.toString());
		
		JSONObject serverResult = getServerMessage();
		
		disconnect();
		if(serverResult.getString("isSuccess").equals("success"))
			System.out.println(
					uid + "님의 비밀번호는 " + serverResult.getString("pwd") + "입니다");
		else
			System.out.println("존재하지 않는 id 입니다");
	}
	
	private JSONObject getServerMessage()
	{
		try
		{
			return new JSONObject(dis.readUTF());
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private void updateMember()
	{
		System.out.println("ID를 입력해주세요 : ");
		String id = scanner.nextLine();
		System.out.println("변경할 비밀번호를 입력해주세요 : ");
		String pwd = scanner.nextLine();
		System.out.println("변경할 이름을 입력해주세요 : ");
		String name = scanner.nextLine();
		System.out.println("변경할 성별을 입력해주세요 : ");
		String sex = scanner.nextLine();
		System.out.println("변경할 주소를 입력해주세요 : ");
		String address = scanner.nextLine();
		System.out.println("변경할 전화번호를 입력해주세요 : ");
		String phone = scanner.nextLine();
		
		connect();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "updateMember");
		jsonObject.put("uid", id);
		jsonObject.put("pwd", pwd);
		jsonObject.put("name", name);
		jsonObject.put("sex", sex);
		jsonObject.put("address", address);
		jsonObject.put("phone", phone);
		
		send(jsonObject.toString());
		
		JSONObject serverResult = getServerMessage();
		
		disconnect();
		if(serverResult.getString("isSuccess").equals("success"))
		{
			System.out.println("회원 정보 변경에 성공했습니다");
			System.out.println("변경된 정보 : " + jsonObject.toString());
		}
		else
		{
			System.out.println("id가 존재하지 않습니다");
		}
			
	}
	
	private void withdrawMember()
	{
		System.out.println("id를 삭제하시겠습니까? Y/N");
		String confirm = scanner.nextLine();
		
		if(confirm.toLowerCase().equals("n"))
		{
			return;
		}
		else
		{
			System.out.println("아이디를 입력해주세요 : ");
			String id= scanner.nextLine();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "withdrawMember");
			jsonObject.put("uid", id);
			
			connect();
			
			send(jsonObject.toString());
			
			JSONObject serverResult = getServerMessage();
			
			if(serverResult.getString("isSuccess").equals("success"))
			{
				System.out.println("회원탈퇴에 성공했습니다");
				SetMenuStatus(MenuStatus.START);
			}
			else
			{
				System.out.println("id가 존재하지 않습니다");
			}
		}
	}
	
//endregion
	
//region server Connect Method	
	private void connect()
	{
		try
		{
			socket = new Socket("localhost", Env.getPort());
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[클라이언트] 서버에 연결됨");
	}
	
	public void send(String json)
	{
		try
		{
			dos.writeUTF(json);
			dos.flush();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		try
		{
			socket.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//endregion	
	
//region fileTransfer Scene Method
	public void fileUpload() {
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
	
	public void fileDownload()  {
        System.out.println("서버 준비완료");
        
        try {
          
            System.out.println("다운로드 할 파일번호를 입력하세요 ");
            int filenumber = scanner.nextInt();
            
            
            JSONObject jsonObject = new JSONObject(); 
            
//            jsonObject.put("statusCode", "0");
            jsonObject.put("command", "fileDownload");
            jsonObject.put("fileNumber", filenumber);
            
            
            
            
            String json = jsonObject.toString();
            connect();
            send(json);
            fileDownloadResponse();

        } 
         catch (Exception ie) {
            System.out.println(ie.getMessage());
        }
 
	}
	
	private void fileDownloadResponse() throws IOException {
	    String json = dis.readUTF();
        JSONObject root = new JSONObject(json);
        String fileName = root.getString("fileName");
        
        System.out.println("===================================");
        System.out.println(root.toString());
        System.out.println("===================================");
        String statusCode = root.getString("statusCode");
        String message = root.getString("message");
        
        if (statusCode.equals("0")) {
            byte [] data = Base64.getDecoder().decode(root.getString("content").getBytes());

           // File file = new File("/Users/kimyoungwook/Desktop/server/client/");
            File file = new File(Env.getWorkPath());
            try {
            	 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
                bos.write(data);
                bos.close();
                if(fileName.contains("jpg")||fileName.contains("png")||fileName.contains("jpeg")) {
                	Desktop.getDesktop().open(new File(Env.getWorkPath()+fileName));
   			 	}
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("파일 다운로드 완료");
        } else {
            System.out.println(message);
        }
    }
	
	public void requestFileList() throws IOException
	{
		connect();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command","requestFileList");
		
		send(jsonObject.toString());
		
		String json = dis.readUTF();
		System.out.println(json);
		disconnect();
	}
	
	
//endregion

//region chatting Method
	private void getRoomList()
	{
		connect();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "requestRoomList");
		
		send(jsonObject.toString());
		
		JSONObject serverResult = getServerMessage();
		
		List<String> roomList = new ArrayList<>();
		serverResult.getJSONArray("chatRooms").
			forEach(s -> roomList.add((String) s));
		disconnect();
		enterRoomRequest(roomList);
	}
	
	private void displayRoomList(List<String> rooms)
	{
		  int idx = 1;
	        System.out.println("----------------");
	        System.out.println("* 채팅방 목록 *");
	        for (String chatRoom : rooms) {
	            System.out.println(idx + ". " + chatRoom);
	            idx++;
	        }
	        if (0 == rooms.size()) {
	            System.out.println("* 입장 가능한 채팅방이 없습니다. 채팅방 생성을 먼저 생성하세요 *");
	            SetMenuStatus(MenuStatus.LOGIN);
	        }        
	}
	
	private void enterRoomRequest(List<String> rooms)
	{
		displayRoomList(rooms);
		
		System.out.println("입장할 채팅방 번호를 입력하세요 : ");
		int roomNum = Integer.parseInt(scanner.nextLine());
		
		if(roomNum <=0 || roomNum > rooms.size())
		{
			System.out.println("잘못된 방 번호 입니다.");
			return;
		}
		String roomName = rooms.get(roomNum -1);
		enterRoom(roomName);
	}
	private void createRoom()
	{
		connect();
		System.out.println("생성할 방 이름을 입력해주세요 : ");
		String roomName = scanner.nextLine();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "createRoom");
		jsonObject.put("roomName", roomName);
		
		send(jsonObject.toString());
		
		JSONObject serverResult = getServerMessage();
		
		//수정
		disconnect();
		if(serverResult.getString("result").equals("success"))
		{
			enterRoom(roomName);
		}
		else
		{
			System.out.println("이미 존재하는 채팅방 이름입니다");
		}
		
	}
	
	private void enterRoom(String roomName)
	{
		connect();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command","enterRoom");
		jsonObject.put("roomName",roomName);
		send(jsonObject.toString());
		
		chattingResponse();
		
		chattingScene(roomName);
	}
	
	private void chattingScene(String roomName)
	{
		System.out.println();
		System.out.println(roomName + " 방에 입장하셨습니다");
		System.out.println("q키를 누르면 방에서 퇴장합니다");
		
		while(true)
		{
			JSONObject jsonObject = new JSONObject();
			String message = scanner.nextLine();
			if(message.toLowerCase().equals("q"))
			{
				//방 나감 처리
				jsonObject.put("command", "leaveRoom");
				break;
			}
			else
			{
				jsonObject.put("command", "sendToAll");
				jsonObject.put("message", message);
			}
			send(jsonObject.toString());
		}
		disconnect();
	}
	
	
//endregion
	
	private void logOut()
	{
		SetMenuStatus(MenuStatus.START);
	}
}
