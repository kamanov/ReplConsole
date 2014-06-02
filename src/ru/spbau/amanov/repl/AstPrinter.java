package ru.spbau.amanov.repl;

/**
 * This class provides printing simplified ast.
 */
public class AstPrinter implements ExpVisitor<String> {

    @Override
    public String visit(AST.Num exp) {
        return exp.number.toString();
    }

    @Override
    public String visit(AST.Div div) {
        return div.left.accept(this) + " / " + div.right.accept(this);
    }

    @Override
    public String visit(AST.Mul mul) {
        return mul.left.accept(this) + " * " + mul.right.accept(this);
    }

    @Override
    public String visit(AST.Sum sum) {
        return "(" + sum.left.accept(this) + " + " + sum.right.accept(this) + ")";
    }

    @Override
    public String visit(AST.Var var) {
        return var.varName;
    }

    @Override
    public String visit(AST.Assigment assign) {
        return assign.value.accept(this);
    }

    @Override
    public String visit(AST.Sub sub) {
        return "(" + sub.left.accept(this) + " - " + sub.right.accept(this) + ")";
    }
}
