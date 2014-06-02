package ru.spbau.amanov.repl;

/**
 * This class provides abstract syntax tree.
 */
public class AST {

    public static abstract class Exp {
        public abstract <T> T accept(ExpVisitor<T> visitor);
        public Lexer.Placement place;
    }

    public static abstract class BiExp extends Exp {
        public final Exp left;
        public final Exp right;

        public BiExp(Exp left, Exp right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Var extends Exp {
        public final String varName;

        public Var(String name) {
            varName = name;
        }

        @Override
        public <T> T accept(ExpVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Num extends Exp {
        public final Number number;
        public Num(Number number) {
            this.number = number;
        }

        @Override
        public <T> T accept(ExpVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Sum extends BiExp {
        public Sum(Exp left, Exp right) {
            super(left, right);
        }

        @Override
        public <T> T accept(ExpVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Sub extends BiExp {
        public Sub(Exp left, Exp right) {
            super(left, right);
        }

        @Override
        public <T> T accept(ExpVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Mul extends BiExp {
        public Mul(Exp left, Exp right) {
            super(left, right);
        }
        @Override
        public <T> T accept(ExpVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Div extends BiExp {
        public Div(Exp left, Exp right) {
            super(left, right);
        }
        @Override
        public <T> T accept(ExpVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Assigment extends Exp {
        public Var var;
        public Exp value;

        public Assigment(Var v, Exp val) {
            var = v;
            value = val;
        }

        @Override
        public <T> T accept(ExpVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

}
