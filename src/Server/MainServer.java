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


public class MainServer
{
	ServerSocket serverSocket;
	ExecutorService threadPool;
	FileRepository fileRepository;
	Map<String,Map<String, ClientSocket>> chatRooms;
	Map<String, ClientSocket> allClients;
	MemberRepository memberRepository;
	RoomManager roomManager;
	
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
		chatRooms = Collections.synchronizedMap(new HashMap<>());
		//memberRepositoryFile = new MemberRepositoryFile();
		//logger = new Logger()
		fileRepository = new FileRepository();
		roomManager = new RoomManager(this);
		try
		{
			serverSocket = new ServerSocket(Env.getPort());
            Class<?> cls = Class.forName(Env.getProperty("MemberRepository"));
            memberRepository = (MemberRepository) cls.getDeclaredConstructor().newInstance();
		} catch (Exception e)
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
			threadPool.shutdownNow();
			chatRooms.values().stream().forEach(charRoom -> allClients.values().stream().forEach(sc -> sc.close()));
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
		System.out.println("서버 퇴장 : " + key);
	}
	
	public MemberRepository getMemberRepository()
	{
		return memberRepository;
	}
	
	public RoomManager getRoomManager()
	{
		return roomManager;
	}
	
	public FileRepository getFileRepository() 
	{
		return fileRepository;
	}
	
}
