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
	 * 获取词法分析的Token序列
	 * @return
	 */
	public List<Token> getTokens() {
		return tokenList;
	}
	
	/**
	 * 获取对应于Token序列的原单词
	 * @return
	 */
	public List<String> getSourceWords() {
		return sourceWordList;
	}
	
	/**
	 * 对源代码进行词法分析
	 * @param sourceCode 源代码
	 */
	public void analyze(String sourceCode) {
		tokenList = new ArrayList<Token>();
		char[] src = sourceCode.toCharArray();
		int i = 0;
		int lineNum = 1;
		while (i < src.length) {
			
			// 跳过空白字符
			while (i < src.length && isCharEmpty(src[i])) {
				if (src[i] == '\n') lineNum++;
				i++;
			}
			
			dfa.init();  // dfa回到初始状态
			StringBuilder record = new StringBuilder();  // 记录每一轮的字符串
			
			// 这两个变量用于记录最后出现的接收状态、对应的字符下标
			String lastAccStat = null;
			int lastAccIndex = -1;
			
			// 每进入该循环一次，就识别一个 Token/错误
			while (i < src.length) {
				
				if(dfa.isNextCharLegal(src[i])) {
					dfa.move(src[i]);
					if (dfa.isAccStat()) {   // 记录最后出现的接收状态, 用于错误处理
						lastAccStat = dfa.getCurrentStat();
						lastAccIndex = i;
					}
					
					record.append(src[i]);
					if (src[i] == '\n') lineNum++;
					i++;
				} 
				else {     // 遇到了无法继续转移的字符
					if (dfa.isAccStat()) { // 若处于接收状态, 可能是本单词结束了
						break;
					}
					else {  // 否则是错误， 调用错误处理
						/*********** 错误处理开始 ************/
						System.err.println("error handle");
						if (lastAccStat != null) {  // 存在上一个接收状态
							
							for (int j = 0; j < i - lastAccIndex - 1; j++) { // 向前回退字符
								record.deleteCharAt(record.length() - 1);
							}
							i = lastAccIndex + 1;
							try
								{dfa.jumpToStat(lastAccStat);}
							catch (DFAException de){}
								
							break;
						}
						else {  // 否则开始错误恢复程序, 采用恐慌模式
							System.err.println("error recovery —— panic!!");
							StringBuilder errorWord = new StringBuilder();
							while (i < src.length && !dfa.isNextCharLegal(src[i])) {
								errorWord.append(src[i]);
								if (src[i] == '\n') lineNum++;
								i++;  // 向后删除字符
							}
							tokenList.add(new Error(errorWord.toString(), lineNum));
							sourceWordList.add(errorWord.toString());
						}
						/*********** 错误处理结束 ************/
					}
				}
			}
			String recStr = record.toString();
			if (dfa.isAccStat()) {  
				String type = dfa.getAccType();
				assert type != null;
				if (!type.equals("COMMENT")) { // 不是注释. 则作为Token加入
					tokenList.add(getTokenFromType(type, recStr, lineNum));
					sourceWordList.add(recStr);
				}
			}
			else if (i < src.length){  // 此单词存在错误
				System.err.println("error");
			}
			
		}
	}
	
	/**
	 * 由当前接收状态及标识的接收类型，生成一个token
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
//	 * 错误处理
//	 */
//	private void errorHandle(String lastAccStat, int lastAccIndex) {
//		
//	}
//	
	private boolean isCharEmpty(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
	}
	
}
