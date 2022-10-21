

import java.awt.Desktop;
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
import java.util.Base64;

import org.json.JSONObject;

public class SocketClient {
	//필드
	ChatServer chatServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;	
	String chatName;
	FileOutputStream fos;
	BufferedOutputStream bos;
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
				while(true) {
					String receiveJson = dis.readUTF();		
					
					JSONObject jsonObject = new JSONObject(receiveJson);
					String command = jsonObject.getString("command");
					
					switch(command) {
						case "incoming":
							this.chatName = jsonObject.getString("data");
							chatServer.sendToAll(this, "들어오셨습니다.");
							chatServer.addSocketClient(this);
							break;
						case "message":
							String message = jsonObject.getString("data");
							chatServer.sendToAll(this, message);
							break;
						case "checkLogin":
							String userId = jsonObject.getString("id");
							String userPassword= jsonObject.getString("password");
							chatServer.checkIdPass(this,userId, userPassword);
							break;
						case "registerMember":
							String regId = jsonObject.getString("regId");
							String regPw = jsonObject.getString("regPassword");
							chatServer.registerMember(this, regId, regPw);
							break;
						case "createRoom":
							String roomName = jsonObject.getString("roomName");
							chatName = jsonObject.getString("chatName");
							chatServer.addSocketClient(this);
							chatServer.createRoom(this,roomName);
							break;
						case "whisper":
							String target = jsonObject.getString("target");
							String whisperMessage = jsonObject.getString("data");
							chatServer.whisper(this, target, whisperMessage);
							break;
						case "fileUpload":
                            fileUpload(jsonObject);
                            stop = true;
                            break;
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
			 JSONObject jsonUpload = new JSONObject(); // 마지막에 보낼 JSON

			//폴더 유무 
			 String filePath = "/Users/kimyoungwook/Desktop/test"; // 끝단 
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
				
				OutputStream out = new FileOutputStream("/Users/kimyoungwook/Desktop/test/" + fileName);
				BufferedOutputStream bos = new BufferedOutputStream(out);
				bos.write(data);
				
			    // 이미지인 경우 오픈			
			   
//				if (fileName.contains("jpg")||fileName.contains("png")||fileName.contains("jpeg")) 
//		    	{Runtime.getRuntime().exec("/Users/kimyoungwook/Desktop/test/"+fileName);}
				
				bos.close();

				System.out.println(fileName + " " + "파일을 저장하였습니다..");
				System.out.println("저장 파일의 사이즈 : " + file.length());
				File file2 = new File("/Users/kimyoungwook/Desktop/test/" + fileName);
				if(fileName.contains("jpg")||fileName.contains("png")||fileName.contains("jpeg")) {
					 Desktop.getDesktop().open(file2);
				 	}


				// 바이트 데이터를 전송받으면서 크기 기록
				int len;
				byte[] data1 = new byte[DEFAULT_BUFFER_SIZE];
				while ((len = dis.read(data1)) != -1) {
					bos.write(data1, 0, len);
				}


			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			jsonUpload.put("statusCode", "0");
			jsonUpload.put("message", "파일수신 완료");
	        send(jsonUpload.toString());
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
}