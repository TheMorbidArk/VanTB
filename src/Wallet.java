package VanTB;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* 钱包类 */
public class Wallet {
	
	public PrivateKey privateKey;	//私钥
	public PublicKey publicKey;	//公钥
	
	//未使用的交易输出
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	/**
	 * @Description: Wallet 构造器
	 */
	public Wallet() {
		generateKeyPair();
	}
		
	/**
	 * @Description: 生成密钥对
	 */
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random); //256 
	        KeyPair keyPair = keyGen.generateKeyPair();
	        // Set the public and private keys from the keyPair
	        privateKey = keyPair.getPrivate();
	        publicKey = keyPair.getPublic();
	        
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @Description: 返回余额并将此钱包拥有的UTXO存储在this.UTXOs中
	 * @return total:float 余额
	 */
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: VanTB.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) {//如果输出属于我（如果硬币属于我）
            	UTXOs.put(UTXO.id,UTXO); //将其添加到我们的未使用交易列表中
            	total += UTXO.value ; 
            }
        }  
		return total;
	}
	
	/**
	 * @Description: 从这个钱包生成并返回一个新的交易。
	 * @param _recipient:PublicKey 接受者的公钥
	 * @param value:float 交易的金额
	 * @return newTransaction:Transaction
	 */
	public Transaction sendFunds(PublicKey _recipient,float value ) {
		if(getBalance() < value) {
			System.out.println("#没有足够的资金进行交易");
			return null;
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}
	
}


