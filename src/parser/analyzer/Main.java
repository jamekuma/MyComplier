package parser.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lexer.analyzer.DFA;
import lexer.analyzer.Lexer;
import lexer.tokenTypes.Token;
import parser.LR1.Grammar;
import parser.LR1.Item;
import parser.LR1.Nonterminal;
import parser.LR1.Symbol;
import parser.LR1.Terminal;
import parser.LR1.TerminalSetPair;
import semantic.Instruction;
import semantic.SymbolTableEntry;

public class Main {

	public static void main(String[] args) throws IOException {
		// 读源文件
		String sourceFilePath;
		if (args.length > 0)
			sourceFilePath = args[0];
		else
			sourceFilePath = "./srcCode_error.txt";
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

//		// 打印结果
//		System.out.println("词法分析结果: ");
//		List<String> words = lexer.getSourceWords();
//		for (int i = 0; i < tokens.size(); i++) {
//			System.out.print(words.get(i) + "\t\t");
//			System.out.println(tokens.get(i));
//		}

		
		// 语法分析
		Grammar grammar = new Grammar("Grammar_new.txt");
		Parser parser = new Parser(grammar);
		parser.analyze(tokens);
//		System.out.println("语法分析树: ");
//		System.out.println(parser.treeString());
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
		
//		// 打印LR分析表、first集到文件
//		File firstFile = new File("FirstSet.txt");
//		File table = new File("LRTable.txt");
//		FileWriter fw = new FileWriter(firstFile);
//		for (Map.Entry<Symbol, TerminalSetPair> entry : grammar.firstSetRecord.entrySet()) {
//			fw.append("First(" + entry.getKey() + ") = ");
//			fw.append(entry.getValue().set.toString());
//			fw.append('\n');
//			fw.flush();
//		}
//		fw = new FileWriter(table);
//		for (int i = 0; i < grammar.actionTable.size(); i++) {
//			fw.append("状态 " + i + ": { ");
//			for (Item it : grammar.itemsSetList.get(i)) {
//				fw.append(it.toString() + " ");
//			}
//			fw.append("}\n");
//			fw.append("Action: {   ");
//			for (Map.Entry<Terminal, String> entry : grammar.actionTable.get(i).entrySet()) {
//				fw.append(entry.getKey().toString() + "-->" + entry.getValue() + "   ");
//			}
//			fw.append("}\n");
//			fw.append("GoTo: {   ");
//			for (Map.Entry<Nonterminal, Integer> entry : grammar.goToTable.get(i).entrySet()) {
//				fw.append(entry.getKey().toString() + "-->" + entry.getValue().toString() + "   ");
//			}
//			fw.append("}\n");
//			fw.flush();
//		}
	}

}
