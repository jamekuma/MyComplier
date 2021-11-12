package lexer.tokenTypes;

/**
 * ×Ö·û³£Á¿
 * @author xjy
 *
 */
public class Ch extends Token {
	private final char value;
	
	public Ch(char value, int lineNum) {
		super(Tag.CHAR, lineNum);
		this.value = value;
	}
	public char getValue() {
		return value;
	}
	@Override
	public String toString() {
		return "< " + tag.toString() + ", \'" + value + "\' >";
	}
}
