/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Seiji Sogabe
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
package hudson.security;

import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.web.context.WebApplicationContext;
import groovy.lang.Binding;
import hudson.model.Descriptor;
import hudson.util.spring.BeanBuilder;
import hudson.Extension;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * {@link SecurityRealm} that accepts {@link ContainerAuthentication} object
 * without any check (that is, by assuming that the such token is
 * already authenticated by the container.)
 * 
 * @author Kohsuke Kawaguchi
 */
public final class LegacySecurityRealm extends SecurityRealm implements AuthenticationManager {
    /**
     * @deprecated as of 2.0
     *      Don't use this field, use injection.
     */
    @Restricted(NoExternalUse.class)
    public static /*almost final*/ Descriptor<SecurityRealm> DESCRIPTOR;

	@DataBoundConstructor
    public LegacySecurityRealm() {
    }

	@Override
	public SecurityComponents createSecurityComponents() {
        return new SecurityComponents(this);
    }

	@Override
	public Authentication authenticate(Authentication authentication) {
        if(authentication instanceof ContainerAuthentication) {
			return authentication;
		} else {
			return null;
		}
    }

	/**
     * To have the username/password authenticated by the container,
     * submit the form to the URL defined by the servlet spec.
     */
    @Override
    public String getAuthenticationGatewayUrl() {
        return "j_security_check";
    }

	@Override
    public String getLoginUrl() {
        return "loginEntry";
    }

	/**
     * Filter to run for the LegacySecurityRealm is the
     * ChainServletFilter legacy from /WEB-INF/security/SecurityFilters.groovy.
     */
    @Override
    public Filter createFilter(FilterConfig filterConfig) {
        Binding binding = new Binding();
        SecurityComponents sc = this.createSecurityComponents();
        binding.setVariable("securityComponents", sc);
        binding.setVariable("securityRealm",this);
        BeanBuilder builder = new BeanBuilder();
        builder.parse(filterConfig.getServletContext().getResourceAsStream("/WEB-INF/security/SecurityFilters.groovy"),binding);
        
        WebApplicationContext context = builder.createApplicationContext();
        
        return (Filter) context.getBean("legacy");
    }

	@Extension @Symbol("legacy")
    public static class DescriptorImpl extends  Descriptor<SecurityRealm> {
        public DescriptorImpl() {
            DESCRIPTOR = this;
        }

        @Override
		public String getDisplayName() {
            return Messages.LegacySecurityRealm_Displayname();
        }
    }
}
