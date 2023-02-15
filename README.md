# Wordle

## INTRODUZIONE
>Wordle è un gioco che consiste nel trovare una parola inglese formata da 10 lettere, 
impiegando un numero massimo di 12 tentativi. Esso dispone di un vocabolario di 
parole da cui estrae casualmente una parola SW (`Secret Word`), che ogni utente deve 
provare ad indovinare. Ogni tot di tempo viene selezionata una nuova SW, che 
rimane invariata fino allo scadere del tempo e che viene proposta a tutti gli utenti che 
si collegano al gioco. L’utente propone una parola GW (`Guessed Word`) e il sistema 
inizialmente verifica se la parola è presente nel vocabolario. In caso negativo avverte 
l’utente che deve immettere un’altra parola. In caso la parola sia presente, il sistema 
fornisce all’utente alcuni indizi utili per indovinare la parola. In caso la parola sia 
presente, il sistema fornisce all’utente tre tipi di indizi, per ogni lettera `l` di GW, il 
sistema indica:
><ul>
  ><li> Se l è stata indovinata e si trova nella posizione corretta rispetto a SW </li>
  ><li> Se l è stata indovinata, ma non si trova nella posizione corretta rispetto a SW </li>
  ><li> Se l non è presente in SW </li>
></ul>

## IMPLEMENTAZIONE

>La struttura del programma si basa su un sistema `Client-Server`. Il Client gestisce 
l’interazione con l’utente tramite linea di comando. Vengono inseriti dei comandi 
che verranno presi, controllati e successivamente inviati al Server aspettando una 
risposta (operazione andata a buon fine o errore) per poi stamparne il risultato. Il 
Server utilizza una `Thradpool` per delegare la gestione di una singola connessione 
con un client a un thread task indipendente. La comunicazione tra Client e Server 
avviene utilizzando una connessione `TCP`. Le informazioni del giocatore vengono 
rappresentati da una classe specifica. Ogni programma contiene un file di 
configurazione che contiene i parametri di configurazione corrispondenti. All’ avvio 
vengono lette le informazioni da questo file e vengono impostate le varie
configurazioni. 

>Lato `Client` viene avviato un thread che si occupa di ricevere tutti i messaggi inviati 
dal Server all'indirizzo di Multicast, usato per condividere le statistiche di un 
giocatore con gli altri utenti collegati sullo stesso indirizzo in modo da dare un 
aspetto “social” al gioco. Il Client si occupa di interagire con l’utente tramite linea di 
comando, ricevendo i comandi e inviandoli al Server per essere eseguiti. 
Successivamente il Client riceverà i risultati processati lato Server e li stamperà a 
video mostrandoli all’utente.

>Lato `Server` vi è la lettura delle informazioni riguardanti i giocatori da un file json che 
si occupa di salvare tutti i dati come oggetti json in una HashMap condivisa tra i 
vari thread task. In questo modo i thread task accedono alle informazioni di tutti i 
giocatori registrati, possono modificare i dati e alla chiusa di ogni connessione con un 
client tutte le informazioni della HashMap vengono sovrascritti nel file json. Viene 
utilizzato una HashSet che tiene traccia di tutti gli oggetti giocatore che sono stati 
occupati, per evitare che più thread agiscano sullo stesso oggetto giocatore e per 
evitare anche che lo stesso utente effettui il login da due client diversi 
contemporaneamente. Il Server inoltre contiene diversi metodi sincronizzati con 
l'utilizzo dei monitor per permette ai thread di eseguire operazioni concorrenti su 
dati condivisi. Per la gestione della Secret Word, che viene scelta casualmente da un 
elenco di parole di gioco salvate su un file e aggiornata ad intervalli di tempo definiti, 
viene avviato un thread dedicato. La lettura viene effettuata eseguendo un accesso 
casuale, difatti conoscendo la struttura del file contenente le parole di gioco è 
possibile ricavare il numero di parole presenti e leggere una parola scelta 
casualmente senza scansionare l'intero file. Tale thread offre un metodo, inoltre, per 
la ricerca di una parola all'interno del file di parole di gioco. Tale metodo sfrutta la 
Ricerca Binaria, data la struttura ordinata del file, in modo da ottimizzare i tempi 
di ricerca della parola.

## STRUTTURA DEL PROGRAMMA

### Client 

>È composto da tre classi principali che interagiscono tra loro:
><ul>
><li> <b>WordleClient</b> : la classe principale che si occupa di interagire con il Server inviando i comandi ricevuti dall’utente.</li>
><li> <b>MulticastClient</b> : la classe che si occupa di ricevere e salvare i messaggi UDP inviati dal server sull'indirizzo di Multicast. </li>
><li> <b>MetodiClient</b> : la classe che raggruppa i metodi utilizzati dal WordleClient.</li>
></ul>

>Inoltre, presenta il file di configurazione `config.txt` da cui prendere i seguenti parametri:
><ul>
  ><li><b>Hostname</b></li>
  ><li><b>Porta</b></li>
  ><li><b>Indirizzo_Multicast</b></li>
  ><li><b>Porta_Multicast</b></li>
></ul>
  
### Server

>È composto da quattro classi principali che interagiscono tra loro:
><ul>
><li> <b>WordleServer</b> : la classe principale che resta in ascolto di nuove connessioni provenienti dai Client.</li>
><li> <b>ServerTask</b> : la classe che implementa il thread che gestisce la singola connessione con il Client.</li>
><li> <b>SelezionaParola</b> : la classe che implementa il thread che gestisce la SW. </li>
><li> <b>Giocatore</b> : la classe che rappresenta un giocatore e tiene traccia di tutti i suoi dati.</li>
></ul>
 
>Inoltre, presenta il file di configurazione config.txt da cui prendere i seguenti parametri:
><ul>
><li><b>Port</b></li>
><li><b>NThread</b></li>
><li><b>Tempo_parola</b></li>
><li><b>Indirizzo_Multicast</b></li>
><li><b>Porta_Multicast</b></li>
></ul>

>e presenta anche il file utenti.json che rappresenta tutte le informazioni sui giocatori sotto forma di file json. Presenta anche una libreria esterna: gson-2.10.

## MANUALE D’ISTRUZIONI

### Istruzioni per compilare ed eseguire il **Server** da Terminale

>1. Entrare nella directory ServerWordle tramite il comando: **`cd ServerWordle`**
>2. Eseguire il comando per la compilazione: **`javac -cp “.;gson-2.10.jar” *.java`**
>3. Eseguire il comando per l’esecuzione: **`java -cp “.;gson-2.10.jar” WordleServer`**

### Istruzioni per compilare ed eseguire il **Client** da Terminale

>1. Entrare nella directory ClientWordle tramite il comando: **`cd ClientWordle`**
>2. Eseguire il comando per la compilazione: **`javac *.java`**
>3. Eseguire il comando per l’esecuzione: **`java WordleClien`**
