package net.betaengine.avrdude.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.betaengine.avrdude.Value;
import net.betaengine.avrdude.Value.BooleanValue;
import net.betaengine.avrdude.Value.DecimalListValue;
import net.betaengine.avrdude.Value.DecimalValue;
import net.betaengine.avrdude.Value.EnumValue;
import net.betaengine.avrdude.Value.HexListValue;
import net.betaengine.avrdude.Value.HexValue;
import net.betaengine.avrdude.Value.InvertedValue;
import net.betaengine.avrdude.Value.StringListValue;
import net.betaengine.avrdude.Value.StringValue;

import com.google.common.collect.ImmutableSet;

public class ValueParser {
    private final static Pattern STRING = Pattern.compile("\"([^\"]*)\"");
    private final static Pattern STRING_ARRAY = Pattern.compile("(?:\"[^\"]*\",? ?){2,}");
    private final static Pattern DECIMAL = Pattern.compile("[0-9]+");
    private final static Pattern DECIMAL_ARRAY = Pattern.compile("(?:[0-9]+,? ?){2,}");
    private final static Pattern INVERTED = Pattern.compile("~\\s*([0-9]+)");
    private final static Pattern HEX = Pattern.compile("0x([0-9a-f]+)");
    private final static Pattern HEX_ARRAY = Pattern.compile("(?:0x[0-9a-f]+,? ?){2,}");
    private final static Pattern BOOLEAN = Pattern.compile("(?:yes|no)");
    private final static Set<String> ENUM = ImmutableSet.of("serial", "usb", "parallel", "dedicated", "io");

    public Value<?> parse(String s) {
        if (s.isEmpty()) {
            // A key with no value means the mapping with this name that
            // was inherited from the parent should be deleted.
            return Value.DELETE_VALUE;
        }

        Matcher m;

        if ((m = STRING.matcher(s)).matches()) {
            return parseString(m);
        } else if (STRING_ARRAY.matcher(s).matches()) {
            return parseStringList(s);
        } else if (DECIMAL.matcher(s).matches()) {
            return parseDecimal(s);
        } else if (DECIMAL_ARRAY.matcher(s).matches()) {
            return parseDecimalList(s);
        } else if ((m = INVERTED.matcher(s)).matches()) {
            // Note: avrdude.conf.in says "spaces are important", which seems to say
            // "~ 1" is different in an important way to "~1" however looking at
            // config_gram.y this does not seem to be the case so we ignore spaces.
            return parseInverted(m);
        } else {
            // The conf file is inconsistent in the keeping to a particular case so...
            s = s.toLowerCase();

            if (BOOLEAN.matcher(s).matches()) {
                return parseBoolean(s);
            } else if (ENUM.contains(s)) {
                return parseEnum(s);
            } else if ((m = HEX.matcher(s)).matches()) {
                return parseHex(m);
            } else if (HEX_ARRAY.matcher(s).matches()) {
                return parseHexList(s);
            } else {
                throw new ParseException("cannot parse value \"" + s + "\"");
            }
        }
    }

    private StringValue parseString(Matcher m) {
        return new StringValue(m.group(1));
    }

    private StringListValue parseStringList(String s) {
        List<String> list = new ArrayList<>();
        Matcher m = STRING.matcher(s);

        while (m.find()) {
            list.add(m.group(1));
        }

        return new StringListValue(list);
    }

    private DecimalValue parseDecimal(String s) {
        return new DecimalValue(Integer.valueOf(s));
    }

    private DecimalListValue parseDecimalList(String s) {
        List<Integer> list = new ArrayList<>();
        Matcher m = DECIMAL.matcher(s);

        while (m.find()) {
            list.add(Integer.valueOf(m.group(0)));
        }

        return new DecimalListValue(list);
    }

    private InvertedValue parseInverted(Matcher m) {
        return new InvertedValue(Integer.valueOf(m.group(1)));
    }

    private BooleanValue parseBoolean(String s) {
        return new BooleanValue(s.equals("yes"));
    }

    private EnumValue parseEnum(String s) {
        return new EnumValue(s);
    }

    private HexValue parseHex(Matcher m) {
        String s = m.group(1);
        int nibbles = s.length();

        return new HexValue(Long.valueOf(s, 16), nibbles);
    }

    private HexListValue parseHexList(String s) {
        List<Integer> list = new ArrayList<>();
        Matcher m = HEX.matcher(s);
        int nibbles = -1;

        while (m.find()) {
            String x = m.group(1);
            assert nibbles == -1 || nibbles == x.length();
            nibbles = x.length();

            list.add(Integer.valueOf(x, 16));
        }

        return new HexListValue(list, nibbles);
    }
}
