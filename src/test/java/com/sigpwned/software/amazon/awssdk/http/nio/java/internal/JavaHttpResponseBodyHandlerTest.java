package com.sigpwned.software.amazon.awssdk.http.nio.java.internal;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler;

public class JavaHttpResponseBodyHandlerTest {

  @Test
  public void BodyHandlerCreatedSuccessfullyTest() {
    SdkAsyncHttpResponseHandler mockSdkHttpResponseHandler = mock(
        SdkAsyncHttpResponseHandler.class);
    HttpResponse.ResponseInfo responseInfo = mock(HttpResponse.ResponseInfo.class);

    ListToByteBufferProcessor listToByteBufferProcessor = new ListToByteBufferProcessor();

    JavaHttpResponseBodyHandler javaBodyHandler = new JavaHttpResponseBodyHandler(
        mockSdkHttpResponseHandler, listToByteBufferProcessor);

    Map<String, List<String>> headers = new HashMap<>();
    headers.put("foo", Collections.singletonList("bar"));

    HttpHeaders httpHeaders = HttpHeaders.of(headers, (s, s2) -> false);

    when(responseInfo.headers()).thenReturn(httpHeaders);
    when(responseInfo.statusCode()).thenReturn(200);

    javaBodyHandler.apply(responseInfo);

    ArgumentCaptor<SdkHttpResponse> capturedResponse = ArgumentCaptor.forClass(
        SdkHttpResponse.class);
    verify(mockSdkHttpResponseHandler).onHeaders(capturedResponse.capture());
    verify(mockSdkHttpResponseHandler).onStream(listToByteBufferProcessor.getPublisherToSdk());

    assertEquals(responseInfo.statusCode(), capturedResponse.getValue().statusCode());
    assertEquals(httpHeaders.map(), capturedResponse.getValue().headers());
  }

}