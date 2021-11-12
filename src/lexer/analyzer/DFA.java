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
 * �ʷ�������DFA
 * 
 * @author xjy
 *
 */
public class DFA {

	private String initStat; // ��ʼ״̬
	private String currentStat; // ��ǰ״̬
	private Map<String, Map<Character, String>> transTable = new HashMap<>(); // ״̬ת����
	// defaultTable�洢ĳЩ״̬��Ĭ����ת. ����ĳЩ״̬������ĳ���ַ�δ�ҵ���ر������ת��Ĭ��״̬
	private Map<String, String> defaultTable = new HashMap<>();  
	private Map<String, String> AccStats = new HashMap<>();  // ����״̬��, �����Դ��� ���յ����
	
	/**
	 * ��ʼ��һ��DFA
	 */
	public DFA() {
	}
	
	/**
	 * ��ʼ��һ��DFA, �����ÿ�ʼ״̬
	 * 
	 * @param initStat
	 */
	public DFA(String initStat) {
		this.initStat = initStat;
	}
	
	/**
	 * ���ó�ʼ״̬
	 * @param initStat
	 */
	public void setInitStat(String initStat) {
		this.initStat = initStat;
	}
	
	public void addAccStat(String AccStat, String AccType) {
		AccStats.put(AccStat, AccType);
	}

	/**
	 * ��ת����stat, ch���һ������nextStat, ���ñ����Ѿ�����, �򸲸�ԭ�еı���
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
		
		// ��json�ļ�
		String encoding = "ISO-8859-1";
		DFA dfa = new DFA();
		Long filelength = dfaFile.length();
		byte[] filecontent = new byte[filelength.intValue()];
		FileInputStream in = new FileInputStream(dfaFile);
		in.read(filecontent);
		in.close();
		String jsStr = "";
		jsStr = new String(filecontent, encoding);
		
		/******************* ����Json�ļ�, ���ÿ�ʼ������״̬��ת���� **********************/
		JSONObject js = JSONObject.parseObject(jsStr);
		// ���ÿ�ʼ������״̬
		for (Entry<String, Object> row : js.entrySet()) {
			String stat = row.getKey();
			
			if (stat.charAt(stat.length() - 1) == '#') {  // ��ʼ״̬
				stat = stat.substring(0, stat.length() - 1);
				dfa.setInitStat(stat);
			}
			
			if (stat.charAt(stat.length() - 1) == '*') {  // ����״̬
				stat = stat.substring(0, stat.length() - 1);
				dfa.addAccStat(stat, ((JSONObject)row.getValue()).getString("accType"));
			}
			
			dfa.transTable.put(stat, new HashMap<>());
		}
		// Ϊ����״̬����ת������
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
					throw new IOException("ת�����Ϸ�"); // ת������ĳ�������ڵ�״̬
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
	 * �ص���ʼ״̬
	 */
	public void init() {
		this.currentStat = initStat;
	}
	
	/**
	 * ��õ�ǰ״̬
	 * @return ��ǰ״̬
	 */
	public String getCurrentStat() {
		return currentStat;
	}
	
	/**
	 * ��ǰ״̬�Ƿ��ǽ���״̬
	 * @return
	 */
	public boolean isAccStat() {
		return AccStats.containsKey(currentStat);
	}
	
	/**
	 * ����ǰ�ǽ���״̬���򷵻ؽ��յ��ʵ�����; ���򷵻�null
	 * @return
	 */
	public String getAccType() {
		return AccStats.get(currentStat);
	}
	
	/**
	 * ǿ���Զ�����ת��ĳ��״̬
	 * @param newStat ��״̬
	 * @throws DFAException 
	 */
	public void jumpToStat(String newStat) throws DFAException {
		if (!transTable.containsKey(newStat)) {
			throw new DFAException();
		}
		currentStat = newStat;
	}
	
	/**
	 * ״̬ת��
	 * @param ch �������
	 * @throws DFAException ��������ڱ���û����
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
