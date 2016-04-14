# dynamo-local

Embedded [DynamoLocal](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html) for the JVM.

```groovy
// gradle
repositories {
  maven { url "https://jitpack.io" }
}

dependencies {
  testCompile 'com.github.casidiablo:dynamo-local:0.4'
}
```

###Usage

```java
DynamoLocal dynamoLocal = new DynamoLocal(); // optionally provide a port
dynamoLocal.start();

// then
AmazonDynamoDBClient amazonDynamoDBClient = dynamoLocal.buildDynamoClient();
DynamoDBMapper dynamoDBMapper = dynamoLocal.dynamoDBMapper();
```
