package Kyw;


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
import java.util.Base64;
import java.util.Scanner;

import org.json.JSONObject;


class SocketClientKyw  {
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	ChatServerKyw chatServer;
    Socket socket;
    DataOutputStream dos;
    FileInputStream fis;
    BufferedInputStream bis;
    DataInputStream dis;
	FileOutputStream fos;
	BufferedOutputStream bos;
	String clientIp;
	String chatName;
	
    public SocketClientKyw(ChatServerKyw chatServer, Socket socket) {
    	  try {
    		this.chatServer = chatServer;
    		this.socket = socket;
       
        	dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.clientIp = isa.getHostName();			
			receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void receive() {
		chatServer.threadPool.execute(() -> {
			try {
				boolean stop = false;
				while(true != stop) {
				    int length = dis.readInt();
				    int pos = 0; 
				    byte [] data = new byte[length];
				    do {
				        int len = dis.read(data, pos, length - pos);
				        pos += len;
				    } while(length != pos);
				    
				    String receiveJson = new String(data, "UTF8");
					
					JSONObject jsonObject = new JSONObject(receiveJson);
					String command = jsonObject.getString("command");
					
					/*
					입장 : incoming,이름
					{command:incoming, data:'홍길동'}
					
					 채팅 : message,내용
					{command:message, data:'안녕'}
					
					*/
					switch(command) {
						case "login":
							login(jsonObject);
							stop = true;
							break;
                        case "passwdSearch":
                            passwdSearch(jsonObject);
                            stop = true;
                            break;
                        case "fileUpload":
                            fileUpload(jsonObject);
                            stop = true;
                            break;
						
//						case "incoming":
//							this.chatName = jsonObject.getString("data");
//							chatServer.sendToAll(this, "들어오셨습니다.");
//							chatServer.addSocketClient(this);
//							break;
//						case "message":
//							String message = jsonObject.getString("data");
//							chatServer.sendToAll(this, message);
//							break;
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
//				chatServer.sendToAll(this, "나가셨습니다.");
//				chatServer.removeSocketClient(this);
			}
		});
	}
    private void passwdSearch(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		
	}
	private void login(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		
	}
    //파일을 서버로 올리기 
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
		   
//			if (fileName.contains("jpg")||fileName.contains("png")||fileName.contains("jpeg")) 
//	    	{Runtime.getRuntime().exec("/Users/kimyoungwook/Desktop/test/"+fileName);}
			
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

    
    //send
    public void send(String file) {
         try {
             
             dos.writeUTF(file);
             dos.flush();
  
      
         }catch (IOException e){
             e.printStackTrace();
         }finally{ // 초기화
             try { dos.close(); } catch (IOException e) { e.printStackTrace(); }
             try { bis.close(); } catch (IOException e) { e.printStackTrace(); }
         }

    }
    
	public void close() {
		try { 
			socket.close();
		} catch(Exception e) {
			
		}   
	}
}
