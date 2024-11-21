import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Teclado {

    private BufferedReader reader;

    public Teclado() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public String lerString(String mensagem) {
        System.out.print(mensagem);
        try {
            return reader.readLine();
        } catch (Exception e) {
            System.out.println("Erro ao ler a entrada.");
            return null;
        }
    }

    public Integer lerInteger(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                String input = reader.readLine();
                return Integer.parseInt(input);
            } catch (Exception e) {
                System.out.println("Por favor, insira um número válido.");
            }
        }
    }

    public String lerDataHora(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                String input = reader.readLine();
                // Você pode adicionar validações de formato de data aqui se necessário
                return input;
            } catch (Exception e) {
                System.out.println("Erro ao ler a data/hora.");
            }
        }
    }
}
