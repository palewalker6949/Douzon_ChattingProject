package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Scheduler implements Runnable
{
	public Scheduler()
	{
		
	}
	
	@Override
	public void run()
	{
		try
		{
			Class.forName(Env.getProperty("driverClass"));
			Connection connection =	DriverManager.getConnection(Env.getProperty("dbServerConn")
					, Env.getProperty("dbUser")
					, Env.getProperty("dbPasswd"));
		} catch (ClassNotFoundException | SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PreparedStatement preparedStatement = null;
		
	}
}
