package yunanl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

public class BlockChain {
    ArrayList<Block> blocks;
    String chainHash;
    String errorMessage;
    public BlockChain(){
        blocks = new ArrayList<>();
        chainHash = "";
        Block b = new Block(0, getTime(), "Genesis", 2);
        addBlock(b);
    }

    public void addBlock(Block b){
        b.setPreviousHash(chainHash);
        chainHash = b.proofOfWork();
        blocks.add(b);
    }

    public boolean isChainValid(){
        //check the chain from the first one to the one before the last
        for(int i = 0; i < blocks.size()-1; i++){
            Block present = blocks.get(i);
            Block next = blocks.get(i+1);
            if(!checkHash(present, next) || !checkBlock(present)){
                String target = "";
                for(int j = 0; j < present.getDifficulty(); j++){
                    target += "0";
                }
                errorMessage = "..Improper hash on node " + present.getIndex() + " Does not begin with " + target;
                System.out.println(errorMessage);
                return false;
            }
        }
        //check the last one chain
        Block last = getLatestBlock();
        if(!checkHash(last, null) || !checkBlock(last)){
            String target = "";
            for(int j = 0; j < last.getDifficulty(); j++){
                target += "0";
            }
            errorMessage = "..Improper hash on node " + last.getIndex() + " Does not begin with " + target;
            System.out.println(errorMessage);
            return false;
        }
        return true;
    }

    public boolean checkHash(Block b, Block next){
        String temp = b.calculateHash();
        //if this chain is the last one
        if(next ==  null){
            return temp.equals(chainHash);
        }
        //the chain is not the last one
        return temp.equals(next.getPreviousHash());
    }

    public boolean checkBlock(Block b){
        String temp = b.calculateHash();
        String target = "";
        for(int i = 0; i < b.getDifficulty(); i++) {
            target += "0";
        }
        return temp.substring(0,b.getDifficulty()).equals(target);
    }

    public void repairChain(){
        boolean isProblem = false;
        for(int i = 0; i < blocks.size(); i++){
            if(!checkBlock(blocks.get(i))){
                isProblem = true;
                break;
            }
        }
        if(isProblem){
            String hash = "";
            for(int i = 0; i < blocks.size()-1; i++){
                hash = blocks.get(i).proofOfWork();
                blocks.get(i+1).setPreviousHash(hash);
                }
            hash = getLatestBlock().proofOfWork();
            chainHash = hash;
        }
    }

    public int getChainSize(){
        return blocks.size();
    }

    public Block getLatestBlock(){
        return blocks.get(blocks.size()-1);
    }

    public Timestamp getTime(){
        return new Timestamp(System.currentTimeMillis());
    }

    public int hashesPerSecond(){
        int count = 0;
        long begin = System.currentTimeMillis();
        while(System.currentTimeMillis() - begin < 1000){
            hashFuction("00000000");
            count++;
        }
        return count;
    }

    private String hashFuction(String input){
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

    private String convertToHex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    sb.append((char) ('0' + halfbyte));
                else
                    sb.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return sb.toString();
    }

    public String getChainHash() {
        return chainHash;
    }

    public Block getBlock(int index){
        return blocks.get(index);
    }

    private void menuBar(){
        System.out.println("0. View basic blockchain status");
        System.out.println("1. Add a transaction to the blockchain");
        System.out.println("2. Verify the blockchain");
        System.out.println("3. View the blockchain");
        System.out.println("4. Corrupt the chain");
        System.out.println("5. Hide the corruption by repairing the chain");
        System.out.println("6. Exit");
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{\"ds_chain\":[");
        for(Block b : blocks) {
            sb.append(b.toString());
            sb.append(",");
        }
        sb = new StringBuffer(sb.substring(0, sb.length()-1));
        sb.append("],");
        sb.append("\"chainHash\":\"" + chainHash + "\"");
        sb.append("}");
        String output = sb.toString();
        return output;
    }

    public static void main(String[] args){
        BlockChain bc = new BlockChain();
        Scanner input = new Scanner(System.in);
        bc.menuBar();
        int choice = Integer.valueOf(input.nextLine());
        long begin = 0;
        long stop = 0;
        long period = 0;
        while(choice != 6){
            switch(choice) {
                case 0:
                    System.out.println("Current size of chain: " + bc.getChainSize());
                    System.out.println("Current hashes per second by this machine: " + bc.hashesPerSecond());
                    System.out.println("Difficulty of most recent block: " + bc.getLatestBlock().getDifficulty());
                    System.out.println("Nonce for most recent block: " + bc.getLatestBlock().getNonce());
                    System.out.println("Chain hash:");
                    System.out.println(bc.getChainHash());
                    break;
                case 1:
                    System.out.println("Enter difficulty > 0");
                    int difficulty = Integer.valueOf(input.nextLine());
                    System.out.println("Enter transaction");
                    String data = input.nextLine();
                    Block b = new Block(bc.getChainSize(), bc.getTime(), data, difficulty);
                    begin = System.currentTimeMillis();
                    bc.addBlock(b);
                    stop = System.currentTimeMillis();
                    period = stop - begin;
                    System.out.println("Total execution time to add this block was " + period + " milliseconds");
                    break;
                case 2:
                    System.out.println("Verifying entire chain");
                    begin = System.currentTimeMillis();
                    System.out.println("Chain verification: " + bc.isChainValid());
                    stop = System.currentTimeMillis();
                    period = stop - begin;
                    System.out.println("Total execution time required to verify the chain was " + period + " milliseconds");
                    break;
                case 3:
                    System.out.println("View the Blockchain");
                    System.out.println(bc);
                    break;
                case 4:
                    System.out.println("Corrupt the Blockchain");
                    System.out.println("Enter block ID of block to corrupt");
                    int index = Integer.valueOf(input.nextLine());
                    System.out.println("Enter new data for block " + index);
                    String newData = input.nextLine();
                    bc.getBlock(index).setData(newData);
                    System.out.println("Block " + index + " now holds " + bc.getBlock(index).getData());
                    break;
                case 5:
                    System.out.println("Repairing the entire chain");
                    begin = System.currentTimeMillis();
                    bc.repairChain();
                    stop = System.currentTimeMillis();
                    period = stop - begin;
                    System.out.println("Total execution time required to repair the chain was " + period + " milliseconds");
                    break;
            }
            bc.menuBar();
            choice = Integer.valueOf(input.nextLine());
        }
        System.out.println("Bye!");

    }

}
