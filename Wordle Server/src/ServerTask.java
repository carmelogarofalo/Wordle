import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTask implements Runnable
{
    private Socket socket;
    private BufferedReader leggi; // stream per lettura dal client
    private PrintWriter scrivi; // stream per scrittura del client
    private Giocatore giocatore = null;
    boolean player_lose = false; // indica se il giocatore ha perso
    private ConcurrentHashMap<Integer, Giocatore> concurrentHashMap; // contenente le informazioni degli account salvati
    private final HashSet<Integer> hashSet; // contiene hashcode degli account
    private int numero_sw; // numero della secret word
    private String secret_word;

    public ServerTask(Socket socket, ConcurrentHashMap<Integer, Giocatore> concurrentHashMap, HashSet<Integer> hashSet)
    {
        this.socket = socket;
        this.numero_sw = 0;
        this.secret_word = "";
        this.concurrentHashMap = concurrentHashMap;
        this.hashSet = hashSet;
    }

    public void run()
    {

        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  Connected");

        try
        {
            // APERTURA STREAM

            leggi = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scrivi = new PrintWriter(socket.getOutputStream(), true);

            boolean run = true; // se settato a false non riceve piu comandi dal client

            while(run)
            {
                switch(leggi.readLine()) // legge comando dal client
                {
                    case "LOGIN":
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  login");
                        login();
                        break;

                    case "REGISTRAZIONE":
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  registrazione");
                        registrazione();
                        break;

                    case "GIOCA":
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  gioca");
                        gioca();
                        break;

                    case "INVIO_PAROLA":
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  invio parola");
                        ricevi_parola();
                        break;

                    case "STATISTICHE":
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  statistiche");
                        invia_statistiche();
                        break;

                    case "CONDIVIDI":
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  condividi");
                        condividi();
                        break;

                    case "LOGOUT":
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  logout");
                        logout();
                        run = false; // setta a false per la fine del ciclo
                        break;

                    default:
                        System.out.println("\n[ip: "+this.socket.getRemoteSocketAddress()+"]  errore");
                }
            }
        } catch (Exception e) {e.printStackTrace();}

        finally
        {
            try
            {
                // CHIUSURA SOCKET E STREAM

                if(socket != null) {socket.close();}
                if(leggi != null) {leggi.close();}
                if(scrivi != null) {scrivi.close();}
            } catch(Exception e) {e.printStackTrace();}

            WordleServer.synchronized_scrivi_json(); // scrive i dati sul file in modo sincronizzato

            logout(); // esce

            System.out.println("[ip: "+this.socket.getRemoteSocketAddress()+"]  Connected Close");
        }
    }

    // METODO PER IL LOGIN

    private void login() throws Exception
    {
        String in_socket;
        String user;
        String password;
        int dim_num;
        int dim_user;

        in_socket = leggi.readLine(); // lettura messaggio strutturato: lunghezza_stringa_username + "-" + username + password

        // ESTRAZIONE USERNAME E PASSWORD DAL CLIENT

        dim_num = in_socket.indexOf("-");
        dim_user = Integer.valueOf(in_socket.split("-")[0]) + dim_num + 1;
        user = in_socket.substring(dim_num+1, dim_user);
        password = in_socket.substring(dim_user);

        // CONTROLLI

        if(giocatore != null) // si assicura che il client non si sia già loggato
        {
            scrivi.println("ERRORE");
            scrivi.println("Utente gia' loggato");
            return ;
        }

        if(concurrentHashMap.containsKey(user.hashCode())) // verifica se l'utente è registrato
        {
            if(concurrentHashMap.get(user.hashCode()).controllo_password(password)) // verifica se la password e' corretta
            {
                boolean log = false;

                synchronized(hashSet)
                {
                    if( hashSet.contains(user.hashCode()) ){log = true;}
                    else{hashSet.add(user.hashCode());} // lo aggiunge
                }

                if(log) // utente gia loggato
                {
                    scrivi.println("ERRORE");
                    scrivi.println("Utente "+ user +" risulta attualmente loggato");
                }

                else
                {
                    giocatore = concurrentHashMap.get(user.hashCode());
                    scrivi.println("LOGIN");
                }
            }

            else // password errata
            {
                scrivi.println("ERRORE");
                scrivi.println("Utente "+ user +": password errata");
            }
        }

        else // utente non registrato
        {
            scrivi.println("ERRORE");
            scrivi.println("User "+ user +" non registrato");
        }
    }

    // METODO PER LA REGISTRAZIONE

    private void registrazione() throws Exception
    {
        String in_socket;
        String user;
        String password;
        int dim_num;
        int dim_user;

        in_socket = leggi.readLine(); // lettura messaggio strutturato: lunghezza_stringa_username + "-" + username + password

        // ESTRAZIONE USERNAME E PASSWORD DAL CLIENT

        dim_num = in_socket.indexOf("-");
        dim_user = Integer.valueOf(in_socket.split("-")[0]) + dim_num + 1;
        user = in_socket.substring(dim_num+1, dim_user);
        password = in_socket.substring(dim_user);

        if(concurrentHashMap.containsKey(user.hashCode())) // controlla se esiste già un username memorizzato con lo stesso nome
        {
            scrivi.println("ERRORE");
            scrivi.println("Utente "+ user+" gia' registrato");
        }

        else // registra il giocatore
        {
            giocatore = new Giocatore(user, password);
            concurrentHashMap.put(giocatore.hashCode(), giocatore);
            giocatore = null;
            scrivi.println("REGISTRATO");
        }
    }

    // METODO PER GIOCARE

    private void gioca()
    {
        numero_sw = WordleServer.getNumberSecretWord(); // legge il numero della secret word

        if(giocatore.remaing_attempt(numero_sw)) // controlla se ha esaurito i tentativi disponibili per giocare
        {
            giocatore.setLastSecretWord(numero_sw); // setta la nuova parola per cui il giocatore sta giocando

            player_lose = true;

            scrivi.println("GIOCA");
            scrivi.println("Tentativi rimasti: " + (12 - giocatore.getTentativi()));
        }

        else // tentativi esauriti
        {
            scrivi.println("ERRORE");
            scrivi.println("Tentativi per questa parola esauriti");
        }
    }

    // METODO PER RICEZIONE PAROLA

    private void ricevi_parola() throws Exception
    {
        secret_word = WordleServer.getParola(); // legge la secret word

        String guessed_word = leggi.readLine(); // legge la guessed word inviata dall'utente
        assert(guessed_word.length() == 10);

        if (!(giocatore.remaing_attempt(numero_sw)) ) // controlla se ha ancora tentativi
        {
            scrivi.println("ERRORE");
            scrivi.println("Tentativi per questa parola esauriti");
            return ;
        }

        if (!WordleServer.synchronized_find_word(guessed_word)) // controlla se la parola è presente nel dizionario
        {
            scrivi.println("ERRORE");
            scrivi.println("Parola non presente nel dizionario");
            return ;
        }

        giocatore.tentativo_effettuato();  // incrementa i tentativi

        if(guessed_word.equals(secret_word)) // controlla se ha indovinato la parola
        {
            scrivi.println("VITTORIA");
            scrivi.println("Complimenti hai indovinato la parola!!!/n/n");
            giocatore.setGameStats("++++++++++");
            giocatore.vittoria();
            player_lose = false;
        }

        else
        {
            // SCHERMATA PER LA VISIONE "COLORATA" DELLA PAROLA

            StringBuilder tmp = new StringBuilder();
            StringBuilder s = new StringBuilder("[ Suggerimento ]");
            s.append("/nGuessed Word: ").append(guessed_word);
            s.append("/nSecret Word:  ");

            // GRIGIO (X), VERDE (+), GIALLO (?)

            for(int i=0; i<guessed_word.length(); i++)
            {
                if(guessed_word.charAt(i) == secret_word.charAt(i)) // se la lettera e' nella posizione giusta allora VERDE
                {
                    s.append("+");
                    tmp.append("+");
                }

                else if( secret_word.indexOf(guessed_word.charAt(i)) != -1) // se la lettera non e' nella posizione giusta allora GIALLO
                {
                    s.append("?");
                    tmp.append("?");
                }

                else // se la lettera non e' presente allora GRIGIO
                {
                    s.append("X");
                    tmp.append("X");
                }
            }

            giocatore.setGameStats(tmp.toString()); // setto le statistiche

            if(giocatore.getTentativi() >= 12) // controlla se ha esaurito i tentativi
            {
                s.append("/n/nAttenzione questo era il tuo ultimo tentativo/n");
                scrivi.println("SCONFITTA");
                scrivi.println(s);
                giocatore.sconfitta();
            }

            else
            {
                s.append("/n/nTentativi utilizzati ").append(giocatore.getTentativi()).append(", rimanenti: ").append(12 - giocatore.getTentativi());
                scrivi.println("INVIO_PAROLA");
                scrivi.println(s);
            }
        }
    }

    // METODO PER INVIO DELLE STATISTICHE

    private void invia_statistiche()
    {
        assert(giocatore != null);

        scrivi.println("STATISTICHE");
        scrivi.println(giocatore.getStatistics());
    }

    // METODO PER LA CONDIVISIONE DELLE STATISTICHE

    private void condividi()
    {
        //assert(giocatore != null);

        WordleServer.condividi_statistiche(giocatore.getStatisticheUltimaPartita());

        scrivi.println("CONDIVIDI");
        scrivi.println(giocatore.getStatisticheUltimaPartita());
    }

    // METODO PER LOGOUT

    private void logout()
    {
        assert(giocatore != null);

        if(player_lose)
        {
            giocatore.sconfitta();
            player_lose = false;
        }

        synchronized(hashSet) {hashSet.remove(giocatore.hashCode());} // rimuove giocatore dalla hashSet di giocatore attualmente connessi
    }
}