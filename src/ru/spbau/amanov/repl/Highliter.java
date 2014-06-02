package ru.spbau.amanov.repl;

import java.util.*;

/**
 * This class provides highlighting user input.
 */
public class Highliter {

    public static class HighlightRegion {
        public HighlightRegion(Lexer.Placement p, String s) {
            place = p;
            style = s;
        }
        public Lexer.Placement place;
        public String style;
    }

    public void highlight(ArrayList<Lexer.Token> tokens, Map<String, AST.Exp> currentContext) {
        this.tokens = tokens;
        this.currentContext = currentContext;
        regions.clear();
        for (Lexer.Token t : tokens) {
            switch (t.type) {
                case VARIABLE :
                case NUMBER : regions.add(new HighlightRegion(t.place, "operand"));
                    break;
                case SUB:
                case ADD:
                case MUL:
                case DIV:
                case ASSIGN: regions.add(new HighlightRegion(t.place, "operator"));
                    break;
                default:
                    break;
            }
        }
    }

    public List<HighlightRegion> getHighlightRegions(boolean lightUndefVars) {
        if (lightUndefVars) {
            highlightUndefVars();
        }
        return regions;
    }

    public void highlightUndefVars() {
        for (int i = 0; i < tokens.size(); i++) {
            Lexer.Token t = tokens.get(i);
            if (t.type.equals(Lexer.TokenType.VARIABLE) && !currentContext.containsKey(t.data)) {
                if ((i < tokens.size() - 1) && !tokens.get(i + 1).type.equals(Lexer.TokenType.ASSIGN))
                regions.add(new HighlightRegion(t.place, "error"));
            }
        }
    }


    private List<HighlightRegion> regions = new LinkedList<>();
    private ArrayList<Lexer.Token> tokens;
    private Map<String, AST.Exp> currentContext;
}
