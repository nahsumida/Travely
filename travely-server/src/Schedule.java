import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Schedule {

    private static final String REQUEST_URL = "https://southamerica-east1-travely-pi4.cloudfunctions.net/getPlaceSchedule";
    private static final String CONTENT_TYPE = "application/json";

    public void getSchedule(String placeID, String datetime) throws Exception {
        // URL da requisição
        URL url = new URL(REQUEST_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configuração do método e cabeçalhos
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setDoOutput(true);

        String jsonInputString = "{"
                + "\"placeID\":" + placeID
                + "\"datetime\":" + datetime
                + "}";

        // Enviando a requisição
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verificando a resposta
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Aqui você pode adicionar código para ler a resposta, se necessário
    }
}
