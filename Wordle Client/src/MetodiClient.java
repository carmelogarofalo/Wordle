import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MetodiClient
{
    private final BufferedReader bufferedReader;
    private final BufferedReader leggi;
    private final PrintWriter scrivi;
    public MetodiClient(BufferedReader bufferedReader, BufferedReader leggi, PrintWriter scrivi)
    {
        this.bufferedReader = bufferedReader;
        this.leggi = leggi;
        this.scrivi = scrivi;
    }

    // METODO CHE STAMPA IL MENU INZIALE
    // ritorna la scelta fatta dall'utente

    public int menu_iniziale() throws IOException
    {
        int scelta;

        System.out.print("\n------------------------------ W O R D L E ------------------------------");

        do
        {
            System.out.print("\n\nInserisci 1 per effettuare il login");
            System.out.print("\nInserisci 2 per registrarti");
            System.out.print("\n---> ");
            scelta = Integer.parseInt(bufferedReader.readLine());
        }while (scelta<1 || scelta>2);

        return scelta;
    }

    // METODO PER LOGIN UTENTE
    // ritorna 1 se login avvenuto con successo, -1 altrimenti

    public int login() throws IOException
    {
        String username,password;

        System.out.print("\n\n- - - - - - L O G I N - - - - - -");

        System.out.print("\n\nUsername: ");
        username = bufferedReader.readLine(); // prende username da tastiera

        System.out.print("Password: ");
        password = bufferedReader.readLine(); // prende password da tastiera

        System.out.print("\n- - - - - - - - - - - - - - - - -");

        // CONTROLLO USERNAME E PASSWORD INSERITI

        if(username.contains(" ") || password.contains(" ")) // controllo che non contengano spazi
        {
            System.out.print("\n\n[ Username e password non devono contenere spazi!!! ]");
            return -1;
        }

        if(username.length() > 25 || password.length() > 25) // controllo che non siano piu' lunghi di 25 caratteri
        {
            System.out.print("\n\n[ Username e password non devono essere più lunghi di 25 caratteri!!! ]");
            return -1;

        }
        if(username.length() == 0 || password.length() == 0) // controllo che non siano vuoti
        {
            System.out.print("\n\n[ Username e password non devono essere vuoti!!! ]");
            return -1;
        }

        // INVIO COMANDO E STRINGA AL SERVER

        scrivi.println("LOGIN"); // invio il comando al server
        scrivi.println(username.length() + "-" + username + password); // invio la stringa al server

        // LETTURA COMANDO DAL SERVER

        try
        {
            switch(leggi.readLine()) // legge il comando ricevuto dal server
            {
                case "LOGIN": // avvenuto correttamente
                    System.out.print("\n\n[ Login eseguito con successo ]");
                    return 1;

                case "ERRORE": // errore
                    System.out.println("\n\n[ ERRORE: " + leggi.readLine() + " ]");
                    return -1;
            }
        } catch (IOException e) {throw new RuntimeException(e);}

        System.out.print("\n- - - - - - - - - - - - - - - - - - -");

        return 0;
    }

    // METODO PER REGISTRAZIONE UTENTE

    public void register() throws IOException
    {
        String username,password;

        System.out.print("\n\n- - - - - - R E G I S T R A Z I O N E - - - - - -");

        System.out.print("\n\nUsername: ");
        username = bufferedReader.readLine(); // prende username da tastiera

        System.out.print("Password: ");
        password = bufferedReader.readLine(); // prende password da tastiera

        System.out.print("\n- - - - - - - - - - - - - - - - - - -");

        // CONTROLLO USERNAME E PASSWORD INSERITI

        if(username.contains(" ") || password.contains(" ")) // controllo che non contengano spazi
        {
            System.out.print("\n\n[ Username e password non devono contenere spazi!!! ]");
            return;
        }

        if(username.length() > 25 || password.length() > 25) // controllo che non siano piu' lunghi di 25 caratteri
        {
            System.out.print("\n\n[ Username e password non devono essere più lunghi di 25 caratteri!!! ]");
            return;

        }
        if(username.length() == 0 || password.length() == 0) // controllo che non siano vuoti
        {
            System.out.print("\n\n[ Username e password non devono essere vuoti!!! ]");
            return;
        }

        // INVIO COMANDO E STRINGA AL SERVER

        scrivi.println("REGISTRAZIONE"); // invio il comando al server
        scrivi.println(username.length() + "-" + username + password); // invio stringa al server

        // LETTURA COMANDO DAL SERVER

        try
        {
            switch (leggi.readLine()) // legge il comando ricevuto dal server
            {
                case "REGISTRATO": // avvenuta corrattamente
                    System.out.print("\n\n[ Registrazione avvenuta con successo ]");
                    return;

                case "ERRORE": // errore
                    System.out.println("\n\n[ ERRORE: " + leggi.readLine() + " ]");
                    return;
            }

        } catch (IOException e) {throw new RuntimeException(e);}

        System.out.print("\n- - - - - - - - - - - - - - - - - - -");
    }

    // METODO CHE STAMPA IL MENU PRINCIPALE
    // ritorna la scelta fatta dall'utente

    public int menu_principale() throws IOException
    {
        int scelta;

        System.out.print("\n\n------------------------------ Menu Principale ------------------------------");

        do
        {
            System.out.print("\n\nInserisci 1 per giocare");
            System.out.print("\nInserisci 2 per vedere le statistiche");
            System.out.print("\nInserisci 3 per vedere le notifiche");
            System.out.print("\nInserisci 4 per effettuare il logout");
            System.out.print("\n---> ");
            scelta = Integer.parseInt(bufferedReader.readLine());
        }while (scelta<1 || scelta>4);

        return scelta;
    }

    // METODO CHE INVIA IL COMANDO DI GIOCO AL SERVER E LEGGE LA RISPOSTA
    // ritorna 1 se puo' giocare, -1 altrimenti

    public int playWORDLE()
    {
        scrivi.println("GIOCA"); // invio del comando al server

        try
        {
            switch (leggi.readLine()) // legge il comando dal server
            {
                case "GIOCA": // puo' giocare
                    System.out.print("\n\n- - - - - - G I O C A - - - - - -");
                    System.out.print("\n\n [ " + leggi.readLine() + " ]\n");
                    return 1;

                case "ERRORE": // errore
                    System.out.println("\n" + leggi.readLine().replace("/n", "\n"));
                    return -1;
            }
        } catch (IOException e) {throw new RuntimeException(e);}

        System.out.print("\n- - - - - - - - - - - - - - - - - - -");

        return 0;
    }

    // METODO PER L'INVIO DELLA PAROLA AL SERVER
    // ritorna 1 se l'utente puo inserire un'altra parola, -1 altrimenti

    public int sendWord(String parola)
    {
        scrivi.println("INVIO_PAROLA"); // invio comando al server
        scrivi.println(parola); // invio parola

        try
        {
            switch (leggi.readLine()) // lettura comando dal server
            {
                case "INVIO_PAROLA": // andato a buon fine
                    System.out.println("\n\n" + leggi.readLine().replace("/n", "\n"));
                    return 1;

                case "VITTORIA": // vittoria
                    System.out.print("\n\n" + leggi.readLine().replace("/n", "\n"));
                    menu_condividere();
                    return -1;

                case "SCONFITTA": // sconfitta
                    System.out.print("\n\n" + leggi.readLine().replace("/n", "\n"));
                    menu_condividere();
                    return -1;

                case "ERRORE": // errore
                    System.out.println("\n\n" + leggi.readLine().replace("/n", "\n"));
                    return -1;
            }
        } catch (Exception e) {throw new RuntimeException(e);}

        return 0;
    }

    // METODO PER LA CONDIVISIONE STATISTICHE

    public void share()
    {
        scrivi.println("CONDIVIDI"); // invio comando al server

        try
        {
            switch(leggi.readLine()) // lettura comando dal server
            {
                case "CONDIVIDI": // buon fine
                    System.out.println(leggi.readLine().replace("/n", "\n")+"\n");
                    break;

                    case "ERRORE": // errore
                        System.out.println("\n[ ERRORE: " + leggi.readLine() + " ]");
				break;

            }
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    // METODO CHE STAMPA IL MENU PER LA CONDIVISIONE DELLE STATISTICHE

    public void menu_condividere() throws Exception
    {
        String scelta;

        System.out.println("\n\n------------------------------ Menu Condividere ------------------------------");

        System.out.print("\n\nInserisci 1 per condividere con altri utenti le statistiche di questa partita");
        System.out.print("\nInserisci un altro numero altrimenti");
        System.out.print("\n---> ");
        scelta = bufferedReader.readLine();

        if(scelta.equals("1"))
        {
            System.out.print("\n\nCondivisione delle statistiche: ");
            share(); // chiama il metodo per l'invio del comando
        }

        else System.out.print("\n\n------------------------------------------------------------------------------");

    }

    // METODO PER LE STATISTICHE

    public void sendMeStatistics()
    {
        System.out.println("\n\n- - - - - - S T A T I S T I C H E - - - - - -");

        scrivi.println("STATISTICHE"); // invio del comando al server

        try
        {
            switch (leggi.readLine()) // legge comando dal server
            {
                case "STATISTICHE": // buon fine
                    System.out.print("\n\n"+leggi.readLine().replace("/n", "\n"));
                    break;
                case "ERRORE": // errore
                    System.out.println(leggi.readLine().replace("/n", "\n"));
                    break;
            }

        } catch (IOException e) {throw new RuntimeException(e);}

        System.out.print("\n- - - - - - - - - - - - - - - - - - - - - - -");
    }

    // METODO PER LA STAMPA DELLE NOTIFICHE

    public void showMeSharing(MulticastClient multicastClient)
    {
        System.out.print("\n\n- - - - - - N O T I F I C H E - - - - - -");

        ArrayList<String> list = multicastClient.prendiMessaggi(); // legge i messaggi dal gruppo multicast e li salva una una lista

        for (String s : list) System.out.print("\n\n" + s); // stampa le notifiche

        System.out.print("\n- - - - - - - - - - - - - - - - - - - - -");
    }

}