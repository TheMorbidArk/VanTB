package VanTB;

/* 交易输入类 */
public class TransactionInput {
	public String transactionOutputId; //引用 TransactionOutputs -> transactionId
	public TransactionOutput UTXO; //包含未使用的交易输出
	
	/**
	 * @Description: TransactionInput 构造器
	 * @param transactionOutputId:String
	 */
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
