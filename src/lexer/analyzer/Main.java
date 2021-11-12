package lexer.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import lexer.tokenTypes.Token;

public class Main {

	public static void main(String[] args) throws IOException {
		// 读源文件
		String sourceFilePath;
		if (args.length > 0)
			sourceFilePath = args[0];
		else 
			sourceFilePath = "./srcCode.txt";
		String encoding = "ISO-8859-1";
		File file = new File(sourceFilePath);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		FileInputStream in = new FileInputStream(file);
		in.read(filecontent);
		in.close();
		String srcCodeStr = new String(filecontent, encoding);
		
		// 词法分析
		DFA dfa = DFA.getFromFile("DFATable.json");
		Lexer lexer = new Lexer(dfa);
		lexer.analyze(srcCodeStr);
		
		// 打印结果
		List<Token> tokens = lexer.getTokens();
		List<String> words = lexer.getSourceWords();
		for (int i = 0; i < tokens.size(); i++) {
			System.out.print(words.get(i) + "\t\t");
			System.out.println(tokens.get(i));
		}
		
	}

}
