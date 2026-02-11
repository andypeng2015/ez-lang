package com.compilerprogramming.ezlang.parser;

public interface ASTVisitor {
    default ASTVisitor enter(AST.Program program) {return this;}
    default void exit(AST.Program program) {}
    default ASTVisitor enter(AST.FuncDecl funcDecl) {return this;}
    default void exit(AST.FuncDecl funcDecl) {}
    default ASTVisitor enter(AST.StructDecl structDecl) {return this;}
    default void exit(AST.StructDecl structDecl) {}
    default ASTVisitor enter(AST.VarDecl varDecl) {return this;}
    default void exit(AST.VarDecl varDecl) {}
    default ASTVisitor enter(AST.BinaryExpr binaryExpr) {return this;}
    default void exit(AST.BinaryExpr binaryExpr) {}
    default ASTVisitor enter(AST.UnaryExpr unaryExpr) {return this;}
    default void exit(AST.UnaryExpr unaryExpr) {}
    default ASTVisitor enter(AST.GetFieldExpr fieldExpr) {return this;}
    default void exit(AST.GetFieldExpr fieldExpr) {}
    default ASTVisitor enter(AST.SetFieldExpr fieldExpr) {return this;}
    default void exit(AST.SetFieldExpr fieldExpr) {}
    default ASTVisitor enter(AST.CallExpr callExpr) { return this;}
    default void exit(AST.CallExpr callExpr) {}
    default ASTVisitor enter(AST.SimpleTypeExpr simpleTypeExpr) { return this;}
    default void exit(AST.SimpleTypeExpr simpleTypeExpr) {}
    default ASTVisitor enter(AST.NullableSimpleTypeExpr simpleTypeExpr) {return this;}
    default void exit(AST.NullableSimpleTypeExpr simpleTypeExpr) {}
    default ASTVisitor enter(AST.ArrayTypeExpr arrayTypeExpr) {return this;}
    default void exit(AST.ArrayTypeExpr arrayTypeExpr) {}
    default ASTVisitor enter(AST.NullableArrayTypeExpr arrayTypeExpr) {return this;}
    default void exit(AST.NullableArrayTypeExpr arrayTypeExpr) {}
    default ASTVisitor enter(AST.ReturnTypeExpr returnTypeExpr) {return this;}
    default void exit(AST.ReturnTypeExpr returnTypeExpr) {}
    default ASTVisitor enter(AST.LiteralExpr literalExpr) {return this;}
    default void exit(AST.LiteralExpr literalExpr) {}
    default ASTVisitor enter(AST.ArrayLoadExpr arrayLoadExpr) {return this;}
    default void exit(AST.ArrayLoadExpr arrayLoadExpr) {}
    default ASTVisitor enter(AST.ArrayStoreExpr arrayStoreExpr) {return this;}
    default void exit(AST.ArrayStoreExpr arrayStoreExpr) {}
    default ASTVisitor enter(AST.NewExpr newExpr) {return this;}
    default void exit(AST.NewExpr newExpr) {}
    default ASTVisitor enter(AST.InitExpr initExpr) {return this;}
    default void exit(AST.InitExpr initExpr) {}
    default ASTVisitor enter(AST.NameExpr nameExpr) {return this;}
    default void exit(AST.NameExpr nameExpr) {}
    default ASTVisitor enter(AST.BreakStmt breakStmt) {return this;}
    default void exit(AST.BreakStmt breakStmt) {}
    default ASTVisitor enter(AST.ContinueStmt continueStmt) {return this;}
    default void exit(AST.ContinueStmt continueStmt) {}
    default ASTVisitor enter(AST.ReturnStmt returnStmt) {return this;}
    default void exit(AST.ReturnStmt returnStmt) {}
    default ASTVisitor enter(AST.IfElseStmt ifElseStmt) {return this;}
    default void exit(AST.IfElseStmt ifElseStmt) {}
    default ASTVisitor enter(AST.WhileStmt whileStmt) {return this;}
    default void exit(AST.WhileStmt whileStmt) {}
    default ASTVisitor enter(AST.VarStmt varStmt) {return this;}
    default void exit(AST.VarStmt varStmt) {}
    default ASTVisitor enter(AST.BlockStmt blockStmt) {return this;}
    default void exit(AST.BlockStmt blockStmt) {}
    default ASTVisitor enter(AST.VarDeclStmt varDeclStmt) {return this;}
    default void exit(AST.VarDeclStmt varDeclStmt) {}
    default ASTVisitor enter(AST.ExprStmt exprStmt) {return this;}
    default void exit(AST.ExprStmt exprStmt) {}
    default ASTVisitor enter(AST.AssignStmt assignStmt) {return this;}
    default void exit(AST.AssignStmt assignStmt) {}
}
