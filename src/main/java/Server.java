import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int localPort = 9999;
    private ServerSocket serverSocket;
    private int threadPoolMax = 64;
    private ExecutorService threadPool;

    public Server() throws IOException {
        serverSocket = new ServerSocket(localPort);
        threadPool = Executors.newFixedThreadPool(threadPoolMax);
        handleClient();
    }

    private void handleClient() {
        while (true) {
            try  {
                threadPool.execute(new RequestHandler(serverSocket.accept()));
            } catch (IOException e) {
                threadPool.shutdown();
                e.getMessage();
            }
        }
    }
}
