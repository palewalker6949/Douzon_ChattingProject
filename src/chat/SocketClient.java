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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;



public class SocketClient {
	//필드
	ChatServer chatServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;	
	FileOutputStream fos;
	BufferedOutputStream bos;
	String curChatName;
	String curRoomName;
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	//생성자
	public SocketClient(ChatServer chatServer, Socket socket) {
		try {
			this.chatServer = chatServer;
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.clientIp = isa.getHostName();			
			receive();
		} catch(IOException e) {
		}
	}	
	//메소드: JSON 받기
	public void receive() {
		chatServer.threadPool.execute(() -> {
			try {
				boolean stop = false;
				while(stop == false) {
					String receiveJson = dis.readUTF();		
					
					JSONObject jsonObject = new JSONObject(receiveJson);
					String command = jsonObject.getString("command");
					
					switch(command) {
						case "incoming":
							curChatName = jsonObject.getString("name");
							curRoomName = jsonObject.getString("roomName");
							chatServer.addSocketClient(this);
							break;
						case "message":
							String message = jsonObject.getString("data");
							chatServer.sendToAll(this, message);
							break;
						case "checkLogin":
							String id = jsonObject.getString("id");
							String userPassword= jsonObject.getString("password");
							chatServer.checkIdPass(this,id, userPassword);
							stop = true;
							break;
						case "registerMember":
							
							//chatServer.registerMember(jsonObject);
							break;
						case "createRoom":
							curRoomName = jsonObject.getString("roomName");
							curChatName = jsonObject.getString("name");
							chatServer.addSocketClient(this);
							chatServer.createRoom(this);
							stop = true;
							break;
						case "whisper":
							String target = jsonObject.getString("targetName");
							String whisperMessage = jsonObject.getString("data");
							chatServer.whisper(this, target, whisperMessage);
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
						case "requestRoomList":
							sendRoomList();
							stop=true;
							break;
						case "leaveRoom":
						{
							chatServer.leaveRoom(this);
							stop=true;
						}break;
							
							
							}
				}
			} catch(IOException e) {
				chatServer.sendToAll(this, "나가셨습니다.");
				chatServer.removeSocketClient(this);
			}
		});
	}
	//메소드: JSON 보내기
	public void send(String json) {
		try {
			dos.writeUTF(json);
			dos.flush();
			System.out.println(json);
		} catch(IOException e) {	
			}	
		}
	//메소드: 연결 종료
	public void close() {
		try { 
			socket.close();
		} catch(Exception e) {}
	}
	
	public void fileUpload(JSONObject jsonObject) {
			
		String fileName = jsonObject.getString("fileName"); // 경로 가져오기 
		 byte [] data = Base64.getDecoder().decode(jsonObject.getString("content").getBytes());
		 JSONObject jsonResult = new JSONObject(); // 마지막에 보낼 JSON

		//폴더 유무 
		 //String filePath = "/Users/kimyoungwook/Desktop/server"; // 끝단 
		 String filePath = EnvServer.getWorkPath();
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
			OutputStream out = new FileOutputStream(EnvServer.getWorkPath() + fileName);
			
			BufferedOutputStream bos = new BufferedOutputStream(out);
			bos.write(data);
			
	
			
			bos.close();

			System.out.println(fileName + " " + "파일을 저장하였습니다..");
			System.out.println("저장 파일의 사이즈 : " + file.length());
//			File file2 = new File("/Users/kimyoungwook/Desktop/server/" + fileName);
			File file2 = new File(EnvServer.getWorkPath() + fileName);
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
		close();
		
		}
	    
    public void fileDownload(JSONObject jsonObject) throws IOException {
    	try {
      		 String fileName = jsonObject.getString("fileName");
      		 JSONObject jsonResult = new JSONObject();
               
              // File file = new File("/Users/kimyoungwook/Desktop/server/" + fileName);
      		 	File file = new File(EnvServer.getWorkPath() + fileName);
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
                   close();
                   
               }  
           } catch (UnknownHostException ue) {
               System.out.println(ue.getMessage());
           } catch (Exception ie) {
               System.out.println(ie.getMessage());
           }
    	
		}
    
    public void sendFileList() {
		String path = String.format(EnvServer.getWorkPath()); // 경로만들기

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
    private void registerMember(JSONObject jsonObject) {
        Member member = new Member(jsonObject);

        JSONObject jsonResult = new JSONObject();
        
        jsonResult.put("statusCode", "-1");
        jsonResult.put("message", "아이디가 존재합니다");
        
        try {
//	            chatServer.memberRepository.insertMember(member);
            jsonResult.put("statusCode", "0");
            jsonResult.put("message", "회원가입이 정상처리 되었습니다");
        } catch (Exception e) {
            e.printStackTrace();
            jsonResult.put("message", e.getMessage());
        }

        send(jsonResult.toString());
        
        close();
	    
	}
    
    
    
    public String getChatName()
    {
    	return curChatName;
    }
    
    public String getRoomName()
    {
    	return curRoomName;
    }
    
    private void sendRoomList()
    {
    	 List<String> chatRoomList = chatServer.getChatRoomList(); 

         JSONObject jsonResult = new JSONObject();
         
         jsonResult.put("chatRooms", chatRoomList);

         send(jsonResult.toString());
         
         close();       
    }
}