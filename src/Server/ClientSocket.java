package Server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NotContextException;

import org.json.JSONArray;
import org.json.JSONObject;

import Common.Member;

public class ClientSocket
{
	MainServer mainServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;	
    String uid = new String();
    private Room curRoom;
    public static final int DEFAULT_BUFFER_SIZE = 4096;
	
	//생성자
	public ClientSocket(MainServer mainServer, Socket socket) {
		try {
			this.mainServer = mainServer;
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.clientIp = isa.getHostName();			
			receive();
		} catch(IOException e) {
		}
	}
	
	public void receive() {
		mainServer.threadPool.execute(() -> {
			try {
				boolean stop = false;
				while(true != stop) {
					String receiveJson = getClientMessage();		
					
					JSONObject jsonObject = new JSONObject(receiveJson);
					String command = jsonObject.getString("command");
					
					
					switch(command) {
					case "login" :
						System.out.println("login : " + jsonObject.getString("uid"));
						login(jsonObject);
						stop = true;
						break;
					case "registerMember" :
						registerMemeber(jsonObject);
						stop = true;
						break;
					case "searchPwd":
						searchPwd(jsonObject);
						stop = true;
						break;
					case "fileUpload":
                        fileUpload(jsonObject);
                        stop= true;
                        break;
					case "fileDownload":
						fileDownload(jsonObject);
						stop =true;
						break;
					case "requestFileList":
						sendFileList();
						stop =true;
						break;
					case "updateMember" :
						updateMember(jsonObject);
						stop=true;
						break;
					case "withdrawMember":
						withdrawMember(jsonObject);
						stop=true;
						break;
					case "requestRoomList":
						sendRoomList();
						stop=true;
						break;
					case "enterRoom":
						System.out.println("enter : " + uid);
						enterRoom(jsonObject);
						break;
					case "createRoom":
						System.out.println("create : " + uid);
						createRoom(jsonObject);
						stop=true;
						break;
					case "leaveRoom":
						leaveRoom(jsonObject);
						stop=true;
						break;
					case "sendToAll":
						System.out.println("send : " + uid);
						sendToAll(jsonObject);
						break;
						
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
				mainServer.deleteClientSocket(this);
                    close();
                }
			
		});
	}
	
	public void close() {
		try { 
			socket.close();
		} catch(Exception e) {}
	}
	
	private String getClientMessage() throws IOException
	{
		return dis.readUTF();
	
	}
	
	public String getKey()
	{
		return uid + "@" + clientIp;
	}
	
//region member method

	private void login(JSONObject jsonObject)
	{
		String id = jsonObject.getString("uid");
		String pwd = jsonObject.getString("pwd");
		
		JSONObject sendMessage = new JSONObject();
		
		Member member = getMember(id);
		if(member == null)//아이디가 존재하지 않을때
			sendMessage.put("isSuccess", "failed");
		else
		{
			if(pwd.equals(member.getPwd()))
			{//비밀번호 체크
				sendMessage.put("isSuccess", "success");
				this.uid = id;
			}
			
			else
				sendMessage.put("isSuccess", "failed");
		}
		
		send(sendMessage.toString());
	}
	
	private void registerMemeber(JSONObject jsonObject)
	{
		String id = jsonObject.getString("uid");
		
		JSONObject root = new JSONObject();
		
		//존재하는 id면 true 리턴후 결과값 send
		if(isExistMember(id))
		{
			root.put("isSuccess", "failed");
		}
		else
		{
			mainServer.getMemberRepository().
			insertMember(new Member(jsonObject));
			root.put("isSuccess", "success");
			this.uid = id;
		}
		send(root.toString());
	}
	
	private void searchPwd(JSONObject jsonObject)
	{
		String id = jsonObject.getString("uid");
		JSONObject root = new JSONObject();
		//존재하는 id면 true 리턴후 결과값 send
		if(isExistMember(id))
		{
			root.put("isSuccess", "success");
			String pwd = getMember(id).getPwd();
			root.put("pwd",pwd);
		}
		else
		{
			root.put("isSuccess", "failed");
		}
		send(root.toString());
	}
	
	private void updateMember(JSONObject jsonObject)
	{
		String id = jsonObject.getString("uid");
		JSONObject root = new JSONObject();
		
		if(isExistMember(id))
		{
			Member member = new Member(jsonObject);
			mainServer.getMemberRepository().updateMember(member);
			root.put("isSuccess", "success");
		}
		else
		{
			root.put("isSuccess","failed");
		}
		
		send(root.toString());
	}
	
	private void withdrawMember(JSONObject jsonObject)
	{
		String id = jsonObject.getString("uid");
		JSONObject root = new JSONObject();
		
		if(isExistMember(id))
		{
			mainServer.getMemberRepository().deleteMember(id);
			root.put("isSuccess", "success");
		}
		else
		{
			root.put("isSuccess","failed");
		}
		send(root.toString());
	}
//endregion
	
//region file transfer scene method
	public void fileUpload(JSONObject jsonObject) {
		
		String fileName = jsonObject.getString("fileName"); // 경로 가져오기 
		 byte [] data = Base64.getDecoder().decode(jsonObject.getString("content").getBytes());
		 JSONObject jsonResult = new JSONObject(); // 마지막에 보낼 JSON

		//폴더 유무 
		 //String filePath = "/Users/kimyoungwook/Desktop/server"; // 끝단 
		 String filePath = Env.getWorkPath();
		File Folder = new File(filePath);
		if (!Folder.exists()) {
			try {
				Folder.mkdir();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
		
		try {
			System.out.println("파일 저장 작업을 시작합니다.");

			String str2 = new String(fileName);
			File file = new File(str2);
			
//			OutputStream out = new FileOutputStream("/Users/kimyoungwook/Desktop/server/" + fileName);
			OutputStream out = new FileOutputStream(Env.getWorkPath() + fileName);
			
			BufferedOutputStream bos = new BufferedOutputStream(out);
			bos.write(data);
			bos.close();
			System.out.println(fileName + " " + "파일을 저장하였습니다..");
			System.out.println("저장 파일의 사이즈 : " + file.length());
//			File file2 = new File("/Users/kimyoungwook/Desktop/server/" + fileName);
			File file2 = new File(Env.getWorkPath() + fileName);
			//이미지 오픈 

			// 바이트 데이터를 전송받으면서 크기 기록
			int len;
			byte[] data1 = new byte[DEFAULT_BUFFER_SIZE];
			while ((len = dis.read(data1)) != -1) {
				bos.write(data1, 0, len);
			}


		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		jsonResult.put("statusCode", "0");
		jsonResult.put("message", "파일수신 완료");
		send(jsonResult.toString());
		//close();
		
		}
	    
    public void fileDownload(JSONObject jsonObject) throws IOException {
    	try {
      		 String fileName = jsonObject.getString("fileName");
      		 JSONObject jsonResult = new JSONObject();
               
              // File file = new File("/Users/kimyoungwook/Desktop/server/" + fileName);
      		 	File file = new File(Env.getWorkPath() + fileName);
               if (!file.exists()) {
                   System.out.println("파일이 존재 하지 않습니다");
                   jsonResult.put("statusCode", "-1");
                   jsonResult.put("message", "파일이 존재 하지 않습니다");
               } else {
                   BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                   byte [] data = new byte[(int)file.length()];
//	                   System.out.println("write length = ");
//	                   System.out.println("write length = " + bin.read(data));
                   bin.read(data);
                   bin.close();
                   
                   System.out.println("file length = " + file.length());
                   
                   jsonResult.put("statusCode", "0");
                   jsonResult.put("fileName", file.getName());
                   jsonResult.put("message", "성공");
                   jsonResult.put("content", new String(Base64.getEncoder().encode(data)));
                   
                   send(jsonResult.toString());
                   //close();
                   
               }  
           } catch (UnknownHostException ue) {
               System.out.println(ue.getMessage());
           } catch (Exception ie) {
               System.out.println(ie.getMessage());
           }
    	
		}
    
    public void sendFileList() {
		String path = String.format(Env.getWorkPath()); // 경로만들기

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
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray(fileList);
		jsonObject.put("fileList", jsonArray);
		send(jsonObject.toString());
	}

//endregion
	
//region room method 
     private void sendRoomList()
    {
    	 List<String> chatRoomList = mainServer.getRoomManager().showRoomList();

         JSONObject jsonResult = new JSONObject();
         
         jsonResult.put("chatRooms", chatRoomList);

         send(jsonResult.toString());
    }
    
    private void createRoom(JSONObject jsonObject)
    {
    	JSONObject jsonResult = new JSONObject();
    	if(isExistRoom(jsonObject.getString("roomName")))
    	{
    		jsonResult.put("result", "failed");
    	}
    	else
    	{
    		jsonResult.put("result","success");
    		getRoomManager().createRoom(jsonObject.getString("roomName"));
    		
    	}
    	send(jsonResult.toString());
    	
    }
    
    private void enterRoom(JSONObject jsonObject)
    {
    	curRoom = getRoomManager().enterRoom(jsonObject.getString("roomName"), this);
    }
    
    private void leaveRoom(JSONObject jsonObject)
    {
    	if(curRoom != null)
    	{
    		getRoomManager().leaveRoom(curRoom.getRoomName(), this);
    	}
    }
    
//endregion
  
//region chat method
    private void sendToAll(JSONObject jsonObject)
    {
    	System.out.println("메시지 보냄");
    	String message = jsonObject.getString("message");
    	curRoom.sendToAll(this, message);
    }
    
//endregion
    
    private Boolean isExistMember(String id)
	{
		return mainServer.getMemberRepository().isExistMember(id);
	}
	
	private Member getMember(String id)
	{
		return mainServer.getMemberRepository().getMemberById(id);
	}
	
	private boolean isExistRoom(String roomName)
	{
		return getRoomManager().isExistRoom(roomName);
	}
	
	private RoomManager getRoomManager()
	{
		return mainServer.getRoomManager();
	}
	public void send(String json)
	{
		try
		{
			dos.writeUTF(json);
			dos.flush();
			System.out.println(json);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
