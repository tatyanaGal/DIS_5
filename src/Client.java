import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

//	String name;
//	public HelloThread(String name){
//	this.name = name;
//	}
//	public void run() {
//	while(true){
//	System.out.println("Hello from " + name);
//	try{
//	Thread.sleep(2000);
//	}catch(InterruptedException e){
//	return;
//	}s
//	}
//	}
//	
//	

/**
 * Die Client-Klasse kann parallel in mehreren Instanzen gestartet werden.
 * Alle Instanzen greifen nacheinander auf den Persistenzmanager zu. 
 * Schema: beginTransaction() write() write() ... commit()
 * 
 * @link clientID : Die Client-Identifikationsnummer
 * @link firstPage : Die erste Seite angelegte Seite
 * @link lastPage : Die zuletzt angelegte Seite
 */
public class Client extends Thread {

	private int clientID;
	private int firstPage;
	private int lastPage;
	private Transaction currentTA;

	public Client(int clientID) {

		this.clientID = clientID;
		this.firstPage = clientID * 10;
		this.lastPage = firstPage + 9;
	}

	/**
	 * Lässt den CLient starten und schreibt die 
	 * Einträge für die erzeugten Clients für die @link firstPage und @link lastPage auf die Konsole
	 * 
	 * 
	 */
	public void run() {

		int counter = firstPage;
		Random randomNumber = new Random();
		int actualPage = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		int i;

		while (counter <= lastPage) {
			try {
				//System.err.println("ClientID: " + this.clientID + "\n");
				currentTA = PersistanceManager.getInstance().beginTransaction();
				Client.sleep(1000);

				for (i = 0; i <= randomNumber.nextInt(4); i++) {
					actualPage = clientID * 10 + randomNumber.nextInt(10);

					//System.err.println("ClientID: " + this.clientID + "\n");
					currentTA.write(actualPage, "Eintrag fuer TA " + currentTA.getTaId() + " wurde um "
							+ sdf.format(new Date()).replaceAll(":", "/") + " vom Client"+ this.clientID +" erzeugt");
					Client.sleep(1000);
				}

				Client.sleep(1000);

				//System.err.println("ClientID: " + this.clientID + "\n");
				PersistanceManager.getInstance().commit(currentTA);
				counter++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}