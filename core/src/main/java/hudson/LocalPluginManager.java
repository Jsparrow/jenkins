/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson;

import jenkins.util.SystemProperties;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Default implementation of {@link PluginManager}.
 *
 * @author Kohsuke Kawaguchi
 */
public class LocalPluginManager extends PluginManager {
    private static final Logger LOGGER = Logger.getLogger(LocalPluginManager.class.getName());

	/**
     * Creates a new LocalPluginManager
     * @param context Servlet context. Provided for compatibility as {@code Jenkins.get().servletContext} should be used.
     * @param rootDir Jenkins home directory.
     */
    public LocalPluginManager(@CheckForNull ServletContext context, @Nonnull File rootDir) {
        super(context, new File(rootDir,"plugins"));
    }

	/**
     * Creates a new LocalPluginManager
     * @param jenkins Jenkins instance that will use the plugin manager.
     */
    public LocalPluginManager(@Nonnull Jenkins jenkins) {
        this(jenkins.servletContext, jenkins.getRootDir());
    }

	/**
     * Creates a new LocalPluginManager
     * @param rootDir Jenkins home directory.
     */
    public LocalPluginManager(@Nonnull File rootDir) {
        this(null, rootDir);
    }

	@Override
    protected Collection<String> loadBundledPlugins() {
        // this is used in tests, when we want to override the default bundled plugins with .jpl (or .hpl) versions
        if (SystemProperties.getString("hudson.bundled.plugins") != null) {
            return Collections.emptySet();
        }

        try {
            return loadPluginsFromWar("/WEB-INF/plugins");
        } finally {
            loadDetachedPlugins();
        }
    }
}
