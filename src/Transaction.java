/**
 * Hier werden die Transaktionen gemanaged. 
 * Eine TA kann einenCommit aufnehmen und eine taID aufweisen.
 * 
 * @link taID = TransaktionsID
 * @link committed = Gibt an, ob die TA committed wurde.
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

	/**
	 * Führt einen Write in der TA durch
	 * @param pageId
	 * @param data
	 */
	public void write(int pageId, String data) {
		PersistanceManager.getInstance().write(this, pageId, data);
	}

	
	public void setCommitted() {
		this.committed = true;
	}

}
