package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Common.Member;

public class MainServer
{
	ServerSocket serverSocket;
	ExecutorService threadPool;
	Map<String, ClientSocket> allClients;
	MemberRepositoryFile memberRepositoryFile;
	MemberRepositoryDB memberRepositoryDB;
	RoomManager roomManager;
	Logger logger;
	
	public void start() throws IOException {
		System.out.println( "[서버] 시작됨");
		
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					Socket socket = serverSocket.accept();
					new ClientSocket(this, socket);
				}
			} catch(IOException e) {
			}
		});
		thread.start();
	}
	
	public MainServer()
	{
		threadPool = Executors.newFixedThreadPool(Env.getThreadPoolSize());
		memberRepositoryFile = new MemberRepositoryFile();
		memberRepositoryDB = new MemberRepositoryDB();
		roomManager = new RoomManager(this);
		logger = new Logger();
		try
		{
			serverSocket = new ServerSocket(Env.getPort());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			MainServer mainServer = new MainServer();
			mainServer.start();
			
			System.out.println("서버 실행");
			

			System.out.println("----------------------------------------------------");
			System.out.println("서버를 종료하려면 q를 입력하고 Enter.");
			System.out.println("----------------------------------------------------");
			
			Scanner scanner = new Scanner(System.in);
			while(true) {
				String key = scanner.nextLine();
				if(key.equals("q")) 	
					break;
			}
			scanner.close();
			mainServer.stop();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void stop() {
		try {
			serverSocket.close();
			logger.endLogger();
			threadPool.shutdownNow();
			allClients.values().stream().forEach(charRoom -> allClients.values().stream().forEach(sc -> sc.close()));
			System.out.println( "[서버] 종료됨 ");
		} catch (IOException e1) {}
	}
	
	public void addSocketClient(ClientSocket clientSocket) {
		String key = clientSocket.getKey();
		allClients.put(key, clientSocket);
		System.out.println("서버 입장 : " + key);
	}
	
	public void deleteClientSocket(ClientSocket clientSocket)
	{
		String key =  clientSocket.getKey();
		allClients.remove(key);
		System.out.println("서버 퇴장 : " + key);
	}
	
	
	public void insertMember(Member member)
	{
		memberRepositoryFile.insertMember(member);
		memberRepositoryDB.insertMember(member);
	}
	
	public void updateMember(Member member)
	{
		memberRepositoryFile.updateMember(member);
		memberRepositoryDB.updateMember(member);
	}
	
	public void deleteMember(String id)
	{
		memberRepositoryFile.deleteMember(id);
		memberRepositoryDB.deleteMember(id);
	}
	
	public Member getMemberById(String id)
	{
		return memberRepositoryDB.getMemberById(id);
	}
	
	public boolean isExistMember(String id)
	{
		return memberRepositoryDB.isExistMember(id);
	}
	
	public RoomManager getRoomManager()
	{
		return roomManager;
	}
	
	public Logger getLogger()
	{
		return logger;
	}
	
}
