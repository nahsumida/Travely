import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.isabellatressino.travely.models.BookingResponse

class BookingManagerClientKT{

    companion object {
        private const val SERVER_IP = "10.0.2.2"//192.168.10.37" // IP do servidor
        private const val SERVER_PORT = 6667 // Porta do servidor
    }

    // Função para enviar dados de reserva para o servidor
    suspend fun sendBookingRequest(authID: String, placeID: String, datetime: String, amount: Int, type: String): String  {
        return withContext(Dispatchers.IO) { // Garante execução em uma thread de IO
            try {
                // Estabelece conexão com o servidor
                Socket(SERVER_IP, SERVER_PORT).use { socket ->
                    PrintWriter(OutputStreamWriter(socket.getOutputStream()), true).use { out ->
                        BufferedReader(InputStreamReader(socket.getInputStream())).use { input ->

                            // Envia os dados para o servidor
                            out.println(authID)
                            out.println(placeID)
                            out.println(datetime)
                            out.println(amount.toString())
                            out.println(type)

                            // Lê a resposta do servidor como JSON
                            var responseJson: String? = null
                            try {
                                responseJson = input.readLine()
                            } catch (e: Exception) {
                                if (responseJson.isNullOrEmpty()) {
                                    throw e
                                }
                            }

                            if (responseJson.isNullOrEmpty()) {
                                return@withContext "Erro: resposta vazia do servidor"
                            }
                            val gson = Gson()
                            val response = gson.fromJson(responseJson, BookingResponse::class.java)

                            if (response.status == "SUCESSO") {
                                "Reserva bem-sucedida: ${response.message}"
                            } else {
                                "Erro: ${response.message}"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Erro ao conectar ao servidor: ${e.message}"
            }
        }
    }
}