import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class BookingManagerClient {
    private static final String SERVER_IP = "localhost"; // IP do servidor
    private static final int SERVER_PORT = 6667; // Porta do servidor

    public static void main(String[] args)  {
        String authID = "grAvCnSRzOOSi814teqiBo7F00C2"; // Exemplo de authID
        String placeID = "BDxWmMnb099UedcmlzeW"; // Exemplo de placeID
        String datetime = "2024-11-12T09:00:00Z"; // Exemplo de datetime
        Integer amount = 12; // Exemplo de amount
/*
        Digite o authID: kAit3w3dYoUET23fIZQAp0byy9g1
        Digite o placeID: BDxWmMnb099UedcmlzeWgrAvCnSRzOOSi814teqiBo7F00/C2BDxWmMnb099UedcmlzeW&&&

        Digite a data e hora (formato: yyyy-MM-ddTHH:mm:ssZ):  2024-11-13T09:00:00Z
        Digite o amount: 5
        Resposta do servidor: null*/

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envia os dados para o servidor
            out.println(authID);
            out.println(placeID);
            out.println(datetime);
            out.println(amount);
            out.println("COMPRA");

            // Lê a resposta do servidor
            String status = in.readLine();
            System.out.println(status);
            String response = in.readLine();
            System.out.println("Resposta do servidor: " + response);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao conectar ao servidor.");
        }
    }
}