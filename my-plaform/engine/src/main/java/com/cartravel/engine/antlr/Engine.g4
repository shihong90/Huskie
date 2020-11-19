grammar Engine;

@header{
package com.cartravel.engine.antlr;
}

//select c1,c2,c3 from tb;select * from tb;
statement
    : (sql ender)*
    ;


sql
    : ('load'|'LOAD') format '.'? path ('where' | 'WHERE')? expression? booleanExpression*  'as' tableName
    | ('save'|'SAVE') (overwrite | append | errorIfExists | ignore | update)* tableName 'as' format '.' path ('where' | 'WHERE')? expression? booleanExpression* ('partitionBy' col)? ('coalesce' numPartition)?
    | ('select'|'SELECT') ~(';')* 'as'? tableName
    | ('insert'|'INSERT') ~(';')*
    | ('create'|'CREATE') ~(';')*
    | ('drop'|'DROP') ~(';')*
    | ('truncate'|'TRUNCATE') ~(';')*
    | ('register'|'REGISTER') format '.' path ('where' | 'WHERE')? expression? booleanExpression*
    | ('show'|'SHOW') ~(';')*
    | ('describe'|'DESCRIBE') ~(';')*
    | ('include'|'INCLUDE') format '.' path ('where' | 'WHERE')? expression? booleanExpression*
    | ('explain'|'EXPLAIN') ~(';')*
    |  SIMPLE_COMMENT
    ;

overwrite
    : ('overwrite' | 'OVERWRITE')
    ;

append
    : ('append' | 'APPEND')
    ;

errorIfExists
    : 'errorIfExists'
    ;

ignore
    : ('ignore' | 'IGNORE')
    ;

update
    : ('update' | 'UPDATE')
    ;

booleanExpression
    : ('and' | 'AND') expression
    ;

expression
    : identifier '=' STRING
    ;

ender
    :';'
    ;
//匹配字符串或者字符
format
    : identifier
    ;

path
    : quotedIdentifier | identifier
    ;

db
    :qualifiedName | identifier
    ;

tableName   //字符串或者数字或者下划线出现
    : identifier
    ;

functionName
    : identifier
    ;

col
    : identifier
    ;
key
    : qualifiedName
    ;
value
    : qualifiedName | quotedIdentifier | STRING | BLOCK_STRING
    ;

qualifiedName
    : identifier ('.' identifier)*
    ;

identifier
    : strictIdentifier
    ;

strictIdentifier
    : IDENTIFIER    //直接左递归
    | quotedIdentifier  //间接左递归
    ;

quotedIdentifier
    : BACKQUOTED_IDENTIFIER
    ;

STRING
    : '\'' ( ~('\''|'\\') | ('\\' .) )* '\''
    | '"' ( ~('"'|'\\') | ('\\' .) )* '"'
    | BLOCK_STRING
    ;



BLOCK_STRING
    : '```' ~[+] .*? '```'
    ;
//匹配数字或者字符串
//（）https://www.cnblogs.com/zengguowang/p/7779699.html
IDENTIFIER
    : (LETTER | DIGIT | '_')+
    ;

BACKQUOTED_IDENTIFIER
    : '`' ( ~'`' | '``' )* '`'
    ;

numPartition
    : DIGIT+
    ;

fragment DIGIT
    : [0-9]
    ;

fragment LETTER
    : [a-zA-Z]
    ;

SIMPLE_COMMENT
    : '--' ~[\r\n]* '\r'? '\n'? -> channel(HIDDEN)
    ;

BRACKETED_EMPTY_COMMENT
    : '/**/' -> channel(HIDDEN)
    ;

BRACKETED_COMMENT
    : '/*' ~[+] .*? '*/' -> channel(HIDDEN)
    ;

WS
    : [ \r\n\t]+ -> channel(HIDDEN)
    ;

// Catch-all for anything we can't recognize.
// We use this to be able to ignore and recover all the text
// when splitting statements with DelimiterLexer
UNRECOGNIZED
    : .
    ;
