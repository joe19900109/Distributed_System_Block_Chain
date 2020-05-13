/**
 * @author Derek Lin
 *
 * This is the server side of TCP transport protocol, the client will send the Request to this
 * designated server. After receiving the message and Request, the server first will check the ID and
 * the sign is good or qualified. And then it will perform the service. Firstly, it will check the hashmap
 * for the ID and take out the existing value. After that the server will perform the operation of arithmetic
 * based on the input such as add, subtract or view and then send back the result to the client.
 */
package yunanl;

import com.google.gson.*;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class OperationServerTCP {
    private String e;
    private String n;
    private String id;
    private String signature;

    private void receiveRequest(Request request){
        id = request.getId();
        e = String.valueOf(request.getE());
        n = String.valueOf(request.getN());
        signature = request.getSignature();
    }

    private String generateID(){
        String tempKey = e + n;
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

    private boolean checkID(){
        String test = generateID();
        return test.equalsIgnoreCase(id);
    }

    private boolean checkSign(Request request){
        BigInteger bigSignature = new BigInteger(signature);
        BigInteger test = bigSignature.modPow(new BigInteger(e), new BigInteger(n));
        String message = "";
        if(request.getVariable1() == null){
            message = request.getId() + request.getE() + request.getN() + request.getOperation();
        }else{
            message = request.getId() + request.getE() + request.getN() + request.getOperation() + request.getVariable1() + request.getVariable2();
        }
        byte[] digest = null;
        try {
            byte[] bytesOfMessage = message.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            digest = md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] answer = new byte[digest.length+1];
        for(int i=1; i < digest.length+1; i++){
            answer[i] = digest[i-1];
        }
        BigInteger outcome = new BigInteger(answer);
        return test.equals(outcome);
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

    public static void main(String args[]) {
        System.out.println("Server is running");
        Socket clientSocket = null;
        ServerSocket listenSocket = null;
        int serverPort = 6789;
        try {
            listenSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        OperationServerTCP ost = new OperationServerTCP();
        BlockChain bc = new BlockChain();
        while (true) {
            try {
                clientSocket = listenSocket.accept();
                Scanner in = new Scanner(clientSocket.getInputStream());
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
                String input = in.nextLine();
                Gson gson = new GsonBuilder().create();
                Request request = gson.fromJson(input, Request.class);
                ost.receiveRequest(request);
                String operation = request.getOperation();
                Respond respond = new Respond();
                if(ost.checkID() && ost.checkSign(request)) {
                    long begin = 0;
                    long stop = 0;
                    long period = 0;
                    switch (operation) {
                        case "0":
                            respond.setOperation("0");
                            respond.setChainSize(bc.getChainSize());
                            respond.setHashesPerSecond(bc.hashesPerSecond());
                            respond.setDifficulty(bc.getLatestBlock().getDifficulty());
                            respond.setNonce(bc.getLatestBlock().getNonce());
                            respond.setChainHash(bc.getChainHash());
                            break;
                        case "1":
                            int difficulty = Integer.valueOf(request.getVariable1());
                            String data = request.getVariable2();
                            Block b = new Block(bc.getChainSize(), bc.getTime(), data, difficulty);
                            begin = System.currentTimeMillis();
                            bc.addBlock(b);
                            stop = System.currentTimeMillis();
                            period = stop - begin;
                            respond.setPeriod(period);
                            respond.setOperation("1");
                            break;
                        case "2":
                            begin = System.currentTimeMillis();
                            boolean result = bc.isChainValid();
                            if(!result) {
                                respond.setErrorMessage(bc.errorMessage);
                            }
                            stop = System.currentTimeMillis();
                            period = stop - begin;
                            respond.setChainValid(result);
                            respond.setPeriod(period);
                            respond.setOperation("2");
                            break;
                        case "3":
                            respond.setJson(bc.toString());
                            respond.setOperation("3");
                            break;
                        case "4":
                            int num = Integer.valueOf(request.getVariable1());
                            String newData = request.getVariable2();
                            bc.getBlock(num).setData(newData);
                            respond.setNewData(bc.getBlock(num).getData());
                            respond.setIndex(num);
                            respond.setOperation("4");
                            break;
                        case "5":
                            begin = System.currentTimeMillis();
                            bc.repairChain();
                            stop = System.currentTimeMillis();
                            period = stop - begin;
                            respond.setPeriod(period);
                            respond.setOperation("5");
                            break;
                    }
                    out.println(gson.toJson(respond));
                    out.flush();
                }
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
        }
    }

}
