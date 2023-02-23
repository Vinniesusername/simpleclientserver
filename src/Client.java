import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;


public class Client  // this class deals with all socket requests to Server.java
{
    private static Client handler = null; // one object following singleton pattern.
    public SSLSocket cs;
    public BufferedReader in = null;
    public PrintWriter out = null;

    public String ksPath = "D:\\SPS\\keystore\\spsclient";

    final String keypass = "testkey"; //hard coded to save time obv dont do this for real software
    final String kspass = "testclient";
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
            System.out.println(handler.askForMagicNumber());
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
            //key store/manager
            KeyManagerFactory keyfact = KeyManagerFactory.getInstance("PKIX");
            KeyStore ks = KeyStore.getInstance("pkcs12");
            ks.load(new FileInputStream(ksPath), kspass.toCharArray());
            keyfact.init(ks, keypass.toCharArray());
            //trust store
            TrustManagerFactory trstfact = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trstfact.init(ks);

            //socket
            SSLContext context = SSLContext.getInstance(protocols[0]);
            context.init(keyfact.getKeyManagers(), trstfact.getTrustManagers(), null);
            SSLSocketFactory sockfact = context.getSocketFactory();


            handler.cs = (SSLSocket) sockfact.createSocket("localhost", 4422);
            //enable only the protocols and ciphers that i want used
            //handler.cs.setEnabledProtocols(protocols);
            //handler.cs.setEnabledCipherSuites(ciphers);
            //set input and output streams of socket
            handler.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(handler.cs.getOutputStream())));
            handler.in = new BufferedReader(new InputStreamReader(handler.cs.getInputStream()));


            // write to socket to test it
            handler.out.println("TESTC");
            handler.out.println();
            handler.out.flush();


            //check for error
            if(handler.out.checkError())
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
        //test code remove later
        String[] temp = handler.cs.getEnabledCipherSuites();
        String[] temp2 = handler.cs.getEnabledProtocols();
        for(int i =0; i < temp.length; i++)
            System.out.println(temp[i]);
        for(int i =0; i < temp2.length; i++)
            System.out.println(temp2[i]);

        //end test code
        handler.out.flush();
        handler.out.println();
        String q = "58;null;null;null;null";
        int r = 0;
        handler.out.println(q);

        try
        {
            String temp0 =  handler.in.readLine();
            if(temp0 != null) {
                String[] parts = temp0.split(";", 5);
                r += Integer.parseInt(parts[0]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return r;
    }


}
