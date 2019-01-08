/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ai.shape.basics.httpclient;

import ai.shape.basics.util.Http;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static ai.shape.basics.util.Exceptions.exceptionWithCause;

/** Fluent, synchronous HTTP client based on Apache Http Components.
 *
 * To obtain a Http object, just use the constructor new Http();
 *
 * To start building a request, use the factory methods
 * {@link #newGet(String url)},
 * {@link #newPost(String url)},
 * {@link #newPut(String url)},
 * {@link #newDelete(String url)} or
 * {@link #newRequest(String method, String url)}
 *
 * To execute the request (synchronous) and get the response, use
 * {@link ClientRequest#execute()}
 *
 * This is a synchronous HTTP library.  That means that the client
 * thread will be blocked when executing the request until the response
 * is being obtained. If you need an async or non-blocking HTTP library,
 * go look elsewhere.
 *
 * The 2 motivations for writing this fluent API on top of Apache HTTP
 * commons are:
 * 1) ClientRequest and ClientResponse classes are serializable
 *    with Gson.
 * 2) Allow access to the response status line as well as the
 *    response body.
 */
public class HttpClient {

  protected static Logger log = LoggerFactory.getLogger(HttpClient.class);

  protected CloseableHttpClient apacheHttpClient = HttpClientBuilder.create().build();
  protected String baseUrl =  null;
  protected Serializer serializer;

  public ClientRequest newGet(String url) {
    return newRequest(Http.Methods.GET, prependBaseUrl(url));
  }

  public ClientRequest newPost(String url) {
    return newRequest(Http.Methods.POST, prependBaseUrl(url));
  }

  public ClientRequest newPatch(String url) {
    return newRequest(Http.Methods.PATCH, prependBaseUrl(url));
  }

  public ClientRequest newPut(String url) {
    return newRequest(Http.Methods.PUT, prependBaseUrl(url));
  }

  public ClientRequest newDelete(String url) {
    return newRequest(Http.Methods.DELETE, prependBaseUrl(url));
  }

  /** The baseUrl will not be prepended to the given url!
   * So this method can be used when you want to skip the base url.
   * @param method constant can be obtained from {@link Http.Methods} */
  public ClientRequest newRequest(String method, String url) {
    return new ClientRequest(this, method, url);
  }

  protected String prependBaseUrl(String url) {
    return baseUrl!=null ? baseUrl+url : url;
  }

  public void close() {
    try {
      apacheHttpClient.close();
    } catch (IOException e) {
      throw exceptionWithCause("close Apache HTTP client", e);
    }
  }

  public void setBaseUrl(String baseUrl) {
    if (baseUrl!=null) {
      this.baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
    } else {
      this.baseUrl = null;
    }
  }

  public HttpClient baseUrl(String baseUrl) {
    setBaseUrl(baseUrl);
    return this;
  }


  public String getBaseUrl() {
    return baseUrl;
  }

  public Serializer getSerializer() {
    return serializer;
  }

  public void setSerializer(Serializer serializer) {
    this.serializer = serializer;
  }

  public HttpClient serializer(Serializer serializer) {
    this.serializer = serializer;
    return this;
  }

  public CloseableHttpClient getApacheHttpClient() {
    return apacheHttpClient;
  }

  public void setApacheHttpClient(CloseableHttpClient apacheHttpClient) {
    this.apacheHttpClient = apacheHttpClient;
  }
}
