import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class serverSocket {
    private static int port = 8080;
    public static void main(String arg[]){
        
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server listening Port" + port);
            while (true) {
                System.out.println("Client Connecting to the Server...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected With IP Address : " + clientSocket.getInetAddress().getHostAddress());
                ClientManage clientm = new ClientManage(clientSocket);
                new Thread(clientm).start(); 
            }
        } catch (IOException e) {
            System.out.println("Exception throw" + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ClientManage implements Runnable{
        
        private Socket clientSocket;

        public ClientManage(Socket clientm){
            this.clientSocket = clientm;
        }

        @Override
        public void run() {
            try(OutputStream out = clientSocket.getOutputStream(); PrintWriter writer = new PrintWriter(out,true)){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                while (true) {
                    String currentTime = formatter.format(new Date());
                    writer.println("Current date and time: " + currentTime);
                    Thread.sleep(1000);
                }
            }catch (IOException | InterruptedException ex) {
                System.out.println("Client disconnected: " + ex.getMessage());
            }
            finally {
                try {
                    System.out.println("Closing the client socket at IP address: " + clientSocket.getInetAddress().getHostAddress());
                    clientSocket.close();
                } catch (IOException ex) {
                    System.out.println("Failed to close client socket: " + ex.getMessage());
                }
        }

    }
    }