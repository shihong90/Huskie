package imooc.naga.core.exception;

import lombok.Getter;

@Getter
public class NagaException extends RuntimeException {
    private String errorMessage;
    private int errorCode;

    public NagaException(String errorMessage,int errorCode,Throwable cause){
        super(cause);
        this.errorMessage=errorMessage;
        this.errorCode=errorCode;
    }

    public NagaException(String errorMessage,int errorCode){
        super(errorMessage);
        this.errorMessage=errorMessage;
        this.errorCode=errorCode;
    }
}
