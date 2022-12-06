package VanTB;

import java.security.PublicKey;

/* 交易输出类 */
public class TransactionOutput {
	public String id;
	public PublicKey reciepient; //接收者，也被称为硬币的新主人
	public float value; //输出金额
	public String parentTransactionId; //this.交易输出的 ID
	
	/**
	 * @Description: TransactionOutput构造器
	 * @param reciepient:PublicKey 接收者公钥	 
	 * @param value:float 金额
	 * @param parentTransactionId:String this.交易输出的 ID
	 */
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
	}
	
	/**
	 * @Description: 检查硬币是否属于你
	 * @param publicKey:PublicKey
	 * @return boolean 如果属于你返回true,不属于返回false
	 */
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
}
