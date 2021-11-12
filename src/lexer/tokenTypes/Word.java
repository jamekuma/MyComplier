package lexer.tokenTypes;

/**
 * 关键字 或 标识符 对应的Token
 * 
 * @author xjy
 *
 */
public class Word extends Token {
	// 属性值
	private String value = "";
	// 所有基本类型的字符串
	private final static String[] basic = new String[] { "int", "double", "float", "bool", "char" };
	// 所有关键字的字符串(除基本类型外)
	private final static String[] keyWord = new String[] {
			"if", "else", "do", "while", "for", "call", "true", "false", "return", "proc", "record"
	};

	private Word(Tag tag, String value, int lineNum) {
		super(tag, lineNum);
		this.value = value;
		for (int i = 0; i < basic.length; i++) {
			if (value.equals(basic[i])) {
				tag = Tag.BASIC;
				
				return;
			}
		}
	}
	
	/**
	 * 根据单词自动识别 关键词、基本类型、标识符
	 * @param value 单词
	 * @return Word类的 Token对象
	 */
	public static Word recognize(String value, int lineNum) {
		// 基本类型, 多词一码
		for (int i = 0; i < basic.length; i++) {
			if (value.equals(basic[i])) {
				return new Word(Tag.BASIC, value, lineNum);
			}
		}
		
		// 其他关键字, 一词一码
		for (int i = 0; i < keyWord.length; i++) {
			if (value.equals(keyWord[i])) {
				return new Word(Tag.valueOf(value.toUpperCase()), "", lineNum);
			}
		}
		
		// 标识符, 多词一码
		return new Word(Tag.ID, value, lineNum);
	}
	
	/**
	 * 如果是基本类型, 则返回其字节大小
	 * @return
	 */
	public int size() {
		if (value.equals("int")) {
			return 4;
		}
		else if (value.equals("double")) {
			return 8;
		}
		else if (value.equals("float")) {
			return 4;
		}
		else if (value.equals("bool")) {
			return 1;
		}
		else if (value.equals("char")) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "< " + tag.toString() + ", " + (value.equals("") ? " " : value) + " >";
	}
}
