import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingManagerClientKT{

    companion object {
        private const val SERVER_IP = "localhost" // IP do servidor
        private const val SERVER_PORT = 6666 // Porta do servidor
    }

    // Função para enviar dados de reserva para o servidor
    suspend fun sendBookingRequest(authID: String, placeID: String, datetime: String, amount: Int): String {
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

                            // Lê a resposta do servidor
                            input.readLine() ?: "Erro: resposta vazia do servidor"
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
