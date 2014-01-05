package org.lesscss;

import java.io.IOException;
import java.io.InputStream;

/**
 * LESS resource interface.
 *
 * <p>Abstracts {@link LessSource} from resource access technology. Makes it possible to load LESS resources from files,
 * classpath resource, URL, etc.</p>
 *
 * @author Anton Pechinsky
 */
public interface Resource {

    /**
     * Tests if resource exists.
     *
     * @return true if resource exists.
     */
    boolean exists();

    /**
     * Returns the time that the LESS source was last modified.
     *
     * @return A <code>long</code> value representing the time the resource was last modified, measured in milliseconds
     * since the epoch (00:00:00 GMT, January 1, 1970).
     */
    long lastModified();

    /**
     * Returns resource input stream.
     *
     * @throws IOException
     */
    InputStream getInputStream() throws IOException;

    /**
     * Creates relative resource for current resource.
     *
     * @param relativeResourcePath String relative resource path
     * @return Resource relative resource
     */
    Resource createRelative(String relativeResourcePath) throws IOException;

    /**
     * Returns a unique name for this resource. (ie file name for files)
     *
     * @return the name of the resource
     */
    String getName();
}
