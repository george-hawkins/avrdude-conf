package net.betaengine.avrdude;

import java.util.List;

public class Value<T> {
	public static Value<Void> DELETE_VALUE = new Value<Void>(null);

	private final T value;

	protected Value(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}
	
	public void accept(ValueVisitor visitor) {
		throw new UnsupportedOperationException();
	}
	
	public static class StringValue extends Value<String> {
		public StringValue(String value) {
			super(value);
		}
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}

	public static class StringListValue extends Value<List<String>> {
		public StringListValue(List<String> value) {
			super(value);
		}
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}

	public static class DecimalValue extends Value<Integer> {
		public DecimalValue(Integer value) {
			super(value);
		}
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}
	
	public static class DecimalListValue extends Value<List<Integer>> {
		public DecimalListValue(List<Integer> value) {
			super(value);
		}
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}
	
	public static class InvertedValue extends Value<Integer> {
		public InvertedValue(Integer value) {
			super(value);
		}
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}

	public static class BooleanValue extends Value<Boolean> {
		public BooleanValue(Boolean value) {
			super(value);
		}
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}

	public static class EnumValue extends Value<String> {
		public EnumValue(String value) {
			super(value);
		}
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}

	// There are some unsigned 8 byte hex values that are too big for Java's signed Integer.
	public static class HexValue extends Value<Long> {
	    private final int nibbles;
	    
		public HexValue(Long value, int nibbles) {
			super(value);
			this.nibbles = nibbles;
		}
		
        public int getNibbles() { return nibbles; }
        
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}

	public static class HexListValue extends Value<List<Integer>> {
        private final int nibbles;
        
		public HexListValue(List<Integer> value, int nibbles) {
			super(value);
            this.nibbles = nibbles;
		}
		
		public int getNibbles() { return nibbles; }
		
		@Override
		public void accept(ValueVisitor visitor) { visitor.visit(this); }
	}
}