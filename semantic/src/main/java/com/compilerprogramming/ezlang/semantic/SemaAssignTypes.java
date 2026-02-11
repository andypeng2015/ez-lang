package com.compilerprogramming.ezlang.semantic;

import com.compilerprogramming.ezlang.exceptions.CompilerException;
import com.compilerprogramming.ezlang.lexer.Token;
import com.compilerprogramming.ezlang.parser.AST;
import com.compilerprogramming.ezlang.parser.ASTVisitor;
import com.compilerprogramming.ezlang.types.Scope;
import com.compilerprogramming.ezlang.types.EZType;
import com.compilerprogramming.ezlang.types.TypeDictionary;

/**
 * The goal of this semantic analysis pass is to define
 * functions and struct types.
 */
public class SemaAssignTypes implements ASTVisitor {
    Scope currentScope;
    AST.StructDecl currentStructDecl;
    AST.FuncDecl currentFuncDecl;
    final TypeDictionary typeDictionary;

    public SemaAssignTypes(TypeDictionary typeDictionary) {
        this.typeDictionary = typeDictionary;
    }

    @Override
    public ASTVisitor enter(AST.Program program) {
        currentScope = program.scope;
        return this;
    }
    @Override
    public void exit(AST.Program program) {
        currentScope = currentScope.parent;
    }

    @Override
    public ASTVisitor enter(AST.FuncDecl funcDecl) {
        currentScope = funcDecl.scope;
        currentFuncDecl = funcDecl;
        return this;
    }
    @Override
    public void exit(AST.FuncDecl funcDecl) {
        currentScope = currentScope.parent;
        currentFuncDecl = null;
    }

    @Override
    public ASTVisitor enter(AST.StructDecl structDecl) {
        currentScope = structDecl.scope;
        currentStructDecl = structDecl;
        return this;
    }
    @Override
    public void exit(AST.StructDecl structDecl) {
        currentScope = currentScope.parent;
        currentStructDecl = null;
    }

    @Override
    public void exit(AST.BinaryExpr binaryExpr) {
        if (binaryExpr.type != null)
            return;
        validType(binaryExpr.expr1.type, true);
        validType(binaryExpr.expr2.type, true);
        if (binaryExpr.expr1.type instanceof EZType.EZTypeInteger &&
            binaryExpr.expr2.type instanceof EZType.EZTypeInteger) {
            // booleans are int too
            binaryExpr.type = typeDictionary.INT;
        }
        else if (((binaryExpr.expr1.type instanceof EZType.EZTypeNull &&
                 binaryExpr.expr2.type instanceof EZType.EZTypeNullable) ||
                (binaryExpr.expr1.type instanceof EZType.EZTypeNullable &&
                 binaryExpr.expr2.type instanceof EZType.EZTypeNull) ||
                (binaryExpr.expr1.type instanceof EZType.EZTypeNull &&
                 binaryExpr.expr2.type instanceof EZType.EZTypeNull)) &&
                (binaryExpr.op.str.equals("==") || binaryExpr.op.str.equals("!="))) {
            binaryExpr.type = typeDictionary.INT;
        }
        else {
            throw new CompilerException("Binary operator " + binaryExpr.op + " not supported for operands");
        }
    }

    @Override
    public void exit(AST.UnaryExpr unaryExpr) {
        if (unaryExpr.type != null) {
            return;
        }
        validType(unaryExpr.expr.type, false);
        if (unaryExpr.expr.type instanceof EZType.EZTypeInteger) {
            unaryExpr.type = unaryExpr.expr.type;
        }
        else {
            throw new CompilerException("Unary operator " + unaryExpr.op + " not supported for operand");
        }
    }

    @Override
    public void exit(AST.GetFieldExpr fieldExpr) {
        if (fieldExpr.type != null)
            return;
        validType(fieldExpr.object.type, false);
        EZType.EZTypeStruct structType;
        if (fieldExpr.object.type instanceof EZType.EZTypeStruct ts) {
            structType = ts;
        }
        else if (fieldExpr.object.type instanceof EZType.EZTypeNullable ptr &&
                ptr.baseType instanceof EZType.EZTypeStruct ts) {
            structType = ts;
        }
        else
            throw new CompilerException("Unexpected struct type " + fieldExpr.object.type);
        var fieldType = structType.getField(fieldExpr.fieldName);
        if (fieldType == null)
            throw new CompilerException("Struct " + structType + " does not have field named " + fieldExpr.fieldName);
        fieldExpr.type = fieldType;
    }

    @Override
    public void exit(AST.SetFieldExpr fieldExpr) {
        if (fieldExpr.type != null)
            return;
        validType(fieldExpr.object.type, true);
        EZType.EZTypeStruct structType;
        if (fieldExpr.object.type instanceof EZType.EZTypeStruct ts) {
            structType = ts;
        }
        else if (fieldExpr.object.type instanceof EZType.EZTypeNullable ptr &&
                ptr.baseType instanceof EZType.EZTypeStruct ts) {
            structType = ts;
        }
        else if (fieldExpr.object.type instanceof EZType.EZTypeArray typeArray) {
            if (fieldExpr.fieldName.equals("len"))
                checkAssignmentCompatible(typeDictionary.INT,fieldExpr.value.type);
            else if (fieldExpr.fieldName.equals("value"))
                checkAssignmentCompatible(typeArray.getElementType(),fieldExpr.value.type);
            else
                throw new CompilerException("Unexpected array initializer " + fieldExpr.fieldName);
            fieldExpr.type = fieldExpr.value.type;
            return;
        }
        else
            throw new CompilerException("Unexpected struct type " + fieldExpr.object.type);
        var fieldType = structType.getField(fieldExpr.fieldName);
        if (fieldType == null)
            throw new CompilerException("Struct " + structType + " does not have field named " + fieldExpr.fieldName);
        validType(fieldExpr.value.type, true);
        checkAssignmentCompatible(fieldType, fieldExpr.value.type);
        fieldExpr.type = fieldType;
    }

    @Override
    public void exit(AST.CallExpr callExpr) {
        if (callExpr.type != null)
            return;
        validType(callExpr.callee.type, false);
        if (callExpr.callee.type instanceof EZType.EZTypeFunction f) {
            callExpr.type = f.returnType;
        }
        else
            throw new CompilerException("Call target must be a function");
    }

    @Override
    public ASTVisitor enter(AST.LiteralExpr literalExpr) {
        if (literalExpr.type != null)
            return this;
        if (literalExpr.value.kind == Token.Kind.NUM) {
            literalExpr.type = typeDictionary.INT;
        }
        else if (literalExpr.value.kind == Token.Kind.IDENT
                 && literalExpr.value.str.equals("null")) {
            literalExpr.type = typeDictionary.NULL;
        }
        else {
            throw new CompilerException("Unsupported literal " + literalExpr.value);
        }
        return this;
    }

    @Override
    public void exit(AST.ArrayLoadExpr arrayIndexExpr) {
        if (arrayIndexExpr.type != null)
            return;
        validType(arrayIndexExpr.array.type, false);
        EZType.EZTypeArray arrayType;
        if (arrayIndexExpr.array.type instanceof EZType.EZTypeArray ta) {
            arrayType = ta;
        }
        else if (arrayIndexExpr.array.type instanceof EZType.EZTypeNullable ptr &&
                ptr.baseType instanceof EZType.EZTypeArray ta) {
            arrayType = ta;
        }
        else
            throw new CompilerException("Unexpected array type " + arrayIndexExpr.array.type);
        if (!(arrayIndexExpr.expr.type instanceof EZType.EZTypeInteger))
            throw new CompilerException("Array index must be integer type");
        arrayIndexExpr.type = arrayType.getElementType();
        validType(arrayIndexExpr.type, false);
    }

    @Override
    public void exit(AST.ArrayStoreExpr arrayIndexExpr) {
        if (arrayIndexExpr.type != null)
            return;
        validType(arrayIndexExpr.array.type, false);
        EZType.EZTypeArray arrayType;
        if (arrayIndexExpr.array.type instanceof EZType.EZTypeArray ta) {
            arrayType = ta;
        }
        else if (arrayIndexExpr.array.type instanceof EZType.EZTypeNullable ptr &&
                ptr.baseType instanceof EZType.EZTypeArray ta) {
            arrayType = ta;
        }
        else
            throw new CompilerException("Unexpected array type " + arrayIndexExpr.array.type);
        if (!(arrayIndexExpr.expr.type instanceof EZType.EZTypeInteger))
            throw new CompilerException("Array index must be integer type");
        arrayIndexExpr.type = arrayType.getElementType();
        validType(arrayIndexExpr.type, false);
        validType(arrayIndexExpr.value.type, true);
        checkAssignmentCompatible(arrayIndexExpr.type, arrayIndexExpr.value.type);
    }

    @Override
    public void exit(AST.NewExpr newExpr) {
        if (newExpr.type != null)
            return;
        if (newExpr.typeExpr.type == null)
            throw new CompilerException("Unresolved type in new expression");
        validType(newExpr.typeExpr.type, false);
        if (newExpr.typeExpr.type instanceof EZType.EZTypeNullable)
            throw new CompilerException("new cannot be used to create a Nullable type");
        if (newExpr.typeExpr.type instanceof EZType.EZTypeStruct) {
            newExpr.type = newExpr.typeExpr.type;
        }
        else if (newExpr.typeExpr.type instanceof EZType.EZTypeArray arrayType) {
            newExpr.type = newExpr.typeExpr.type;
            if (newExpr.len != null) {
                if (!(newExpr.len.type instanceof EZType.EZTypeInteger))
                    throw new CompilerException("Array len must be integer type");
                if (newExpr.initValue != null) {
                    if (!arrayType.getElementType().isAssignable(newExpr.initValue.type))
                        throw new CompilerException("Array init value must be assignable to array element type");
                }
            }
        }
        else
            throw new CompilerException("Unsupported type in new expression");
    }

    @Override
    public void exit(AST.InitExpr initExpr) {
        if (initExpr.newExpr.type == null)
            throw new CompilerException("Unresolved type in new expression");
        if (initExpr.type != null)
            return;
        validType(initExpr.newExpr.type, false);
        if (initExpr.newExpr.type instanceof EZType.EZTypeNullable)
            throw new CompilerException("new cannot be used to create a Nullable type");
        if (initExpr.newExpr.type instanceof EZType.EZTypeStruct typeStruct) {
            for (AST.Expr expr: initExpr.initExprList) {
                if (expr instanceof AST.SetFieldExpr setFieldExpr) {
                    var fieldType = typeStruct.getField(setFieldExpr.fieldName);
                    checkAssignmentCompatible(fieldType, setFieldExpr.value.type);
                }
            }
        }
        else if (initExpr.newExpr.type instanceof EZType.EZTypeArray arrayType) {
            if (initExpr.initExprList.size() > 0)
                initExpr.initExprList.removeIf(e->e instanceof AST.InitFieldExpr);
            for (AST.Expr expr: initExpr.initExprList) {
                checkAssignmentCompatible(arrayType.getElementType(), expr.type);
            }
        }
        else
            throw new CompilerException("Unsupported type in new expression");
        initExpr.type = initExpr.newExpr.type;
    }

    @Override
    public ASTVisitor enter(AST.NameExpr nameExpr) {
        if (nameExpr.type != null)
            return this;
        var symbol = currentScope.lookup(nameExpr.name);
        if (symbol == null) {
            throw new CompilerException("Unknown symbol " + nameExpr.name);
        }
        validType(symbol.type, false);
        nameExpr.symbol = symbol;
        nameExpr.type = symbol.type;
        return this;
    }

    @Override
    public void exit(AST.ReturnStmt returnStmt) {
        EZType.EZTypeFunction functionType = (EZType.EZTypeFunction) currentFuncDecl.symbol.type;
        if (returnStmt.expr != null) {
            validType(returnStmt.expr.type, true);
            checkAssignmentCompatible(functionType.returnType, returnStmt.expr.type);
        }
        else if (!(functionType.returnType instanceof EZType.EZTypeVoid)) {
            throw new CompilerException("A return value of type " + functionType.returnType + " is expected");
        }
    }

    @Override
    public void exit(AST.VarStmt varStmt) {
        validType(varStmt.expr.type, true);
        var symbol = currentScope.lookup(varStmt.varName);
        symbol.type = typeDictionary.merge(varStmt.expr.type, symbol.type);
        if (symbol.type == typeDictionary.NULL)
            throw new CompilerException("Variable " + varStmt.varName + " cannot be Null type");
    }

    @Override
    public ASTVisitor enter(AST.BlockStmt blockStmt) {
        currentScope = blockStmt.scope;
        return this;
    }
    @Override
    public void exit(AST.BlockStmt blockStmt) {
        currentScope = currentScope.parent;
    }

    @Override
    public void exit(AST.AssignStmt assignStmt) {
        validType(assignStmt.nameExpr.type, false);
        validType(assignStmt.rhs.type, true);
        checkAssignmentCompatible(assignStmt.nameExpr.type, assignStmt.rhs.type);
    }

    public void analyze(AST.Program program) {
        program.accept(this);
    }

    private void validType(EZType t, boolean allowNull) {
        if (t == null)
            throw new CompilerException("Undefined type");
        if (t == typeDictionary.UNKNOWN)
            throw new CompilerException("Undefined type");
        if (!allowNull && t == typeDictionary.NULL)
            throw new CompilerException("Null type not allowed");
    }

    private void checkAssignmentCompatible(EZType var, EZType value) {
        if (!var.isAssignable(value))
            throw new CompilerException("Value of type " + value + " cannot be assigned to type " + var);
    }
}
