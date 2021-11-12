package lexer.tokenTypes;

/**
 * ×Ö·û´®³£Á¿
 * @author xjy
 *
 */
public class Str extends Token {
	public final String value;
	
	public Str(String value, int lineNum) {
		super(Tag.STRING, lineNum);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "< " + tag.toString() + ", \"" + value + "\" >";
	}
}
