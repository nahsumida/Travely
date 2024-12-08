package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BookingManagerServer {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6667)) {
            System.out.println("Servidor de reservas iniciado na porta 6666...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nova conexão aceita: " + clientSocket.getInetAddress().getHostAddress());

                    // Inicia uma nova thread para gerenciar a conexão com o cliente
                    new Thread(new ClientHandler(clientSocket)).start();
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
