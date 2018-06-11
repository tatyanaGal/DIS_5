/*
 * TODO Beschreibung
 */
public class Page {

	private int pageId;
	private int lsn; // log sequence number
	private String data;
	private Transaction transaction;

	public Page(int pageId, int lsn, String data, Transaction ta) {

		this.pageId = pageId;
		this.lsn = lsn;
		this.data = data;
		this.transaction = ta;

	}

	public Transaction getTransaction() {
		return transaction;
	}

	public int getLsn() {
		return lsn;
	}

	public void setLsn(int lsn) {
		this.lsn = lsn;
	}

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	// TODO Sinnvoll?
	public boolean checkCommit() {

		if (!(transaction == null)) {
			return transaction.getCommitted();
		} else {
			return false;
		}
	}
}
