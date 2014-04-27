package net.betaengine.avrdude;

import net.betaengine.avrdude.Value.BooleanValue;
import net.betaengine.avrdude.Value.DecimalListValue;
import net.betaengine.avrdude.Value.DecimalValue;
import net.betaengine.avrdude.Value.EnumValue;
import net.betaengine.avrdude.Value.HexListValue;
import net.betaengine.avrdude.Value.HexValue;
import net.betaengine.avrdude.Value.InvertedValue;
import net.betaengine.avrdude.Value.StringListValue;
import net.betaengine.avrdude.Value.StringValue;

public interface ValueVisitor {
	void visit(StringValue value);
	void visit(StringListValue value);
	void visit(DecimalValue value);
	void visit(DecimalListValue value);
	void visit(InvertedValue value);
	void visit(BooleanValue value);
	void visit(EnumValue value);
	void visit(HexValue value);
	void visit(HexListValue value);
}