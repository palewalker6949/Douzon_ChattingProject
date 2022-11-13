package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Common.Member;

public class MemberRepositoryDB  {
	// 필드
	Connection connection = null;
	PreparedStatement preparedStatement = null;
//	// map
//	List<Member> memberList = null;
//	Map<String, Member> memberMap = null;
	
	// DB 연결
	
	public MemberRepositoryDB()
	{
		try
		{
			Class.forName(Env.getProperty("driverClass"));
			System.out.println("드라이버 로딩 성공");
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void openDB() {
		try {
			connection = DriverManager.getConnection(Env.getProperty("dbServerConn")
					, Env.getProperty("dbUser")
					, Env.getProperty("dbPasswd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// DB 연결 끊기
	public void closeDB() {
		try {
			if (preparedStatement != null) {
				preparedStatement.close();
			} 
			
			if (connection != null) {
				connection.close();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// 회원 정보 등록
	public synchronized void insertMember(Member member) {
		try {
			openDB();
			
			preparedStatement = connection.prepareStatement(Env.getProperty("EXIST_MEMBER"));
			
			// uid 중복검사
			preparedStatement.setString(1, member.getUid());
			ResultSet resultSet = preparedStatement.executeQuery();
			int count = 0;
			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}
			resultSet.close();
			
			if (0 != count) {
				throw new Member.ExistMember("아이디 : [" + member.getUid() + "] 가 이미 존재합니다.");
			}
			preparedStatement.close();
			preparedStatement = connection.prepareStatement(Env.getProperty("INSERT_MEMBER"));
			
			// 동일한 uid가 없을 시 회원 정보 등록
			preparedStatement.setString(1, member.getUid());
			preparedStatement.setString(2, member.getPwd());
			preparedStatement.setString(3, member.getName());
			preparedStatement.setString(4, member.getSex());
			preparedStatement.setString(5, member.getAddress());
			preparedStatement.setString(6, member.getPhone());
			preparedStatement.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB();
		}
	}
	
	// 회원 정보 수정
	public synchronized void updateMember(Member member) {
		try {
			openDB();
			
			preparedStatement = connection.prepareStatement(Env.getProperty("UPDATE_MEMBER"));
			
			// 회원 정보 수정			
			preparedStatement.setString(1, member.getPwd());
			preparedStatement.setString(2, member.getName());
			preparedStatement.setString(3, member.getSex());
			preparedStatement.setString(4, member.getAddress());
			preparedStatement.setString(5, member.getPhone());
			preparedStatement.setString(6, member.getUid());
			
			if (-1 == preparedStatement.executeUpdate()) {
				throw new Member.NotExistUidPwd("아이디 : [" + member.getUid() + "] 를 찾을 수 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB();
		}
	}
	
	public Boolean isExistMember(String uid) {
		try {
			openDB();
			
			preparedStatement = connection.prepareStatement(Env.getProperty("EXIST_MEMBER"));
			
			// uid 중복검사
			preparedStatement.setString(1, uid);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			int count = 0;
			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}
			resultSet.close();
			
			if (0 == count) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB();
		}
		return true;
	}
	
	// 회원 조회
	public Member getMemberById(String uid) {
		try {
			openDB();
			
			preparedStatement = connection.prepareStatement(Env.getProperty("findByUidMember"));
			
			// uid 중복검사
			preparedStatement.setString(1, uid);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) {
				Member member = new Member();
				member.setUid(resultSet.getString("USERID"));
				member.setPwd(resultSet.getString("PWD"));
				member.setName(resultSet.getString("NAME"));
				member.setSex(resultSet.getString("SEX"));
				member.setAddress(resultSet.getString("ADDRESS"));
				member.setPhone(resultSet.getString("PHONE"));
				resultSet.close();
				return member;
			} else {
				throw new Member.NotExistUidPwd("아이디 : [" + uid + "] 를 찾을 수 없습니다.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB();
		} return null;
	}
	
	// 회원 정보 삭제
	public synchronized void deleteMember(String uid) {
		try {
			openDB();
			
			preparedStatement = connection.prepareStatement(Env.getProperty("DELETE_MEMBER"));
			
			preparedStatement.setString(1, uid);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			resultSet.close();
			
			preparedStatement.close();
									
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB();
		}
	}		
}