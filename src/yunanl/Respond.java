package yunanl;

import java.math.BigInteger;

public class Respond {
    private String operation;
    private int chainSize;
    private int hashesPerSecond;
    private int difficulty;
    private BigInteger nonce;
    private String chainHash;
    private long period;
    private String json;
    private String newData;
    private boolean isChainValid;
    private int index;
    private String errorMessage;
    public int getChainSize() {
        return chainSize;
    }

    public void setChainSize(int chainSize) {
        this.chainSize = chainSize;
    }

    public int getHashesPerSecond() {
        return hashesPerSecond;
    }

    public void setHashesPerSecond(int hashesPerSecond) {
        this.hashesPerSecond = hashesPerSecond;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public String getChainHash() {
        return chainHash;
    }

    public void setChainHash(String chainHash) {
        this.chainHash = chainHash;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getNewData() {
        return newData;
    }

    public void setNewData(String newData) {
        this.newData = newData;
    }

    public boolean isChainValid() {
        return isChainValid;
    }

    public void setChainValid(boolean chainValid) {
        isChainValid = chainValid;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
