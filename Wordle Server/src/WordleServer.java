import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordleServer
{
    private static SelezionaParola selezionaParola;
    private static int tempo_parola;
    private static ServerSocket serverSocket;
    private static int porta;
    private static ExecutorService executorService;
    private static int nthread;
    private static int porta_multicast;
    private static InetAddress multicast_inet;
    private static final String jsonFile = "utenti.json";
    private static final ConcurrentHashMap<Integer, Giocatore> concurrentHashMap = new ConcurrentHashMap<>(); // hashmap condivisa tra i thread ServerTask contenente le informazioni degli account salvati
    private static final HashSet<Integer> hashSet = new HashSet<>();   // hasSet contenente gli hashCode degli account loggati

    public static void main(String[] args)
    {
        try
        {

            // CONFIGURAZIONE INIZIALE

            config(); // lettura parametri di configurazione

            leggi_json(); // carica nella HashMap tutti gli account registrati sul file

            // AVVIO THREAD PER SELEZIONARE LA PAROLA

            selezionaParola = new SelezionaParola("words.txt", tempo_parola);
            Thread thread = new Thread (selezionaParola);
            thread.start();

            // CREAZIONE THREADPOOL E SOCKET

            executorService = Executors.newFixedThreadPool(nthread);
            serverSocket = new ServerSocket(porta);

            System.out.print("\n\n[ In ascolto sulla porta: " + porta + " ]\n");
            
            while (true) 
            {
                Socket socket;
                
                try {socket = serverSocket.accept();} // accetta le connessioni dei client
                catch(SocketException e){break;}

                executorService.execute(new ServerTask(socket, concurrentHashMap, hashSet)); // esecuzione dei ServerTask
            }

        } catch(Exception e) {e.printStackTrace();}
        
        finally 
        {
            selezionaParola.stop(); // termina il thread SelezionaParola

            // CHIUSURA SERVERSOCKET E THREADPOOL
            try 
            {
                if(serverSocket != null) {serverSocket.close();}
                if(executorService != null) {executorService.shutdownNow();}
            } catch(Exception e) {e.printStackTrace();}

            scrivi_json(); // salvataggio degli account sul file
        }
    }

    // METODO PER CONFIGURAZIONE

    private static void config() throws Exception
    {
        File file = new File("config.txt");
        Scanner scanner = new Scanner(file);

        porta = Integer.parseInt(scanner.nextLine().split(":")[1].trim());
        nthread = Integer.parseInt(scanner.nextLine().split(":")[1].trim());
        tempo_parola = Integer.parseInt(scanner.nextLine().split(":")[1].trim());
        multicast_inet = InetAddress.getByName(scanner.nextLine().split(":")[1].trim());
        porta_multicast = Integer.parseInt(scanner.nextLine().split(":")[1].trim());
    }

    // METODO PER LEGGERE IL FILE E SALVARE GLI ACCOUNT SU UNA HASHMAP

    private static void leggi_json()
    {
        Gson gson = new Gson();
        
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(jsonFile)))) // apre il file in lettura
        {
            reader.beginArray();
            
            while (reader.hasNext()) // fino alla fine
            {
                Giocatore giocatore = gson.fromJson(reader, Giocatore.class);
                concurrentHashMap.put(giocatore.hashCode(), giocatore); // salva il giocare sulla hashmap
            }
            reader.endArray();
        }

        catch(FileNotFoundException e) // nel caso il file non esista, lo crea
        {
            PrintWriter writer = null;

            try
            {
                writer = new PrintWriter(jsonFile, "UTF-8");
                writer.print("[]");
                writer.close();
            } catch (Exception e2)
            {
                if(writer != null) {writer.close();}
                e2.printStackTrace();
                System.exit(1);
            }
            finally {if(writer!=null) {writer.close();}}
        } catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // METODO PER SCRIVERE NEL FILE TUTTI GLI ACCOUNT DELL'HASHMAP

    private static void scrivi_json()
    {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        try (PrintWriter printWriter = new PrintWriter(jsonFile)) {printWriter.print(gson.toJson(concurrentHashMap.values()));} // scrive sul file

        catch (Exception exception)
        {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    // METODI SINCRONIZZATI (E NON) UTILIZZATI DAI THREAD SERVERTASK //

    // METODO SINCRONIZZAO PER SCRITTURA DEL FILE
    public static void synchronized_scrivi_json()
    {
        synchronized(concurrentHashMap){scrivi_json();}
    }

    // METODO SINCRONIZZATO PER LA RICERCA DI UNA PAROLA NEL FILE
    public static boolean synchronized_find_word(String key) throws IOException {return selezionaParola.ricerca_binaria_synchronized(key);}

    // METODO PER RITORNARE LA PAROLA (SECRET WORD)
    public static String getParola() {return selezionaParola.getSecretWord();}

    // METODO PER RITORNARE IL NUMERO DELLA PAROLA (SECRET WORD)
    public static int getNumberSecretWord() {return selezionaParola.getNumeroSecretWord();}

    // METODO PER INVIARE LE STATISTICHE IN MULTICAST
    public static void condividi_statistiche(String statistics)
    {
        DatagramSocket serverMulticast = null;

        try
        {
            serverMulticast = new DatagramSocket(); // apre un DatagramSocket dove saranno inviati i messaggi
            DatagramPacket datagramPacket = new DatagramPacket(statistics.getBytes(StandardCharsets.US_ASCII), statistics.getBytes().length, multicast_inet, porta_multicast);
            serverMulticast.send(datagramPacket); // invia i pacchetti
        } catch (Exception exception) {exception.printStackTrace();}

        finally{if(serverMulticast != null)	{serverMulticast.close();}} // chiusura socket
    }
}
