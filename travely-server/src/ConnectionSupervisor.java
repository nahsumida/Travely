import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionSupervisor extends Thread{
    private User            user;
    private Socket              connection;
    private ArrayList<User> users;
    private static int qtdJogadores	=0;

    public ConnectionSupervisor
            (Socket connection, ArrayList<User> users)
            throws Exception
    {
        if (connection==null)
            throw new Exception ("Conexao ausente");

        if (users==null)
            throw new Exception ("Usuarios ausentes");

        this.connection  = connection;
        this.users = users;
    }

    public void run ()
    {
        ObjectOutputStream streamer;
        try
        {
            streamer =
                    new ObjectOutputStream(
                            this.connection.getOutputStream());
        }
        catch (Exception erro)
        {
            return;
        }

        ObjectInputStream receiver = null;
        try
        {
            receiver =
                    new ObjectInputStream(
                            this.connection.getInputStream());
        }
        catch (Exception err0)
        {
            try
            {
                streamer.close();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread

            return;
        }

       try
        {
            this.user =
                    new User (this.connection,
                            receiver,
                            streamer);
        }
        catch (Exception erro)
        {} // sei que passei os parametros corretos

        try
        {
            synchronized (this.users)
            {
                this.users.add (this.user);


                this.qtdJogadores++;
                if(this.qtdJogadores == 3)
					for(User usuario: this.users)
					{
						usuario.receive(new ConnectionStartMessage(true));
					}
					else if(this.qtdJogadores > 3)
					{
						this.user.receive(new DisconnectionMessage());
					}


            }

            UserCountMessage qtdJogadores = new UserCountMessage();

            for(;users.size()<3;)
            {
                this.user.receive(qtdJogadores);
            }

            for(;;)
            {
                Message comunicado = this.user.send ();

                if (comunicado==null)
                    return;

                else if (comunicado instanceof BookingRequest)
                {
                    //implementar aqui o pedido de reserva
                   // this.user.reserve();
                    Schedule schedule = new Schedule();

                   var placeId = "";
                   var datetime = "";
                    try {
                        schedule.getSchedule(placeId, datetime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                else if (comunicado instanceof PaymentRequest)
                {
                    //implementar aqui o pedido de pagamento
                    // this.user.reserve();
                }
                else if (comunicado instanceof DisconnectionRequest)
                {
                    synchronized (this.user)
                    {
                        this.users.remove (this.user);
                    }
                    this.user.bye();
                }
            }
        }
        catch (Exception erro)
        {
            try
            {
                streamer.close ();
                receiver   .close ();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread
        }
    }

}