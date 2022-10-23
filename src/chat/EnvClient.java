package chat;
import java.io.IOException;
import java.util.Properties;

public class EnvClient {
    static Properties properties = new Properties();
    static {
        try {
            properties.load(EnvClient.class.getResourceAsStream("client.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static int getPort() {
        return Integer.parseInt(properties.getProperty("server.port", "50001"));
    }
    
    public static String getWorkPath() {
        return properties.getProperty("work.path");
    }
    public static String getStartPath() {
        return properties.getProperty("Start.path");
    }

    public static void main(String [] args) {
        System.out.println(getWorkPath());
    }
}