import java.util.*;

public class ReservationManager {
    public static String DEFAULT_PORT = "3000";

    public static void main (String[] args)
    {
        if (args.length>1)
        {
            System.err.println ("Uso esperado: java Servidor [PORTA]\n");
            return;
        }

        String port = ReservationManager.DEFAULT_PORT;

        if (args.length==1)
            port = args[0];

        ArrayList<User> users =
                new ArrayList<>();

        ConnectionAcceptor aceitadoraDeConexao=null;
        try
        {
            aceitadoraDeConexao = new ConnectionAcceptor (port, users);
            aceitadoraDeConexao.start();
        }
        catch (Exception erro)
        {
            System.err.println ("Escolha uma porta apropriada e liberada para uso!\n");
            return;
        }


        for(;;) {
            System.out.println("O servidor esta ativo! Para desativa-lo,");

            //precisa codar algo pra fazer parar aqui
        }
    }
}