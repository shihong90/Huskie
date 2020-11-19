package com.kaikeba.antlr;

public class ListenerRewrite extends learnAntlrBaseListener {
    /**
     * 这是一个回调方法，就是antlr对输入进行语法解析完成之后会调用它
     * 也就是我们使用antlr对输入(sql 语句)做语法分析完成后要做的业务处理就是在回调中做
     * 做之前需要对输入进行语法与词法的格式校验
     * @param ctx
     */
    @Override
    public void exitR(learnAntlrParser.RContext ctx) {
        String aCase = ctx.getChild(0).getText().toLowerCase();
        String bCase = ctx.getChild(1).getText().toLowerCase();
        System.out.println(aCase+" "+bCase);
    }
}
