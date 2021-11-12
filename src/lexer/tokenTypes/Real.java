package lexer.tokenTypes;

/**
 * ¸¡µã³£Á¿
 * @author xjy
 *
 */
public class Real extends Token {
	private final double value;
	
	public Real(double value, int lineNum) {
		super(Tag.REAL, lineNum);
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "< " + tag.toString() + ", " + value + " >";
	}
}
