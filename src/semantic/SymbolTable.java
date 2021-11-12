package semantic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SymbolTable implements Iterable<SymbolTableEntry> {
	private String name;  // ���ű������
	private List<SymbolTableEntry> entrys = new ArrayList<>();  // ����
	private SymbolTable previousTable; // ������, ָ�������ű�
	private int width; // ���

	private SymbolTable() {
	}

	/**
	 * ����һ���µķ��ű�
	 * 
	 * @param previous ��Χ���̵ķ��ű�
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
	 * ��ȡ��Χ���̵ķ��ű�
	 * 
	 * @return
	 */
	public SymbolTable getPrevious() {
		return previousTable;
	}

	/**
	 * ����в���һ������
	 * 
	 * @param name
	 * @param type
	 * @param offset
	 */
	public void enter(String name, String type, int offset) {
		entrys.add(new SymbolTableEntry(name, type, offset));
	}

	/**
	 * �ڱ����ű��в�ѯ��Ϊname�ķ���
	 * @param name
	 * @return ��Ӧ�ı���
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
