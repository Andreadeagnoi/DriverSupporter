package tesideagnoi.dei.unipd.it.driversupporter;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Invia un messaggio UDP al server.
 * Il messaggio contiene un identificativo del telefono, ad esempio il MAC ADDRESS, e dati riguardanti
 * la posizione, la velocità, l'accelerazione e i giri motore.
 * Created by Andrea on 04/08/2015.
 */
public class UDPClient {
    private static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 0;
    private AsyncTask<Void, Void, Void> async_client;
    private InetAddress serverAddress ;

    public UDPClient(){
        setServer();
    }

    /**
     * Imposta il server default a cui mandare i messaggi UDP.
     */
    public void setServer(){
        try {
            serverAddress = InetAddress.getByName(DEFAULT_SERVER_ADDRESS);
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    /**
     * Imposta il server a cui mandare i messaggi UDP. Da utilizzare solo se diverso dal server default.
     * @param serverAddressString
     */
    public void setServer(String serverAddressString){
        try {
            serverAddress = InetAddress.getByName(serverAddressString);
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String messageData)
    {
        final String message = messageData;
        async_client = new AsyncTask<Void, Void, Void>()

        {
            @Override
            protected Void doInBackground(Void... params)
            {
                DatagramSocket socket = null;

                try
                {
                    socket = new DatagramSocket();
                    DatagramPacket packet;
                    packet = new DatagramPacket(message.getBytes(), message.length(), serverAddress, SERVER_PORT);
                    socket.setBroadcast(true);
                    socket.send(packet);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (socket != null)
                    {
                        socket.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
            }
        };

        async_client.execute();
    }
}
