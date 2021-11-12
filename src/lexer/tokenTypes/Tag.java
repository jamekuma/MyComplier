package lexer.tokenTypes;

/**
 * Token中的所有种别码
 * @author xjy
 *
 */
public enum Tag {
	/************ 标识符 ***********/
	ID, 
	
	/************ 关键字 ***********/
	/**基本类型 int, double, char, bool ... */
	BASIC,
	
	IF,
	ELSE,
	DO,
	WHILE,
	FOR,
	/** 函数调用*/
	PROC,
	CALL,
	RETURN,
	
	TRUE,
	FALSE,
	
	/**记录*/
	RECORD,
	
	/************ 运算符 ***********/
	ADD,
	SUB,
	MULT,
	DIV,
	MOD,
	
	/*********** 关系运算符 **********/
	GE,
	LE,
	G,
	L,
	E,
	NE,
	
	/*********** 逻辑运算符 **********/
	AND,
	OR,
	NOT,
	
	/************* 界符 ***********/
	/**=赋值号*/
	ASSIGN, 
	/**;分号*/
	SEMI,
	/**(左小括号*/
	SLP,
	/**)右小括号*/
	SRP,
	/**[左中括号*/
	LM,   
	/**]右中括号*/
	RM,
	/**{左大括号*/
	LP,
	/**}右大括号*/
	RP,
	/**逗号*/
	COMMA,
	/**点*/
	DOT,
	
	/**十进制整型常量*/
	DEC,
	/**八进制整型常量*/
	OCT,
	/**十六进制整型常量*/
	HEX,
	/**浮点型常量*/
	REAL,
	/**字符串常量*/
	STRING,
	/**字符常量*/
	CHAR,
	
	/**错误*/
	ERROR;
}
