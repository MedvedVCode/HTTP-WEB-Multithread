
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static int localPort = 9999;

    public static final String GET = "GET";
    public static final String POST = "POST";

    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler(GET, "/index.html", Main::processFile);
        server.addHandler(GET, "/spring.svg", Main::processFile);
        server.addHandler(GET, "/spring.png", Main::processFile);
        server.addHandler(GET, "/resources.html", Main::processFile);
        server.addHandler(GET, "/styles.css", Main::processFile);
        server.addHandler(GET, "/app.js", Main::processFile);
        server.addHandler(GET, "/links.html", Main::processFile);
        server.addHandler(GET, "/forms.html", Main::processFile);
        server.addHandler(GET, "/events.html", Main::processFile);
        server.addHandler(GET, "/events.js", Main::processFile);
        server.addHandler(GET, "/classic.html", Main::processFileClassic);

        server.addHandler(GET, "/messages", (request, out) -> {
            var message = String.format("This is %s message", GET);
            try {
                processOk(out, "text/plain", message.length(), message);
                out.flush();
            } catch (IOException e) {
                e.getMessage();
            }
        });
        server.addHandler(POST, "/messages", (request, out) -> {
            var message = String.format("This is %s message", POST);
            try {
                processOk(out, "text/plain", message.length(), message);
                out.flush();
            } catch (IOException e) {
                e.getMessage();
            }
        });

        server.listen(localPort);
    }

    private static void processFile(Request request, BufferedOutputStream out) {
        try {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            processOk(out, mimeType, length, "");
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.getMessage();
        }
    }

    private static void processFileClassic(Request request, BufferedOutputStream out) {
        try {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            processOk(out, mimeType, content.length, "");
            out.write(content);
            out.flush();
        } catch (IOException e) {
            e.getMessage();
        }
    }

    private static void processOk(BufferedOutputStream out, String mimeType, long length, String message) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" + message
        ).getBytes());
    }
}


