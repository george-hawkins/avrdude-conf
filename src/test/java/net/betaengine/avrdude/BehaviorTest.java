package net.betaengine.avrdude;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class BehaviorTest {
	@Test
	public void programmerPartTest() {
		Pattern p = Pattern.compile("(programmer|part)(?: parent \"(.*)\")?");
		
		Matcher matcher;
		
		matcher = p.matcher("programmer");
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals("programmer", matcher.group(1));
		Assert.assertNull(matcher.group(2));
		
		matcher = p.matcher("part");
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals("part", matcher.group(1));
		Assert.assertNull(matcher.group(2));
		
		matcher = p.matcher("part parent \"alpha\"");
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals("part", matcher.group(1));
		Assert.assertEquals("alpha", matcher.group(2));
		
		matcher = p.matcher("programmer parent \"beta\"");
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals("programmer", matcher.group(1));
		Assert.assertEquals("beta", matcher.group(2));
	}
	
	@Test
	public void decimalArrayTest() {
		Pattern p = Pattern.compile("([0-9]+(?:, )?){2,}");

		Matcher matcher;
		
		matcher = p.matcher("2, 3, 4, 5"); // 2, 3, 4, 5
		Assert.assertTrue(matcher.matches());
	}
	
	@Test
	public void paddingTest() {
	    // "#" is mapped to "0x" but is included in the width so the width is two
	    // less for the second format to achieve the same result.
        String s1 = String.format("%#04x", 16);
        String s2 = String.format("0x%02x", 16);
	    
        Assert.assertEquals("0x10", s1);
        Assert.assertEquals("0x10", s2);
	}
}
