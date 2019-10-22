package jenkins.security.s2m;

import jenkins.ReflectiveFilePathFilter;

import java.io.File;

/**
 * Tests a match against file operation name of {@link ReflectiveFilePathFilter#op(String, File)}.
 *
 * @author Kohsuke Kawaguchi
 */
interface OpMatcher {
    OpMatcher ALL = (String op) -> true;

	boolean matches(String op);
}
