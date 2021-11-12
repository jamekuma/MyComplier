package lexer.tokenTypes;

/**
 * 一个Token
 * @author xjy
 *
 */
public class Token {
	protected Tag tag;  // 种别码
	protected int lineNum;
	
	public Token(Tag tag, int lineNum) {
		this.tag = tag; 
		this.lineNum = lineNum;
	}
	
	public Tag getTag() {
		return tag;
	}
	
	public int getLineNum() {
		return lineNum;
	}
	
	@Override
	public String toString() {
		return "< " + tag.toString() + "," + "  >";
	}
}
