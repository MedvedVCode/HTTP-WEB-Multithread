import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;

    private List<NameValuePair> listQuery;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public Request(String method, String path, List<NameValuePair> listQuery) {
        this.method = method;
        this.path = path;
        this.listQuery = listQuery;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<NameValuePair> getQueryParam() {
        return listQuery;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return listQuery.stream()
                .filter(str -> Objects.equals(str.getName(),name))
                .collect(Collectors.toList());
    }

}
