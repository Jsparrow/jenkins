package jenkins.slaves;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Slave;
import java.security.SecureRandom;
import javax.annotation.Nonnull;
import org.jenkinsci.remoting.engine.JnlpClientDatabase;
import org.jenkinsci.remoting.engine.JnlpConnectionStateListener;

/**
 * Receives incoming agents connecting through {@link JnlpSlaveAgentProtocol2}, {@link JnlpSlaveAgentProtocol3}, {@link JnlpSlaveAgentProtocol4}.
 *
 * <p>
 * This is useful to establish the communication with other JVMs and use them
 * for different purposes outside {@link Slave}s.

 * <ul>
 * <li> When the {@link jenkins.slaves.JnlpAgentReceiver#exists(String)} method is invoked for an agent, the {@link jenkins.slaves.JnlpAgentReceiver#owns(String)} method is called on all the extension points: if no owner is found an exception is thrown.</li>
 * <li> If owner is found, then the {@link org.jenkinsci.remoting.engine.JnlpConnectionState} lifecycle methods are invoked for all registered {@link JnlpConnectionStateListener} until the one which changes the state of {@link org.jenkinsci.remoting.engine.JnlpConnectionState} by setting an approval or rejected state is found.
 *      When found, that listener will be set as the owner of the incoming connection event. </li>
 * <li> Subsequent steps of the connection lifecycle are only called on the {@link JnlpAgentReceiver} implementation owner for that connection event.</li>
 * </ul>
 *
 *
 * @author Kohsuke Kawaguchi
 * @since 1.561
 */
public abstract class JnlpAgentReceiver extends JnlpConnectionStateListener implements ExtensionPoint {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static final JnlpClientDatabase DATABASE = new JnlpAgentDatabase();

    public static ExtensionList<JnlpAgentReceiver> all() {
        return ExtensionList.lookup(JnlpAgentReceiver.class);
    }

    public static boolean exists(String clientName) {
        return all().stream().anyMatch(receiver -> receiver.owns(clientName));
    }

    protected abstract boolean owns(String clientName);

    public static String generateCookie() {
        byte[] cookie = new byte[32];
        secureRandom.nextBytes(cookie);
        return Util.toHexString(cookie);
    }

    private static class JnlpAgentDatabase extends JnlpClientDatabase {
        @Override
        public boolean exists(String clientName) {
            return JnlpAgentReceiver.exists(clientName);
        }

        @Override
        public String getSecretOf(@Nonnull String clientName) {
            return JnlpSlaveAgentProtocol.SLAVE_SECRET.mac(clientName);
        }
    }
}
