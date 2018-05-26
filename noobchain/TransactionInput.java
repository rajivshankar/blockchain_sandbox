package noobchain;

public class TransactionInput {
	public String transactionOutputID; //Reference to TransactionOutputs -> transactionId
	public TransactionOutput UTXO; //Contains the unspent transaction Output
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputID = transactionOutputID;
	}
}
