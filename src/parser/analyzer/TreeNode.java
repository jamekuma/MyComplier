package parser.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lexer.tokenTypes.Token;
import parser.LR1.Nonterminal;
import parser.LR1.Symbol;
import parser.LR1.Terminal;

/**
 * 语法分析树的叶节点
 * @author xjy
 *
 */
public class TreeNode implements Iterable<TreeNode> {
	private Symbol symbol;
	private int lineNumber;
	private Token token = null;
	private List<TreeNode> children = new ArrayList<>();
	
	public TreeNode(Symbol symbol, int lineNumber) {
		this.symbol = symbol;
		this.lineNumber = lineNumber;
	}
	
	public TreeNode(Terminal symbol, int lineNumber, Token token) {
		this.symbol = symbol;
		this.lineNumber = lineNumber;
		this.token = token;
	}
	
	public void addChild(TreeNode child) {
		children.add(child);
	}
	
	public Symbol getSymbol() {
		return symbol;
	}
	
	public int getLineNum() {
		return lineNumber;
	}
	
	@Override
	public Iterator<TreeNode> iterator() {
		return children.iterator();
	}
	
	@Override
	public String toString() {
		if (symbol instanceof Nonterminal || symbol.getText().equals("$")) {
			return symbol.toString() + " (" + lineNumber + ")"; 
		}
		else {
			return token.toString() + " (" + lineNumber + ")"; 
		}
	}
}
