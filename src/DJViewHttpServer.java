import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class DJViewHttpServer {

    public static void main(String[] args) throws IOException {
        byte[] html = Files.readAllBytes(Path.of("index.html"));

        BeatModelInterface model = new BeatModel();
        model.initialize();

        DJViewHttpHandler controller =
                new DJViewHttpHandler(model, html);

        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8080),
                0
        );

        server.createContext("/", controller);
        server.start();

        System.out.println("DJ-сервер запущен.");
        System.out.println("Откройте http://127.0.0.1:8080/");
    }
}