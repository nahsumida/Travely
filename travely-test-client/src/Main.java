import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {

    private static final String SERVER_IP = "localhost"; // IP do servidor
    private static final int SERVER_PORT = 6666; // Porta do servidor

    public static void main(String[] args)  {


        //String authID = "1iDKDVNRXFNQ6liMkLZYaA3Tl1h1"; // Exemplo de authID
        //String placeID = "BDxWmMnb099UedcmlzeW"; // Exemplo de placeID
        //String datetime = "2024-11-12T09:00:00Z"; // Exemplo de datetime
        //Integer amount = 0; // Exemplo de amount

        // Instanciando a classe Teclado para ler os inputs do usuário
        Teclado teclado = new Teclado();

        // Lê os dados do usuário
        String authID = teclado.lerString("Digite o authID: ");
        String placeID = teclado.lerString("Digite o placeID: ");
        String datetime = teclado.lerDataHora("Digite a data e hora (formato: yyyy-MM-ddTHH:mm:ssZ): ");
        Integer amount = teclado.lerInteger("Digite o amount: ");

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envia os dados para o servidor
            out.println(authID);
            out.println(placeID);
            out.println(datetime);
            out.println(amount);

            // Lê a resposta do servidor
            String status = in.readLine();
            String id = in.readLine();
            String date = in.readLine();
            String price = in.readLine();
            System.out.println(status);
            System.out.println(id);
            System.out.println(date);
            System.out.println(price);

          /*  String response = in.readLine();
            System.out.println("Resposta do servidor: " + response);*/

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao conectar ao servidor.");
        }
    }
}
