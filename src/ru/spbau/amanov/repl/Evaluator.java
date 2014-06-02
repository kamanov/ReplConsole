package ru.spbau.amanov.repl;

import java.util.Map;

/**
 * This class provides ast evaluation.
 */
public class Evaluator implements ExpVisitor<AST.Exp> {

    public Evaluator(Map<String, AST.Exp> c) {
        context = c;
    }

    @Override
    public AST.Exp visit(AST.Num num) {
        return num;
    }

    @Override
    public AST.Exp visit(AST.Sub sub) {
        AST.Exp op1 = sub.left.accept(this);
        AST.Exp op2 = sub.right.accept(this);
        if (op1 instanceof AST.Num && op2 instanceof AST.Num) {
            return new AST.Num(((AST.Num) op1).number.floatValue() - ((AST.Num) op2).number.floatValue());
        } else {
            return new AST.Sub(op1, op2);
        }
    }

    @Override
    public AST.Exp visit(AST.Sum sum) {
        AST.Exp op1 = sum.left.accept(this);
        AST.Exp op2 = sum.right.accept(this);
        if (op1 instanceof AST.Num && op2 instanceof AST.Num) {
            return new AST.Num(((AST.Num) op1).number.floatValue() + ((AST.Num) op2).number.floatValue());
        } else {
            return new AST.Sum(op1, op2);
        }
    }

    @Override
    public AST.Exp visit(AST.Mul mul) {
        AST.Exp op1 = mul.left.accept(this);
        AST.Exp op2 = mul.right.accept(this);
        if (op1 instanceof AST.Num && op2 instanceof AST.Num) {
            return new AST.Num(((AST.Num) op1).number.floatValue() * ((AST.Num) op2).number.floatValue());
        } else {
            return new AST.Mul(op1, op2);
        }
    }

    @Override
    public AST.Exp visit(AST.Div div) {
        AST.Exp op1 = div.left.accept(this);
        AST.Exp op2 = div.right.accept(this);
        if (op1 instanceof AST.Num && op2 instanceof AST.Num) {
            return new AST.Num(((AST.Num) op1).number.floatValue() / ((AST.Num) op2).number.floatValue());
        } else {
            return new AST.Div(op1, op2);
        }
    }


    @Override
    public AST.Exp visit(AST.Var var) {
        if (context.containsKey(var.varName)) {
            AST.Exp val = context.get(var.varName);
            return val.accept(this);
        } else {
            errorMsg += (var.varName + " is undefined; ");
            return var;
        }
    }

    @Override
    public AST.Exp visit(AST.Assigment assign) {
        AST.Exp res = assign.value.accept(this);
        context.put(assign.var.varName, res);
        return new AST.Assigment(assign.var, res);
    }

    public void setContext(Map<String, AST.Exp> c) {
        context = c;
    }

    public Map<String, AST.Exp> getContext() {
        return context;
    }

    private Map<String, AST.Exp> context;
    public String errorMsg = "";

}