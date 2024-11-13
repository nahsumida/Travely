import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {

    private static final String SERVER_IP = "localhost"; // IP do servidor
    private static final int SERVER_PORT = 6666; // Porta do servidor

    public static void main(String[] args)  {
        String authID = "grAvCnSRzOOSi814teqiBo7F00C2"; // Exemplo de authID
        String placeID = "BDxWmMnb099UedcmlzeW"; // Exemplo de placeID
        String datetime = "2024-11-12T09:00:00Z"; // Exemplo de datetime
        Integer amount = 1; // Exemplo de amount

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envia os dados para o servidor
            out.println(authID);
            out.println(placeID);
            out.println(datetime);
            out.println(amount);

            // Lê a resposta do servidor
            String response = in.readLine();
            System.out.println("Resposta do servidor: " + response);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao conectar ao servidor.");
        }
    }
}
