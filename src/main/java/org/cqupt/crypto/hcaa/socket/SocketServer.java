package org.cqupt.crypto.hcaa.socket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.reflect.Method;
import org.json.JSONObject;

public class SocketServer extends Thread {

    private static final int PORT = 8082; // 可配置，避免与 HTTP 冲突
    private static ServerSocket serverSocket;
    private static Method handlerMethod;

    public static boolean init(Method handler) {
        try {
            serverSocket = new ServerSocket(PORT);
            handlerMethod = handler;
            SocketServer server = new SocketServer();
            server.start();
            System.out.println("Socket server started on port " + PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to start socket server: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket client = serverSocket.accept();
                handleClient(client);
            } catch (IOException e) {
                System.err.println("Error accepting client: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket client) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true)
        ) {
            String request = in.readLine();
            if (request == null) return;

            JSONObject jsonReq = new JSONObject(request);
            String operation = jsonReq.getString("operation");
            JSONObject params = jsonReq.getJSONObject("params");

            try {
                Object result = handlerMethod.invoke(null, operation, params); // 假设方法是静态的
                out.println("{\"result\": " + new JSONObject(result).toString() + "}");
            } catch (Exception e) {
                out.println("{\"error\": \"" + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
