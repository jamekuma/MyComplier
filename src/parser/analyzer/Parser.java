 package parser.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lexer.tokenTypes.Ch;
import lexer.tokenTypes.Dec;
import lexer.tokenTypes.Hex;
import lexer.tokenTypes.Oct;
import lexer.tokenTypes.Real;
import lexer.tokenTypes.Str;
import lexer.tokenTypes.Token;
import lexer.tokenTypes.Word;
import parser.LR1.Grammar;
import parser.LR1.Nonterminal;
import parser.LR1.Production;
import parser.LR1.Symbol;
import parser.LR1.Terminal;
import semantic.Instruction;
import semantic.SymbolTable;
import semantic.SymbolTableEntry;

public class Parser {
	private Grammar grammar;
	private Stack<Integer> statStack = new Stack<>();
	private Stack<TreeNode> symStack = new Stack<>();
	private TreeNode root;
	private String errorMsg = "";
	private static final String retract = "  ";   // һ�������ַ���������������� 
	
	/**********����������ӵ�����***********/
	private SymbolTable table = SymbolTable.makeTable("main", null);   // ��ǰ�ķ��ű� 
	private Stack<SymbolTable> tableStack = new Stack<>();  // ���ű�ջ
	private Stack<Integer> offsetStack = new Stack<>(); // ƫ�� ջ����¼���ű�ջ�ж�Ӧ�ķ��ű�ĵ�ǰƫ��
	private Stack<List<Instruction>> instrSeqStack = new Stack<>();   // ָ������ջ
	private Map<String, List<Instruction>> instrSeqTable = new HashMap<>();  // ������->ָ������
	
 	private List<Instruction> instrSeq = new ArrayList<Instruction>();   // ��ǰ��ָ������
	private int offset = 0;      // ��ǰ�� ���ű����ƫ��
	
	
	// ������ʱ��ȫ�ֱ���
	private String t;
	private int w;
	
	public Parser(Grammar grammar) {
		this.grammar = grammar;
	}
	
	
	/**
	 * �﷨����
	 * @param tokenList ��һ���ʷ��������ص�token����
	 */
	public void analyze(List<Token> tokenList) {
		statStack.clear();
		symStack.clear();
		statStack.add(0);
		instrSeqTable.put("main", instrSeq);
		symStack.add(new TreeNode(new Terminal("$"), -1));
//		tableStack.push(table);
		int next = 0;
		while (next <= tokenList.size()) {
			Terminal t;
			int nextLine;
			Token nextToken;
			if (next < tokenList.size()) {
				nextToken = tokenList.get(next);
				nextLine = nextToken.getLineNum();
				t = Terminal.tokenToTerminal(nextToken);
				t.setToken(nextToken);
			}
			else {   // ĩβ, $��
				nextToken = null;
				nextLine = tokenList.get(tokenList.size() - 1).getLineNum();
				t = new Terminal("$");
			}
			
			String action = grammar.getAction(statStack.peek(), t);
			
			if (action != null) {  // action���б���
				if (action.charAt(0) == 's') {  // ����
					int nextStat = Integer.parseInt(action.substring(1));
					statStack.add(nextStat);
					symStack.add(new TreeNode(t, nextLine, nextToken));
					if (next < tokenList.size()) next++;
				}
				else if (action.charAt(0) == 'r') {  // ��Լ
					int produtionNum = Integer.parseInt(action.substring(1));
					Production p = grammar.getProduction(produtionNum);   // Ҫ����p���й�Լ
					
					/********************ִ�����嶯��*****************************/
					Nonterminal pLeft = executeSemantic(p);    // �������
					// Nonterminal pLeft = p.getLeft();
					
					/********************���﷨������*************************/
					List<TreeNode> tempPop = new ArrayList<TreeNode>();  // �ݴ����ջ�����ķ���(����)
					for (int i = 0; i < p.getRightLength(); i++) {
						tempPop.add(symStack.pop());
						statStack.pop();   // ����ջͬʱ����
					}
					
					// ��Լ�Ĳ���ʽ�������Ϊ���ڵ�, ��Լ���Ҳ���һ�����ŵ�������Ϊ�½ڵ������
					TreeNode newNode;
					if (!tempPop.isEmpty())
						newNode = new TreeNode(pLeft, tempPop.get(tempPop.size() - 1).getLineNum());
					else {
						newNode = new TreeNode(pLeft, nextLine);
					}
					// ��Լ�Ĳ���ʽ�������Ϊ���ڵ㣬�Ҳ���Ϊ���ӽڵ�, ���Ϲ����﷨������
					for (int i = tempPop.size() - 1; i >= 0; i--) {
						newNode.addChild(tempPop.get(i));
					}
					root = newNode;
					symStack.add(newNode);
					statStack.add(grammar.getGoTo(statStack.peek(), (Nonterminal)newNode.getSymbol()));  // GOTO
					
					
					
				}
				else if (action.equals("acc")) {
					 root = symStack.pop();
					 break;
				}
				else {
					assert false;
				}
			}
			else { // action���ޱ���, ����ָ�, �ֻ�ģʽ
				
				
				
				int afterStat = -1;
				TreeNode afterSym = null;
				// ��ʱ��¼�����ķ���, Ҫ��ΪA���ӽڵ�
				
				List<TreeNode> tempList = new ArrayList<TreeNode>();
				// ��ջ�����£��ҵ���һ��״̬si�����ж�Ӧĳ�����ս��A��GOTOĿ��
				//stat: s0 s1 s2 s3...si si+1...sm
				// sym: $  X1 X2 X3...Xi Xi+1...Xm
				// (�������״̬si�ĺ�һ��״̬si+1������goto(si, Xi+1))(�����֤�����Ѿ��ɹ���Լ��)
				while (grammar.goToTable.get(statStack.peek()).isEmpty()
						|| (afterSym != null && afterStat != -1
								&& afterSym.getSymbol() instanceof Nonterminal 
								&& afterStat == grammar.getGoTo(statStack.peek(), (Nonterminal)afterSym.getSymbol()))) {  
					nextLine = symStack.peek().getLineNum();
					afterStat = statStack.pop();
					afterSym = symStack.pop();
					tempList.add(afterSym);
				}
				Nonterminal A = null;
				for (Map.Entry<Nonterminal, Integer> entry : grammar.goToTable.get(statStack.peek()).entrySet()) {
					A = entry.getKey();
					break;
				}
//				System.out.println("A = " + A);
				int sNext = grammar.getGoTo(statStack.peek(), A);
				errorMsg +=  "Error at Line[" + nextLine + "]: " + grammar.getErrorMsg(A) + "����\n";
				TreeNode ANode = new TreeNode(A, nextLine);
				for (int i = tempList.size() - 1; i >= 0; i--) {
					ANode.addChild(tempList.get(i));
				}
				root = ANode;
				symStack.add(ANode);
				statStack.add(sNext);
				
				while (grammar.getAction(sNext, t) == null) {  // �����������, ֱ�����ֺϷ�����A��ķ���
					if (next <= tokenList.size()) next++;
					else break;
					if (next < tokenList.size()) {
						nextToken = tokenList.get(next);
						nextLine = nextToken.getLineNum();
						t = Terminal.tokenToTerminal(nextToken);
					}
					else {   // ĩβ, $��
						nextToken = null;
						nextLine = tokenList.get(tokenList.size() - 1).getLineNum();
						t = new Terminal("$");
					}
				}
				
			}
			
		}
	}
	
	/**
	 * ִ�е�ǰ��Լ�Ĳ���ʽ�����嶯��
	 * @param p ��ǰ��Լ�Ĳ���ʽ
	 * @return ����ʽ�󲿵ķ��ս��(��ͬ������)
	 */
	public Nonterminal executeSemantic(Production p) {
		
		Nonterminal res = new Nonterminal(p.getLeft().getText());
		/*
		 * ���µ�ÿ��if-else��Ӧһ������ʽĩβ�����嶯��
		 */
		if (p.getLeft().getText().equals("P")) {
			if (p.getRightLength() == 4) {
				// P ->  PStart S PM P `{backpatch(S.nextList, PM.instr)}`
				Nonterminal S = (Nonterminal)symStack.get(symStack.size() - 3).getSymbol();
				Nonterminal PM = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				backpatch((List<Integer>)S.getAttribute("nextList"), (Integer)PM.getAttribute("instr"));
			}
		}
		else if (p.getLeft().getText().equals("PM")) {
			// PM -> �� `{PM.instr = nextInstr}`
			res.setAttribute("instr", instrSeq.size());
		}
		else if (p.getLeft().getText().equals("D")) {
			if (p.getRightLength() == 10) {
				// D -> proc X id DM ( M ) { P } `
				// {{table.setWidth(offset);
				//tableStack.top().enterProc(id.lexeme, table);
				// table = tableStack.pop(); offset = offsetStack.pop();}}` 
				Terminal id = (Terminal)symStack.get(symStack.size() - 8).getSymbol();
				table.setWidth(offset);
				if (table.lookUp(((Word)id.getToken()).getValue()) != null) {
					int line = symStack.get(symStack.size() - 8).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id.getToken()).getValue() + "�ظ�����\n";
				}
				else
					tableStack.peek().enterProc(((Word)id.getToken()).getValue(), table);
				table = tableStack.pop();
				offset = offsetStack.pop();
				instrSeq = instrSeqStack.pop();
			}
			
			else if (p.getRightLength() == 6) {
				// D -> record id DC { P }  
				// {{table.setWidth(offset);tableStack.top().enterRecord(id.lexeme, table);
				// table = tableStack.pop(); offset = offsetStack.pop();}}` 
				Terminal id = (Terminal)symStack.get(symStack.size() - 5).getSymbol();
				
				table.setWidth(offset);
				
				if (table.lookUp(((Word)id.getToken()).getValue()) != null) {
					int line = symStack.get(symStack.size() - 5).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "��¼" + ((Word)id.getToken()).getValue() + "�ظ�����\n";
				}
				else
					tableStack.peek().enterRecord(((Word)id.getToken()).getValue(), table);
				table = tableStack.pop();
				offset = offsetStack.pop();
			}
			
			else {
				// D -> T id A ; 
				// `{{table.enter(id.lexeme, T.type, offset);offset = offset + T.width;}}`
				Terminal id = (Terminal)symStack.get(symStack.size() - 3).getSymbol();   // �ӷ���ջ���ó�id
				Nonterminal T = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				if (table.lookUp(((Word)id.getToken()).getValue()) != null) {
					int line = symStack.get(symStack.size() - 3).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id.getToken()).getValue() + "�ظ�����\n";
				}
				else {
					table.enter(((Word)id.getToken()).getValue(), (String)T.getAttribute("type"), offset);
					offset += (Integer)T.getAttribute("width");
				}
			}
		}else if (p.getLeft().getText().equals("DM")) {
			// DM -> �� 
			// {{ tableStack.push(table);instrSeqStack.push(instrSeq); table = mkTable(table);
			// offsetStack.push(offset); offset = 0;}}`
			Terminal id = (Terminal)symStack.get(symStack.size() - 1).getSymbol();
			tableStack.push(table);
			instrSeqStack.push(instrSeq);
			table = SymbolTable.makeTable(((Word)id.getToken()).getValue(), table);
			offsetStack.push(offset);
			offset = 0;
			instrSeq = new ArrayList<>();
			instrSeqTable.put(((Word)id.getToken()).getValue(), instrSeq);
		}
		else if (p.getLeft().getText().equals("DC")) {
			// DC -> �� `{{ tableStack.push(table); table = mkTable(table);offsetStack.push(offset); offset = 0;}}`
			Terminal id = (Terminal)symStack.get(symStack.size() - 1).getSymbol();
			tableStack.push(table);
			table = SymbolTable.makeTable(((Word)id.getToken()).getValue(), table);
			offsetStack.push(offset);
			offset = 0;
		}
		else if (p.getLeft().getText().equals("M")) {
			if (p.getRightLength() == 4) {
				// M -> M , X id 
				// `{{table.enter(id.lexeme, X.type, offset); offset = offset + X.width; M.size = M1.size + 1;}}`
				Terminal id = (Terminal)symStack.get(symStack.size() - 1).getSymbol();   
				Nonterminal X = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Nonterminal M1 = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				if (table.lookUp(((Word)id.getToken()).getValue()) != null) {
					int line = symStack.get(symStack.size() - 1).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id.getToken()).getValue() + "�ظ�����\n";
				}
				else {
					table.enter(((Word)id.getToken()).getValue(), (String)X.getAttribute("type"), offset);
					offset += (Integer) X.getAttribute("width");
				}
				res.setAttribute("size", (Integer)M1.getAttribute("size") + 1);
			}
			else {
				// M -> X id `{{table.enter(id.lexeme, X.type, offset); offset = offset + X.width; M.size = 1;}}`
				Terminal id = (Terminal)symStack.get(symStack.size() - 1).getSymbol();  
				Nonterminal X = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				if (table.lookUp(((Word)id.getToken()).getValue()) != null) {
					int line = symStack.get(symStack.size() - 1).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id.getToken()).getValue() + "�ظ�����\n";
				}
				else {
					table.enter(((Word)id.getToken()).getValue(), (String)X.getAttribute("type"), offset);
					offset += (Integer) X.getAttribute("width");
				}
				res.setAttribute("size", 1);
			}
		}
		else if (p.getLeft().getText().equals("T")) {
			// T -> X  XM  C `{{T.type = C.type; T.width = C.width;}}`
			Nonterminal C = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
			res.setAttribute("type", C.getAttribute("type"));
			res.setAttribute("width", C.getAttribute("width"));
		}
		else if (p.getLeft().getText().equals("XM")) {
			// XM ->  �� `{{t = X.type; w = X.width;}}
			Nonterminal X = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
			t = (String)X.getAttribute("type");
			w = (Integer)X.getAttribute("width");
		}
		else if (p.getLeft().getText().equals("X")) { 
			// X -> basic `{{X.type = basic.value; X.width = sizeof(basic.value);}}`
			Terminal basic = (Terminal)symStack.get(symStack.size() - 1).getSymbol();
			res.setAttribute("type",  ((Word)basic.getToken()).getValue());
			res.setAttribute("width", ((Word)basic.getToken()).size());
		}
		else if (p.getLeft().getText().equals("C")) { 
			if (p.getRightLength() == 4) {
				// C -> [ dec ] C 
				// `{{C.type = '[' + dec.value + ']' + C1.type; C.width = dec.value * C1.width;}}` 
				Nonterminal C1 = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				Terminal dec = (Terminal)symStack.get(symStack.size() - 3).getSymbol();
				res.setAttribute("type", "[" + ((Dec)dec.getToken()).getValue() + "]" + (String)C1.getAttribute("type"));
				res.setAttribute("width", ((Dec)dec.getToken()).getValue() * (Integer)C1.getAttribute("width"));
			}
			else {
				// C -> �� `{{C.type = t; C.width = w;}}`
				res.setAttribute("type", t);
				res.setAttribute("width", w);
			}
		}
		else if (p.getLeft().getText().equals("S")) { 
			if (p.getRightLength() == 4 && p.getRightAt(0).getText().equals("id")) {
				// S -> id = E ; 
				// `{{S.nextList = null; pp = table.lookUp(id.lexeme); if pp == null then error else gen(pp, '=', E.addr);}}`
				Terminal id = (Terminal)symStack.get(symStack.size() - 4).getSymbol();
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				res.setAttribute("nextList", new ArrayList<>());
				SymbolTableEntry pp = table.lookUp(((Word)id.getToken()).getValue());
				if (pp == null) {
					// error�� δ����
					int line = symStack.get(symStack.size() - 4).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id.getToken()).getValue() + "δ����\n";
				}
				else {
					instrSeq.add(new Instruction("=", (String)E.getAttribute("addr"), "", ((Word)id.getToken()).getValue()));
				}
			}
			else if (p.getRightLength() == 10) {
				// S -> if ( B ) BM S N else BM S 
				//`{{backpatch(B.trueList, BM1.instr); backpatch(B.falseList, BM2.instr); temp = merge(S1.nextList, N.nextList); S.nextList = merge(temp, S2.nextList); }}` 
				Nonterminal B = (Nonterminal)symStack.get(symStack.size() - 8).getSymbol();
				Nonterminal BM1 = (Nonterminal)symStack.get(symStack.size() - 6).getSymbol();
				Nonterminal S1 = (Nonterminal)symStack.get(symStack.size() - 5).getSymbol();
				Nonterminal N = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				Nonterminal BM2 = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Nonterminal S2 = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				backpatch((List<Integer>)B.getAttribute("trueList"), (Integer)BM1.getAttribute("instr"));
				backpatch((List<Integer>)B.getAttribute("falseList"), (Integer)BM2.getAttribute("instr"));
				res.setAttribute("nextList", merge(merge((List<Integer>)S1.getAttribute("nextList"), (List<Integer>)N.getAttribute("nextList")), 
						(List<Integer>)S2.getAttribute("nextList")));
			}
			else if (p.getRightLength() == 7) {
				// S -> while BM ( B ) BM S 
				// `{{backpatch(S1.nextList, BM1.instr); backpatch(B.trueList, BM2.instr); S.nextList = B.falseList; gen('goto', BM1.instr); }}` 
				Nonterminal B = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				Nonterminal BM1 = (Nonterminal)symStack.get(symStack.size() - 6).getSymbol();
				Nonterminal S1 = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				Nonterminal BM2 = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				backpatch((List<Integer>)S1.getAttribute("nextList"), (Integer)BM1.getAttribute("instr"));
				backpatch((List<Integer>)B.getAttribute("trueList"), (Integer)BM2.getAttribute("instr"));
				res.setAttribute("nextList", B.getAttribute("falseList"));
				instrSeq.add(new Instruction("goto", "", "", BM1.getAttribute("instr") + ""));
			}
			else if (p.getRightLength() == 8 && p.getRightAt(0).getText().equals("id")) {
				// S -> id = call id ( Elist ) ;
				// `S.nextList = null; gen('call', id2.lexeme, Elist.size, id1.lexeme)`
				Terminal id1 = (Terminal)symStack.get(symStack.size() - 8).getSymbol();
				Terminal id2 = (Terminal)symStack.get(symStack.size() - 5).getSymbol();
				Nonterminal Elist = (Nonterminal)symStack.get(symStack.size() - 3).getSymbol();
				res.setAttribute("nextList", new ArrayList<>());
				SymbolTableEntry pp = table.lookUp(((Word)id2.getToken()).getValue());
				if (pp == null) {
					// error�� δ����
					int line = symStack.get(symStack.size() - 8).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id2.getToken()).getValue() + "δ����\n";
				}
				else if (!pp.getType().equals("PROC")){
					int line = symStack.get(symStack.size() - 8).getLineNum();
					errorMsg += "Error at Line " + line + ": " + ((Word)id2.getToken()).getValue() + "���ǹ�����, ���ɵ��ã�"
							+ "\n";
				}
				else {	
					instrSeq.add(new Instruction("call", ((Word)id2.getToken()).getValue()
						, Elist.getAttribute("size") + "", ((Word)id1.getToken()).getValue()));
				}
			}
			else if (p.getRightLength() == 3) {
				// S -> return E ; 
				// `S.nextList = null  gen('return', 'E.addr')`
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				res.setAttribute("nextList", new ArrayList<>());
				instrSeq.add(new Instruction("return", "", "", (String)E.getAttribute("addr")));
			}
			else if (p.getRightLength() == 6 && p.getRightAt(0).getText().equals("if")) {
				// S -> if ( B ) BM S 
				// `{{backpatch(B.trueList, BM.instr); S.nextList = merge(B.falseList, S1.nextList); }}` 
				Nonterminal B = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				Nonterminal BM = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Nonterminal S1 = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				backpatch((List<Integer>)B.getAttribute("trueList"), (Integer)BM.getAttribute("instr"));
//				System.err.println((List<Integer>)B.getAttribute("falseList"));
//				System.err.println((List<Integer>)S1.getAttribute("nextList"));
				res.setAttribute("nextList", merge((List<Integer>)B.getAttribute("falseList"), (List<Integer>)S1.getAttribute("nextList")));
			}
			else if (p.getRightLength() == 4 && p.getRightAt(0).getText().equals("L")){
				// S -> L = E ; `{{S.nextList = null;gen(L.array, L.addr, '=', E.addr)}}`
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Nonterminal L = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				res.setAttribute("nextList", new ArrayList<>());
				instrSeq.add(new Instruction("[]=", (String)E.getAttribute("addr"), (String)L.getAttribute("array"), (String)L.getAttribute("addr")));
			}
		}
		else if (p.getLeft().getText().equals("N")) { 
			// N -> �� 
			// `{{N.nextList = makeList(nextInstr); gen('goto'); }}`
			res.setAttribute("nextList", makeList(instrSeq.size()));
			instrSeq.add(new Instruction("goto", "", "", ""));  // ������
		}
		else if (p.getLeft().getText().equals("L")) { 
			if (p.getRightAt(0).getText().equals("L")) {
				// L -> L [ E ] 
				// `{{L.array = L1.array; L.type = L1.type.elem; L.width = L.type.width; tt = new Temp();
				// L.addr = new Temp(); gen(tt, '=', E.addr, '*', L.width); gen(L.addr, '=', L1.addr, '+', tt); }}`
				Nonterminal L1 = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				res.setAttribute("array", L1.getAttribute("array"));
				res.setAttribute("type", elementOfType((String)L1.getAttribute("type")));
				res.setAttribute("width", widthOfType((String)res.getAttribute("type")));
				Temp tt = new Temp();
				res.setAttribute("addr", new Temp().toString());
				instrSeq.add(new Instruction("*", (String)E.getAttribute("addr"), ((Integer)res.getAttribute("width")).toString(), tt.toString()));
				instrSeq.add(new Instruction("+", (String)L1.getAttribute("addr"), tt.toString(), (String)res.getAttribute("addr")));
			}
			else if (p.getRightAt(0).getText().equals("id")) {
				// L -> id [ E ] 
				//`{{p = lookUp(id.lexeme); if p == null then error else L.array = p; L.type = id.type.elem; L.addr = new Temp(); 
				// gen(L.addr, '=', E.addr, '*', L.type.width)}}`
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Terminal id = (Terminal)symStack.get(symStack.size() - 4).getSymbol();
				SymbolTableEntry pp = table.lookUp(((Word)id.getToken()).getValue());
				if (pp == null) {
					// error δ����
					int line = symStack.get(symStack.size() - 4).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id.getToken()).getValue() + "δ����\n";
				}
				else if (pp.getType().charAt(0) != '[') {
					int line = symStack.get(symStack.size() - 4).getLineNum();
					errorMsg += "Error at Line " + line + ": " +  ((Word)id.getToken()).getValue() + "��������!����ʹ��������ʲ�����!\n";
				}
				else {
					res.setAttribute("array", pp.getName());
					res.setAttribute("type", elementOfType(pp.getType()));
					res.setAttribute("addr", new Temp().toString());
					instrSeq.add(new Instruction("*", (String)E.getAttribute("addr"), widthOfType((String)res.getAttribute("type")) + "", (String)res.getAttribute("addr")));
				}
			}
		}
		else if (p.getLeft().getText().equals("E")) { 
			if (p.getRightLength() == 3) {
				// E -> E + G 
				// `{{E.addr = newTemp(); gen(E.addr, '=', E1.addr, '+', G.addr);}}`
				Nonterminal E1 = (Nonterminal)symStack.get(symStack.size() - 3).getSymbol();
				Nonterminal G = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("addr", new Temp().toString());
				instrSeq.add(new Instruction("+", (String)E1.getAttribute("addr"), (String)G.getAttribute("addr"), (String)res.getAttribute("addr")));
				SymbolTableEntry E1_entry = table.lookUp((String)E1.getAttribute("addr"));
				SymbolTableEntry G_entry = table.lookUp((String)G.getAttribute("addr"));
				
				if (E1_entry != null && G_entry != null) {
					if (!E1_entry.getType().equals(G_entry.getType())) {
						int line = symStack.get(symStack.size() - 3).getLineNum();
						errorMsg += "Error at Line " + line + ": " + "����" + (String)E1.getAttribute("addr") + ", "
								+ (String)G.getAttribute("addr") + "���Ͳ�ƥ��\n";
					}
				}
			}
			else {
				// E -> G `{{E.addr = G.addr;}}`
				Nonterminal G = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("addr", G.getAttribute("addr"));
			}
		}
		else if (p.getLeft().getText().equals("G")) { 
			if (p.getRightLength() == 3) {
				// G -> G * F 
				// `{{G.addr = newTemp(); gen(G.addr, '=', G1.addr, '*', F.addr);}}`
				Nonterminal G1 = (Nonterminal)symStack.get(symStack.size() - 3).getSymbol();
				Nonterminal F = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("addr", new Temp().toString());
				instrSeq.add(new Instruction("*", (String)G1.getAttribute("addr"), (String)F.getAttribute("addr"), (String)res.getAttribute("addr")));
				SymbolTableEntry G1_entry = table.lookUp((String)G1.getAttribute("addr"));
				SymbolTableEntry F_entry = table.lookUp((String)F.getAttribute("addr"));
				
				if (G1_entry != null && F_entry != null) {
					if (!G1_entry.getType().equals(F_entry.getType())) {
						int line = symStack.get(symStack.size() - 3).getLineNum();
						errorMsg += "Error at Line " + line + ": " + "����" + (String)G1.getAttribute("addr") + ", "
								+ (String)F.getAttribute("addr") + "���Ͳ�ƥ��\n";
					}
				}
			}
			else {
				// G ->  F `{{G.addr = F.addr;}}`
				Nonterminal F = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("addr", F.getAttribute("addr"));
			}
		}
		else if (p.getLeft().getText().equals("F")) { 
			if (p.getRightLength() == 3) {
				// F -> ( E ) `{{F.addr = E.addr;}}`
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				res.setAttribute("addr", E.getAttribute("addr"));
			}
			else if (p.getRightLength() == 1 && p.getRightAt(0).getText().equals("id")) {
				// F ->  id `{{F.addr = lookup(id.lexeme); if F.addr == null then error;}}`
				Terminal id = (Terminal)symStack.get(symStack.size() - 1).getSymbol();
				SymbolTableEntry entry = table.lookUp(((Word)id.getToken()).getValue());
				if (entry == null) {
					// error δ����
					int line = symStack.get(symStack.size() - 1).getLineNum();
					errorMsg += "Error at Line " + line + ": " + "����" + ((Word)id.getToken()).getValue() + "δ����\n";
				}
				else {
					res.setAttribute("addr", entry.getName());
				}
			}
			else if (p.getRightLength() == 1 && p.getRightAt(0).getText().equals("L")) {
				// F ->  L `{{F.addr = new Temp();gen(F.addr '=', L.array , L.addr)}}`
				Nonterminal L = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("addr", new Temp().toString());
				instrSeq.add(new Instruction("=[]", (String)L.getAttribute("array"), (String)L.getAttribute("addr"), (String)res.getAttribute("addr")));
			}
			else {
				/*
				 * F ->  real `{{F.addr = real.value;}}`
				 * F -> dec   `{{F.addr = dec.value;}}`
				 * F -> hex     `{{F.addr = hex.value;}}`
				 * F -> oct      `{{F.addr = oct.value;}}`
				 * F -> char     `{{F.addr = char.value;}}`
				 * F -> string    `{{F.addr = string.value;}}`
				 * */
				Terminal right = (Terminal)symStack.get(symStack.size() - 1).getSymbol();
				if (right.getToken() instanceof Ch) {
					res.setAttribute("addr", ((Ch)right.getToken()).getValue() + "");
				}
				else if (right.getToken() instanceof Dec) {
					res.setAttribute("addr", ((Dec)right.getToken()).getValue() + "");
				}
				else if (right.getToken() instanceof Real) {
					res.setAttribute("addr", ((Real)right.getToken()).getValue() + "");
				}
				else if (right.getToken() instanceof Hex) {
					res.setAttribute("addr", ((Hex)right.getToken()).getValue() + "");
				}
				else if (right.getToken() instanceof Oct) {
					res.setAttribute("addr", ((Oct)right.getToken()).getValue() + "");
				}
				else if (right.getToken() instanceof Str) {
					res.setAttribute("addr", ((Str)right.getToken()).getValue() + "");
				}
				else assert false;
			}
		}
		else if (p.getLeft().getText().equals("B")) { 
			if (p.getRightLength() == 4) {
				// B -> B || BM H `
				// {{backpatch(B1.falseList, BM.instr); B.trueList = merge(B1.trueList, H.trueList); 
				// B.falstList = H.falseList;}}`
				Nonterminal H = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				Nonterminal BM = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Nonterminal B1 = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				backpatch((List<Integer>)B1.getAttribute("trueList"), (Integer)BM.getAttribute("instr"));
				res.setAttribute("trueList", merge((List<Integer>)B1.getAttribute("trueList"), (List<Integer>)H.getAttribute("trueList")));
				res.setAttribute("falseList", H.getAttribute("falseList"));
				
			}
			else {
				// B -> H `{{B.trueList = H.trueList; B.falseList = H.falseList;}}`
				Nonterminal H = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("trueList", H.getAttribute("trueList"));
				res.setAttribute("falseList", H.getAttribute("falseList"));
			}
		}
		else if (p.getLeft().getText().equals("H")) { 
			if (p.getRightLength() == 4) {
				// H -> H && BM I 
				// `{{backpatch(H1.trueList, BM.instr); H.trueList = I.trueList; 
				// H.falseList = merge(H1.falseList, I.falseList);}}`
				Nonterminal I = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				Nonterminal BM = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Nonterminal H1 = (Nonterminal)symStack.get(symStack.size() - 4).getSymbol();
				backpatch((List<Integer>)H1.getAttribute("trueList"), (Integer)BM.getAttribute("instr"));
				res.setAttribute("trueList", I.getAttribute("trueList"));
				res.setAttribute("falseList", merge((List<Integer>)H1.getAttribute("falseList"), (List<Integer>)I.getAttribute("falseList")));
			}
			else {
				// H ->  I `{{H.trueList = I.trueList; H.falseList = I.falseList;}}`
				Nonterminal I = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("trueList", I.getAttribute("trueList"));
				res.setAttribute("falseList", I.getAttribute("falseList"));
			}
			
		}
		else if (p.getLeft().getText().equals("I")) { 
			if (p.getRightLength() == 2) {
				// I -> ! I `{{I.trueList = I1.falseList; I.falseList = I1.falseList;}}`
				Nonterminal I1 = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("trueList", I1.getAttribute("falseList"));
				res.setAttribute("falseList", I1.getAttribute("trueList"));
			}
			else if (p.getRightLength() == 3 && p.getRightAt(0).getText().equals("(")) {
				// I -> ( B ) `{{I.trueList = B.trueList; I.falseList = B.falseList;}}`
				Nonterminal B = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				res.setAttribute("trueList", B.getAttribute("trueList"));
				res.setAttribute("falseList", B.getAttribute("falseList"));
			}
			else if (p.getRightLength() == 3 && p.getRightAt(0).getText().equals("E")) {
				// I -> E Relop E \
				// `{{I.trueList = makeList(nextInstr); I.falseList = makeList(nextInstr + 1);
				// gen('if', E1.addr, Relop.op, E2.addr, 'goto'); gen('goto');}}` 
				Nonterminal E1 = (Nonterminal)symStack.get(symStack.size() - 3).getSymbol();
				Nonterminal Relop = (Nonterminal)symStack.get(symStack.size() - 2).getSymbol();
				Nonterminal E2 = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("trueList", makeList(instrSeq.size()));
				res.setAttribute("falseList", makeList(instrSeq.size() + 1));
				instrSeq.add(new Instruction((String)Relop.getAttribute("op"), (String)E1.getAttribute("addr")
						, (String)E2.getAttribute("addr"), ""));
				instrSeq.add(new Instruction("goto", "", "", ""));
			}
			else if (p.getRightAt(0).getText().equals("true")) {
				// I -> true `{{I.trueList = makeList(nextInstr); gen('goto');}}`
				res.setAttribute("trueList", makeList(instrSeq.size()));
				instrSeq.add(new Instruction("goto", "", "", ""));
			}
			else {
				// I -> false `{{I.falseList = makeList(nextInstr); gen('goto');}}`
				res.setAttribute("falseList", makeList(instrSeq.size()));
				instrSeq.add(new Instruction("goto", "", "", ""));
			}
		}
		else if (p.getLeft().getText().equals("BM")) { 
			// BM -> �� `{{BM.instr = nextInstr}}`
			res.setAttribute("instr", instrSeq.size());
		}
		else if (p.getLeft().getText().equals("Relop")) { 
			/*
			 * Relop -> < `{{Relop.op = op}}`
			 * Relop -> <= `{{Relop.op = op}}`
			 * Relop ->  > `{{Relop.op = op}}`
			 * Relop ->  >= `{{Relop.op = op}}`
			 * Relop -> == `{{Relop.op = op}}`
			 * Relop ->  != `{{Relop.op = op}}`
			 */
			res.setAttribute("op", p.getRightAt(0).getText());
		}
		else if (p.getLeft().getText().equals("Elist")) {
			if (p.getRightLength() == 3) {
				// Elist -> Elist , E `{{Elist.size = Elist1.size + 1;}}`
				Nonterminal Elist1 = (Nonterminal)symStack.get(symStack.size() - 3).getSymbol();
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("size", (Integer)Elist1.getAttribute("size") + 1);
				instrSeq.add(new Instruction("param", "", "", (String)E.getAttribute("addr")));
			}
			else {
				// Elist ->  E `{{Elist.size = 1;}}`
				Nonterminal E = (Nonterminal)symStack.get(symStack.size() - 1).getSymbol();
				res.setAttribute("size", 1);
				instrSeq.add(new Instruction("param", "", "", (String)E.getAttribute("addr")));
			}
		}
		return res;
	}
	
	/**
	 * ����ĳ�����͵��ֽڿ��
	 * @param arrayType
	 * @return
	 */
	public int widthOfType(String arrayType) {
		if (arrayType.equals("int")) {
			return 4;
		}
		else if (arrayType.equals("double")) {
			return 8;
		}
		else if (arrayType.equals("float")) {
			return 4;
		}
		else if (arrayType.equals("bool")) {
			return 1;
		}
		else if (arrayType.equals("char")) {
			return 1;
		}
		else {
			int width = 1;
			Pattern p1 = Pattern.compile("[a-z]+");
			Matcher m1 = p1.matcher(arrayType);
			if (m1.find()) {
//				System.out.println(m1.group());
				width *= widthOfType(m1.group());
			}
			else assert false;
			
			Pattern p2 = Pattern.compile("\\d+");
			Matcher m2 = p2.matcher(arrayType);
			while (m2.find()) {
				width *= Integer.parseInt(m2.group());
			}
			return width;
		}
	}
	
	/**
	 * ��ȡ�����Ԫ������
	 * @param arrayType
	 * @return
	 */
	public String elementOfType(String arrayType) {
		StringBuilder res = new StringBuilder();
		Pattern p = Pattern.compile("\\[\\d+\\]");
		Matcher m = p.matcher(arrayType);
		if (m.find()) {
//			res.append(arrayType.substring(0, m.start()));
			res.append(arrayType.substring(m.end()));
		}
		else assert false;
		
		return res.toString();
	}
	
	/**
	 * ����ֻ����instrIndex���б� 
	 * @param instrIndex ��תָ��ı��
	 * @return �´����б������
	 */
	public List<Integer> makeList(int instrIndex) {
		List<Integer> res = new ArrayList<Integer>();
		res.add(instrIndex);
		return res;
	}
	
	/**
	 * ��p1��p2���б�ϲ�
	 * @param p1
	 * @param p2
	 * @return �ϲ�����б������
	 */
	public List<Integer> merge(List<Integer> p1, List<Integer> p2) {
		p1.addAll(p2);
		return p1;
	}
	
	/**
	 * �����dest��ΪĿ����,���뵽p��ָ�б�ĸ���ָ����
	 * @param p
	 * @param dest
	 */
	public void backpatch(List<Integer> p, int dest) {
		for (Integer i : p) {
			instrSeq.get(i).setDest(dest + "");
		}
	}
	
	/**
	 * ��÷��ű�
	 * @return
	 */
	public SymbolTable getSymbolTable() {
		return table;
	}
	
	/**
	 * ������й��̵�ָ������
	 * @return
	 */
	public Map<String, List<Instruction>> getIntructions() {
		return instrSeqTable;
	}
	
	/**
	 * ��ȡ������Ϣ
	 * @return
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	
	/**
	 * �����﷨���������ַ���
	 * @return 
	 */
	public String treeString() {
		StringBuilder res = new StringBuilder();
		preOrder(root, 0, res);
		return res.toString();
	}
	
	/**
	 * ��������﷨������
	 * @param depth
	 */
	private void preOrder(TreeNode node, int depth, StringBuilder str) {
		for (int i = 0; i < depth; i++) {
			str.append(retract);
		}
		str.append(node.toString());
		str.append("\n");
		
		boolean hasChild = false;
		for (TreeNode child : node) {
			hasChild = true;
			preOrder(child, depth + 1, str);
		}
		if (!hasChild && node.getSymbol() instanceof Nonterminal) {
			for (int i = 0; i < depth + 1; i++) {
				str.append(retract);
			}
			str.append("��\n");
		}
	}
	
}

/**
 * �м����
 * @author xjy
 *
 */
class Temp {
	private static int indexCnt = 1;
	private int index;
	public Temp() {
		index = indexCnt;
		indexCnt++;
	}
	
	@Override
	public String toString() {
		return "t" + index;
	}
}