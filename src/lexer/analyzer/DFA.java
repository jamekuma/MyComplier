package lexer.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import lexer.myException.DFAException;

/**
 * 词法分析的DFA
 * 
 * @author xjy
 *
 */
public class DFA {

	private String initStat; // 初始状态
	private String currentStat; // 当前状态
	private Map<String, Map<Character, String>> transTable = new HashMap<>(); // 状态转换表
	// defaultTable存储某些状态的默认跳转. 对于某些状态，对于某个字符未找到相关表项，则跳转至默认状态
	private Map<String, String> defaultTable = new HashMap<>();  
	private Map<String, String> AccStats = new HashMap<>();  // 接收状态集, 及各自代表 接收的类别
	
	/**
	 * 初始化一个DFA
	 */
	public DFA() {
	}
	
	/**
	 * 初始化一个DFA, 并设置开始状态
	 * 
	 * @param initStat
	 */
	public DFA(String initStat) {
		this.initStat = initStat;
	}
	
	/**
	 * 设置初始状态
	 * @param initStat
	 */
	public void setInitStat(String initStat) {
		this.initStat = initStat;
	}
	
	public void addAccStat(String AccStat, String AccType) {
		AccStats.put(AccStat, AccType);
	}

	/**
	 * 给转换表stat, ch添加一个表项nextStat, 若该表项已经存在, 则覆盖原有的表项
	 * 
	 * @param stat
	 * @param ch
	 * @param nextStat
	 */
	public void addItem(String stat, char ch, String nextStat) {
		if (!transTable.containsKey(stat)) {
			transTable.put(stat, new HashMap<Character, String>());
		}
		transTable.get(stat).put(ch, nextStat);
	}
	
	public static DFA getFromFile(File dfaFile) throws IOException {
		
		// 读json文件
		String encoding = "ISO-8859-1";
		DFA dfa = new DFA();
		Long filelength = dfaFile.length();
		byte[] filecontent = new byte[filelength.intValue()];
		FileInputStream in = new FileInputStream(dfaFile);
		in.read(filecontent);
		in.close();
		String jsStr = "";
		jsStr = new String(filecontent, encoding);
		
		/******************* 解析Json文件, 设置开始、接收状态、转换表 **********************/
		JSONObject js = JSONObject.parseObject(jsStr);
		// 设置开始、接收状态
		for (Entry<String, Object> row : js.entrySet()) {
			String stat = row.getKey();
			
			if (stat.charAt(stat.length() - 1) == '#') {  // 开始状态
				stat = stat.substring(0, stat.length() - 1);
				dfa.setInitStat(stat);
			}
			
			if (stat.charAt(stat.length() - 1) == '*') {  // 接受状态
				stat = stat.substring(0, stat.length() - 1);
				dfa.addAccStat(stat, ((JSONObject)row.getValue()).getString("accType"));
			}
			
			dfa.transTable.put(stat, new HashMap<>());
		}
		// 为所有状态设置转换表项
		for (Entry<String, Object> row : js.entrySet()) {
			String stat = row.getKey();
			char lastChar = stat.charAt(stat.length() - 1);
			if (lastChar == '#' || lastChar == '*') {
				stat = stat.substring(0, stat.length() - 1);
			}
			JSONObject items = ((JSONObject) row.getValue()).getJSONObject("next");
			for (Entry<String, Object> item : items.entrySet()) {
				String nextStat = items.getString(item.getKey());
				if (!dfa.transTable.containsKey(nextStat)) {
					throw new IOException("转换表不合法"); // 转换到了某个不存在的状态
				}
				String key = item.getKey();
				if (key.length() == 1) {
					char ch = key.charAt(0);
					
					dfa.addItem(stat, ch, nextStat);
				}
				else {
					if (key.equals("default")) {
						dfa.defaultTable.put(stat, nextStat);
					}
					else {
						char start = key.charAt(0);
						char end = key.charAt(key.length() - 1);
						for (char ch = start; ch <= end; ch++) {
							dfa.addItem(stat, ch, nextStat);
						}
					}
				}
			}
		}
		
		return dfa;
	}

	public static DFA getFromFile(String tableFilePath) throws IOException {
		File tableFile = new File(tableFilePath);
		return getFromFile(tableFile);
	}
	
	
	
	/**
	 * 回到初始状态
	 */
	public void init() {
		this.currentStat = initStat;
	}
	
	/**
	 * 获得当前状态
	 * @return 当前状态
	 */
	public String getCurrentStat() {
		return currentStat;
	}
	
	/**
	 * 当前状态是否是接收状态
	 * @return
	 */
	public boolean isAccStat() {
		return AccStats.containsKey(currentStat);
	}
	
	/**
	 * 若当前是接收状态，则返回接收单词的类型; 否则返回null
	 * @return
	 */
	public String getAccType() {
		return AccStats.get(currentStat);
	}
	
	/**
	 * 强制自动机跳转至某个状态
	 * @param newStat 新状态
	 * @throws DFAException 
	 */
	public void jumpToStat(String newStat) throws DFAException {
		if (!transTable.containsKey(newStat)) {
			throw new DFAException();
		}
		currentStat = newStat;
	}
	
	/**
	 * 状态转移
	 * @param ch 输入符号
	 * @throws DFAException 输入符号在表中没有项
	 */
	public void move(char ch) {
//		System.err.println(currentStat);
		if (!transTable.get(currentStat).containsKey(ch)) {
			if (defaultTable.containsKey(currentStat)) {
				this.currentStat = defaultTable.get(currentStat);
			}
			else {
				System.err.println("Error!!");
				assert false;
			}
		}
		else {
			this.currentStat = transTable.get(currentStat).get(ch);
		}
	}
	
	public boolean isNextCharLegal(char ch) {
		return transTable.get(currentStat).containsKey(ch) || defaultTable.containsKey(currentStat);
	}
}
