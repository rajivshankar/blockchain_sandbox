package noobchain;

import java.security.*;
import java.util.ArrayList;

public class Transaction {
    
	public String transactionId; // this is also the hash of the transaction
	public PublicKey sender; // sender's address/public key
	public PublicKey recipient; // recipient's address/public key
	public float value;
	public byte[] signature; // this is to prevent anybody else spending funds in our account
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	public static int sequence = 0; //a rough count of how many transactions have been generated
	
	//Constructor:
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	// this calculates the transaction hash (which will be used as its ID)
	private String calculateHash() {
		sequence++; // increase the sequence to avoid 2 identical transactions having the same hash value
		return StringUtil.applySha256(
							StringUtil.getStringFromKey(sender) + 
							StringUtil.getStringFromKey(recipient) + 
							Float.toString(value) + 
							sequence
							);
	}
	
	//gets the String data that cannot be tampered with
	public String getDataToVerify() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
		return data;
	}
	
	//Signs all the data we dont wish to be tampered with
	public void generateSignature(PrivateKey privateKey) {
		signature = StringUtil.applyECDSASig(privateKey, getDataToVerify());
	}
	
	//Verifies the data we signed has not been tampered with
	public boolean verifySignature() {
		return StringUtil.verifyECDSASig(sender, getDataToVerify(), signature);
	}
	
	//Returns true if new transaction could be created
	public boolean processTransaction() {
		
		if (verifySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
		
		for (TransactionInput i: inputs) {
			i.UTXO = NoobChain.UTXOs.get(i.transactionOutputID);
		}
		
		//check if transaction is valid:
		if (getInputsValue() < NoobChain.minimumTransaction) {
			System.out.println("#Transaction inputs too small: " + getInputsValue());
			return false;
		}
		
		//generate Transaction Outputs:
		float leftOver = getInputsValue() - value;
		transactionId = calculateHash();
		outputs.add(new TransactionOutput(recipient, value, transactionId)); //send value to recipient
		outputs.add(new TransactionOutput(sender, leftOver, transactionId)); //send the left over change back to the sender
		
		//add outputs to Unspent list
		for (TransactionOutput o: outputs) {
			NoobChain.UTXOs.put(o.id, o);
		}
		
		//remove transaction inputs from UTXOs list as spent
		for(TransactionInput i: inputs) {
			if(i.UTXO == null) continue; //if Transaction cannot be found, skip it
			NoobChain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	//returns sum of inputs(UTXOs) values
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i: inputs) {
			if(i.UTXO == null) continue; //If Transaction cannot be found, skip it
			total += i.UTXO.value;
		}
		return total;
	}
	
	//returns sum of outputs:
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o: outputs) {
			total += o.value;
		}
		return total;
	}
}