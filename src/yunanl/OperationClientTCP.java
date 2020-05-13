/**
 * @author Derek Lin
 *
 * This is the client side of TCP transport protocol, the client will send the Request to the
 * designated port server and wait for the server's feedback, the client can choose the function by the
 * table shown, they can be add, subtract, view and quit by the table with 1, 2, 3, 4. If the input is other
 * than these, the program will require the user to reinput. Before sending the Request to the server, the
 * client will generate the public and private key by RSA algorithm. The id is computed by the hash function
 * of the combination of public key of e and n. And it will create the Request by combining id , e, n, operation
 * they choose and the input number. Moreover, at last it will add the signature which is the hash function of
 * all the message above. At last, the client will pass all the Request as stream passing to the server and wait
 * for the feedback. This program implements the concept of proxy which make all connection relevant codes to one
 * method rather than put all them in the main method. This is for separation of concerns.
 */
package yunanl;

import com.google.gson.*;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class OperationClientTCP {
    private BigInteger d;
    private BigInteger e;
    private BigInteger n;
    private String id;
    public OperationClientTCP(){
        createKey();
        id = generateID();
    }
    private void createKey(){
        Random rand = new Random();
        BigInteger p = new BigInteger(400, 100, rand);
        BigInteger q = new BigInteger(400, 100, rand);
        n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("65537");
        d = e.modInverse(phi);
    }
    private String generateID(){
        String tempKey = e.toString() + n.toString();
        byte[] tempID = null;
        try {
            byte[] messageBytes = tempKey.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            tempID = md.digest(messageBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] ID = new byte[20];
        int end = tempID.length - 20;
        for(int i = 0; i < ID.length; i++){
            ID[i] = tempID[end];
            end++;
        }
        return convertToHex(ID);
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
    private String sign(String str){
        byte[] digest = null;
        try {
            byte[] bytesOfMessage = str.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            digest = md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] output = new byte[digest.length+1];
        output[0] = 0;
        for(int i = 1; i < digest.length+1; i++){
            output[i] = digest[i-1];
        }
        BigInteger m = new BigInteger(output);
        BigInteger c = m.modPow(d, n);
        return c.toString();
    }

    private String getSignature(ArrayList<String> list){
        StringBuilder sb = new StringBuilder();
        String index = list.get(0);
        sb.append(id);
        sb.append(e);
        sb.append(n);
        switch (index){
            case "0":
            case "2":
            case "3":
            case "5":
                sb.append(list.get(0));
                break;
            case "1":
            case "4":
                sb.append(list.get(0));
                sb.append(list.get(1));
                sb.append(list.get(2));
        }
        return sign(sb.toString());
    }

    private String createRequest(ArrayList<String> list){
        Request request = new Request();
        request.setId(id);
        request.setE(e);
        request.setN(n);
        String index = list.get(0);
        switch (index){
            case "0":
            case "2":
            case "3":
            case "5":
                request.setOperation(index);
                request.setSignature(getSignature(list));
                break;
            case "1":
            case "4":
                request.setOperation(index);
                request.setVariable1(list.get(1));
                request.setVariable2(list.get(2));
                request.setSignature(getSignature(list));
                break;
        }
        Gson gson = new GsonBuilder().create();
        return gson.toJson(request);
    }

    private String proxy(String input){
        Socket clientSocket = null;
        String res = null;
        String output = null;
        try {
            int serverPort = 6789;
            clientSocket = new Socket("localhost", serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            out.println(input);
            out.flush();
            res = in.readLine();
            Gson gson = new GsonBuilder().create();
            Respond respond = gson.fromJson(res, Respond.class);
            output = printResponse(respond);
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
            }
        }
        return output;
    }

    private String printResponse(Respond respond){
        String operation = respond.getOperation();
        StringBuilder sb = new StringBuilder();
        long period = 0;
        switch (operation){
            case "0":
                int chainSize = respond.getChainSize();
                int hashesPerSecond = respond.getHashesPerSecond();
                int difficulty = respond.getDifficulty();
                BigInteger nonce = respond.getNonce();
                String chainHash = respond.getChainHash();
                sb.append("Current size of chain: " + chainSize);
                sb.append("\n");
                sb.append("Current hashes per second by this machine: " + hashesPerSecond);
                sb.append("\n");
                sb.append("Difficulty of most recent block: " + difficulty);
                sb.append("\n");
                sb.append("Nonce for most recent block: " + nonce);
                sb.append("\n");
                sb.append("Chain hash:");
                sb.append("\n");
                sb.append(chainHash);
                break;
            case "1":
                period = respond.getPeriod();
                sb.append("Total execution time to add this block was " + period + " milliseconds");
                break;
            case "2":
                boolean isChainValid = respond.isChainValid();
                String errorMessage = "";
                if(!isChainValid){
                    errorMessage = respond.getErrorMessage();
                    sb.append(errorMessage);
                    sb.append("\n");
                }
                period = respond.getPeriod();
                sb.append("Chain verification: " + isChainValid);
                sb.append("\n");
                sb.append("Total execution time required to verify the chain was " + period + " milliseconds");
                break;
            case "3":
                String json = respond.getJson();
                sb.append(json);
                break;
            case "4":
                int index = respond.getIndex();
                String newData = respond.getNewData();
                sb.append("Block " + index + " now holds " + newData);
                break;
            case "5":
                period = respond.getPeriod();
                sb.append("Total execution time required to repair the chain was " + period + " milliseconds");
                break;
        }
        return sb.toString();
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

    public static void main(String args[]){
        Scanner input = new Scanner(System.in);
        Integer choice = null;
        String request = null;
        OperationClientTCP oct = new OperationClientTCP();
        oct.menuBar();
        choice = Integer.parseInt(input.nextLine());
        while(choice != 6){
            ArrayList<String> requestList = new ArrayList<>();
            switch(choice) {
                case 0:
                    requestList.add("0");
                    break;
                case 1:
                    System.out.println("Enter difficulty > 0");
                    int difficulty = Integer.valueOf(input.nextLine());
                    System.out.println("Enter transaction");
                    String data = input.nextLine();
                    requestList.add("1");
                    requestList.add(String.valueOf(difficulty));
                    requestList.add(data);
                    break;
                case 2:
                    System.out.println("Verifying entire chain");
                    requestList.add("2");
                    break;
                case 3:
                    System.out.println("View the Blockchain");
                    requestList.add("3");
                    break;
                case 4:
                    System.out.println("Corrupt the Blockchain");
                    System.out.println("Enter block ID of block to corrupt");
                    int index = Integer.valueOf(input.nextLine());
                    System.out.println("Enter new data for block " + index);
                    String newData = input.nextLine();
                    requestList.add("4");
                    requestList.add(String.valueOf(index));
                    requestList.add(newData);
                    break;
                case 5:
                    System.out.println("Repairing the entire chain");
                    requestList.add("5");
                    break;
            }
            request = oct.createRequest(requestList);
            System.out.println(oct.proxy(request));
            oct.menuBar();
            choice = Integer.valueOf(input.nextLine());
        }
        System.out.println("Bye!");

    }

}
