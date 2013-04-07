package com.metadave.contactweb;

import com.basho.contact.RuntimeContext;
import org.antlr.v4.runtime.*;

import java.io.PrintStream;

public class WebErrorListener extends BaseErrorListener{
    RuntimeContext runtimeCtx;

    public WebErrorListener(RuntimeContext runtimeCtx) {
        this.runtimeCtx = runtimeCtx;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        runtimeCtx.appendError("Error at line " + line + ":" + charPositionInLine + " " + msg);
        //underlineError(recognizer, (Token)offendingSymbol, line, charPositionInLine);
        runtimeCtx.setParseError(true);
        throw new RuntimeException("Syntax error at line " + line + " character " + charPositionInLine);
    }

//    protected void underlineError(Recognizer recognizer, Token offendingToken, int line, int charPositionInLine) {
//        StringBuilder b = new StringBuilder();
//        CommonTokenStream tokens = (CommonTokenStream)recognizer.getInputStream();
//        String input = tokens.getTokenSource().getInputStream().toString();
//        String lines[] = input.split("\n");
//        String errorLine = lines[line - 1];
//        b.append(errorLine);
//        for(int i = 0; i < charPositionInLine; i++) b.append(" ");
//        int start = offendingToken.getStartIndex();
//        int stop = offendingToken.getStopIndex();
//        if(start >=0 && stop >= 0) {
//            for(int i = start; i <= stop; i++)
//                b.append("^");
//        }
//        b.println();
//    }

}
