package parser.LR1;

import lexer.tokenTypes.Tag;
import lexer.tokenTypes.Token;
import lexer.tokenTypes.Word;

/**
 * 终结符
 * @author xjy
 *
 */
public class Terminal extends Symbol {
	
	private Token token;
	public Terminal(String text) {
		super(text);
	}
	
	
	
	/**
	 * 验证某token是否与本终结符匹配
	 * @param token
	 * @return
	 */
	public boolean match(Token token) {
		String text = this.getText();
		if (text.equals("id")) {
			if (!(token instanceof Word)) 
				return false;
			Word word = (Word)token;
			return word.getTag() == Tag.ID;
		}
		else if (text.equals("(")) {
			return token.getTag() == Tag.SLP;
		}
		else if (text.equals(")")) {
			return token.getTag() == Tag.SRP;
		}
		else if (text.equals("[")) {
			return token.getTag() == Tag.LM;
		}
		else if (text.equals("]")) {
			return token.getTag() == Tag.RM;
		}
		else if (text.equals(";")) {
			return token.getTag() == Tag.SEMI;
		}
		else if (text.equals(",")) {
			return token.getTag() == Tag.COMMA;
		}
		else if (text.equals("=")) {
			return token.getTag() == Tag.ASSIGN;
		}
		else if (text.equals("+")) {
			return token.getTag() == Tag.ADD;
		}
		else if (text.equals("-")) {
			return token.getTag() == Tag.SUB;
		}
		else if (text.equals("*")) {
			return token.getTag() == Tag.MULT;
		}
		else if (text.equals("/")) {
			return token.getTag() == Tag.DIV;
		}
		else if (text.equals("!")) {
			return token.getTag() == Tag.NOT;
		}
		else if (text.equals("&&")) {
			return token.getTag() == Tag.AND;
		}
		else if (text.equals("||")) {
			return token.getTag() == Tag.OR;
		}
		else if (text.equals("<")) {
			return token.getTag() == Tag.L;
		}
		else if (text.equals("<=")) {
			return token.getTag() == Tag.LE;
		}
		else if (text.equals(">")) {
			return token.getTag() == Tag.G;
		}
		else if (text.equals(">=")) {
			return token.getTag() == Tag.GE;
		}
		else if (text.equals("==")) {
			return token.getTag() == Tag.E;
		}
		else if (text.equals("!=")) {
			return token.getTag() == Tag.NE;
		}
		else if (text.equals("basic")) {
			return token.getTag() == Tag.BASIC;
		}
		else {   // keyword
			if (!(token instanceof Word)) 
				return false;
			Word word = (Word)token;
			return word.getTag() == Tag.valueOf(text.toUpperCase());
		}
		
	}
	
	/**
	 * 把token转为所属的终结符
	 * @param token
	 * @return
	 */
	public static Terminal tokenToTerminal(Token token) {
			if (token.getTag() == Tag.ID) {
				return new Terminal("id");
			}
			else if (token.getTag() == Tag.SLP) {
				return new Terminal("(");
			}
			else if (token.getTag() == Tag.SRP) {
				return new Terminal(")");
			}
			else if (token.getTag() == Tag.LM) {
				return new Terminal("[");
			}
			else if (token.getTag() == Tag.RM) {
				return new Terminal("]");
			}
			else if (token.getTag() == Tag.LP) {
				return new Terminal("{");
			}
			else if (token.getTag() == Tag.RP) {
				return new Terminal("}");
			}
			else if (token.getTag() == Tag.SEMI) {
				return new Terminal(";");
			}
			else if (token.getTag() == Tag.COMMA) {
				return new Terminal(",");
			}
			else if (token.getTag() == Tag.ASSIGN) {
				return new Terminal("=");
			}
			else if (token.getTag() == Tag.ADD) {
				return new Terminal("+");
			}
			else if (token.getTag() == Tag.SUB) {
				return new Terminal("-");
			}
			else if (token.getTag() == Tag.MULT) {
				return new Terminal("*");
			}
			else if (token.getTag() == Tag.DIV) {
				return new Terminal("/");
			}
			else if (token.getTag() == Tag.NOT) {
				return new Terminal("!");
			}
			else if (token.getTag() == Tag.AND) {
				return new Terminal("&&");
			}
			else if (token.getTag() == Tag.OR) {
				return new Terminal("||");
			}
			else if (token.getTag() == Tag.L) {
				return new Terminal("<");
			}
			else if (token.getTag() == Tag.LE) {
				return new Terminal("<=");
			}
			else if (token.getTag() == Tag.G) {
				return new Terminal(">");
			}
			else if (token.getTag() == Tag.GE) {
				return new Terminal(">=");
			}
			else if (token.getTag() == Tag.E) {
				return new Terminal("==");
			}
			else if (token.getTag() == Tag.NE) {
				return new Terminal("!=");
			}
			else if (token.getTag() == Tag.BASIC) {
				return new Terminal("basic");
			}
			else {   // keyword
				return new Terminal(token.getTag().toString().toLowerCase());
			}
			
		}



	public Token getToken() {
		return token;
	}



	public void setToken(Token token) {
		this.token = token;
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if (!(obj instanceof Terminal)) {
//			return false;
//		}
//		Terminal nt = (Terminal) obj;
//		return nt.getText().equals(this.getText());
//	}
//	
//	@Override
//	public int hashCode() {
//		return this.getText().hashCode();
//	}
}
