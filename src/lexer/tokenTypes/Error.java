package lexer.tokenTypes;

public class Error extends Token {
	private String errorWord;
	
	public Error(String errorWord, int lineNum) {
		super(Tag.ERROR, lineNum);
		this.errorWord = errorWord;
	}
	
	@Override
	public String toString() {
		return "< " + tag.toString() + ", " + errorWord + " >";
	}
}
