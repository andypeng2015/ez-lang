/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * <p>
 * Contributor(s):
 * <p>
 * The Initial Developer of the Original Software is Dibyendu Majumdar.
 * <p>
 * This file is part of the Sea Of Nodes Simple Programming language
 * implementation. See https://github.com/SeaOfNodes/Simple
 * <p>
 * The contents of this file are subject to the terms of the
 * Apache License Version 2 (the "APL"). You may not use this
 * file except in compliance with the License. A copy of the
 * APL may be obtained from:
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.compilerprogramming.ezlang.exceptions;

public class CompilerException extends RuntimeException {
    int lineNumber;

    public CompilerException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }
    public CompilerException(String message) {
        this(message,-1);
    }
    public CompilerException(String message, Throwable cause) {
        super(message, cause);
        lineNumber = -1;
    }
}
