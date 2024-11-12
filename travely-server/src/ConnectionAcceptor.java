import java.net.*;
import java.util.*;
public class ConnectionAcceptor extends Thread {
    private ServerSocket connOrder;
    private ArrayList<User> users;

    public ConnectionAcceptor
            (String port, ArrayList<User> users)
            throws Exception
    {
        if (port==null)
            throw new Exception ("Porta ausente");

        try
        {
            this.connOrder =
                    new ServerSocket (Integer.parseInt(port));
        }
        catch (Exception  erro)
        {
            throw new Exception ("Porta invalida");
        }

        if (users==null)
            throw new Exception ("Usuarios ausentes");

        this.users = users;
    }

    public void run ()
    {
        for(;;)
        {
            Socket connection=null;
            try
            {
                connection = this.connOrder.accept();
            }
            catch (Exception erro)
            {
                continue;
            }

            ConnectionSupervisor supervisoraDeConexao = null;
            try
            {
                supervisoraDeConexao =
                        new ConnectionSupervisor (connection, users);//, baralho);
            }
            catch (Exception erro)
            {} // sei que passei parametros corretos para o construtor
            supervisoraDeConexao.start();
        }
    }

}