package com.kaikeba.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class AntlrTest {
    public static void main(String[] args) {
        //1.构建antlr输入流
        ANTLRInputStream inputStream = new ANTLRInputStream("hello kkb");
        //2.构建词法分析器
        learnAntlrLexer lexer = new learnAntlrLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        learnAntlrParser parser = new learnAntlrParser(tokenStream);
        //开始语法解析
        learnAntlrParser.RContext r = parser.r();

        ListenerRewrite listenr = new ListenerRewrite();

        ParseTreeWalker.DEFAULT.walk(listenr,r);
    }
}
