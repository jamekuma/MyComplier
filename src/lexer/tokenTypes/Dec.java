package lexer.tokenTypes;

/**
 * 无符号十进制整数
 * @author xjy
 *
 */
public class Dec extends Token {
	private final int value;
	
	public Dec(int value, int lineNum) {
		super(Tag.DEC, lineNum);
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "< " + tag.toString() + ", " + value + " >";
	}
}
