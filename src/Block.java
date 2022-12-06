package VanTB;

import java.util.ArrayList;
import java.util.Date;

/* Block 类是对区块链中块的封装 */
public class Block {

	public String hash;		//this.块的哈希值
	public String previousHash; 		//记录上一区块的哈希值
	public String merkleRoot;		//哈希树，校验当前区块里所有的交易记录
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //事务ArrayList，记录交易事务
	public long timeStamp; //时间戳，毫秒数，用于生成hash
	public int nonce;//记录计算hash值的运算次数
	/**
	 * @Description: Block 构造器，初始化Block
	 * @param previousHash:String 上一区块的哈希值
	 */
	public Block(String previousHash ) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash(); //计算hash值
	}

	/**
	 * @Description: 使用SHA256算法并根据块内容计算hash值
	 * @return hash值
	 */
	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				merkleRoot
				);
		return calculatedhash;
	}
	
	/**
	 * @Description: 设置运算难度&匹配合法hash值,增加随机数值直到达到哈希目标。
	 * @param difficulty:int 难度系数
	 */
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty); //创建一个 difficulty*0 的字符串
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		System.out.println("Block 已挖掘!!! : " + hash);
	}
	
	/**
	 * @Description: 向该区块添加交易
	 * @param transaction:Transaction 
	 * @return  boolean
	 */
	public boolean addTransaction(Transaction transaction) {
		//处理交易并检查是否有效，除非块是创世块然后忽略
		if(transaction == null) return false;
		if((!"0".equals(previousHash))) {
			if((transaction.processTransaction() != true)) {
				System.out.println("交易未能处理");
				return false;
			}
		}

		//添加 transaction,向区块添加交易
		transactions.add(transaction);
		System.out.println("交易成功，添加进Block");
		return true;
	}
	
}
