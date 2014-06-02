package ru.spbau.amanov.repl;

import java.util.*;

/**
 * Main class of interpretator.
 */
public class Interpreter {

    public Interpreter() {
        saveContext();
    }

    public void interpret(String input) {
        clean();
        ArrayList<Lexer.Token> tokens = lexer.lex(input);
        highliter.highlight(tokens, contextHist.peekLast());
        try {
            astRoot = parser.parse(tokens);
        } catch (SyntaxException e) {
            astRoot = null;
            errorMsg = getErrorMsg() + e.getMessage();
            errorPos = e.getErrorPos();
        }
    }

    public Float execute () {
        if (astRoot != null ) {
            AST.Exp result = astRoot.accept(eval);
            if (result instanceof AST.Num) {
                return ((AST.Num) result).number.floatValue();
            } else if (result instanceof AST.Assigment) {
                AST.Assigment assign = (AST.Assigment) result;
                if (assign.value instanceof AST.Num) {
                    return ((AST.Num) assign.value).number.floatValue();
                }
            }
        }
        errorMsg = getErrorMsg() + eval.errorMsg;
        return null;
    }

    public String simplify () {
        if (astRoot != null ) {
            return astRoot.accept(eval).accept(astPrinter);
        }
        return null;
    }

    public boolean isValid() {
        return astRoot != null;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void clean() {
        eval.errorMsg = "";
        errorMsg = "";
        astRoot = null;
        errorPos = 0;
    }

    public void repareContext() {
        contextHist.removeLast();
        eval.setContext(contextHist.peekLast());
    }

    public void saveContext() {
        Map<String, AST.Exp> context = new HashMap<>();
        context.putAll(eval.getContext());
        contextHist.add(context);
    }

    public int getErrorPos() {
        return errorPos;
    }

    public List<Highliter.HighlightRegion> getHighlightRegions(boolean isEvalMode) {
        return highliter.getHighlightRegions(isEvalMode);
    }

    private Lexer lexer = new Lexer();
    private Parser parser = new Parser();
    private AST.Exp astRoot;
    private Evaluator eval = new Evaluator(new HashMap<String, AST.Exp>());
    private AstPrinter astPrinter = new AstPrinter();
    private String errorMsg = "";
    private LinkedList<Map<String, AST.Exp>> contextHist = new LinkedList<>();
    private Highliter highliter = new Highliter();
    private int errorPos = 0;

}
