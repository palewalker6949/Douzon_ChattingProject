

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;

public class SocketClient {
	//필드
	MainServer mainServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;	
	String chatName;
	RoomManager roomManager;
	//생성자
	public SocketClient(MainServer mainServer, Socket socket) {
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
	//메소드: JSON 받기
	public void receive() {
		mainServer.threadPool.execute(() -> {
			try {
				while(true) {
					CommandForServer();
				}
			} catch(IOException e) {
				exitAlarm();
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
	
	void CommandForServer() throws IOException
	{
		String receiveJson = dis.readUTF();		
		
		JSONObject jsonObject = new JSONObject(receiveJson);
		String command = jsonObject.getString("command");
		
		switch(command) {
			case "income":
				this.chatName = jsonObject.getString("data");
				roomManager.sendToAll(this, "들어오셨습니다.");
				roomManager.enterRoom(null);
				break;
			case "message":
				String message = jsonObject.getString("data");
				roomManager.sendToAll(this, message);
				break;
			case "checkLogin":
				String userId = jsonObject.getString("id");
				String userPassword= jsonObject.getString("password");
				mainServer.checkLogin(this, userId, userPassword);
				break;
			case "registerMember":
				String regId = jsonObject.getString("regId");
				String regPw = jsonObject.getString("regPassword");
				mainServer.registerMember(this, regId, regPw);
				break;
			case "passwordSearch":
		}
	}
	
	void exitAlarm()
	{
		roomManager.sendToAll(this, "나가셨습니다.");
		//roomManager.removeSocketClient(this);
	}
	public void enterRoom(RoomManager roomManager)
	{
		
	}

}