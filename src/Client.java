import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;



public class Client  // this class deals with all socket requests to Server.java
{
    private static Client handler = null; // one object following singleton pattern.
    public SSLSocketFactory sslf = (SSLSocketFactory) SSLSocketFactory.getDefault();
    public SSLSocket cs;
    public BufferedReader in = null;
    public PrintWriter out = null;
    private static final String[] protocols = new String[]{"TLSv1.3"};

    private static final String[] ciphers = new String[]{"TLS_AES_256_GCM_SHA384", "TLS_AES_128_GCM_SHA256"};

    private Client() {} //private for singleton

    public static Client getInstance()
    {
        if(handler == null)
            handler = new Client();
        return handler;
    }

    public static void main(String[] args)
    {
        Client.getInstance();
        try
        {
            handler.connect();
            //System.out.println(handler.askForMagicNumber());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public int connect() throws IOException // connects handler to server returns a status code
    {
        if(handler.cs != null)
        {
            try
            {
                handler.cs.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        int status = -1;
        boolean connected = false;
        try
        {
            handler.cs = (SSLSocket)handler.sslf.createSocket("localhost", 4422);
            handler.cs.setEnabledProtocols(protocols);
            //enable only the protocols and ciphers that i want used
            handler.cs.setEnabledProtocols(protocols);
            handler.cs.setEnabledCipherSuites(ciphers);
            //set input and output streams of socket
            handler.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(handler.cs.getOutputStream())));
            handler.in = new BufferedReader(new InputStreamReader(handler.cs.getInputStream()));
            System.out.println(handler.out.checkError());

            // write to socket to test it
            handler.out.println("TESTC");
            handler.out.println();
            handler.out.flush();


            //check for error
            if(handler.out.checkError() == false)
            {
                throw new Exception("java.io.printwriter error");
            }
            //set up input stream

            status = 0;
            connected = true;


        }
        catch (Exception e)
        {
            e.printStackTrace();
            //if any exception is thrown then make sure the socket is clear
            int s = disconnect();
            if(s != 0)
                System.out.println("disconnect issuse");
            status = -2;
        }
        finally
        {
            handler.cs.close();
            connected = false;

        }

        if(!connected)
            status = -1;
        return status;

    }

    public int askForMagicNumber()
    {
        handler.out.flush();
        handler.out.println();
        String q = "58;null;null;null;null";
        int r = 0;
        handler.out.println(q);

        try
        {
            String temp =  handler.in.readLine();
            String[] parts = temp.split(";", 5);
            r += Integer.parseInt(parts[0]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return r;
    }

    public void listenToServer()
    {
        try
        {
            handler.cs = (SSLSocket) handler.sslf.createSocket("localhost", 4422);
            handler.cs.setEnabledProtocols(protocols);
            //enable only the protocols and ciphers that i want used
            handler.cs.setEnabledProtocols(protocols);
            handler.cs.setEnabledCipherSuites(ciphers);
            //set input and output streams of socket
            handler.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(handler.cs.getOutputStream())));
            handler.in = new BufferedReader(new InputStreamReader(handler.cs.getInputStream()));
            System.out.println(handler.out.checkError());

            while(true)
            {
                while(handler.in.ready())
                {
                    String q = handler.in.readLine();
                    System.out.println(q);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



    }


    public int disconnect()

    {
        int status = 0;
        if(handler.cs == null)
            return status;
        try
        {
            handler.cs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            status = -1;
        }
        return status;
    }



}
