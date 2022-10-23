package Kyw;

import java.io.IOException;
import java.util.Properties;

public class EnvServer {
    static Properties properties = new Properties();
    static {
        try {
            properties.load(EnvServer.class.getResourceAsStream("server.properties"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static int getPort() {
        return Integer.parseInt(properties.getProperty("server.port", "50001"));
    }
    
    public static String getMemberFileName() {
        return properties.getProperty("member.file.name");
    }
    
    public static int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("thread.poll.size", "100"));
    }

    public static String getWorkPath() {
        return properties.getProperty("work.path");
    }

}
