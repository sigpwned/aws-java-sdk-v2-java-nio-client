package com.sigpwned.software.amazon.awssdk.http.nio.java.internal;

import com.sigpwned.software.amazon.awssdk.http.nio.java.JavaHttpClientNioAsyncHttpClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import org.junit.BeforeClass;
import software.amazon.awssdk.awscore.util.AwsHostNameUtils;
import software.amazon.awssdk.http.Protocol;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.testutils.service.AwsTestBase;

public class AbstractTestCase extends AwsTestBase {

  protected static KinesisClient client;
  protected static KinesisAsyncClient asyncClient;

  @BeforeClass
  public static void init() throws IOException {
    setUpCredentials();
    KinesisClientBuilder builder = KinesisClient.builder()
        .credentialsProvider(CREDENTIALS_PROVIDER_CHAIN);
    setEndpoint(builder);
    client = builder.build();

    SdkAsyncHttpClient javaHttpClient = JavaHttpClientNioAsyncHttpClient.builder()
        .protocol(Protocol.HTTP2).build();

    asyncClient = KinesisAsyncClient.builder().httpClient(javaHttpClient).region(Region.US_EAST_1)
        .credentialsProvider(CREDENTIALS_PROVIDER_CHAIN).build();
  }

  private static void setEndpoint(KinesisClientBuilder builder) throws IOException {
    File endpointOverrides = new File(new File(System.getProperty("user.home")),
        ".aws/awsEndpointOverrides.properties");

    if (endpointOverrides.exists()) {
      Properties properties = new Properties();
      properties.load(new FileInputStream(endpointOverrides));

      String endpoint = properties.getProperty("kinesis.endpoint");

      if (endpoint != null) {
        Region region = AwsHostNameUtils.parseSigningRegion(endpoint, "kinesis").orElseThrow(
            () -> new IllegalArgumentException("Unknown region for endpoint. " + endpoint));
        builder.region(region).endpointOverride(URI.create(endpoint));
      }
    }
  }
}