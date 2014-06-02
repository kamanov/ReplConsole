package ru.spbau.amanov.repl;

/**
 * Expression visitor interface.
 */
public interface ExpVisitor<T> {
    T visit(AST.Num num);
    T visit(AST.Sum sum);
    T visit(AST.Mul mul);
    T visit(AST.Div div);
    T visit(AST.Sub sub);
    T visit(AST.Var var);
    T visit(AST.Assigment assign);
}