
import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
  public static int localPort = 9999;
  public static void main(String[] args) throws IOException {
    final var server = new Server();
    server.listen(localPort);
  }
}


