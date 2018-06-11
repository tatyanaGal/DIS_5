/*
 * TODO Beschreibung
 */
public class Transaction {

	private boolean committed = false;
	private int taId;

	public Transaction(int taId) {

		this.taId = taId;
	}

	public boolean getCommitted() {

		return committed;
	}

	public int getTaId() {

		return taId;
	}

	public void setTaId(int taId) {

		this.taId = taId;
	}

	// TODO Sinnvoll?? Ja, weil ein write von der TA angestossen werden muss.
	public void write(int pageId, String data) {

		PersistanceManager.getInstance().write(this, pageId, data);
	}

	// TODO Sinnvoll??
	public void setCommitted() {

		// if (!committed) {
		// committed = true;
		// }
		this.committed = true;
	}

}
