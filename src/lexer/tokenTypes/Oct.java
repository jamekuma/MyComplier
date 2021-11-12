package lexer.tokenTypes;

/**
 * �޷��Ű˽�������
 * @author xjy
 *
 */
public class Oct extends Token {
	private final int value;
	
	public Oct(int value, int lineNum) {
		super(Tag.OCT, lineNum);
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
