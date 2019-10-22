package hudson.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class PackedMapTest {

    private XStream2 xs = new XStream2();

	@Test
    public void basic() {
        Map<String,String> o = new TreeMap<>();
        o.put("a","b");
        o.put("c","d");

        PackedMap<String,String> p = PackedMap.of(o);
        assertEquals("b",p.get("a"));
        assertEquals("d", p.get("c"));
        assertEquals(p.size(),2);
        p.entrySet().forEach(e -> System.out.println(new StringBuilder().append(e.getKey()).append('=').append(e.getValue()).toString()));

        Holder h = new Holder();
        h.pm = p;
        String xml = xs.toXML(h);
        assertEquals(
                new StringBuilder().append("<hudson.util.PackedMapTest_-Holder>\n").append("  <pm>\n").append("    <entry>\n").append("      <string>a</string>\n").append("      <string>b</string>\n").append("    </entry>\n").append("    <entry>\n")
						.append("      <string>c</string>\n").append("      <string>d</string>\n").append("    </entry>\n").append("  </pm>\n").append("</hudson.util.PackedMapTest_-Holder>").toString(),
                xml);

        xs.fromXML(xml);
    }

	static class Holder {
        PackedMap pm;
    }
}
