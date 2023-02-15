import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ThreadLocalRandom;

public class SelezionaParola implements Runnable
{
    private String secretWord;
    private int numero_sw; // numero di sequenza della secretWord
    private final String file; // file che contiene tutte le parole di gioco
    private final int tempo; // tempo di durata della secret word
    private volatile boolean exit; // variabile che server a far terminare il thread
    private RandomAccessFile randomAccessFile; // stream per la lettura del file

    public SelezionaParola(String file, int tempo)
    {
        this.file = file;
        this.tempo = tempo;
        this.secretWord = "";
        this.numero_sw = 0;
        exit = false;
    }

    public void run()
    {
        try
        {
            this.randomAccessFile = new RandomAccessFile(this.file, "r"); // apro il file in lettura

            while(!this.exit) // exit viene settata a true nel metodo stop()
            {
                this.secretWord = parola_random(); // estrae dal file una parola random e la setta come secretWord
                this.numero_sw++; // incrementa il numero di sequenza
                System.out.println("\nParola da indovinare: " + this.numero_sw + ")  " + this.secretWord);
                Thread.sleep(this.tempo); // attende prima di cambiare la parola
            }
        } catch(Exception e){e.printStackTrace();}

        finally
        {
            if(this.randomAccessFile != null )
            {
                try {this.randomAccessFile.close();} // chiusura file
                catch(Exception e){e.printStackTrace();}
            }
        }
    }

    // METODO PER ESTRARRE UNA PAROLA RANDOM DAL FILE

    public String parola_random() throws IOException
    {
        final int StringByte = (11); // 11 = 10 lunghezza parola + \n
        final int numElements = (int) (this.randomAccessFile.length() / StringByte); // calcola il numero di parole nel file
        int casual = ThreadLocalRandom.current().nextInt(0, numElements + 1); // calcola una posizione casuale tra 0 e il numero di parole nel file

        this.randomAccessFile.seek((long) casual * (StringByte)); // si posiziona nella posizione casual

        return this.randomAccessFile.readLine();
    }

    // METODO PER LA RICERCA DELLA PAROLA NEL FILE
    // ritorna true se la parola e' presente nel file, false altrimenti

    private boolean ricerca_binaria(String parola) throws IOException
    {
        final int StringByte = (11);
        final int numElements = (int) this.randomAccessFile.length() / StringByte;

        int lower = 0;
        int upper = numElements - 1;
        int mid;

        while (lower <= upper)
        {
            mid = (lower + upper) / 2; // calcola l'indice di mezzo

            this.randomAccessFile.seek((long) mid * (StringByte)); // si posiziona nella posizione mid

            String value = this.randomAccessFile.readLine(); // legge la parola in posizione mid

            if (parola.compareTo(value) == 0) return true;
            if (parola.compareTo(value) < 0) upper = mid - 1;
            else lower = mid + 1;
        }
        return false;
    }

    // METODO SINCRONIZZATO PER LA RICERCA DI UNA PAROLA NEL FILE

    public boolean ricerca_binaria_synchronized(String parola) throws IOException
    {
        boolean res;
        synchronized(this.randomAccessFile) {res = this.ricerca_binaria(parola);}
        return res;
    }

    // METODI GETTER

    public String getSecretWord() {return this.secretWord;}
    public int getNumeroSecretWord() {return this.numero_sw;}

    // METODO PER TERMINAZIONE DEL CICLO

    public void stop()
    {
        exit = true;

        try {Thread.sleep(3000);}
        catch (Exception ignored) {}
        finally{System.exit(1);}
    }

}
