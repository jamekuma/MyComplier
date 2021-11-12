package semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import lexer.analyzer.DFA;
import lexer.analyzer.Lexer;
import lexer.tokenTypes.Token;
import parser.LR1.Grammar;
import parser.analyzer.Parser;

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
				List<Token> tokens = lexer.getTokens();

				
				// 语法制导翻译
				Grammar grammar = new Grammar("Grammar_new.txt");
				Parser parser = new Parser(grammar);
				parser.analyze(tokens);
				
				if (parser.getErrorMsg().equals("")) {
					System.out.println("符号表：");
					System.out.println(parser.getSymbolTable().toString());
					System.out.println("指令序列：");
					for (Map.Entry<String, List<Instruction>> entry : parser.getIntructions().entrySet()) {
						System.out.println(entry.getKey() + ":");
						for (int i = 0; i < entry.getValue().size(); i++) {
							System.out.print(i + ":" + "\t");
							System.out.printf("%-30s", entry.getValue().get(i).getQuadra());
							System.out.println(entry.getValue().get(i).getTriple());
						}
					}
				}
				else {
					System.out.println(parser.getErrorMsg());
				}
	}

}
