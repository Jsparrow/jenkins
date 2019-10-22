package hudson.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import hudson.util.CyclicGraphDetector.CycleDetectedException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class CyclicGraphDetectorTest {

    @Test
    public void cycle1() throws Exception {
        new Graph().e("A","B").e("B","C").e("C","A").mustContainCycle("A","B","C");
    }

	@Test
    public void cycle2() throws Exception {
        new Graph().e("A","B").e("B","C").e("C","C").mustContainCycle("C");
    }

	@Test
    public void cycle3() throws Exception {
        new Graph().e("A","B").e("B","C").e("C","D").e("B","E").e("E","D").e("E","A").mustContainCycle("A","B","E");
    }

	private class Edge {
        String src;
		String dst;

        private Edge(String src, String dst) {
            this.src = src;
            this.dst = dst;
        }
    }

    private class Graph extends ArrayList<Edge> {
        Graph e(String src, String dst) {
            add(new Edge(src,dst));
            return this;
        }

        Set<String> nodes() {
            Set<String> nodes = new LinkedHashSet<>();
            this.forEach(e -> {
                nodes.add(e.src);
                nodes.add(e.dst);
            });
            return nodes;
        }

        Set<String> edges(String from) {
            Set<String> edges = new LinkedHashSet<>();
            this.stream().filter(e -> e.src.equals(from)).forEach(e -> edges.add(e.dst));
            return edges;
        }

        /**
         * Performs a cycle check.
         */
        void check() throws Exception {
            new CyclicGraphDetector<String>() {
                @Override
				protected Set<String> getEdges(String s) {
                    return edges(s);
                }
            }.run(nodes());
        }

        void mustContainCycle(String... members) throws Exception {
            try {
                check();
                fail("Cycle expected");
            } catch (CycleDetectedException e) {
                String msg = new StringBuilder().append("Expected cycle of ").append(Arrays.asList(members)).append(" but found ").append(e.cycle).toString();
                for (String s : members) {
					assertTrue(msg, e.cycle.contains(s));
				}
            }
        }
    }
}
