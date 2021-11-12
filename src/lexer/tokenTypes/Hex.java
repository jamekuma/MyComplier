package lexer.tokenTypes;

/**
 * 无符号十六进制整数
 * @author xjy
 *
 */
public class Hex extends Token {
	private final int value;
	
	public Hex(int value, int lineNum) {
		super(Tag.HEX, lineNum);
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
