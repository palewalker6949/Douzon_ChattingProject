package Server;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import Common.Member;
import lombok.Data;

enum CommandType {
	NORMAL_CMD,
	EXIT_CMD
}

@Data
class Message {
	private CommandType commandType;
	private String message;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private Date createDate;
	private String userName;
	
	private Message(CommandType commandType, JSONObject log) {
		this.commandType = commandType;
		this.message = log.getString("message");
		this.createDate = new Date(Calendar.getInstance().getTime().getTime());
		this.userName = log.getString("uid");
	}
	
	static Message of(JSONObject log) {
		return new Message(CommandType.NORMAL_CMD, log);
	}
	
	static Message exitMessage() {
		return new Message(CommandType.EXIT_CMD, null);
	}
	
	public String getMessageStr() {
		return "[" + sdf.format(createDate) + "] : " + "[" + userName+"]" + message;
	}
	
	public Date getCreateDate() {
		return createDate;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getName()
	{
		return userName;
	}
}

//Consumer ( 소비자 )
public class Logger implements Runnable {
  private LinkedBlockingQueue<Message> queue;
  private PrintStream out;
  private boolean dbWrite = false;
  private Connection conn = null;
  private CallableStatement cstmt = null;
  
  public Logger() {
      try {
    	  this.queue = new LinkedBlockingQueue<Message>();
    	  this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream("c:\\temp\\chatServer\\log.txt")));
          this.dbWrite = Boolean.parseBoolean(Env.getProperty("logger.DBWrite", "false"));
          
          open();
          cstmt = conn.prepareCall(Env.getProperty("INSERT_LOG"));
      } catch (Exception e) {
    	  e.printStackTrace();
      }

      new Thread(this).start();
  }
  
  @Override
  public void run() {
      try {
          while(!Thread.currentThread().isInterrupted()) {
        	  Message msg = queue.take();
        	  if (msg.getCommandType() == CommandType.EXIT_CMD) {
        		  break;
        	  }
              //System.out.println(name + " : " + msg);
        	  System.out.println(msg.getMessageStr());
        	  out.println(msg.getMessageStr());
        	  out.print("1");
        	  System.out.println("dbwrite: " + this.dbWrite);
        	  if (this.dbWrite) {
	        	  //DB에 기록
	        	  writeLogDB(msg);
        	  }
        	  try {
        	  }catch (Exception ex) {
        		  
        	  }
        	  
          }
          
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
    	  out.close();
    	  close();
      }
  }
  
  public void write(JSONObject log) {
	  queue.offer(Message.of(log));
  }
  
  public void endLogger() {
	queue.offer(Message.exitMessage());
  }
  

	private void open() {
		try {
			Class.forName(Env.getProperty("driverClass"));
			
			System.out.println("JDBC 드라이버 로딩 성공");
			
			conn = DriverManager.getConnection(Env.getProperty("dbServerConn")
					, Env.getProperty("dbUser")
					, Env.getProperty("dbPasswd"));
			conn.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void close() {
		try {
			if (cstmt != null) {
				cstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private void writeLogDB(Message message) {
		try {
			//로그 정보 설정
			System.out.println("db 인서트 실행");
			cstmt.setString(1, message.getUserName());
			cstmt.setDate(2, message.getCreateDate());
			cstmt.setString(3, message.getMessage());
			cstmt.execute();
			conn.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}	  
	}
}
