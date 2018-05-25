package noobchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import com.google.gson.GsonBuilder;

public class NoobChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static int difficulty = 2;
	public static Wallet walletA;
	public static Wallet walletB;
	
	private static int blockCount = 1;
	
	public static void main(String[] args) {
		
		//Set up BouncyCastle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		//create new wallets
		walletA = new Wallet();
		walletB = new Wallet();
		//test public and private keys
		System.out.println("Public and Private Keys: walletA");
		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		System.out.println("Public and Private Keys: walletB");
		System.out.println(StringUtil.getStringFromKey(walletB.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletB.publicKey));
		//Create test transaction from walletA to walletB
		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
		transaction.generateSignature(walletA.privateKey);
		//verify that the signature works and verify it from the public key
		System.out.println("Is Signature verified? ");
		System.out.println(transaction.verifySignature());
	}
	
	private static void addBlock(String body, String previousHash) {
		blockchain.add(new Block(body, previousHash));
		System.out.println("Trying to mine Block " + blockCount + "...");
		blockchain.get(blockCount-1).mineBlock(difficulty);
		blockCount++;
	}
	
	public static boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		
		//loop through blockchain to check hashes:
		for (int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ) {
				System.out.println("Current Hashes are not Equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(previousBlock.calculateHash())) {
				System.out.println("Previous Hashes are not Equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0,difficulty).equals(hashTarget)) {
				System.out.println("This block has not been mined");
				return false;
			}
		}
		return true;
	}

}
