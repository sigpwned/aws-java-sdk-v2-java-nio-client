package com.sigpwned.software.amazon.awssdk.http.nio.java.internal;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler;
import software.amazon.awssdk.utils.AttributeMap;


@SdkPublicApi
public final class JavaHttpRequestExecutor {

  private JavaHttpRequestFactory javaHttpRequestFactory;
  private HttpRequest javaHttpRequest;
  private HttpClient javaHttpClient;
  private HttpResponse.BodyHandler<Void> javaHttpClientBodyHandler;
  private ListToByteBufferProcessor listToByteBufferProcessor;

  public JavaHttpRequestExecutor(HttpClient javaHttpClient, AttributeMap serviceDefaultsMap) {
    this.listToByteBufferProcessor = new ListToByteBufferProcessor();
    this.javaHttpClient = javaHttpClient;
    this.javaHttpRequestFactory = new JavaHttpRequestFactory(serviceDefaultsMap);
  }

  public CompletableFuture<Void> requestExecution(AsyncExecuteRequest asyncExecuteRequest) {
    this.javaHttpRequest = javaHttpRequestFactory.createJavaHttpRequest(asyncExecuteRequest);
    SdkAsyncHttpResponseHandler sdkAsyncHttpResponseHandler = asyncExecuteRequest.responseHandler();
    this.javaHttpClientBodyHandler = new JavaHttpResponseBodyHandler(sdkAsyncHttpResponseHandler,
        listToByteBufferProcessor);
    return execute(sdkAsyncHttpResponseHandler);
  }

  /**
   * Creates the {@link ListToByteBufferProcessor} and pass the Publisher and Subscriber to
   * SdkAsyncHttpResponseHandler and HttpResponse.BodyHandler respectively to connect these two
   * ends. Then execute the request asynchronously.
   *
   * @return The CompletableFuture object that indicates whether the request has been execute
   * successfully
   */
  private CompletableFuture<Void> execute(SdkAsyncHttpResponseHandler sdkAsyncHttpResponseHandler) {
    CompletableFuture<HttpResponse<Void>> future = javaHttpClient.sendAsync(javaHttpRequest,
        javaHttpClientBodyHandler);

    future.whenComplete((r, t) -> {
      if (t != null) {
        future.completeExceptionally(t);
        sdkAsyncHttpResponseHandler.onError(t); // Handle errors of HttpResponse
      }
    });

    CompletableFuture<Void> terminated = listToByteBufferProcessor.getTerminated();
    terminated.whenComplete((r, t) -> {
      if (t != null) {
        terminated.completeExceptionally(t);
        sdkAsyncHttpResponseHandler.onError(t); // Handle errors of listToByteBufferProcessor
      }
    });
    return terminated;
  }

}