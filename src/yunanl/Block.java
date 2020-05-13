package yunanl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

public class Block {
    private int index;
    private Timestamp timestamp;
    private String data;
    private int difficulty;
    private String previousHash;
    private BigInteger nonce;

    public Block(int index, Timestamp timestamp, String data, int difficulty){
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = new BigInteger("0");
        this.previousHash = "";
    }

    public String calculateHash(){
        String input = String.valueOf(index) + timestamp + data + previousHash + nonce + difficulty;
        byte[] temp = null;
        try {
            byte[] messageBytes = input.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            temp = md.digest(messageBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return convertToHex(temp);
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public int getIndex() {
        return index;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String proofOfWork(){
        String target = "";
        for(int i = 0; i < difficulty; i++){
            target += "0";
        }
        String output = calculateHash();
        while(!output.substring(0, difficulty).equals(target)){
            nonce = nonce.add(BigInteger.ONE);
            output = calculateHash();
        }
        return output;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{\"index\":" + index + ",");
        sb.append("\"time stamp\":\"" + timestamp + "\",");
        sb.append("\"Tx \":\"" + data + "\",");
        sb.append("\"PrevHash\":\"" + previousHash + "\",");
        sb.append("\"nonce\":" + nonce + ",");
        sb.append("\"difficulty\":" + difficulty +"}");
        return sb.toString();
    }

}
