public class Giocatore
{
    private final String username;
    private final String password;
    private int partite_giocate;
    private int partite_vinte;
    private int streak; // numero di vittorie di fila
    private int max_streak; // streak massima ottenuta
    private final int[] guess_distribution; // vettore dove la posizione k-esima contiene il numero di parole indovinate con esattamente k tentativi
    private int last_SecretWord; // numero dell'ultima SecretWord con cui il giocatore ha giocato
    private int tentativi; // tentativi utilizzati
    private String[] statistiche_ultima_partita; // vettore contenente le statistiche dell'ultima partita

    public Giocatore(String username, String password)
    {
        this.username = username;
        this.password = password;

        this.partite_giocate = 0;
        this.partite_vinte = 0;
        this.streak = 0;
        this.max_streak = 0;
        this.guess_distribution = new int[] {0,0,0,0,0,0,0,0,0,0,0,0};
        this.last_SecretWord = 0;
        this.tentativi = 0;
        this.statistiche_ultima_partita = new String[] { "","","","","","","","","","","",""};
    }

    // METODO PER IL CONTROLLO DELLA PASSWORD

    public boolean controllo_password(String pass) {return this.password.equals(pass);}

    // METODO PER CONTROLLARE I TENTATIVI RIMASTI

    public boolean remaing_attempt(int secretW) {return last_SecretWord != secretW || tentativi < 12;}

    // METODO PER INCREMENTARE IL NUMERO DI TENTATIVI EFFETTUATI

    public void tentativo_effettuato() {this.tentativi += 1;}

    // METODO IN CASO DI VITTORIA DEL GIOCATORE

    public void vittoria()
    {
        // INCREMENTO PARTIRE GIOCATE, VINTE E STREAK

        this.partite_giocate += 1;
        this.partite_vinte += 1;
        this.streak += 1;

        if(this.streak > this.max_streak) {this.max_streak = this.streak;} // aggiornamento della streak (in caso sia maggiore quella attuale)

        this.guess_distribution[tentativi-1] += 1; // aggiornamento del vettore
        this.tentativi = 12; // esaurisce i tentativi per non far rigiocare con la stessa parola
    }

    // METODO IN CASO DI SCONFITTA

    public void sconfitta()
    {
        // AGGIORNAMENTO DEI DATI

        this.partite_giocate += 1;
        this.streak = 0;
        this.tentativi = 12;
    }

    // METODO PER SETTARE L'ULTIMA SECRET WORD

    public void setLastSecretWord(int sw)
    {

        if(this.tentativi < 12) {this.sconfitta();} // se aveva esaurito tutti i tentativi per l'ultima partita allora sconfitta

        this.last_SecretWord = sw;
        this.tentativi = 0;
        this.statistiche_ultima_partita = new String[] { "","","","","","","","","","","","" };
    }

    // METODO PER SETTARE LE STATISTICHE DELL'ULTIMA PARTITA

    public void setGameStats(String s) {this.statistiche_ultima_partita[this.tentativi-1] = s;}

    // METODI GETTER

    public int getTentativi() {return this.tentativi;}

    public String getStatistics()
    {
        // utilizza '/n' al posto di '\n' per permette al server di effettuare una lettura come unica stringa

        String s = "";
        s += "Partite giocate: " + partite_giocate + "/n";

        if(partite_giocate != 0) s += "Percentuale vittoria: " + ( (partite_vinte*100)/partite_giocate ) + "%/n";
        else s += "Percentuale vittoria: 0%/n";

        s += "Ultima streak: " + streak + "/n";
        s += "Streak migliore: " + max_streak + "/n";

        s += "Guess distribution:/n";
        for(int i=0; i<guess_distribution.length; i++) s += " - ["+(i+1)+"]\t  "+guess_distribution[i]+"/n";

        s += "/n";

        return s;
    }

    public String getStatisticheUltimaPartita()
    {
        String s = "";

        s += "\n\nUsername: " + this.username + "/n";
        s += "Secret word: " + this.last_SecretWord + "/n";

        for(int i=0; i<this.tentativi; i++)
            s += "Tentativo "+(i+1)+"\t\t"+this.statistiche_ultima_partita[i]+"/n";

        return s;
    }

    // METODO PER RESTITUIRE L'HASHCODE DELL USERNAME

    @Override
    public int hashCode() {return this.username.hashCode();}

    // METODO PER RAPPRESENTARE L'OGGETTO GIOCATORE

    @Override
    public String toString() {return "[" + username + ", " + password + ", " + partite_giocate + ", " + partite_vinte + ", " + streak + ", " + max_streak + ", " + toString_guessDis() + ", " + last_SecretWord + ", " + tentativi +" ]";}

    // METODO PER RAPPRESENTARE LA STRINGA DELLA GUESS DISTRIBUTION

    private String toString_guessDis()
    {
        String s = "";

        s += "[" + guess_distribution[0];
        for(int i=1; i<guess_distribution.length; i++) s += " ," + guess_distribution[i];
        s += "]";

        return s;
    }

}
