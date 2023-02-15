import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class WordleClient
{
    public static String hostname;
    public static String porta;
    public static String indirizzo_multicast;
    public static String porta_multicast;
    private static Socket socket;
    private static BufferedReader leggi;
    private static PrintWriter scrivi;
    private static MulticastClient multicastClient;

    public static void main(String[] args)
    {
        try
        {
            // LETTURA PARAMETRI CONFIGURAZIONE

            config(); // legge i parametri di configurazione dal file config.txt

            // SETTAGGIO INIZIALE

            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader); // stream per la lettura da tastiera

            socket = new Socket(hostname,Integer.parseInt(porta)); // socket
            leggi = new BufferedReader(new InputStreamReader(socket.getInputStream())); // stream per la lettura dal server
            scrivi = new PrintWriter(socket.getOutputStream(),true); // stream per la scrittura del server

            MetodiClient metodiClient = new MetodiClient(bufferedReader, leggi, scrivi); // classe per i metodi del client

            boolean login = true; // variabile per non ripetere il login/registrazione

            while(login)
            {
                switch (metodiClient.menu_iniziale()) // switch tra scelta fatta dall'utente nel menu iniziale
                {
                    case 1: // LOGIN
                        if(metodiClient.login() == 1) login = false; // se effettua correttamente il login non ripete la fase di accesso/registrazione
                        break;

                    case 2: // REGISTRAZIONE
                        metodiClient.register();
                        break;
                }
            }

            // AVVIO THREAD PER ASCOLTO MESSAGGI UDP DAL SERVER

            multicastClient = new MulticastClient(indirizzo_multicast,Integer.parseInt(porta_multicast));
            Thread thread = new Thread (multicastClient);
            thread.start();

            Thread.sleep(1000); // aspetta avvio del thread MulticastClient

            boolean logout = false;    // se settato a false chiude il client

            while(!logout)
            {
                switch(metodiClient.menu_principale()) // switch tra scelta effettuata dall'utente nel menu di gioco
                {
                    case 1: // GIOCA
                        if (metodiClient.playWORDLE() == 1) // vale 1 se il giocatore puo' giocare (-1 se ha esaurito i tentativi)
                        {
                            System.out.print("\n[ La parola deve essere di 10 caratteri e non deve contenere spazi ]");
                            System.out.print("\n[ Premi invio lasciando vuoto se vuoi terminare l'invio di parole ]");

                            while(true) // loop per l'inserimento delle parole
                            {
                                String parola;

                                while(true) // loop per la correttezza della parola inserita
                                {

                                    // INSERIMENTO PAROLA

                                    System.out.print("\n\nInserisci una parola: ");
                                    parola = bufferedReader.readLine();

                                    if((( parola.length() != 10 && parola.length() != 0) || parola.contains(" ")) ) System.out.print("\n\n[ Inserire una parola corretta ]"); // controllo sulla parola inserita dal giocatore
                                    else break; // se adatta allora interrompe il loop
                                }

                                if(parola.equals("")) // il giocatore preme invio allora termina il gioco
                                {
                                    System.out.print("\n\n------------------------------ F I N E ------------------------------");
                                    break;
                                }

                                else
                                    if( metodiClient.sendWord(parola) == -1 ) break; // invia la parola al server e controlla il ritorno (1 se puo' inviare ancora, -1 altrimenti)
                            }
                        }

                        break;

                    case 2: // STATISTICHE
                        metodiClient.sendMeStatistics(); // chiede al server le statistiche del giocatore
                        break;

                    case 3: // NOTIFICHE
                        metodiClient.showMeSharing(multicastClient); // mostra le notifiche nel gruppo multicast
                        break;

                    case 4: // LOGOUT
                        scrivi.println("LOGOUT"); // invio al server LOGOUT
                        logout = true; // setta a true per uscire dal ciclo
                        break;
                }
            }
        } catch(Exception e) {e.printStackTrace();}

        // FASE DI CHIUSURA

        finally
        {

            multicastClient.stop(); // stoppa il thread MulticastClient

            // CHIUSURA SOCKET E STREAM

            try
            {
                if(socket != null) socket.close();
                if(leggi != null) leggi.close();
                if(scrivi != null) scrivi.close();
            } catch(Exception e) {e.printStackTrace();}
        }

        System.out.print("\n\n-------------------------------------------------------------------------");
    }

    // METODO PER CONFIGURAZIONE
    private static void config() throws FileNotFoundException
    {
        File file = new File("config.txt");
        Scanner scanner = new Scanner(file);

        hostname = scanner.nextLine().split(":")[1].trim();
        porta = scanner.nextLine().split(":")[1].trim();
        indirizzo_multicast = scanner.nextLine().split(":")[1].trim();
        porta_multicast = scanner.nextLine().split(":")[1].trim();
    }
}
