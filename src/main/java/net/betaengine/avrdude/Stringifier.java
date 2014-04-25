package net.betaengine.avrdude;

public class Stringifier {
	
//	public String toString() {
//		StringWriter stringer = new StringWriter();
//		IndentWriter indenter = new IndentWriter(stringer);
//		PrintWriter writer = new PrintWriter(indenter);
//		
//		toString(writer, indenter);
//		
//		writer.flush();
//		writer.close();
//		
//		return stringer.toString();
//	}
//	
//	private void toString(PrintWriter writer, IndentWriter indenter) {
//		writer.print("<" + type + "> ");
//		if (parent != null) {
//			writer.print("(" + parent + ") ");
//		}
//		if (qualifier != null) {
//			writer.println("[" + qualifier + "] {");
//		}
//		writer.println("{");
//		indenter.increment();
//		
//		for (Map.Entry<String, String> entry : properties.entrySet()) {
//			writer.println("<" + entry.getKey() + "> = <" + entry.getValue() + ">");
//		}
//		
//		for (ConfContainer container : containers) {
//			writer.println();
//			container.toString(writer, indenter);
//		}
//		
//		indenter.decrement();
//		writer.println("}");
//	}

}
