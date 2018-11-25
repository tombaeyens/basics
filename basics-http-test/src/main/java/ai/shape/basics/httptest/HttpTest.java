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
package ai.shape.basics.httptest;

import ai.shape.basics.httpclient.ClientRequest;
import ai.shape.basics.httpclient.ClientResponse;
import ai.shape.basics.httpclient.HttpClient;
import ai.shape.basics.httpclient.Serializer;
import ai.shape.basics.httpserver.HttpServer;
import ai.shape.basics.routerservlet.RouterServlet;
import org.junit.Before;

/** Base test class for testing a HTTP server.
 *
 * There are convenience methods to build and execute HTTP requests:
 * {@link #newGet(String)}, {@link #newPost(String)}, {@link #newPut(String)},
 * {@link #newDelete(String)}, etc.
 * HTTP requests are executed synchronous, which is convenient for testing
 * and debugging purposes.
 *
 * The cool feature is that server side exceptions are captured and added
 * as the cause to the client side exceptions when asserting the http
 * response status with {@link ClientResponse#assertStatusOk()} etc.
 *
 * Usage see example/src/test/java/be/tombaeyens/magicless/ExampleTest.java */
public abstract class HttpTest {

  protected static HttpClient httpClient = null;
  protected static Throwable latestServerException = null;

  @Before
  public void setUp() {
    if (httpClient==null) {
      setUpStatic();
    }
    // This ensures that no exceptions will pass through from the previous
    // test to this test because latestServerException is static
    latestServerException = null;
  }

  /** performs 'static' initializations and is called only once before
   * the first test starts.  It's called from inside the setUp.
   * Caution, when using statics ensure that no interference between
   * tests occurs through the statics.  They should be stateless so
   * that no interference can occur between tests. */
  protected void setUpStatic() {
    HttpServer httpServer = createHttpServer();
    httpServer
      .getServlet(RouterServlet.class)
      .setExceptionListener(new ServerExceptionListener());

    httpClient = createHttpClient(httpServer);
  }

  protected TestHttpClient createHttpClient(HttpServer httpServer) {
    TestHttpClient httpClient = new TestHttpClient();
    httpClient.setBaseUrl("http://localhost:" + httpServer.getPort());
    httpClient.setSerializer(createSerializer());
    return httpClient;
  }

  protected Serializer createSerializer() {
    return null;
  }

  /** Called once before the first test is executed and can be used
   * to initialize static fields.  Ensure that those static fields
   * do not carry state from one test to another. */
  public abstract HttpServer createHttpServer();

  /** Starts building a new GET request.
   * The path is relative to the root like eg newGet("/hello").
   * The request is performed against the server that was created in the {@link #createHttpServer()}.
   * The base url is "http://localhost:" + serverPort */
  public ClientRequest newGet(String url) {
    return httpClient.newGet(url);
  }

  /** Starts building a new POST request.
   * The path is relative to the root like eg newGet("/hello").
   * The request is performed against the server that was created in the {@link #createHttpServer()}.
   * The base url is "http://localhost:" + serverPort */
  public ClientRequest newPost(String url) {
    return httpClient.newPost(url);
  }

  /** Starts building a new PUT request.
   * The path is relative to the root like eg newGet("/hello").
   * The request is performed against the server that was created in the {@link #createHttpServer()}.
   * The base url is "http://localhost:" + serverPort */
  public ClientRequest newPut(String url) {
    return httpClient.newPut(url);
  }

  /** Starts building a new DELETE request.
   * The path is relative to the root like eg newGet("/hello").
   * The request is performed against the server that was created in the {@link #createHttpServer()}.
   * The base url is "http://localhost:" + serverPort */
  public ClientRequest newDelete(String url) {
    return httpClient.newDelete(url);
  }
}
