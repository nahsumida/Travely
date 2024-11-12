import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
public class User {
    private Socket connection;
    private ObjectInputStream receiver;
    private ObjectOutputStream streamer;

    private Message proximoComunicado=null;

    private Semaphore mutex = new Semaphore (1,true);

    public User (Socket connection, ObjectInputStream  receiver, ObjectOutputStream streamer)
            throws Exception // se parametro nulos
    {
        if (this.connection == null)
            throw new Exception ("Conexao ausente");

        if (this.receiver ==null)
            throw new Exception ("Receptor ausente");

        if (this.streamer ==null)
            throw new Exception ("Transmissor ausente");

        this.connection = this.connection;
        this.receiver = this.receiver;
        this.streamer = this.streamer;
    }

    public void receive (Message x) throws Exception
    {
        try
        {
            this.streamer.writeObject (x);
            this.streamer.flush       ();
        }
        catch (IOException erro)
        {
            throw new Exception ("Erro de transmissao");
        }
    }

    public Message espie () throws Exception
    {
        try
        {
            this.mutEx.acquireUninterruptibly();
            if (this.proximoComunicado==null) this.proximoComunicado = (Message) this.receiver.readObject();
            this.mutEx.release();
            return this.proximoComunicado;
        }
        catch (Exception erro)
        {
            throw new Exception ("Erro de recepcao");
        }
    }

    public Message send () throws Exception
    {
        try
        {
            if (this.proximoComunicado==null) this.proximoComunicado = (Message) this.receiver.readObject();
            Message ret         = this.proximoComunicado;
            this.proximoComunicado = null;
            return ret;
        }
        catch (Exception erro)
        {
            throw new Exception ("Erro de recepcao");
        }
    }

    public void bye () throws Exception
    {
        try
        {
            this.streamer.close();
            this.receiver.close();
            this.connection.close();
        }
        catch (Exception erro)
        {
            throw new Exception ("Erro de desconexao");
        }
    }
}