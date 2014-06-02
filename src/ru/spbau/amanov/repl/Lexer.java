package ru.spbau.amanov.repl;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class provides lexical analyze.
 */
public class Lexer {
    public static enum TokenType {
        NUMBER("[0-9]+"),
        SUB("-"),
        ADD("\\+"),
        DIV("/"),
        MUL("\\*"),
        WHITESPACE("[ \t\f\r\n]+"),
        VARIABLE("[a-zA-Z_][a-zA-Z0-9]*"),
        LEFTBRACKET("\\("),
        RIGHTBRACKET("\\)"),
        ASSIGN("="),
        UNKNOWN("\\p{ASCII}");

        public final String pattern;

        private TokenType(String pattern) {
            this.pattern = pattern;
        }
    }

    public static class Placement {
        public int offset = 0;
        public int len = 0;
    }

    public static class Token {
        public TokenType type;
        public String data;
        public Placement place = new Placement();

        public Token(TokenType type, String data, int offset) {
            this.type = type;
            this.data = data;
            place.offset = offset;
            place.len = data.length();
        }

        public boolean equals(TokenType t) {
            return type.equals(t);
        }

    }

    public static ArrayList<Token> lex(String input) {
        ArrayList<Token> tokens = new ArrayList<Token>();

        StringBuffer tokenPatternsBuffer = new StringBuffer();
        for (TokenType tokenType : TokenType.values())
            tokenPatternsBuffer.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
        Pattern tokenPatterns = Pattern.compile(new String(tokenPatternsBuffer.substring(1)));

        // Begin matching tokens
        Matcher matcher = tokenPatterns.matcher(input);

        while (matcher.find()) {
            if (matcher.group(TokenType.VARIABLE.name()) != null) {
                tokens.add(new Token(TokenType.VARIABLE, matcher.group(TokenType.VARIABLE.name()), matcher.start()));
            } else if (matcher.group(TokenType.NUMBER.name()) != null) {
                tokens.add(new Token(TokenType.NUMBER, matcher.group(TokenType.NUMBER.name()), matcher.start()));
            } else if (matcher.group(TokenType.SUB.name()) != null) {
                tokens.add(new Token(TokenType.SUB, matcher.group(TokenType.SUB.name()), matcher.start()));
            } else if (matcher.group(TokenType.ADD.name()) != null) {
                tokens.add(new Token(TokenType.ADD, matcher.group(TokenType.ADD.name()), matcher.start()));
            } else if (matcher.group(TokenType.DIV.name()) != null) {
                tokens.add(new Token(TokenType.DIV, matcher.group(TokenType.DIV.name()), matcher.start()));
            } else if (matcher.group(TokenType.MUL.name()) != null) {
                tokens.add(new Token(TokenType.MUL, matcher.group(TokenType.MUL.name()), matcher.start()));
            } else if (matcher.group(TokenType.WHITESPACE.name()) != null) {
                continue;
            } else if (matcher.group(TokenType.ASSIGN.name()) != null) {
                tokens.add(new Token(TokenType.ASSIGN, matcher.group(TokenType.ASSIGN.name()), matcher.start()));
            } else if (matcher.group(TokenType.LEFTBRACKET.name()) != null) {
                tokens.add(new Token(TokenType.LEFTBRACKET, matcher.group(TokenType.LEFTBRACKET.name()), matcher.start()));
            } else if (matcher.group(TokenType.RIGHTBRACKET.name()) != null) {
                tokens.add(new Token(TokenType.RIGHTBRACKET, matcher.group(TokenType.RIGHTBRACKET.name()), matcher.start()));
            } else if (matcher.group(TokenType.UNKNOWN.name()) != null) {
                tokens.add(new Token(TokenType.UNKNOWN, matcher.group(TokenType.UNKNOWN.name()), matcher.start()));
            }
        }

        tokens.add(new Token(TokenType.UNKNOWN, "", -1));
        return tokens;
    }
}