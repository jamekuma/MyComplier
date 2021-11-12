package parser.LR1;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ����һ���ķ�
 * @author xjy
 *
 */
public class Grammar {
	private Nonterminal startSymbol;
	private List<Production> productionsList = new ArrayList<Production>(); // �ķ������в���ʽ, ÿ������ʽ���ж����ı��
	private Map<Production, Integer> productionToNum = new HashMap<Production, Integer>();
	private Map<Nonterminal, List<Integer>> leftMap = new HashMap<>();   // "����ʽ��" -> "���и��󲿵����в���ʽ"
//	private List<Item> itemsList;             // ���ķ����в���ʽ���ɵ����е���Ŀ
	
	public List<Set<Item>> itemsSetList = new ArrayList<>();     // ������Ŀ���ֳ�����Ŀ���б��洢������Ŀ���, ��ӦitemList�е���ţ�����Ӧ��LR�Զ�����״̬
	private Map<Set<Item>, Integer> itemsSetToNum = new HashMap<>(); // ��Ŀ��ӳ�䵽 ���� 
	public List<Map<Terminal, String>> actionTable = new ArrayList<>();
	public List<Map<Nonterminal, Integer>> goToTable = new ArrayList<>();	
	public Map<Nonterminal, String> errorMsg = new HashMap<Nonterminal, String>();   // �洢���ս���Ĵ�����ʾ��Ϣ
	
	public Map<Symbol, TerminalSetPair> firstSetRecord = new HashMap<>();  // ��¼���е������ŵ�first���� �����ظ�����
	
	public Grammar() {}
	
	public Grammar(String grammarPath) throws IOException {
		
		// read from file
		List<String> lines = Files.readAllLines((new File(grammarPath)).toPath(), Charset.forName("GBK"));
		for (String line : lines) {
			String[] sides = line.split(" -> ");
			Nonterminal left = new Nonterminal(sides[0]);
			String[] rightWords = sides[1].split(" +");
			List<Symbol> right = new ArrayList<Symbol>();
			for (String word : rightWords) {
				if (!word.equals("")) {
					if (word.equals("��")) {
						break;
					}
					else if (Character.isUpperCase(word.charAt(0))) { // ��д��ͷ�����ս��
						right.add(new Nonterminal(word));
					}
					else if(word.charAt(0) != '$'){  // �ս��
						right.add(new Terminal(word));
					}
					else {    // ������Ϣ
						errorMsg.put(left, word.substring(1, word.length() - 1));
					}
						
				}
			}
			this.addProduction(new Production(left, right));
		}
		
		// construct table
		this.constructLRTable();
	} 
	
	
	
	public void addProduction(Production p) {
		if (leftMap.containsKey(p.getLeft())) {
			leftMap.get(p.getLeft()).add(productionsList.size());
		}
		else {
			leftMap.put(p.getLeft(), new ArrayList<Integer>());
			leftMap.get(p.getLeft()).add(productionsList.size());
			if (productionsList.size() == 0) {
				startSymbol = p.getLeft();
			}
		}
		productionToNum.put(p, productionsList.size());
		productionsList.add(p);
	}
	
	public void addProduction(List<Production> pList) {
		for (Production p : pList) {
			addProduction(p);
		}
	}
	
	public Production getProduction(int num) {
		return productionsList.get(num);
	}
	
	/**
	 * ����ĳ����Ŀ���� ��Ŀ���հ�
	 * @param itemsSet ������հ�����Ŀ��
	 * @return ��Ŀ���հ�
	 */
	public Set<Item> closure(Set<Item> itemsSet) {
		Set<Item> resSet = new HashSet<>(itemsSet);
		Set<Item> add = new HashSet<>();
		Set<Item> newPart = new HashSet<>(itemsSet); 
		do {  
			add.clear();
			for (Item it : newPart) {   // ��ǰ�հ��е�ÿһ����ĿA -> ����B��
				Symbol B = it.getDotSymbol(); 
				Terminal next = it.getNext();  // չ���� a
				if (B == null) {
					continue;
				}
				if (B instanceof Nonterminal) {   // B�Ƿ��ս���� ����A -> ����B��
					Nonterminal nextNonterminal = (Nonterminal)B;  
					List<Symbol> beta = new ArrayList<Symbol>();  // 
					for (int i = it.getDotPosition() + 1; i < it.getRightLength(); i++) {
						beta.add(it.getSymbolAt(i));
					}
					
					for (Integer num : leftMap.get(nextNonterminal)) { // ����B -> ��
						Production p = productionsList.get(num);
						List<Symbol> betaA = new ArrayList<Symbol>();
						betaA.addAll(beta);
						betaA.add(next);
						TerminalSetPair first = first(betaA);
						for (Terminal b : first.set) {   // ��First(��a)�е�ÿ������b
							add.add(new Item(p, 0, b));
						}
					}
				}
			}
			if (resSet.containsAll(add)) break; // ��û�и��µļ��룬���˳�
			resSet.addAll(add);   // ����ԭ����
			newPart = new HashSet<>(add);
		} while (!add.isEmpty()); // ��û�и��µļ��룬���˳�
		
		return resSet;
	}
	
	/**
	 * ��ĳ�����ŵ�first��
	 * @param s �ķ�����
	 * @return
	 */
	public TerminalSetPair first(Symbol s) {
		if (firstSetRecord.containsKey(s)) {
			return firstSetRecord.get(s);
		}
		Set<Terminal> resSet = new HashSet<Terminal>();
		boolean emptySymbol = false;
		if (s instanceof Terminal) {
			resSet.add((Terminal)s);
			return new TerminalSetPair(resSet, emptySymbol);
		}
		else {
			Nonterminal X = (Nonterminal)s;
			
			for (int num : leftMap.get(X)) {  // �������� X -> Y1 Y2 Y3...Yk�Ĳ���ʽ
				Production p = productionsList.get(num);
				boolean emptyLeft = true;  // �Ƿ��� X -> ��
				int i;
				for (i = 0; i < p.getRightLength(); i++) {
					emptyLeft = false;
					Symbol Yi = p.getRightAt(i);
//					System.out.println(Yi);
					if (X.equals(Yi)) break; // ��ݹ�������
					TerminalSetPair firstYi = first(Yi);
					resSet.addAll(firstYi.set);
					if (firstYi.emptySymbol == false) break;  // ���First(Yi)���ަ�, ��YiΪֹ
				}
				// �������ĳ��X -> ��, ����X -> Y1 Y2 Y3...Yk�� forall i, �š�First(Yi), �����к���
				if (emptyLeft || i == p.getRightLength()) emptySymbol = true;  
			}
		}
		TerminalSetPair resPair = new TerminalSetPair(resSet, emptySymbol);
		firstSetRecord.put(s, resPair);
		return resPair;
	}
	
	/**
	 * ����Ŵ���first��
	 * @param seq �ķ����Ŵ�
	 * @return
	 */
	private TerminalSetPair first(List<Symbol> seq) {
		// ��First(X1X2...Xn)
		Set<Terminal> resSet = new HashSet<>();
		boolean emptySymbol = false;
		int i;  
		for (i = 0; i < seq.size(); i++) {
			TerminalSetPair firstXi = first(seq.get(i));
			resSet.addAll(firstXi.set);
			if (firstXi.emptySymbol == false) break;
		}
		
		if (i == seq.size()) emptySymbol = true;
		return new TerminalSetPair(resSet, emptySymbol);
	}
	
	/**
	 * GOTO����
	 * @param I
	 * @param X
	 * @return
	 */
	public Set<Item> goTo(Set<Item> I, Symbol X) {
		Set<Item> res = new HashSet<Item>();
		for (Item it : I) {
			if (it.getDotSymbol() != null && it.getDotSymbol().equals(X)) {
				res.add(new Item(it.getProduction(), it.getDotPosition() + 1, it.getNext()));
			}
		}
		
		return closure(res);
	}

	
	/**
	 * ������LR������, ����goTo��Action��ȷ������
	 */
	public void constructLRTable() {
		// BFS, ����LR�Զ���
		Queue<Integer> queue = new LinkedBlockingQueue<>();
		Set<Integer> closed = new HashSet<>();  // BFS�����е�Closed��
		Set<Item> I0 = closure(new HashSet<>(Arrays.asList(
				new Item(productionsList.get(0), 0, new Terminal("$")))));
		itemsSetToNum.put(I0, 0);
		itemsSetList.add(I0);
		actionTable.add(new HashMap<>());
		goToTable.add(new HashMap<>());
		closed.add(0); // ��ǰ״̬����closed��
		queue.add(0);
		while (!queue.isEmpty()) {
			
			int statNum = queue.poll();
			
			Set<Item> pop = itemsSetList.get(statNum);
			for (Item it : pop) {
				if (it.getDotPosition() == it.getRightLength()) {  // ��Լ״̬
					if (!it.getLeft().equals(startSymbol)) {   // �󲿲����ķ���ʼ����
						actionTable.get(statNum).put(it.getNext(), "r" + productionToNum.get(it.getProduction())); // action[i, a] = rj
					}
					else {
						actionTable.get(statNum).put(it.getNext(), "acc");
					}
				}
				else if (it.getDotSymbol() instanceof Terminal) {  // ����״̬
					Terminal a = (Terminal)it.getDotSymbol();
					Set<Item> nextItemsSet = goTo(pop, a);  // �¸�״̬����Ŀ���հ�
					if (!itemsSetToNum.containsKey(nextItemsSet)) {
						itemsSetToNum.put(nextItemsSet, itemsSetList.size());
						itemsSetList.add(nextItemsSet);
						actionTable.add(new HashMap<>());
						goToTable.add(new HashMap<>());
					}
					int nextNum = itemsSetToNum.get(nextItemsSet);
					actionTable.get(statNum).put(a, "s" + nextNum);
					
					// ������״̬��û����������������������
					if (!closed.contains(nextNum)) {
						queue.add(nextNum);
						closed.add(nextNum);
					}
				}
				else { // ��Լ״̬
					Nonterminal B = (Nonterminal)it.getDotSymbol();
					Set<Item> nextItemsSet = goTo(pop, B);  // �¸�״̬����Ŀ���հ�
					if (!itemsSetToNum.containsKey(nextItemsSet)) {
						itemsSetToNum.put(nextItemsSet, itemsSetList.size());
						itemsSetList.add(nextItemsSet);
						actionTable.add(new HashMap<>());
						goToTable.add(new HashMap<>());
					}
					int nextNum = itemsSetToNum.get(nextItemsSet);
					goToTable.get(statNum).put(B, nextNum);
					
					// ������״̬��û����������������������
					if (!closed.contains(nextNum)) {
						queue.add(nextNum);
						closed.add(nextNum);
					}
				}
			}
			
			
		}
	}
	
	public String getAction(int stat, Terminal a) {
		return actionTable.get(stat).get(a);
	}
	
	public int getGoTo(int stat, Nonterminal A) {
		return goToTable.get(stat).get(A);
	}
	
	public String getErrorMsg(Nonterminal nt) {
		return errorMsg.get(nt);
	}
}
