import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MulticastClient implements Runnable
{
    private final String INET_ADDR;
    private final int PORT;
    private InetAddress inetAddress = null;
    MulticastSocket multicastSocket = null;
    private static HashMap<Integer, String> hashMap; // hashmap contenente tutti i messaggi ricevuti dal server

    public MulticastClient(String inet_addr, int port)
    {
        this.INET_ADDR = inet_addr;
        this.PORT = port;
        hashMap = new HashMap<>();
    }

    public void run()
    {
        try {inetAddress = InetAddress.getByName(INET_ADDR);} // prendo l'indirizzo
        catch(Exception ex) {ex.printStackTrace();}

        // RICEZIONE PACCHETTI UDP

        try
        {
            multicastSocket = new MulticastSocket(PORT);

            byte[] buf = new byte[512]; // buffer per la ricezione informazioni dal server

            multicastSocket.joinGroup(inetAddress); // crea un nuovo socket multicast

            System.out.println("\n\n[ Connesso al gruppo multicast " + inetAddress.getHostAddress() + ":"+multicastSocket.getLocalPort() + " ]");

            String in;
            String user;

            while(true) // ciclo per la ricezione dei pacchetti
            {

                // RICEZIONE E SALVATAGGIO

                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);

                try {multicastSocket.receive(datagramPacket);} // ricezione
                catch (SocketTimeoutException ex)
                {
                    ex.printStackTrace();
                    System.exit(1);
                } catch(SocketException e) {break;}

                in = new String(buf, 0, buf.length).replace("/n", "\n").trim();
                user = in.substring(0, in.indexOf("\n"));
                hashMap.put(user.hashCode(), in);  // salvataggio nel hashmap
            }
        } catch (Exception ex) {ex.printStackTrace();}

        finally
        {
            if (multicastSocket != null) multicastSocket.close(); // chiusura socket
        }
    }

    // METODO PER TRASFERIRE I MESSAGGI IN UNA ARRAYLIST

    public ArrayList<String> prendiMessaggi()
    {
        ArrayList<String> list = new ArrayList<>();

        for (HashMap.Entry<Integer, String> set : hashMap.entrySet()) list.add(set.getValue());

        return list;
    }

    // METODO PER TERMINAZIONE

    public void stop(){multicastSocket.close();}

}