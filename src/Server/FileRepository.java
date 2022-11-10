package Server;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mariadb.jdbc.MariaDbBlob;



public class FileRepository {
	Connection conn = null;
	PreparedStatement pstmt = null;
	
	public FileRepository() {
		try {
			Class.forName(Env.getProperty("driverClass"));
			System.out.println("JDBC 드라이버 로딩 성공");
			
			conn = DriverManager.getConnection(Env.getProperty("dbServerConn")
					, Env.getProperty("dbUser")
					, Env.getProperty("dbPasswd"));
			System.out.println("DB 서버에 연결됨");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public synchronized void UploadFile(JSONObject jsonObject) {
			try {
				
				PreparedStatement pstmt = conn.prepareStatement(Env.getProperty("INSERT_FILEmaria"));
				LocalDateTime currentDate = LocalDateTime.now();
	
				pstmt.setString(1, "uid");
				pstmt.setTimestamp(2, Timestamp.valueOf(currentDate));
				pstmt.setString(3, jsonObject.getString("fileName"));
				pstmt.setString(4, jsonObject.getString("filePath"));
				pstmt.executeUpdate();
				
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
	
			}
		}
	
	public synchronized String getFileLocation(int filenumber) {
		String filelocation = "";
		try {
			PreparedStatement pstmt = conn.prepareStatement(Env.getProperty("SELECT_LOCATION"));
			
			pstmt.setInt(1, filenumber);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				 filelocation = rs.getString("filelocation");
			}
		} catch (Exception e) {
			
		} 
		 return filelocation;
	}
	
	
	public synchronized JSONArray showFilelist() {
		JSONArray data = new JSONArray();
		try {
			
			Statement stmt = conn.createStatement();
			
			//ResultSet rs = stmt.executeQuery(Env.getProperty("SELECT_FILE"));
			ResultSet rs = stmt.executeQuery("select filename, filenumber from FILEDATA");
			
			while(rs.next()) {
//				System.out.println(rs.getString(1));
//				System.out.println(rs.getInt(2));
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("filenumber", rs.getInt("filenumber"));
				jsonObject.put("filename", rs.getString("filename"));
		
				data.put(jsonObject);
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		return data;
	}
	

}
