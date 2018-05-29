package noobchain;

import java.security.Security;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;
//import com.google.gson.GsonBuilder;
//import java.util.Map;

public class NoobChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static int difficulty = 2;
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;
		
	public static void main(String[] args) {
		
		//Set up BouncyCastle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		//create new wallets
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet coinbase = new Wallet();
		
		//create genesis transaction that sends 100 coins to walletA
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey); //manually sign the genesis transaction
		genesisTransaction.transactionId = "0"; //manually set the transactionId to "0"
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //it is important to store the first transaction into the UTXOs list
		
		System.out.println("Creating and mining the genesis block...");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		//testing
		Block block1 = new Block(genesis.hash);
		System.out.println("\nWalletA balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is attempting to send (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA balance is: " + walletA.getBalance());
		System.out.println("\nWalletB balance is: " + walletB.getBalance());
		
		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is attempting to send (1000) to WalletB...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 100f));
		addBlock(block2);
		System.out.println("\nWalletA balance is: " + walletA.getBalance());
		System.out.println("\nWalletB balance is: " + walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB balance is: " + walletB.getBalance());
		System.out.println("\nWalletB is attempting to send (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20f));
		addBlock(block3);
		System.out.println("\nWalletA balance is: " + walletA.getBalance());
		System.out.println("\nWalletB balance is: " + walletB.getBalance());
		
		isChainValid();
		
	}
	
	public static boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
//		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		String hashTarget = StringUtil.getDifficultyString(difficulty);
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for (int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ) {
				System.out.println("#Current Hashes are not Equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(previousBlock.calculateHash())) {
				System.out.println("#Previous Hashes are not Equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0,difficulty).equals(hashTarget)) {
				System.out.println("#This block has not been mined");
				return false;
			}
			
			//loop through blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t<currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if (!currentTransaction.verifySignature()) {
					System.out.println("Signatue on transaction (" + t + ") is invalid");
					return false;
				}
				
				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are not equal to outputs on Transaction (" + t + ")");
					return false;
				}
				
				for (TransactionInput input: currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if (tempOutput == null) {
						System.out.println("#Referenced output on Transaction (" + t + ") is missing!");
						return false;
					}
					
					if (input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced output on Transaction (" + t + "): value is invalid!");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for (TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
					System.out.println("#Transaction (" + t + "): output recipient is not who it should be");
					return false;
				}
				
				if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
					System.out.println("#Transaction (" + t + "): output 'change' is not sender");
					return false;
				}
			}
		}
		
		System.out.println("Blockchain is Valid!!!");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}

}
