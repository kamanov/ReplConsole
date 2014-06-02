package ru.spbau.amanov.repl;

/**
 * This class provides syntax exception.
 */
public class SyntaxException extends Exception {
    public SyntaxException(String msg, int pos) {
        super(msg);
        errorPos = pos;
    }

    public int getErrorPos() {
        return errorPos;
    }

    private int errorPos;
}
