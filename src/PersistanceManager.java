
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * TODO Beschreibung NEU
 * 
 */

public class PersistanceManager {

	// class Singleton {
	// static final private Singleton singleton;
	// static {
	// try {
	// singleton = new Singleton();
	// }
	// catch (Throwable e) {
	// throw new RuntimeException(e.getMessage());
	// }
	// }
	// private Singleton() {}
	// static public Singleton getInstance() {
	// return singleton;
	// }
	// }

	static final private PersistanceManager manager;

	private int currentTAid = 0;
	private int lsn = 0; // Log sequence number
	private Hashtable<Integer, Page> buffer = new Hashtable<Integer, Page>(); // for buffer (int=PageID; Page)

	static {
		try {
			manager = new PersistanceManager();
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private PersistanceManager() {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("LogData"));
			// Parst die letzte Zeile von LogData, um das letzte LSN rauszufinden
			String last = "";
			String line;
			String[] characters;

			while ((line = reader.readLine()) != null) {
				last = line;
			}
			characters = last.split(",");

			this.lsn = Integer.parseInt(characters[0].split(":")[1]);
			this.currentTAid = Integer.parseInt(characters[1].split(":")[1]);

		} catch (FileNotFoundException e) {
			this.lsn = 0;
			this.currentTAid = 0;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static PersistanceManager getInstance() {

		return manager;
	}

	private synchronized void incrementTaId() {

		this.currentTAid++;
	}

	private synchronized void incrementLSN() {

		this.lsn++;
	}

	// starts a new transaction. The persistence manager creates a unique tran-
	// saction ID and returns it to the client.
	public synchronized Transaction beginTransaction() {

		incrementLSN();
		incrementTaId();

		log(currentTAid, RecordType.BEGINOFTA);
		return new Transaction(currentTAid);
	}

	// TODO Methode wurde nicht nach der Aufgabestellung implementiert
	// commit(int taid): commits the transaction speci ed by the given transaction
	// ID.
	public synchronized void commit(Transaction ta) {

		// commit the given transaction
		incrementLSN();

		log(ta.getTaId(), RecordType.COMMIT);
		ta.setCommitted();
	}

	// write(int taid, int pageid, String data): writes the given data with the
	// given page ID
	// on behalf of the given transaction to the buer. If the given page already
	// exists, its content is
	// replaced completely by the given data.
	/**
	 * TODO Beschreibung // write the data to a file // one file for each page //
	 * LSN also into the file for redoing
	 * 
	 * @param ta
	 * @param pageId
	 * @param data
	 */
	public synchronized void write(Transaction ta, int pageId, String data) {

		int i;
		Boolean modified=false;
		incrementLSN();

		// update Pages im Buffer, wenn sie da zurzeit vorhanden ist
		Set<Integer> keys = buffer.keySet();
        for(int key: keys){
        	if (key==pageId) {
        		buffer.get(key).setTransaction(ta);
        		buffer.get(key).setPageId(pageId);
        		buffer.get(key).setData(data);
        		buffer.get(key).setLsn(lsn);
        		log(ta.getTaId(), RecordType.WRITE, pageId, "Veraenderter "+data);
        		modified=true;
        	}
        }

        // Wenn die Page nicht im Buffer vorhanden ist, wird ein neues Datensatz im Buffer angelegt
        if (!modified) {
        	log(ta.getTaId(), RecordType.WRITE, pageId, data);

    		Page page = new Page(pageId, lsn, data, ta);
    		buffer.put(pageId, page);
        }
		
		System.out.println("Buffer-Groesse: " + buffer.size());

		// TODO TODO
		// Was passiert, wenn es mehr als 5 datensГ¤tze im Buffer
		// gespeichert werden und keine davon ist commitet
		if (buffer.size() > 5) {
			Object[] allValues = buffer.values().toArray();
			for (i = 0; i < allValues.length; i++) {
				Page currentPage = (Page) allValues[i];
				if (currentPage.checkCommit()) {
					savePage(currentPage);
					buffer.remove(currentPage.getPageId());

				}
			}
		}

	}

	/**
	 * TODO Beschreibung
	 * 
	 * @param page
	 */
	private synchronized void savePage(Page page) {

		FileWriter writer = null;

		try {
			writer = new FileWriter("Page" + page.getPageId());
			writer.write("PageID:" + Integer.toString(page.getPageId()) + ",");
			writer.write("LSN:" + Integer.toString(page.getLsn()) + ",");
			writer.write("TAID:" + Integer.toString(page.getTransaction().getTaId()) + ",");

			writer.write("Data:" + page.getData());

			System.out.println("Gespeichert: " + "\nPageID " + page.getPageId() + ",LSN " + page.getLsn() + ",TAID "
					+ page.getTransaction().getTaId() + ",Data: " + page.getData() + "\n.......................");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * TODO - Beschreibung This method logs all writing actions. There is another
	 * log method used for BOT and Write. Both methods are the same apart from the
	 * given parameters.
	 * 
	 * @param taId
	 * @param RecordType
	 * @param pageId
	 * @param data
	 */
	private synchronized void log(int taId, RecordType type, int pageId, String data) {

		FileWriter writer = null;

		try {

			writer = new FileWriter("LogData", true);
			writer.write("LSN:" + Integer.toString(lsn) + ",");
			writer.write("TAID:" + Integer.toString(taId) + ",");

			if (type == RecordType.WRITE) {
				writer.write("Data:" + data + ",");
				writer.write("PageID:" + pageId);
				System.out.println("LOG: " + "\nPageID " + pageId + ",LSN " + lsn + ",TAID " + taId + ",Data: "
						+ data + "\n....................................");
			} else {
				System.err.println("Es wurde falsche Log-Methode verwendet.");
			}
			writer.write("\n");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * TODO - Beschreibung This method is used for the logging of BOT and Commit
	 * actions. There is another log method for Writing actions which is the same
	 * apart from the given parameters.
	 * 
	 * @param taId
	 * @param type
	 */
	private synchronized void log(int taId, RecordType type) {

		FileWriter writer = null;

		try {

			writer = new FileWriter("LogData", true);
			writer.write("LSN:" + Integer.toString(lsn) + ",");
			writer.write("TAID:" + Integer.toString(taId) + ",");

			if (type == RecordType.COMMIT) {
				writer.write("Data:committed");
				System.out.println("LOG: " + "\nLSN " + lsn + ",TAID " + taId
						+ ",COMMITTED\n.....................................");

			} else if (type == RecordType.BEGINOFTA) {
				writer.write("Data:BOT");
				System.out.println("LOG: " + "\nLSN " + lsn + ",TAID " + taId
						+ ",BEGIN OF TRANSACTION \n....................................");
			} else {
				System.err.println("Es wurde falsche Log-Methode verwendet.");
			}

			writer.write("\n");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * TODO Beschreibung
	 */
	// PageID in LogData berГјcksichtigen und dann erst fГјr eine bestimmte Page die
	// LSNs vergleichen.
	public void crashRecovery() {

		// Winner TAs sind diejenige, fГјr die ein Commit angestossen wurde
		Integer[] winnerTAs = checkWinnerTaIds();
		if (winnerTAs != null) {
			redoTransactions(winnerTAs);
		} else {
			System.out.println("Ein Crash Recovery ist nicht notwendig!");
		}
	}

	/**
	 * TODO - Beschreibung Г¤ndern Reads all of the logs in order to determine which
	 * of the logged transactions have been committed
	 * 
	 * @return all of those transaction's ids
	 */
	// PageID in LogData berГјcksichtigen und dann erst fГјr eine bestimmte Page die
	// LSNs vergleichen.
	private Integer[] checkWinnerTaIds() {

		BufferedReader reader = null;
		ArrayList<Integer> winnerTas = new ArrayList<Integer>();

		try {
			reader = new BufferedReader(new FileReader("LogData"));
			String line = reader.readLine();
			String[] values;

			while (line != null) {

				values = line.split(",");

				if ((values[2].split(":")[1]).equalsIgnoreCase("committed")) {
					winnerTas.add(Integer.parseInt(values[1].split(":")[1]));
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.err.println("Es wurde keine LogData-Datei gefunden.\nCrash Recovery kann nicht ausgefuehrt werden!");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		return (Integer[]) winnerTas.toArray(new Integer[winnerTas.size()]);

	}

	/**
	 * TODO Beschreibung
	 * 
	 * @param winnerTaIds
	 */
	private void redoTransactions(Integer[] winnerTaIds) {

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader("LogData"));
			String line = reader.readLine();
			String[] values;
			int counter = 0;

			while (line != null) {
				values = line.split(",");

				// Parst die ganze Log-Datei und sucht sich write-Eintraege aus (Laenge 4)
				if (values.length != 3) {

					for (int currentTaId : winnerTaIds) {

						// Vergleiche TAid aus Log-Datei mit TAid von WinnerTAIds
						if (currentTaId == Integer.parseInt(values[1].split(":")[1])) {

							// vergleiche write-LSN mit LSN-EIntrag in der Page mit entsprechende PageID
							if (compareLSNWithPageLSN(Integer.parseInt(values[0].split(":")[1]),
									Integer.parseInt(values[3].split(":")[1]))) {

								// mache REDO
								redoWrite(values);

								// zum Checken, ob ueberhaupt ein REDO durchgefuehrt wurde
								counter++;
							}
						}
					}
				}
				line = reader.readLine();
			}
			if (counter == 0) {
				System.out.println("Alle TAs sind uptoday. Es wird kein Crash Recovery benoetigt!\\n");
				System.out.println("\n...................");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * TODO - Beschreibung Г¤ndern return true, wenn LSN > ist als Pagelsn. In
	 * diesem Fall muss ein redo gemacht werden.
	 * 
	 * returns true if LSN is smaller than PageLsn -> in this case the write action
	 * does not have to be redone; And returns false in case the operation has to be
	 * redone;
	 * 
	 * @param lsn
	 * @param pageId
	 * @return
	 */
	private boolean compareLSNWithPageLSN(int lsn, int pageId) {

		BufferedReader reader = null;

		String[] line;
		try {
			reader = new BufferedReader(new FileReader("Page" + pageId));
			line = reader.readLine().split("\\,");
		} catch (FileNotFoundException e) {
			System.err.println("Page" + pageId + " wurde nicht gefunden und wird neu angelegt.");
			return false;
		} catch (Exception e) {
			System.err.println("Auf Page" + pageId + " konnte nicht zugegriffen werden!");
			return false;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return (Integer.parseInt(line[1].split(":")[1]) < lsn);
	}

	/**
	 * TODO Beschreibung
	 * 
	 * @param values
	 */
	private void redoWrite(String[] values) {

		FileWriter writer = null;
		String newLSN = values[0].split(":")[1];
		String ta = values[1].split(":")[1];
		String data = values[2].split(":")[1];
		String pageId = values[3].split(":")[1];

		try {
			writer = new FileWriter("Page" + pageId);

			writer.write("PageID:" + pageId + ",");
			writer.write("LSN:" + newLSN + ",");
			writer.write("TAID:" + ta + ",");
			writer.write("Data:" + data);

			System.out.println("REDO: " + "\nPageID " + pageId + ",LSN " + newLSN + ",TAID " + ta + ",Data: " + data
					+ "\n....................................");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
