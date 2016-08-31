package ca.seenet.seecraft.validator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidatorServer
{
    private static final int DEFAULT_PORT = 25565;

    private ServerSocket server;
    private ArrayList<ValidatorServerThread> threads;

    public ValidatorServer(int port, URL postbackUrl) throws IOException, InterruptedException
    {
        this.server = new ServerSocket(port);
        this.threads = new ArrayList<>();

        log.info("Listening for connections on port {}. Data on incoming connections will be posted back to {}.",
                 port, postbackUrl);

        while (!Thread.currentThread().isInterrupted())
        {
            Socket socket = this.server.accept();
            ValidatorServerThread thread = new ValidatorServerThread(socket, postbackUrl);
            thread.start();

            this.threads.add(thread);
        }

        log.info("Shutting down...");
        for (ValidatorServerThread thread : this.threads)
        {
            thread.join();
        }
        log.info("Done.");
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1)
        {
            log.error("Usage: java -jar validator.jar postback_url [listen_port]");
            System.exit(1);
        }

        URL postbackUrl = new URL(args[0]);
        int port = ValidatorServer.DEFAULT_PORT;
        if (args.length >= 2)
        {
            port = Integer.valueOf(args[1]);
        }

        log.info("Starting up...");

        new ValidatorServer(port, postbackUrl);
    }
}
