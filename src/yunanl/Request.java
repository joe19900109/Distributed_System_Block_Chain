package yunanl;

import java.math.BigInteger;

public class Request {
    private String id;
    private BigInteger e;
    private BigInteger n;
    private String operation;
    private String variable1;
    private String variable2;
    private String signature;
    public void setId(String id){
        this.id = id;
    }
    public void setE(BigInteger e){
        this.e = e;
    }
    public void setN(BigInteger n){
        this.n = n;
    }
    public void setOperation(String operation){
        this.operation = operation;
    }
    public void setVariable1(String variable1){
        this.variable1 = variable1;
    }
    public void setVariable2(String variable2){
        this.variable2 = variable2;
    }
    public void setSignature(String signature){
        this.signature = signature;
    }
    public String getId(){
        return id;
    }
    public BigInteger getE(){
        return e;
    }
    public BigInteger getN() {
        return n;
    }

    public String getOperation() {
        return operation;
    }

    public String getVariable1() {
        return variable1;
    }

    public String getVariable2() {
        return variable2;
    }

    public String getSignature() {
        return signature;
    }
}
