package semantic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SymbolTable implements Iterable<SymbolTableEntry> {
	private String name;  // 符号表的名称
	private List<SymbolTableEntry> entrys = new ArrayList<>();  // 表项
	private SymbolTable previousTable; // 访问链, 指向外层符号表
	private int width; // 宽度

	private SymbolTable() {
	}

	/**
	 * 创建一个新的符号表
	 * 
	 * @param previous 外围过程的符号表
	 * @return
	 */
	public static SymbolTable makeTable(String name, SymbolTable previous) {
		SymbolTable res = new SymbolTable();
		res.name = name;
		res.previousTable = previous;
		return res;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * 获取外围过程的符号表
	 * 
	 * @return
	 */
	public SymbolTable getPrevious() {
		return previousTable;
	}

	/**
	 * 向表中插入一个表项
	 * 
	 * @param name
	 * @param type
	 * @param offset
	 */
	public void enter(String name, String type, int offset) {
		entrys.add(new SymbolTableEntry(name, type, offset));
	}

	/**
	 * 在本符号表中查询名为name的符号
	 * @param name
	 * @return 对应的表项
	 */
	public SymbolTableEntry lookUp(String name) {
		for (SymbolTableEntry entry : this) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		SymbolTableEntry res = null;
		if (previousTable != null)
			res = previousTable.lookUp(name);
		return res;
	}

	public void enterProc(String name, SymbolTable table) {
		entrys.add(new SymbolTableEntry(name, "PROC", table));
	}
	
	public void enterRecord(String name, SymbolTable table) {
		entrys.add(new SymbolTableEntry(name, "RECORD", table));
	}

	@Override
	public Iterator<SymbolTableEntry> iterator() {
		return entrys.iterator();
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append(this.name + " {\n");
		for (SymbolTableEntry entry : this) {
			if (entry.getTable() == null) {
				res.append(entry.toString() + "\n");
			}
			else {
				res.append("<" + entry.getName() + ", " + entry.getType() + ", _>\n");
				res.append(entry.getTable().toString());
			}
		}
		res.append("}\n");
		return res.toString();
	}

}
