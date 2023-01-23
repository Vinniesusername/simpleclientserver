import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client  // this class deals with all socket requests to Server.java
{
    private static Client handler = null; // one object following singleton pattern.
    public SSLSocketFactory sslf = (SSLSocketFactory) SSLSocketFactory.getDefault();
    public Socket cs;
    public BufferedReader in = null;
    public PrintWriter out = null;

    private Client(){} //private for singleton

    public static Client getInstance()
    {
        if(handler == null)
            handler = new Client();
        return handler;
    }

    public static void main(String[] args)
    {
        Client nc = Client.getInstance();
        try
        {
            handler.cs = handler.sslf.createSocket("localhost", 4422);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
