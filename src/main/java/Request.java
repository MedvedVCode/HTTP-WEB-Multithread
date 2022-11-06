import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;
    private List<NameValuePair> listQuery;
    private List<NameValuePair> postParams;

    public Request(String method, String path, List<NameValuePair> listQuery, List<NameValuePair> postParams) {
        this.method = method;
        this.path = path;
        this.listQuery = listQuery;
        this.postParams = postParams;
    }


    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }


    public List<NameValuePair> getQueryParams() {
        return listQuery;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return listQuery.stream()
                .filter(str -> Objects.equals(str.getName(), name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getPostParams() {
        return postParams;
    }

    public List<NameValuePair> getPostParam(String name) {
        return postParams.stream()
                .filter(str -> Objects.equals(str.getName(), name))
                .collect(Collectors.toList());
    }
}
