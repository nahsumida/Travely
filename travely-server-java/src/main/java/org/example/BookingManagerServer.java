package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BookingManagerServer {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6666)) {
            System.out.println("Servidor de reservas iniciado na porta 6666...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
                System.out.println("oi");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
