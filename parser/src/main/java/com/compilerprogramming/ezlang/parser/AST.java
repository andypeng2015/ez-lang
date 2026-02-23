package com.compilerprogramming.ezlang.parser;

import com.compilerprogramming.ezlang.lexer.Token;
import com.compilerprogramming.ezlang.types.Scope;
import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.EZType;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple AST definition.
 */
public abstract class AST {

    public int lineNumber;
    protected AST(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    public abstract void accept(ASTVisitor visitor);

    public abstract StringBuilder toStr(StringBuilder sb);
    @Override
    public String toString() {
        return toStr(new StringBuilder()).toString();
    }

    public static class Program extends AST {
        public final List<Decl> decls = new ArrayList<>();
        public Scope scope = null;

        public Program(int lineNumber) {
            super(lineNumber);
        }

        @Override
        public StringBuilder toStr(StringBuilder sb) {
            for (Decl decl : decls) {
                decl.toStr(sb);
            }
            return sb;
        }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            for (AST.Decl d : decls) {
                d.accept(visitor);
            }
            visitor.exit(this);
        }
    }

    public static abstract class Decl extends AST {
        protected Decl(int lineNumber) {
            super(lineNumber);
        }
    }

    public enum VarType
    {
        STRUCT_FIELD,
        FUNCTION_PARAMETER,
        VARIABLE
    }

    public static class VarDecl extends Decl {
        public final String name;
        public final VarType varType;
        public final TypeExpr typeExpr;
        public Symbol symbol;
        public VarDecl(final String name, VarType varType, final TypeExpr typeExpr, int lineNumber) {
            super(lineNumber);
            this.name = name;
            this.varType = varType;
            this.typeExpr = typeExpr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("var ").append(name).append(": ");
            return typeExpr.toStr(sb);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            typeExpr.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class StructDecl extends Decl {
        public final String name;
        public final VarDecl[] fields;
        public Scope scope;
        public Symbol symbol;
        public StructDecl(final String name, final VarDecl[] fields, int lineNumber) {
            super(lineNumber);
            this.name = name;
            this.fields = fields;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("struct ").append(name).append("{\n");
            for (VarDecl field : fields) {
                sb.append("  ");
                field.toStr(sb);
                sb.append("\n");
            }
            sb.append("}\n");
            return sb;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            for (VarDecl field : fields) {
                field.accept(visitor);
            }
            visitor.exit(this);
        }
    }

    public static class FuncDecl extends Decl {
        public final String name;
        public final VarDecl[] args;
        public final ReturnTypeExpr returnType;
        public final BlockStmt block;
        public Scope scope;
        public Symbol symbol;

        public FuncDecl(final String name, final VarDecl[] args, final TypeExpr returnType, BlockStmt block, int lineNumber) {
            super(lineNumber);
            this.name = name;
            this.args = args;
            this.returnType = new ReturnTypeExpr(returnType, lineNumber);
            this.block = block;
        }

        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("func ").append(name).append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                args[i].toStr(sb);
            }
            sb.append(")");
            sb.append("->");
            returnType.toStr(sb);
            sb.append("\n");
            return block.toStr(sb);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            for (VarDecl arg : args) {
                arg.accept(visitor);
            }
            returnType.accept(visitor);
            block.accept(visitor);
            visitor.exit(this);
        }
    }

    public abstract static class Expr extends AST {
        public EZType type;

        protected Expr(int lineNumber) {
            super(lineNumber);
        }
    }

    public abstract static class TypeExpr extends Expr {
        protected TypeExpr(int lineNumber) {
            super(lineNumber);
        }

        public abstract String name();
    }

    public static class SimpleTypeExpr extends TypeExpr {
        protected final String name;

        public SimpleTypeExpr(String name, int lineNumber) {
            super(lineNumber);
            this.name = name;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(name);
        }
        @Override
        public String name() { return toStr(new StringBuilder()).toString(); }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            visitor.exit(this);
        }
    }

    public static class NullableSimpleTypeExpr extends SimpleTypeExpr {

        public NullableSimpleTypeExpr(String name, int lineNumber) {
            super(name, lineNumber);
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return super.toStr(sb).append("?");
        }
        public String baseTypeName() {
            return super.name;
        }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            visitor.exit(this);
        }
    }

    public static class ArrayTypeExpr extends TypeExpr {
        public final SimpleTypeExpr elementType;

        public ArrayTypeExpr(SimpleTypeExpr elementType, int lineNumber) {
            super(lineNumber);
            this.elementType = elementType;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("[");
            elementType.toStr(sb);
            return sb.append("]");
        }
        @Override
        public String name() { return toStr(new StringBuilder()).toString(); }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            elementType.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class NullableArrayTypeExpr extends ArrayTypeExpr {

        public NullableArrayTypeExpr(SimpleTypeExpr elementType, int lineNumber) {
            super(elementType, lineNumber);
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            super.toStr(sb);
            return sb.append("?");
        }
        public String baseTypeName() {
            return super.toStr(new StringBuilder()).toString();
        }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            elementType.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class ReturnTypeExpr extends Expr {
        public final TypeExpr returnType;

        public ReturnTypeExpr(TypeExpr returnType, int lineNumber) {
            super(lineNumber);
            this.returnType = returnType;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            if (returnType != null)
                returnType.accept(visitor);
            visitor.exit(this);
        }

        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return returnType != null ? returnType.toStr(sb) : sb;
        }
    }

    public static class NameExpr extends Expr {
        public String name;
        public Symbol symbol;
        public NameExpr(String name, int lineNumber) {
            super(lineNumber);
            this.name = name;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(name);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            visitor.exit(this);
        }
    }

    public static class BinaryExpr extends Expr {
        public final Token op;
        public final Expr expr1;
        public final Expr expr2;
        public BinaryExpr(Token op, Expr expr1, Expr expr2, int lineNumber) {
            super(lineNumber);
            this.op = op;
            this.expr1 = expr1;
            this.expr2 = expr2;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("(");
            expr1.toStr(sb);
            sb.append(op.toString());
            expr2.toStr(sb);
            return sb.append(")");
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            expr1.accept(visitor);
            expr2.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class UnaryExpr extends Expr {
        public final Token op;
        public final Expr expr;
        public UnaryExpr(Token op, Expr expr, int lineNumber) {
            super(lineNumber);
            this.op = op;
            this.expr = expr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("(").append(op.toString()).append("(");
            expr.toStr(sb);
            return sb.append("))");
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            expr.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class LiteralExpr extends Expr {
        public final Token value;
        public LiteralExpr(Token value) {
            super(value.lineNumber);
            this.value = value;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(value.str);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            visitor.exit(this);
        }
    }

    // array[index]
    public static class ArrayLoadExpr extends Expr {
        public final Expr array;
        public final Expr expr;
        public ArrayLoadExpr(Expr array, Expr expr, int lineNumber) {
            super(lineNumber);
            this.array = array;
            this.expr = expr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            array.toStr(sb);
            sb.append("[");
            expr.toStr(sb);
            return sb.append("]");
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            array.accept(visitor);
            expr.accept(visitor);
            visitor.exit(this);
        }
    }

    // array[index] = value
    public static class ArrayStoreExpr extends Expr {
        public final Expr array;
        public final Expr expr;
        public final Expr value;
        public ArrayStoreExpr(Expr array, Expr expr, Expr value, int lineNumber) {
            super(lineNumber);
            this.array = array;
            this.expr = expr;
            this.value = value;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            array.toStr(sb);
            sb.append("[");
            expr.toStr(sb);
            sb.append("]").append("=");
            value.toStr(sb);
            return sb;
        }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            array.accept(visitor);
            expr.accept(visitor);
            value.accept(visitor);
            visitor.exit(this);
        }
    }

    /**
     * Used inside InitExpr
     * Specializes how we display
     */
    public static class ArrayInitExpr extends ArrayStoreExpr {
        public ArrayInitExpr(Expr array, Expr expr, Expr value, int lineNumber) {
            super(array, expr, value, lineNumber);
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            value.toStr(sb);
            return sb;
        }
    }

    public static class GetFieldExpr extends Expr {
        public final Expr object;
        public final String fieldName;
        public GetFieldExpr(Expr object, String fieldName, int lineNumber) {
            super(lineNumber);
            this.object = object;
            this.fieldName = fieldName;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            object.toStr(sb);
            return sb.append(".").append(fieldName);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            object.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class SetFieldExpr extends Expr {
        public final Expr object;
        public final String fieldName;
        public final Expr value;
        public SetFieldExpr(Expr object, String fieldName, Expr value, int lineNumber) {
            super(lineNumber);
            this.object = object;
            this.fieldName = fieldName;
            this.value = value;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            object.toStr(sb);
            sb.append(".").append(fieldName).append("=");
            value.toStr(sb);
            return sb;
        }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            object.accept(visitor);
            value.accept(visitor);
            visitor.exit(this);
        }
    }

    /**
     * Used inside InitExpr
     * Specializes how we display
     */
    public static class InitFieldExpr extends SetFieldExpr {
        public InitFieldExpr(Expr object, String fieldName, Expr value, int lineNumber) {
            super(object, fieldName, value, lineNumber);
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append(fieldName).append("=");
            value.toStr(sb);
            return sb;
        }
    }

    public static class CallExpr extends Expr {
        public final Expr callee;
        public final List<Expr> args;
        public CallExpr(Expr callee, List<Expr> args, int lineNumber) {
            super(lineNumber);
            this.callee = callee;
            this.args = new ArrayList<>(args);
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            callee.toStr(sb);
            sb.append("(");
            boolean first = true;
            for (AST.Expr expr: args) {
                if (!first)
                    sb.append(",");
                expr.toStr(sb);
                first = false;
            }
            return sb.append(")");
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            callee.accept(visitor);
            for (AST.Expr expr: args) {
                expr.accept(visitor);
            }
            visitor.exit(this);
        }
    }

    /**
     * new T
     * result type is T
     */
    public static class NewExpr extends Expr {
        public final TypeExpr typeExpr;
        public final Expr len;
        public final Expr initValue;
        public NewExpr(TypeExpr typeExpr, int lineNumber) {
            super(lineNumber);
            this.typeExpr = typeExpr;
            this.len = null;
            this.initValue = null;
        }
        public NewExpr(TypeExpr typeExpr, Expr len, Expr initValue, int lineNumber) {
            super(lineNumber);
            this.typeExpr = typeExpr;
            this.len = len;
            this.initValue = initValue;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("new ");
            typeExpr.toStr(sb);
            return sb;
        }
        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            typeExpr.accept(visitor);
            if (len != null) {
                len.accept(visitor);
                if (initValue != null)
                    initValue.accept(visitor);
            }
            visitor.exit(this);
        }
    }

    /**
     * new T { initializers }
     * result type is T
     */
    public static class InitExpr extends Expr {
        public final NewExpr newExpr;
        public final List<Expr> initExprList;
        public InitExpr(NewExpr newExpr, List<Expr> initExprList, int lineNumber) {
            super(lineNumber);
            this.initExprList = initExprList;
            this.newExpr = newExpr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            newExpr.toStr(sb);
            sb.append("{");
            boolean first = true;
            for (Expr expr: initExprList) {
                if (!first)
                    sb.append(", ");
                first = false;
                expr.toStr(sb);
            }
            sb.append("}");
            return sb;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            newExpr.accept(visitor);
            for (Expr expr: initExprList) {
                expr.accept(visitor);
            }
            visitor.exit(this);
        }
    }

    public abstract static class Stmt extends AST {
        protected Stmt(int lineNumber) {
            super(lineNumber);
        }
    }

    public static class IfElseStmt extends Stmt {
        public final Expr condition;
        public final Stmt ifStmt;
        public final Stmt elseStmt;
        public IfElseStmt(Expr expr, Stmt ifStmt, Stmt elseStmt, int lineNumber) {
            super(lineNumber);
            this.condition = expr;
            this.ifStmt = ifStmt;
            this.elseStmt = elseStmt;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("if(");
            condition.toStr(sb);
            sb.append(")\n");
            ifStmt.toStr(sb);
            if (elseStmt != null) {
                sb.append("\nelse\n");
                elseStmt.toStr(sb);
            }
            return sb;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            condition.accept(visitor);
            ifStmt.accept(visitor);
            if (elseStmt != null)
                elseStmt.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class WhileStmt extends Stmt {
        public final Expr condition;
        public Stmt stmt;
        public WhileStmt(Expr expr, int lineNumber) {
            super(lineNumber);
            this.condition = expr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("while(");
            condition.toStr(sb);
            sb.append(")\n");
            stmt.toStr(sb);
            return sb;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            condition.accept(visitor);
            stmt.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class BreakStmt extends Stmt {
        public final WhileStmt whileStmt;
        public BreakStmt(WhileStmt whileStmt, int lineNumber) {
            super(lineNumber);
            this.whileStmt = whileStmt;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("break");
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            visitor.exit(this);
        }
    }

    public static class ContinueStmt extends Stmt {
        public final WhileStmt whileStmt;
        public ContinueStmt(WhileStmt whileStmt, int lineNumber) {
            super(lineNumber);
            this.whileStmt = whileStmt;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("continue");
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            visitor.exit(this);
        }
    }

    public static class ReturnStmt extends Stmt {
        public final AST.Expr expr;
        public ReturnStmt(AST.Expr expr, int lineNumber) {
            super(lineNumber);
            this.expr = expr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("return");
            if (expr != null) {
                sb.append(" ");
                expr.toStr(sb);
            }
            return sb;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            if (expr != null)
                expr.accept(visitor);
            visitor.exit(this);
        }
    }

    /* name = value */
    public static class AssignStmt extends Stmt {
        public final NameExpr nameExpr;
        public final Expr rhs;
        public AssignStmt(NameExpr nameExpr, Expr rhs, int lineNumber) {
            super(lineNumber);
            this.nameExpr = nameExpr;
            this.rhs = rhs;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            nameExpr.toStr(sb);
            sb.append(" = ");
            rhs.toStr(sb);
            return sb;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            nameExpr.accept(visitor);
            rhs.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class VarStmt extends Stmt {
        public final String varName;
        public Symbol.VarSymbol symbol;
        public final AST.Expr expr;

        public VarStmt(String symbol, Expr expr, int lineNumber) {
            super(lineNumber);
            this.varName = symbol;
            this.expr = expr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("var ").append(varName).append(" = ");
            return expr.toStr(sb);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            expr.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class ExprStmt extends Stmt {
        public final Expr expr;
        public ExprStmt(Expr expr, int lineNumber) {
            super(lineNumber);
            this.expr = expr;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return expr.toStr(sb);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            expr.accept(visitor);
            visitor.exit(this);
        }
    }

    public static class VarDeclStmt extends Stmt {
        public final VarDecl varDecl;
        public VarDeclStmt(VarDecl varDec, int lineNumber) {
            super(lineNumber);
            this.varDecl = varDec;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return varDecl.toStr(sb);
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            varDecl.accept(visitor);
            visitor.exit(this);
        }
    }


    public static class BlockStmt extends Stmt {
        public final List<Stmt> stmtList = new ArrayList<>();
        public Scope scope;
        public BlockStmt(int lineNumber) {
            super(lineNumber);
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("{\n");
            for (Stmt stmt: stmtList) {
                stmt.toStr(sb);
                sb.append("\n");
            }
            return sb.append("}\n");
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor = visitor.enter(this);
            if (visitor == null)
                return;
            for (Stmt stmt: stmtList) {
                stmt.accept(visitor);
            }
            visitor.exit(this);
        }
    }
}
