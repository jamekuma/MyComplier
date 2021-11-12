package lexer.gui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
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

public class Gui extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel panel = new JPanel();
	JButton jb1 = new JButton("词法分析");
	JButton jb2 = new JButton("导入文本");
	JButton jb3 = new JButton("导入转换表");
	JTextArea dfaJTA = new JTextArea();
	JTextArea out = new JTextArea();
	JTextArea in = new JTextArea();

	File source = new File("./srcCode.txt");
	File dfatran = new File("./DFATable.json");

	public Gui() {
		// TODO 自动生成的构造函数存根
		this.setTitle("词法分析v1.0");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		placeComponents(panel);
		this.add(panel);
		this.setBounds(((Toolkit.getDefaultToolkit().getScreenSize().width) / 2) - 300,
				((Toolkit.getDefaultToolkit().getScreenSize().height) / 2) - 300, 935, 780);

		this.setVisible(true);
	}

	private void placeComponents(JPanel panel) {
		panel.setLayout(null);

		JPanel dfa = new JPanel(null);

		JLabel m = new JLabel("DFA状态转换表", JLabel.CENTER);
		m.setFont(new Font("黑体", Font.PLAIN, 30));
		m.setBounds(20, 20, 850, 50);
		JScrollPane js1 = new JScrollPane(dfaJTA);
		js1.setBounds(20, 80, 850, 480);
		dfaJTA.setEditable(false);
		dfaJTA.setFont(new Font("黑体", Font.PLAIN, 20));

		jb3.setBounds(400, 570, 100, 40);
		dfa.add(m);
		dfa.add(js1);
		dfa.add(jb3);

		JPanel work = new JPanel(null);

		JScrollPane js2 = new JScrollPane(out);
		js2.setBounds(20, 80, 400, 550);

		JScrollPane js3 = new JScrollPane(in);
		js3.setBounds(470, 80, 400, 470);

		out.setEditable(false);
		in.setEditable(true);

		JLabel o = new JLabel("输出区", JLabel.CENTER);
		JLabel i = new JLabel("输入区", JLabel.CENTER);
		o.setBounds(20, 20, 400, 50);
		o.setFont(new Font("黑体", Font.PLAIN, 22));
		i.setBounds(470, 20, 400, 50);
		i.setFont(new Font("黑体", Font.PLAIN, 22));

		in.setFont(new Font("黑体", Font.PLAIN, 20));
		out.setFont(new Font("黑体", Font.PLAIN, 20));

		jb1.setBounds(540, 570, 100, 40);
		jb2.setBounds(680, 570, 100, 40);

		work.add(o);
		work.add(i);
		work.add(js2);
		work.add(js3);
		work.add(jb1);
		work.add(jb2);

		JTabbedPane tp = new JTabbedPane();

		tp.addTab("dfa", dfa);
		tp.setEnabledAt(0, true);
		tp.setTitleAt(0, "状态转换表");

		tp.addTab("work", work);
		tp.setEnabledAt(1, true);
		tp.setTitleAt(1, "工作区");

		tp.setTabPlacement(JTabbedPane.TOP);
		tp.setBounds(10, 10, 900, 700);
		panel.add(tp);

		jb1.addActionListener(this);
		jb2.addActionListener(this);
		jb3.addActionListener(this);

		try {
			dfaJTA.setText(readsource(dfatran));
			in.setText(readsource(source));
		} catch (IOException e) {
			e.printStackTrace();
		}

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
				List<String> words = lexer.getSourceWords();
				for (int i = 0; i < tokens.size(); i++) {
					out.setText(out.getText() + words.get(i) + "\t\t");
					out.setText(out.getText() + tokens.get(i) + "\n");
				}
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
					dfaJTA.setText(readsource(selectedFile));
				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
			}
		}

	}

}
