package br.com.wferreiracosta.louis.exceptions;

public class TransactionException extends RuntimeException {

    public TransactionException() {
    }

    public TransactionException(String message) {
        super(message);
    }

}
