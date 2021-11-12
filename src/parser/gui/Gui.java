package parser.gui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileSystemView;

import lexer.analyzer.DFA;
import lexer.analyzer.Lexer;
import lexer.tokenTypes.Token;
import parser.LR1.Grammar;
import parser.LR1.Item;
import parser.LR1.Nonterminal;
import parser.LR1.Symbol;
import parser.LR1.Terminal;
import parser.LR1.TerminalSetPair;
import parser.analyzer.Parser;

public class Gui extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel panel = new JPanel();
	JButton jb1 = new JButton("语法分析");
	JButton jb2 = new JButton("导入文本");
	JButton jb3 = new JButton("导入文法");
	JTextArea LR1JTA = new JTextArea();
	JTextArea out = new JTextArea();
	JTextArea in = new JTextArea();
	JTextArea firstSetText = new JTextArea();
	JTextArea itemSetText = new JTextArea();

	File source = new File("./srcCode.txt");
	File dfatran = new File("./DFATable.json");
//	File grammarFile = new File("./Grammar_error.txt");

	Grammar grammar;

	public Gui() throws IOException {

		grammar = new Grammar("./Grammar_error_text.txt");
		// TODO 自动生成的构造函数存根
		this.setTitle("语法分析v1.0");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		placeComponents(panel);
		this.add(panel);
		this.setBounds(((Toolkit.getDefaultToolkit().getScreenSize().width) / 2) - 700,
				((Toolkit.getDefaultToolkit().getScreenSize().height) / 2) - 700, 935 * 18 / 10, 780 * 18 / 10);

		this.setVisible(true);
	}

	private void placeComponents(JPanel panel) throws IOException {
		panel.setLayout(null);

		JPanel dfa = new JPanel(null);

		JLabel m = new JLabel("LR分析表", JLabel.CENTER);
		m.setFont(new Font("黑体", Font.PLAIN, 30));
		m.setBounds(20, 10, 850 * 18 / 10, 65 * 18 / 10);
		JScrollPane js0 = new JScrollPane(LR1JTA);
		JScrollPane js1 = new JScrollPane(itemSetText);
		js0.setBounds(20, 80 * 18 / 10, 850 * 18 / 10, 280 * 18 / 10);
		js1.setBounds(20, 380 * 18 / 10, 850 * 18 / 10, 280 * 18 / 10);
		LR1JTA.setEditable(false);
		LR1JTA.setFont(new Font("黑体", Font.PLAIN, 20));
		itemSetText.setEditable(false);
		itemSetText.setFont(new Font("黑体", Font.PLAIN, 20));

		jb3.setBounds(400 * 18 / 10, 640 * 18 / 10, 100, 40);
		dfa.add(m);
		dfa.add(js0);
		dfa.add(js1);
		dfa.add(jb3);

		JPanel firstSetPanel = new JPanel(null);
		JLabel mm = new JLabel("first集(部分文法符号)", JLabel.CENTER);
		mm.setFont(new Font("黑体", Font.PLAIN, 30));
		mm.setBounds(20, 20, 850 * 18 / 10, 50 * 18 / 10);
		JScrollPane jss = new JScrollPane(firstSetText);
		jss.setBounds(20, 80, 850 * 18 / 10, 480 * 18 / 10);
		firstSetText.setEditable(false);
		firstSetText.setFont(new Font("黑体", Font.PLAIN, 20));
		firstSetPanel.add(mm);
		firstSetPanel.add(jss);

		JScrollPane js2 = new JScrollPane(out);
		js2.setBounds(20, 80, 400 * 18 / 10, 550 * 18 / 10);

		JScrollPane js3 = new JScrollPane(in);
		js3.setBounds(470 * 18 / 10, 80, 400 * 18 / 10, 470 * 18 / 10);

		out.setEditable(false);
		in.setEditable(true);

		JLabel o = new JLabel("输出区", JLabel.CENTER);
		JLabel i = new JLabel("输入区", JLabel.CENTER);
		o.setBounds(20, 15, 400 * 18 / 10, 50 * 18 / 10);
		o.setFont(new Font("黑体", Font.PLAIN, 22));
		i.setBounds(470 * 18 / 10, 15, 400 * 18 / 10, 50 * 18 / 10);
		i.setFont(new Font("黑体", Font.PLAIN, 22));

		in.setFont(new Font("黑体", Font.PLAIN, 20));
		out.setFont(new Font("黑体", Font.PLAIN, 20));

		jb1.setBounds(540 * 18 / 10, 570 * 18 / 10, 100, 40);
		jb2.setBounds(680 * 18 / 10, 570 * 18 / 10, 100, 40);

		JPanel work = new JPanel(null);
		work.add(o);
		work.add(i);
		work.add(js2);
		work.add(js3);
		work.add(jb1);
		work.add(jb2);

		JTabbedPane tp = new JTabbedPane();

		tp.addTab("work", work);
		tp.setEnabledAt(0, true);
		tp.setTitleAt(0, "工作区");

		tp.addTab("dfa", dfa);
		tp.setEnabledAt(1, true);
		tp.setTitleAt(1, "LR分析表");

		tp.addTab("firstSet", firstSetPanel);
		tp.setEnabledAt(2, true);
		tp.setTitleAt(2, "first集");

		tp.setTabPlacement(JTabbedPane.TOP);
		tp.setBounds(10, 10, 900 * 18 / 10, 700 * 18 / 10);
		panel.add(tp);

		jb1.addActionListener(this);
		jb2.addActionListener(this);
		jb3.addActionListener(this);

		// LR1JTA.setText(readsource(dfatran));
		// LR table
		StringBuilder fw = new StringBuilder();
		for (int ii = 0; ii < grammar.actionTable.size(); ii++) {
			fw.append("状态 " + ii + "\n");
			fw.append("Action: {   ");
			for (Map.Entry<Terminal, String> entry : grammar.actionTable.get(ii).entrySet()) {
				fw.append(entry.getKey().toString() + " --> " + entry.getValue() + "   ");
			}
			fw.append("}\n");
			fw.append("GoTo: {   ");
			for (Map.Entry<Nonterminal, Integer> entry : grammar.goToTable.get(ii).entrySet()) {
				fw.append(entry.getKey().toString() + " --> " + entry.getValue().toString() + "   ");
			}
			fw.append("}\n");
		}
		
		in.setText(readsource(source));
		LR1JTA.setText(fw.toString());

		fw = new StringBuilder();
		for (int ii = 0; ii < grammar.actionTable.size(); ii++) {
			fw.append("状态 " + ii + ": \n");
			for (Item it : grammar.itemsSetList.get(ii)) {
				fw.append(it.toString() + "\n");
			}
			fw.append('\n');
		}
		itemSetText.setText(fw.toString());

		// first
		fw = new StringBuilder();
		for (Map.Entry<Symbol, TerminalSetPair> entry : grammar.firstSetRecord.entrySet()) {
			fw.append("First(" + entry.getKey() + ") = ");
			fw.append(entry.getValue().set.toString());
			if (entry.getValue().emptySymbol) {
				fw.append("  ε");
			}
			fw.append('\n');
		}
		firstSetText.setText(fw.toString());

	}

	private String readsource(File f) throws IOException {

		File file = f;
		String encoding = "GBK";
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		FileInputStream in = new FileInputStream(file);
		in.read(filecontent);
		in.close();
		return new String(filecontent, encoding);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jb1) {
			out.setText("");
			DFA dfa;
			try {
				dfa = DFA.getFromFile("DFATable.json");
				Lexer lexer = new Lexer(dfa);
				lexer.analyze(in.getText());
				List<Token> tokens = lexer.getTokens();
//				for (int i = 0; i < tokens.size(); i++) {
//					out.setText(out.getText() + words.get(i) + "\t\t");
//					out.setText(out.getText() + tokens.get(i) + "\n");
//				}
				Parser parser = new Parser(grammar);
				parser.analyze(tokens);
				out.setText(parser.treeString());
			} catch (IOException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}

		} else if (e.getSource() == jb2) {
			JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
			int returnValue = jfc.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jfc.getSelectedFile();
				try {
					in.setText(readsource(selectedFile));
				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
			}

		} else {
			JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
			int returnValue = jfc.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jfc.getSelectedFile();
				try {
					grammar = new Grammar(selectedFile.getPath());
					StringBuilder fw = new StringBuilder();
					for (int i = 0; i < grammar.actionTable.size(); i++) {
						fw.append("状态 " + i + "\n");
						fw.append("Action: {   ");
						for (Map.Entry<Terminal, String> entry : grammar.actionTable.get(i).entrySet()) {
							fw.append(entry.getKey().toString() + " --> " + entry.getValue() + "   ");
						}
						fw.append("}\n");
						fw.append("GoTo: {   ");
						for (Map.Entry<Nonterminal, Integer> entry : grammar.goToTable.get(i).entrySet()) {
							fw.append(entry.getKey().toString() + " --> " + entry.getValue().toString() + "   ");
						}
						fw.append("}\n");
					}
//					LR1JTA.setText(readsource(selectedFile));
					LR1JTA.setText(fw.toString());

					fw = new StringBuilder();
					for (int i = 0; i < grammar.actionTable.size(); i++) {
						fw.append("状态 " + i + ": \n");
						for (Item it : grammar.itemsSetList.get(i)) {
							fw.append(it.toString() + "\n");
						}
						fw.append('\n');
					}
					itemSetText.setText(fw.toString());

					// first
					fw = new StringBuilder();
					for (Map.Entry<Symbol, TerminalSetPair> entry : grammar.firstSetRecord.entrySet()) {
						fw.append("First(" + entry.getKey() + ") = ");
						
						fw.append(entry.getValue().set.toString());
						if (entry.getValue().emptySymbol) {
							fw.append("  ε");
						}
						fw.append('\n');
					}
					firstSetText.setText(fw.toString());
				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
			}
		}

	}

}
