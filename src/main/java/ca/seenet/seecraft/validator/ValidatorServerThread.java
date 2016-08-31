package ca.seenet.seecraft.validator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidatorServerThread extends Thread
{
    private Socket socket;
    private URL postbackUrl;

    public ValidatorServerThread(Socket socket, URL postbackUrl)
    {
        super("connection from " + socket.getRemoteSocketAddress());
        this.socket = socket;
        this.postbackUrl = postbackUrl;
    }

    @Override
    public void run()
    {
        try (MinecraftStreamReader reader = new MinecraftStreamReader(this.socket.getInputStream());
             MinecraftStreamWriter writer = new MinecraftStreamWriter(this.socket.getOutputStream()))
        {
            /* We don't really care about the handshake packet size. */
            reader.readVarint();
            long packetId = reader.readVarint();

            /* Ignore the legacy server list ping. */
            if (packetId != 0x00)
            {
                log.debug("Client at {} wanted to do something other than handshake but we ignored it.",
                          socket.getRemoteSocketAddress());
                return;
            }

            long protocolVersion = reader.readVarint();
            String serverAddress = reader.readString();
            short serverPort = reader.readShort();

            long nextState = reader.readVarint();
            /* If we're just being asked for our status, hang up. */
            if (nextState == 1)
            {
                log.debug("Client at {} wanted our status but we ignored it.",
                          socket.getRemoteSocketAddress());
                return;
            }

            /* We don't care about the login packet size... */
            reader.readVarint();
            /* ...or ID either. */
            reader.readVarint();
            String username = reader.readString();

            MojangApiClient client = new MojangApiClient();
            String uuid = client.getPlayerUuid(username);

            log.info("Received a connection to {}:{} from {} (UUID {}; protocol version {}).",
                     serverAddress, serverPort, username, uuid, protocolVersion);

            /* Tell whoever cares that we saw a valid connection. */
            byte[] postData = String.format("protocolVersion=%d"
                                            + "&serverAddress=%s"
                                            + "&serverPort=%d"
                                            + "&username=%s"
                                            + "&uuid=%s",
                                            protocolVersion, serverAddress, serverPort, username, uuid)
                                    .getBytes(StandardCharsets.UTF_8);
            HttpURLConnection connection = (HttpURLConnection) this.postbackUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(postData.length));
            connection.setUseCaches(false);
            connection.connect();
            try (OutputStream connectionStream = connection.getOutputStream())
            {
                connectionStream.write(postData);
            }
            connection.getInputStream().close();

            writer.writeDisconnect("Thank you for logging in. Please return to your browser to complete the authentication process.");
        }
        catch (IOException e)
        {
            /* Fail horribly. */
            log.error("Encountered an error: {}", e);
        }
        finally
        {
            try
            {
                this.socket.close();
            }
            catch (IOException e)
            {
                /* Whoopsie daisy. */
            }
        }
    }
}
