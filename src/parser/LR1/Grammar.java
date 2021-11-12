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
 * 描述一个文法
 * @author xjy
 *
 */
public class Grammar {
	private Nonterminal startSymbol;
	private List<Production> productionsList = new ArrayList<Production>(); // 文法的所有产生式, 每个产生式都有独立的标号
	private Map<Production, Integer> productionToNum = new HashMap<Production, Integer>();
	private Map<Nonterminal, List<Integer>> leftMap = new HashMap<>();   // "产生式左部" -> "具有该左部的所有产生式"
//	private List<Item> itemsList;             // 由文法所有产生式生成的所有的项目
	
	public List<Set<Item>> itemsSetList = new ArrayList<>();     // 所有项目划分出的项目集列表（存储的是项目序号, 对应itemList中的序号），对应了LR自动机的状态
	private Map<Set<Item>, Integer> itemsSetToNum = new HashMap<>(); // 项目集映射到 其标号 
	public List<Map<Terminal, String>> actionTable = new ArrayList<>();
	public List<Map<Nonterminal, Integer>> goToTable = new ArrayList<>();	
	public Map<Nonterminal, String> errorMsg = new HashMap<Nonterminal, String>();   // 存储非终结符的错误提示信息
	
	public Map<Symbol, TerminalSetPair> firstSetRecord = new HashMap<>();  // 记录所有单个符号的first集， 减少重复计算
	
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
					if (word.equals("ε")) {
						break;
					}
					else if (Character.isUpperCase(word.charAt(0))) { // 大写开头，非终结符
						right.add(new Nonterminal(word));
					}
					else if(word.charAt(0) != '$'){  // 终结符
						right.add(new Terminal(word));
					}
					else {    // 错误信息
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
	 * 计算某个项目集的 项目集闭包
	 * @param itemsSet 待计算闭包的项目集
	 * @return 项目集闭包
	 */
	public Set<Item> closure(Set<Item> itemsSet) {
		Set<Item> resSet = new HashSet<>(itemsSet);
		Set<Item> add = new HashSet<>();
		Set<Item> newPart = new HashSet<>(itemsSet); 
		do {  
			add.clear();
			for (Item it : newPart) {   // 当前闭包中的每一个项目A -> α·Bβ
				Symbol B = it.getDotSymbol(); 
				Terminal next = it.getNext();  // 展望符 a
				if (B == null) {
					continue;
				}
				if (B instanceof Nonterminal) {   // B是非终结符， 满足A -> α·Bβ
					Nonterminal nextNonterminal = (Nonterminal)B;  
					List<Symbol> beta = new ArrayList<Symbol>();  // 
					for (int i = it.getDotPosition() + 1; i < it.getRightLength(); i++) {
						beta.add(it.getSymbolAt(i));
					}
					
					for (Integer num : leftMap.get(nextNonterminal)) { // 所有B -> γ
						Production p = productionsList.get(num);
						List<Symbol> betaA = new ArrayList<Symbol>();
						betaA.addAll(beta);
						betaA.add(next);
						TerminalSetPair first = first(betaA);
						for (Terminal b : first.set) {   // 对First(βa)中的每个符号b
							add.add(new Item(p, 0, b));
						}
					}
				}
			}
			if (resSet.containsAll(add)) break; // 当没有更新的加入，就退出
			resSet.addAll(add);   // 更新原集合
			newPart = new HashSet<>(add);
		} while (!add.isEmpty()); // 当没有更新的加入，就退出
		
		return resSet;
	}
	
	/**
	 * 求某个符号的first集
	 * @param s 文法符号
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
			
			for (int num : leftMap.get(X)) {  // 所有形如 X -> Y1 Y2 Y3...Yk的产生式
				Production p = productionsList.get(num);
				boolean emptyLeft = true;  // 是否有 X -> ε
				int i;
				for (i = 0; i < p.getRightLength(); i++) {
					emptyLeft = false;
					Symbol Yi = p.getRightAt(i);
//					System.out.println(Yi);
					if (X.equals(Yi)) break; // 左递归则跳过
					TerminalSetPair firstYi = first(Yi);
					resSet.addAll(firstYi.set);
					if (firstYi.emptySymbol == false) break;  // 如果First(Yi)中无ε, 则到Yi为止
				}
				// 如果存在某个X -> ε, 或者X -> Y1 Y2 Y3...Yk， forall i, ε∈First(Yi), 则结果中含ε
				if (emptyLeft || i == p.getRightLength()) emptySymbol = true;  
			}
		}
		TerminalSetPair resPair = new TerminalSetPair(resSet, emptySymbol);
		firstSetRecord.put(s, resPair);
		return resPair;
	}
	
	/**
	 * 求符号串的first集
	 * @param seq 文法符号串
	 * @return
	 */
	private TerminalSetPair first(List<Symbol> seq) {
		// 求First(X1X2...Xn)
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
	 * GOTO函数
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
	 * 构建出LR分析表, 即把goTo、Action表确定下来
	 */
	public void constructLRTable() {
		// BFS, 构建LR自动机
		Queue<Integer> queue = new LinkedBlockingQueue<>();
		Set<Integer> closed = new HashSet<>();  // BFS搜索中的Closed表
		Set<Item> I0 = closure(new HashSet<>(Arrays.asList(
				new Item(productionsList.get(0), 0, new Terminal("$")))));
		itemsSetToNum.put(I0, 0);
		itemsSetList.add(I0);
		actionTable.add(new HashMap<>());
		goToTable.add(new HashMap<>());
		closed.add(0); // 当前状态放入closed集
		queue.add(0);
		while (!queue.isEmpty()) {
			
			int statNum = queue.poll();
			
			Set<Item> pop = itemsSetList.get(statNum);
			for (Item it : pop) {
				if (it.getDotPosition() == it.getRightLength()) {  // 规约状态
					if (!it.getLeft().equals(startSymbol)) {   // 左部不是文法开始符号
						actionTable.get(statNum).put(it.getNext(), "r" + productionToNum.get(it.getProduction())); // action[i, a] = rj
					}
					else {
						actionTable.get(statNum).put(it.getNext(), "acc");
					}
				}
				else if (it.getDotSymbol() instanceof Terminal) {  // 移入状态
					Terminal a = (Terminal)it.getDotSymbol();
					Set<Item> nextItemsSet = goTo(pop, a);  // 下个状态的项目集闭包
					if (!itemsSetToNum.containsKey(nextItemsSet)) {
						itemsSetToNum.put(nextItemsSet, itemsSetList.size());
						itemsSetList.add(nextItemsSet);
						actionTable.add(new HashMap<>());
						goToTable.add(new HashMap<>());
					}
					int nextNum = itemsSetToNum.get(nextItemsSet);
					actionTable.get(statNum).put(a, "s" + nextNum);
					
					// 如果这个状态还没搜索到，则放入待搜索队列
					if (!closed.contains(nextNum)) {
						queue.add(nextNum);
						closed.add(nextNum);
					}
				}
				else { // 待约状态
					Nonterminal B = (Nonterminal)it.getDotSymbol();
					Set<Item> nextItemsSet = goTo(pop, B);  // 下个状态的项目集闭包
					if (!itemsSetToNum.containsKey(nextItemsSet)) {
						itemsSetToNum.put(nextItemsSet, itemsSetList.size());
						itemsSetList.add(nextItemsSet);
						actionTable.add(new HashMap<>());
						goToTable.add(new HashMap<>());
					}
					int nextNum = itemsSetToNum.get(nextItemsSet);
					goToTable.get(statNum).put(B, nextNum);
					
					// 如果这个状态还没搜索到，则放入待搜索队列
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
