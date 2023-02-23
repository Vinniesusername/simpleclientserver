import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;


public class Client  // this class deals with all socket requests to Server.java
{
    private static Client handler = null; // one object following singleton pattern.
    public SSLSocket cs;
    public BufferedReader in = null;
    public PrintWriter out = null;
    public SecureRandom sr = new SecureRandom();

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
            handler.cs.setEnabledProtocols(protocols);
            handler.cs.setEnabledCipherSuites(ciphers);
            //set input and output streams of socket
            handler.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(handler.cs.getOutputStream())));
            handler.in = new BufferedReader(new InputStreamReader(handler.cs.getInputStream()));


            // write to socket to test it
            int randomRequestId =  genRandom();

            handler.out.println("0;" + String.valueOf(randomRequestId) + ";null;null");
            handler.out.println();
            handler.out.flush();

            //read input from server to make sure the connection is working as intended
            String serverResponse = "";
            while(serverResponse != null &&serverResponse.equals(""))
                serverResponse = handler.in.readLine();
            String[] parts = serverResponse.split(";", 4);
            if(Integer.parseInt(parts[0]) == 0)
            {
                System.out.println("Connected to server");
            }




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

        if(!connected)
            status = -1;
        return status;

    }

    public int askForMagicNumber()
    {
        handler.out.flush();
        handler.out.println();
        int randomRequestId =  genRandom();
        String q = "1;" + String.valueOf(randomRequestId) + ";null;null;null";
        int r = 0;


        try
        {
            handler.out.println(q);
            handler.out.flush();
            String response = null;
            while (response == null)
                response = handler.in.readLine();

            String[] parts = response.split(";", 4);
            r += Integer.parseInt(parts[2]);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return r;
    }

    public int genRandom()
    {
        int n = sr.nextInt();
        n = n % 1000;
        return n;
    }


}
