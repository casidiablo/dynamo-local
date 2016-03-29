import com.almworks.sqlite4java.SQLite;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBRequestHandler;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBServerHandler;

import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws Exception {
        SQLite.setLibraryPath("libs/");

        String tempDir = Files.createTempDirectory("dynamo").toString();
        LocalDynamoDBRequestHandler primaryHandler = new LocalDynamoDBRequestHandler(0, false, tempDir, false, false);
        DynamoDBProxyServer dynamoServer = new DynamoDBProxyServer(6389, new LocalDynamoDBServerHandler(primaryHandler, "*"));

        dynamoServer.start();
    }
}