package VanTB;
import java.security.*;
import java.util.ArrayList;

/* 交易类,生成/验证签名和验证交易 */
public class Transaction {
	
	public String transactionId; //交易的hash'ID
	public PublicKey sender; //发件人的公钥
	public PublicKey reciepient; //收件人的公钥
	public float value; //发送给收件人的金额
	public byte[] signature; //签名,防止其他人在我们的钱包中花费资金
	
	/*输入&输出*/
	//交易的输入
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	//交易的输出
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; //记录已生成的交易数量
	
	/**
	 * @Description: Transaction构造器
	 * @param from:PublicKey 发件人的公钥
	 * @param to:PublicKey 收件人的公钥
	 * @param value:float 交易的金额
	 * @param inputs:ArrayList<TransactionInput>  交易的输入
	 */
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}

		/**
	 * @Description: 使用SHA256计算交易hash(将用作其 Id)
	 * @return String 交易hash
	 */
	private String calulateHash() {
		sequence++; //增加Index以避免出现具有相同哈希的相同事务
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(reciepient) +
				Float.toString(value) + sequence
				);
	}
	
	/**
	 * @Description: 检测是否可以创建新事务
	 * @return 如果可以创建新事务返回true,否则返回false
	 */
	public boolean processTransaction() {
		
		if(verifySignature() == false) {
			System.out.println("#交易签名验证失败");
			return false;
		}
				
		//收集交易输入(确保其未被使用)
		for(TransactionInput i : inputs) {
			i.UTXO = VanTB.UTXOs.get(i.transactionOutputId);
		}

		//检查交易是否有效
		if(getInputsValue() < VanTB.minimumTransaction) {
			System.out.println("交易输入太小: " + getInputsValue());
			System.out.println("请输入大于" + VanTB.minimumTransaction+"的金额");
			return false;
		}
		
		//生成交易输出
		float leftOver = getInputsValue() - value; //获取交易输出后剩下的金额
		transactionId = calulateHash();
		outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //向收件人发送金额
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //将剩余的金额发送回发件人	
				
		//将输出添加到UTXOs
		for(TransactionOutput o : outputs) {
			VanTB.UTXOs.put(o.id , o);
		}
		
		//从 UTXO 列表中删除已花费的交易输入
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //如果找不到交易就跳过
			VanTB.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	/**
	 * @Description: 获取输入的金额
	 * @return total:float 返回输出金额
	 */
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //如果找不到交易则跳过
			total += i.UTXO.value;
		}
		return total;
	}
	
	/**
	 * @Description: 用私钥签署不希望被篡改的数据
	 * @param privateKey:PrivateKey
	 */
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey,data);
	}
	
	/**
	 * @Description: 验证签署的数据有没有被篡改
	 * @return boolean 如果没有被篡改则返回true,否则返回false
	 */
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	/**
	 * @Description: 获取输出的总金额
	 * @return total:float 返回输出总和
	 */
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	} 

}
