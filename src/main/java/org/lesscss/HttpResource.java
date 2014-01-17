package org.lesscss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class HttpResource implements Resource {

    URI url;

    public HttpResource(String url) throws URISyntaxException {
        this.url = new URI( url );
    }

    public HttpResource(URI url) {
        this.url = url;
    }

    public boolean exists() {
        try {
            URL u = url.toURL();
            URLConnection connection = u.openConnection();
            connection.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public long lastModified() {
        try {
            URL u = url.toURL();
            URLConnection connection = u.openConnection();
            return connection.getLastModified();
        } catch( IOException e ) {
            return 0;
        }
    }

    public InputStream getInputStream() throws IOException {
        return url.toURL().openStream();
    }

    public Resource createRelative(String relativeResourcePath) throws IOException {
        try {
            return new HttpResource(url.resolve(new URI(relativeResourcePath)));
        } catch (URISyntaxException e) {
            throw (IOException)new IOException( "Could not resolve " + url + " against " + relativeResourcePath ).initCause(e);
        }
    }

    public String getName() {
        return url.toASCIIString();
    }
}
