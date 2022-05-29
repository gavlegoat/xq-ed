grammar PGN;

/**
 * Adapted from github.com/antlr/grammars-v4/blob/master/pgn/PGN.g4
 */
 
parse : game EOF ;

game : tags movetext ;

tags : tag* ;

tag : '[' ID STRING ']' ;

movetext : elementSeq termination ;

elementSeq : (element | recursiveVariation)* ;

element
  : moveNumber
  | sanMove
  | comment
  ;
  
moveNumber : INTEGER '.'? ;

sanMove : '.'+ | SYMBOL ;

recursiveVariation : '(' elementSeq ')' ;

termination
  : '1-0'
  | '0-1'
  | '1/2-1/2'
  | '*'
  |
  ;
  
comment
  : LINE_COMMENT
  | BRACE_COMMENT
  ;
  
LINE_COMMENT : ';' ~[\r\n]* ;

BRACE_COMMENT : '{' ~'}'* '}' ;

SPACES : [ \t\r\n]+ -> skip ;

STRING : '"' ('\\\\' | '\\"' | ~[\\"])* '"' ;

INTEGER : [0-9]+ ;

ID : [a-zA-Z]+ ;

SYMBOL : [a-zA-Z0-9] [a-zA-Z0-9_+#=:-]* ;

SUFFIX_ANNOTATION : [?!] [?!]? ;

UNEXPECTED : . ;

