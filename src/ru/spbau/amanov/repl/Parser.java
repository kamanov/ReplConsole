package ru.spbau.amanov.repl;

import java.util.*;
/**
 * This class provides syntax analyze.
 */
public class Parser {

    public AST.Exp parse(ArrayList<Lexer.Token> t) throws SyntaxException {
        tokens = t;
        currentToken = 0;
        return parseStatement();
    }

    private AST.Exp parseStatement() throws SyntaxException {
        AST.Exp res;
        if (tokens.size() > 1 && tokens.get(1).equals(Lexer.TokenType.ASSIGN)) {
            res = parseVariableAssignement();
        } else {
            res = parseExpresion();
        }

        if (currentToken != tokens.size() - 1) {
            raiseSyntaxError();
        }
        return res;
    }

    private AST.Exp parseExpresion() throws SyntaxException {
        AST.Exp left;

        if(isUnaryOperation()){
            left = new AST.Num(0);
        } else {
            left = parseSummand();
        }

        while ( checkType(Lexer.TokenType.SUB) || checkType(Lexer.TokenType.ADD) ) {
            left = processExpressionSummands(left);
        }
        return left;
    }

    private AST.Exp parseVariableAssignement() throws SyntaxException {
        assertType(Lexer.TokenType.VARIABLE);
        AST.Var var = new AST.Var(getCurrentToken().data);
        nextToken();
        assertType(Lexer.TokenType.ASSIGN);
        nextToken();
        AST.Exp value = parseExpresion();
        return new AST.Assigment(var, value);
    }


    private boolean checkType(Lexer.TokenType t) {
        return tokens.get(currentToken).equals(t);
    }

    private boolean nextToken() throws SyntaxException {
        currentToken++;
        if (currentToken == tokens.size()) {
            raiseSyntaxError();
        }
        return (currentToken < tokens.size());
    }


    private AST.Exp parseSummand() throws SyntaxException {
        AST.Exp left = parseFactor();
        while ( checkType(Lexer.TokenType.MUL) || checkType(Lexer.TokenType.DIV) ) {
            left = processSummandFactors(left);
        }
        return left;
    }

    private AST.Exp parseFactor() throws SyntaxException {
        if(checkType(Lexer.TokenType.NUMBER)) {
            return parseNumber();
        }
        else if(checkType(Lexer.TokenType.LEFTBRACKET) ) {
            return parseNestedExpression();
        } else if(checkType(Lexer.TokenType.VARIABLE)) {
            return parseVariable();
        } else {
            raiseSyntaxError();
            return null;
        }
    }

    private AST.Exp parseVariable() throws SyntaxException {
        String varName = getCurrentToken().data;
        nextToken();
        return new AST.Var(varName);
    }

    private Lexer.Token getCurrentToken() {
        return tokens.get(currentToken);
    }

    private boolean isUnaryOperation() {
        return checkType(Lexer.TokenType.SUB);
    }

    private AST.Exp processExpressionSummands(AST.Exp left) throws SyntaxException {
        Lexer.TokenType op = getCurrentToken().type;
        nextToken();
        AST.Exp right = parseSummand();
        if (op.equals(Lexer.TokenType.ADD)) {
            return new AST.Sum(left, right);
        } else {
            return new AST.Sub(left, right);
        }
    }

    private AST.Exp processSummandFactors(AST.Exp left) throws SyntaxException {
        Lexer.TokenType op = tokens.get(currentToken).type;
        nextToken();
        AST.Exp right = parseFactor();
        if (op.equals(Lexer.TokenType.MUL)) {
            return new AST.Mul(left, right);
        } else {
            return new AST.Div(left, right);
        }
    }

    private AST.Exp parseNumber() throws SyntaxException {
        int value = Integer.parseInt(tokens.get(currentToken).data);
        nextToken();
        return new AST.Num(value);
    }

    private AST.Exp parseNestedExpression() throws SyntaxException {
        nextToken();
        AST.Exp expr = parseExpresion();
        assertType(Lexer.TokenType.RIGHTBRACKET);
        nextToken();
        return expr;
    }

    private void raiseSyntaxError() throws SyntaxException {
        throw new SyntaxException("invalid syntax", getCurrentToken().place.offset);
    }

    private void assertType(Lexer.TokenType type) throws SyntaxException {
        if(!checkType(type)) {
            raiseSyntaxError();
        }
    }



    private ArrayList<Lexer.Token> tokens;
    private int currentToken = 0;

}
