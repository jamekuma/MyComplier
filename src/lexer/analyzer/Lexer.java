package lexer.analyzer;

import java.util.ArrayList;
import java.util.List;

import lexer.myException.DFAException;
import lexer.tokenTypes.Ch;
import lexer.tokenTypes.Dec;
import lexer.tokenTypes.Error;
import lexer.tokenTypes.Hex;
import lexer.tokenTypes.Oct;
import lexer.tokenTypes.Real;
import lexer.tokenTypes.Str;
import lexer.tokenTypes.Tag;
import lexer.tokenTypes.Token;
import lexer.tokenTypes.Word;

public class Lexer {
	
	private DFA dfa;
	private List<Token> tokenList = new ArrayList<Token>();
	private List<String> sourceWordList = new ArrayList<String>();
	public Lexer(DFA dfa) {
		this.dfa = dfa;
	}
	
	/**
	 * ��ȡ�ʷ�������Token����
	 * @return
	 */
	public List<Token> getTokens() {
		return tokenList;
	}
	
	/**
	 * ��ȡ��Ӧ��Token���е�ԭ����
	 * @return
	 */
	public List<String> getSourceWords() {
		return sourceWordList;
	}
	
	/**
	 * ��Դ������дʷ�����
	 * @param sourceCode Դ����
	 */
	public void analyze(String sourceCode) {
		tokenList = new ArrayList<Token>();
		char[] src = sourceCode.toCharArray();
		int i = 0;
		int lineNum = 1;
		while (i < src.length) {
			
			// �����հ��ַ�
			while (i < src.length && isCharEmpty(src[i])) {
				if (src[i] == '\n') lineNum++;
				i++;
			}
			
			dfa.init();  // dfa�ص���ʼ״̬
			StringBuilder record = new StringBuilder();  // ��¼ÿһ�ֵ��ַ���
			
			// �������������ڼ�¼�����ֵĽ���״̬����Ӧ���ַ��±�
			String lastAccStat = null;
			int lastAccIndex = -1;
			
			// ÿ�����ѭ��һ�Σ���ʶ��һ�� Token/����
			while (i < src.length) {
				
				if(dfa.isNextCharLegal(src[i])) {
					dfa.move(src[i]);
					if (dfa.isAccStat()) {   // ��¼�����ֵĽ���״̬, ���ڴ�����
						lastAccStat = dfa.getCurrentStat();
						lastAccIndex = i;
					}
					
					record.append(src[i]);
					if (src[i] == '\n') lineNum++;
					i++;
				} 
				else {     // �������޷�����ת�Ƶ��ַ�
					if (dfa.isAccStat()) { // �����ڽ���״̬, �����Ǳ����ʽ�����
						break;
					}
					else {  // �����Ǵ��� ���ô�����
						/*********** ������ʼ ************/
						System.err.println("error handle");
						if (lastAccStat != null) {  // ������һ������״̬
							
							for (int j = 0; j < i - lastAccIndex - 1; j++) { // ��ǰ�����ַ�
								record.deleteCharAt(record.length() - 1);
							}
							i = lastAccIndex + 1;
							try
								{dfa.jumpToStat(lastAccStat);}
							catch (DFAException de){}
								
							break;
						}
						else {  // ����ʼ����ָ�����, ���ÿֻ�ģʽ
							System.err.println("error recovery ���� panic!!");
							StringBuilder errorWord = new StringBuilder();
							while (i < src.length && !dfa.isNextCharLegal(src[i])) {
								errorWord.append(src[i]);
								if (src[i] == '\n') lineNum++;
								i++;  // ���ɾ���ַ�
							}
							tokenList.add(new Error(errorWord.toString(), lineNum));
							sourceWordList.add(errorWord.toString());
						}
						/*********** ��������� ************/
					}
				}
			}
			String recStr = record.toString();
			if (dfa.isAccStat()) {  
				String type = dfa.getAccType();
				assert type != null;
				if (!type.equals("COMMENT")) { // ����ע��. ����ΪToken����
					tokenList.add(getTokenFromType(type, recStr, lineNum));
					sourceWordList.add(recStr);
				}
			}
			else if (i < src.length){  // �˵��ʴ��ڴ���
				System.err.println("error");
			}
			
		}
	}
	
	/**
	 * �ɵ�ǰ����״̬����ʶ�Ľ������ͣ�����һ��token
	 * @param type
	 * @param str
	 * @return
	 */
	private Token getTokenFromType(String type, String str, int lineNum) {
		if (type.equals("DEC")) {
			return new Dec(Integer.parseInt(str, 10), lineNum);
		}
		else if (type.equals("HEX")) {
			return new Hex(Integer.parseInt(str.substring(2), 16), lineNum);
		}
		else if (type.equals("OCT")) {
			return new Oct(Integer.parseInt(str, 8), lineNum);
		}
		else if (type.equals("REAL")) {
			return new Real(Double.parseDouble(str), lineNum);
		}
		else if (type.equals("ID")) {
			return Word.recognize(str, lineNum);
		}
		else if (type.equals("STRING")) {
			str = str.substring(1, str.length() - 1);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i);
				if (ch == '\\') {
					if (str.charAt(i + 1) == 'n') {
						sb.append('\n');
					}
					else if (str.charAt(i + 1) == '"') {
						sb.append('"');
					}
					else if (str.charAt(i + 1) == 'r') {
						sb.append('\r');
					}
					else if (str.charAt(i + 1) == 't') {
						sb.append('\t');
					}
					i++;
				}
				else {
					sb.append(ch);
				}
			}
			
			return new Str(sb.toString(), lineNum);
		}
		else if (type.equals("CHAR")) {
			str = str.substring(1, str.length() - 1);
			char res = '\0';
			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i);
				if (ch == '\\') {
					if (str.charAt(i + 1) == 'n') {
						res = '\n';
					}
					else if (str.charAt(i + 1) == '"') {
						res = '"';
					}
					else if (str.charAt(i + 1) == 'r') {
						res = '\r';
					}
					else if (str.charAt(i + 1) == 't') {
						res = '\t';
					}
					i++;
				}
				else {
					res = ch;
				}
			}
			return new Ch(res, lineNum);
		}
		else {
			return new Token(Tag.valueOf(type.toUpperCase()), lineNum);
		}
	}
	
//	/**
//	 * ������
//	 */
//	private void errorHandle(String lastAccStat, int lastAccIndex) {
//		
//	}
//	
	private boolean isCharEmpty(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
	}
	
}
