package com.linkedin.metadata.restli;

import com.linkedin.common.callback.FutureCallback;
import com.linkedin.common.util.None;
import com.linkedin.d2.balancer.D2Client;
import com.linkedin.d2.balancer.D2ClientBuilder;
import com.linkedin.r2.transport.common.Client;
import com.linkedin.r2.transport.common.bridge.client.TransportClient;
import com.linkedin.r2.transport.common.bridge.client.TransportClientAdapter;
import com.linkedin.r2.transport.http.client.HttpClientFactory;
import com.linkedin.restli.client.RestClient;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class DefaultRestliClientFactory {

  private static final String DEFAULT_REQUEST_TIMEOUT_IN_MS = "10000";

  private DefaultRestliClientFactory() {
  }

  @Nonnull
  public static RestClient getRestLiD2Client(@Nonnull String restLiClientD2ZkHost,
                                             @Nonnull String restLiClientD2ZkPath) {
    final D2Client d2Client = new D2ClientBuilder()
            .setZkHosts(restLiClientD2ZkHost)
            .setBasePath(restLiClientD2ZkPath)
            .build();
    d2Client.start(new FutureCallback<None>());
    return new RestClient(d2Client, "d2://");
  }

  @Nonnull
  public static RestClient getRestLiClient(@Nonnull String restLiServerHost, int restLiServerPort) {
    return getRestLiClient(restLiServerHost,restLiServerPort, false, null);
  }

  @Nonnull
  public static RestClient getRestLiClient(@Nonnull String restLiServerHost, int restLiServerPort, boolean useSSL, @Nullable String sslProtocol) {
    if (StringUtils.isBlank(restLiServerHost) || restLiServerPort <= 0) {
      throw new InvalidParameterException("Invalid restli server host name or port!");
    }

    if (useSSL) {
      return getHttpsRestClient(restLiServerHost, restLiServerPort, sslProtocol);
    } else {
      return getHttpRestClient(restLiServerHost, restLiServerPort);
    }

  }

  private static RestClient getHttpsRestClient(@Nonnull String restLiServerHost, int restLiServerPort,
                                               @Nullable String sslProtocol) {


    HttpClientFactory factory = new HttpClientFactory.Builder().build();
    Map<String,Object> params = new HashMap<>();
    SSLParameters sslParameters = new SSLParameters();
    if (sslProtocol != null) {
      sslParameters.setProtocols(new String[]{sslProtocol});
    }

    try {
      params.put(HttpClientFactory.HTTP_SSL_CONTEXT, SSLContext.getDefault());
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    params.put(HttpClientFactory.HTTP_SSL_PARAMS, sslParameters);
    params.put(HttpClientFactory.HTTP_REQUEST_TIMEOUT, DEFAULT_REQUEST_TIMEOUT_IN_MS);

    TransportClient transportClient = factory.getClient(Collections.unmodifiableMap(params));
    Client r2Client = new TransportClientAdapter(transportClient);
    return new RestClient(r2Client, "https://" + restLiServerHost + ":" + restLiServerPort + "/");

  }

  private static RestClient getHttpRestClient(@Nonnull String restLiServerHost, int restLiServerPort) {
    HttpClientFactory http = new HttpClientFactory.Builder().build();
    TransportClient transportClient = http
            .getClient(Collections.singletonMap(HttpClientFactory.HTTP_REQUEST_TIMEOUT, DEFAULT_REQUEST_TIMEOUT_IN_MS));
    Client r2Client = new TransportClientAdapter(transportClient);
    return new RestClient(r2Client, "http://" + restLiServerHost + ":" + restLiServerPort + "/");
  }
}
