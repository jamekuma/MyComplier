package lexer.tokenTypes;

/**
 * �ؼ��� �� ��ʶ�� ��Ӧ��Token
 * 
 * @author xjy
 *
 */
public class Word extends Token {
	// ����ֵ
	private String value = "";
	// ���л������͵��ַ���
	private final static String[] basic = new String[] { "int", "double", "float", "bool", "char" };
	// ���йؼ��ֵ��ַ���(������������)
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
	 * ���ݵ����Զ�ʶ�� �ؼ��ʡ��������͡���ʶ��
	 * @param value ����
	 * @return Word��� Token����
	 */
	public static Word recognize(String value, int lineNum) {
		// ��������, ���һ��
		for (int i = 0; i < basic.length; i++) {
			if (value.equals(basic[i])) {
				return new Word(Tag.BASIC, value, lineNum);
			}
		}
		
		// �����ؼ���, һ��һ��
		for (int i = 0; i < keyWord.length; i++) {
			if (value.equals(keyWord[i])) {
				return new Word(Tag.valueOf(value.toUpperCase()), "", lineNum);
			}
		}
		
		// ��ʶ��, ���һ��
		return new Word(Tag.ID, value, lineNum);
	}
	
	/**
	 * ����ǻ�������, �򷵻����ֽڴ�С
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
