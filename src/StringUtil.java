package VanTB;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import com.google.gson.GsonBuilder;
import java.util.List;

/* 工具类 */
public class StringUtil {

    /**
     * @Description: 将Sha256应用于字符串并返回结果 
     * @param input:String
     * @return hashString
     */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //将 sha256 应用于input字符串
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();// 以十六进制保存hash
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description: 应用 ECDSA 签名并返回结果(byte[])
     * 									接受发送者的私钥和字符串输入，对其进行签名并返回一个字节数组
     * @param privateKey:PrivateKey 私钥
     * @param input:String 
     * @return ECDSA签名结果
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String input){
        Signature dsa;
        byte[] output = new byte[0];

        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            output = dsa.sign();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    /**
     * @Description: 接受签名、公钥和字符串数据，应用ECDSA验证签名是否有效
     * @param publicKey:PublicKey
     * @param data:String
     * @param signature:byte[] 
     * @return boolean 如果签名有效则返回true,否则返回false
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature){
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description: 返回难度字符串目标，与哈希进行比较。 例如难度为 5 将返回“00000”
     * @param difficulty:int
     * @return String 难度对应的目标字符串
     */
    public static String getDificultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    /**
     * @Description: 将 Object 转换为 json 字符串
     * @param o:Object
     * @return String json 字符串
     */
    public static String getJson(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }

    /**
     * @Description: 从任何键返回Base64编码字符串
     * @param key:Key
     * @return String Base64字符串
     */
     public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * @Description: 通过交易数组,返回一个merkleRoot
     * @extra: merkleRoot -> 通过SHA256层层相加
     * @param transactions:ArrayList<Transaction>
     * @return merkleRoot:String
     */
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }

}
