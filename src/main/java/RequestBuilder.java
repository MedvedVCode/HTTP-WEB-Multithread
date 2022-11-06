import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RequestBuilder {
    public static final String GET = "GET";
    public static final String POST = "POST";

    public static Request build(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        final var allowedMethods = List.of(GET, POST);
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // печать всего буффера с запросом и телом
        System.out.println("******");
        final var strBuffer = new String(buffer);
        System.out.println(strBuffer);
        System.out.println("******\n");

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            badRequest(out);
            return null;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            badRequest(out);
            return null;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            badRequest(out);
            return null;
        }
        //System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            badRequest(out);
            return null;
        }
        //System.out.println(path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            badRequest(out);
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        //System.out.println(headers);

        var pathWithoutParams = path;
        List<NameValuePair> queryParam = null;
        List<NameValuePair> bodyParam = null;

        //определяем content-Type, а точнее есть ли там application/x-www-form-urlencoded
        var contentType = "";
        final var contentTypeOptional = extractHeader(headers, "Content-Type");
        if(contentTypeOptional.isPresent()){
            contentType = contentTypeOptional.get();
        }

        // для GET тела нет
        if (!method.equals(GET) && contentType.equals("application/x-www-form-urlencoded")) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                final var body = new String(bodyBytes);
                //System.out.println(body);

                bodyParam = URLEncodedUtils.parse(body, Charset.defaultCharset());
                //bodyParam.stream().forEach(System.out::println);
            }
        } else if (method.equals(GET)){
            URI uri;
            try {
                uri = new URI(path);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            pathWithoutParams = uri.getPath();
            queryParam = URLEncodedUtils.parse(uri, Charset.defaultCharset());
            //queryParam.stream().forEach(System.out::println);
        }

        return new Request(method, pathWithoutParams, queryParam, bodyParam);
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
