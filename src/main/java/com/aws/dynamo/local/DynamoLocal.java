package com.aws.dynamo.local;

import com.almworks.sqlite4java.SQLite;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBRequestHandler;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBServerHandler;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

public class DynamoLocal {
    public static final String SQL_LITE_LIBS_DIRECTORY = "/tmp/dynamo-local-sqlite-libs";

    private final int port;
    private final DynamoDBProxyServer dynamoServer;

    public DynamoLocal() {
        this(new Random().nextInt(3000) + 6000);
    }

    public DynamoLocal(int port) {
        this.port = port;

        // make sure the native libraries are in place
        ensureSqliteLibs();

        // configure sqlite4java to look for the libraries there
        SQLite.setLibraryPath(SQL_LITE_LIBS_DIRECTORY);

        // start the dynamo local server
        LocalDynamoDBRequestHandler primaryHandler = new LocalDynamoDBRequestHandler(0, true, null, false, false);
        dynamoServer = new DynamoDBProxyServer(this.port, new LocalDynamoDBServerHandler(primaryHandler, "*"));
    }

    public AmazonDynamoDBClient buildDynamoClient() {
        AmazonDynamoDBClient dynamoClient = new AmazonDynamoDBClient(new DefaultAWSCredentialsProviderChain());
        dynamoClient.setEndpoint("http://localhost:$port");
        return dynamoClient;
    }

    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(buildDynamoClient());
    }

    public void start() throws Exception {
        dynamoServer.start();
    }

    public void stop() throws Exception {
        dynamoServer.stop();
    }

    public void createTable(Consumer<CreateTableRequest> createTableRequestConsumer) {
        AmazonDynamoDBClient dynamoClient = buildDynamoClient();
        CreateTableRequest createTableRequest = new CreateTableRequest();
        createTableRequest.setProvisionedThroughput(new ProvisionedThroughput(100L, 100L));
        createTableRequestConsumer.accept(createTableRequest);
        dynamoClient.createTable(createTableRequest);
    }

    /**
     * Copy the libraries to the
     */
    private void ensureSqliteLibs() {
        File sqliteLibsDir = new File(SQL_LITE_LIBS_DIRECTORY);
        sqliteLibsDir.mkdirs();
        if (!sqliteLibsDir.isDirectory()) {
            throw new IllegalStateException("$sqliteLibsDir does not exists");
        }

        Arrays.asList("libsqlite4java-linux-amd64.so", "libsqlite4java-linux-i386.so", "libsqlite4java-osx.dylib")
              .stream()
              .filter(file -> !new File(sqliteLibsDir, file).exists())
              .forEach(libName -> {
                  try (InputStream stream = ClassLoader.getSystemResourceAsStream(libName);
                       FileOutputStream fos = new FileOutputStream(new File(sqliteLibsDir, libName))) {
                      IOUtils.copy(stream, fos);
                  } catch (IOException e) {
                      throw new RuntimeException(e);
                  }
              });
    }

}
