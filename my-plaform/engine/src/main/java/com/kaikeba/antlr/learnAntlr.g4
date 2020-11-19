grammar learnAntlr;
@header{
package com.kaikeba.antlr;
}

r  : 'hello' ID;
ID : [a-z]+;
WS : [\t\r\n]+ -> skip ;

