package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String baseUrl = "https://southamerica-east1-travely-pi4.cloudfunctions.net";

            String authID = in.readLine();
            String placeID = in.readLine();
            String datetime = in.readLine();
            String amount = in.readLine();

            // Chamar função para consultar horários
            String jsonInputString = "{\"placeID\": \"" + placeID + "\", \"datetime\": \"" + datetime + "\"}";
            System.out.println(jsonInputString);
            String response = callFirebaseFunction(baseUrl + "/getPlaceSchedule", jsonInputString);
            PlaceSchedule availableTimesResponse =  processResponse(response);

            // Verificar se há disponibilidade antes de tentar reservar
            if (availableTimesResponse.getAvailability() >= Integer.parseInt(amount)) {
                // Usando String.format() para formatar a string
                jsonInputString = String.format("{\"authID\": \"%s\", \"schedule\": {\"amount\": \"%d\", \"placeID\": \"%s\", \"datetime\": \"%s\"}}",
                        authID, Integer.parseInt(amount), placeID, datetime);
                String bookingResponse = callFirebaseFunction(baseUrl + "/addReservation", jsonInputString);

                out.println(bookingResponse);
            } else {
                out.println("Horário não disponível para reserva.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String callFirebaseFunction(String urlStr, String jsonInputString) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // Enviar o JSON
            try (OutputStream os = connection.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                writer.write(jsonInputString);
                writer.flush();
            }

            // Ler a resposta
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            in.close();
            System.out.println("Response: " + response.toString());
        } catch(IOException e){
            e.printStackTrace();
            return "Erro ao chamar função Firebase";
        }
        return response.toString();
    }

    public PlaceSchedule processResponse(String jsonResponse) {
        Gson gson = new Gson();
        PlaceSchedule placeSchedule = gson.fromJson(jsonResponse, PlaceSchedule.class);

        System.out.println("ID: " + placeSchedule.getId());
        System.out.println("Availability: " + placeSchedule.getAvailability());
        System.out.println("Datetime: " + placeSchedule.getDatetime());
        System.out.println("Price: " + placeSchedule.getPrice());

        return placeSchedule;
    }
}
