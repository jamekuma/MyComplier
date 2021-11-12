package semantic;

public class Instruction {
	private String op;
	private String param1;
	private String param2;
	private String dest;
	
	public Instruction(String op, String param1, String param2, String dest) {
		this.setOp(op);
		this.setParam1(param1);
		this.setParam2(param2);
		this.setDest(dest);
	} 
	
	/**
	 * 获取三地址码的输出形式
	 * @return
	 */
	public String getTriple() {
		if (op.equals("=")) {
			return dest + " = "  + param1;
		}
		else if (op.equals("+")) {
			return dest + " = "  + param1 + " + " + param2;
		}
		else if (op.equals("*")) {
			return dest + " = "  + param1 + " * " + param2;
		}
		else if (op.equals("goto")) {
			return "goto " + dest;
		}
		else if (op.equals("[]=")) {
			return param2 + "[" + dest + "]" + " = " + param1;
		}
		else if (op.equals("=[]")) {
			return dest + " = "  + param1 + "[" + param2 + "]";
		}
		else if (op.equals("return")) {
			return "return " + dest;
		}
		else if (op.equals("param")) {
			return "param " + dest;
		}
		else if (op.equals("call")) {
			return dest + " = call " + param1 + ", " + param2;
		}
		else {
			return "if " + param1 + " " + op + " " + param2 + " goto " + dest;
		}
	}
	
	/**
	 * 获取四元式的输出形式
	 * @return
	 */
	public String getQuadra() {
		return "(" + op + ", " + (param1.equals("") ? "_" : param1) + ", " 
	+ (param2.equals("") ? "_" : param2) + ", " + (dest.equals("") ? "_" : dest) + ")";
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}
}
