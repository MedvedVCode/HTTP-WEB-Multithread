import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

public class RequestBuilder {
    public static Request build(String method, String path){
        try {
            URI uri = new URI(path);
            path = uri.getPath();
            List<NameValuePair> queryList = URLEncodedUtils.parse(uri, Charset.defaultCharset());
            return new Request(method, path, queryList);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
