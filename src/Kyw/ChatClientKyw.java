package Kyw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;





public class ChatClientKyw {
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;

	public void connect() throws IOException {
		socket = new Socket("localhost", EnvClient.getPort());
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[클라이언트] 서버에 연결됨");
		System.out.println();
	}
	
	public void send(String json) throws IOException {
		byte[] data = json.getBytes("UTF8");
	    System.out.println("길이 = " + data.length);


		dos.writeInt(data.length);// 문자열의 길이(4byte)
		dos.write(data);// 내용
		dos.flush();
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

			System.out.println("파일전송 완료");
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
	            
//	            jsonObject.put("statusCode", "0");
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
	            byte [] data = Base64.getDecoder().decode(root.getString("content").getBytes());

	           // File file = new File("/Users/kimyoungwook/Desktop/server/client/");
	            File file = new File(EnvClient.getWorkPath());
	            try {
	            	 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
	                bos.write(data);
	                bos.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            System.out.println("파일 다운로드 완료");
	        } else {
	            System.out.println(message);
	        }
	    }
//목록 조회 
	public List<String> getFileList( Scanner scanner ) {
		System.out.println("조회 하실 폴더 명을 입력하세요.");
		String folderName = scanner.next();
		String path = String.format(EnvClient.getStartPath()+ folderName); // 경로만들기

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
	

	public void disconnect() throws IOException {
		socket.close();
	}

	public static void main(String[] args) {
		try {
			ChatClientKyw chatClient = new ChatClientKyw();
			boolean stop = false;

			while (false == stop) {
				System.out.println();
				System.out.println("1. 로그인");
				System.out.println("2. 회원가입");
				System.out.println("3. 비밀번호검색");
				System.out.println("4. 파일업로드");
				System.out.println("5. 서버파일목록");
				System.out.println("6. 서버파일전송");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				switch (menuNum) {
				case "1":
//						chatClient.login(scanner);
					break;
				case "2":
//						chatClient.registerMember(scanner);
					break;
				case "3":
//						chatClient.passwdSearch(scanner);
					break;
				case "4":
					chatClient.fileUpload(scanner);
					break;
				case "5":
					chatClient.getFileList(scanner);
					break;
				case "6":
					chatClient.fileDownload(scanner);
					break;
				case "Q", "q":
					scanner.close();
					stop = true;
					System.out.println("프로그램 종료됨");
					break;
				}
			}

//				ChatClient chatClient = new ChatClient();
//				chatClient.connect();
//				System.out.println("대화명 입력: ");
//				chatClient.chatName = scanner.nextLine();
//				
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("command", "incoming");
//				jsonObject.put("data", chatClient.chatName);
//				String json = jsonObject.toString();
//				chatClient.send(json);
//				
//				chatClient.receive();			
//				
//				System.out.println("--------------------------------------------------");
//				System.out.println("보낼 메시지를 입력하고 Enter");
//				System.out.println("채팅를 종료하려면 q를 입력하고 Enter");
//				System.out.println("--------------------------------------------------");
//				while(true) {
//					String message = scanner.nextLine();
//					if(message.toLowerCase().equals("q")) {
//						break;
//					} else {
////						jsonObject = new JSONObject();
//						jsonObject.put("command", "message");
//						jsonObject.put("data", message);
//						chatClient.send(jsonObject.toString());
//					}
//				}
//				scanner.close();
//				chatClient.unconnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}
}
