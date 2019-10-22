package jenkins.util.xml;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * An EntityResolver that will fail to resolve any entities.
 * Useful in preventing External XML Entity injection attacks.
 */
@Restricted(NoExternalUse.class)
public final class RestrictiveEntityResolver implements EntityResolver {

    public static final RestrictiveEntityResolver INSTANCE = new RestrictiveEntityResolver();

    private RestrictiveEntityResolver() {
        // prevent multiple instantiation.
    }

    /**
     * Throws a SAXException if this tried to resolve any entity.
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        throw new SAXException(new StringBuilder().append("Refusing to resolve entity with publicId(").append(publicId).append(") and systemId (").append(systemId).append(")").toString());
    }
}
