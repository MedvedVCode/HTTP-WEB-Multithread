import org.apache.http.NameValuePair;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int threadPoolMax = 64;
    private ExecutorService threadPool;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlersMap = new ConcurrentHashMap<>();

    public void listen(int localPort) {
        try (var serverSocket = new ServerSocket(localPort);) {
            threadPool = Executors.newFixedThreadPool(threadPoolMax);
            System.out.println("Server start!");
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> requestClient(socket));
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlersMap.putIfAbsent(method, new ConcurrentHashMap<>());
        handlersMap.get(method).put(path, handler);
    }

    private void requestClient(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            System.out.printf("Handle client port %d in thread %s\n", socket.getPort(), Thread.currentThread().getName());

            String requestLine = null;
            while (requestLine == null) {
                requestLine = in.readLine();
            }
            //System.out.println(requestLine);
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                badRequest(out);
                return;
            }

            //var requst = new Request(parts[0], parts[1]);
            var requst = RequestBuilder.build(parts[0], parts[1]);

            printParams(requst.getQueryParam());
            printParams(requst.getQueryParam("password"));

            if (!handlersMap.containsKey(requst.getMethod())) {
                notFound(out);
                return;
            }

            if (!handlersMap.get(requst.getMethod()).containsKey(requst.getPath())) {
                notFound(out);
                return;
            }

            var handler = handlersMap.get(requst.getMethod()).get(requst.getPath());

            if (handler == null) {
                notFound(out);
                return;
            }

            handler.handle(requst, out);

        } catch (IOException e) {
            e.getMessage();
        }
    }

    private static void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

public void printParams(List<NameValuePair> list){
        list.forEach(System.out::println);
}
}
