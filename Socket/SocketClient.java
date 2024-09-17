import java.io.*;
import java.net.*;

public class SocketClient {
    private static int port = 8080;
    private static String add = "127.0.0.1";
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(add, port)) {
            System.out.println("Connected to server");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String serverResponse;
            while ((serverResponse = reader.readLine()) != null) {
                System.out.println(serverResponse);
            }

        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

