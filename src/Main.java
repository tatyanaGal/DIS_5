import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Der DB Instruktor erzeugt mehrere Clients, 
 * die Transaktionen in unserer lokalen Datenbank (Buffer) auslösen.
 * Die  Buffergröße beträgt 5 Commits, danach folt ein Commmit-Force 
 * der bereits bestätigten Transaktionen.
 * 
 * Die Anzahl der zu erschaffenden Clients wird durch @link clientsAmount festgelegt.
 * 
 */

public class Main {

	public static void main(String[] args) {

		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		int clientsAmount = 1;

		System.out.println("..............................................................................");
		System.out.print(
				"Willkomen zu einem DB-Konstruktor. \n\nWie viele Clients sollen erzeugt werden (min 1, max 10)?\n ");
		try {
			clientsAmount = Integer.parseInt(inputReader.readLine()) % 10;

			System.out.println("........." + clientsAmount + " Clients werden erzeugt und gestartet" + ".........");
			System.out.println("....................................\n");

			// Recovery wird auf jeden Fall ausgefuehrt.
			System.out.println(
					".........Checke, ob seit der letzten Anwendung ein Crash Recovery notwendig ist.........");
			PersistanceManager.getInstance().crashRecovery();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("....................................");
		System.out.println(".........Starte die DB-Anwendung.........");
		System.out.println("....................................");

		System.out.println("....................................");
		System.out.println(".........Erzeuge Clients.........");
		System.out.println("....................................");

		startClients(createClients(clientsAmount));

	}

	/**
	 * Erzeugt die Clients in einer Liste als Array
	 * @param clientsAmount
	 * @return Client[] - Liste der Klienten
	 */
	private static Client[] createClients(int clientsAmount) {

		ArrayList<Client> clientList = new ArrayList<Client>();
		for (int i = 1; i <= clientsAmount; i++) {
			clientList.add(new Client(i));
		}
		return clientList.toArray(new Client[clientList.size()]);
	}

	// public class ThreadCreator {
	// public static void main(String[] args) {
	// Thread t1 = new HelloThread("Thread1");
	// Thread t2 = new HelloThread("Thread2");
	// t1.start();
	// t2.start();
	// try {
	// Thread.sleep(10000);
	// } catch (InterruptedException e) {
	// }
	// t1.interrupt();
	// t2.interrupt();
	// }
	// }
	//
	
	/**
	 * Startet die Clients für die hier übergebene Client-Liste @link clients 
	 * 
	 * @param clients - Die zu startenden Clients
	 */
	private static void startClients(Client[] clients) {

		for (int i = 0; i < clients.length; i++) {
			clients[i].start();
		}

		try {
			Thread.sleep(1000 * clients.length);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].interrupt();
		}
	}
}