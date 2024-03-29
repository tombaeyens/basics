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
import ai.shape.basics.util.Io;
import ai.shape.basics.util.Log;
import ai.shape.basics.util.ParameterType;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ai.shape.basics.util.Os.NEWLINE;


/** Obtain a response by starting from the {@link HttpClient} object
 * use one of the newXxx methods to get a request and then
 * invoke {@link ClientRequest#execute()} */
public class ClientResponse {

  /**
   * transient because this field should not be serialized by gson
   */
  transient ClientRequest request;
  /**
   * transient because this field should not be serialized by gson
   */
  transient CloseableHttpResponse apacheResponse;

  protected int status;
  protected Map<String, List<String>> headers;
  protected String body;

  protected ClientResponse(ClientRequest request) throws IOException {
    this.request = request;
    this.apacheResponse = request.httpClient.apacheHttpClient.execute(request.apacheRequest);
    try {
      this.status = apacheResponse.getStatusLine().getStatusCode();
      this.headers = extractHeaders(apacheResponse);

      HttpEntity entity = apacheResponse.getEntity();
      if (entity != null) {
        try {
          String charset = getContentTypeCharset("UTF-8");
          InputStream content = entity.getContent();
          this.body = Io.getString(content, charset);
        } catch (Exception e) {
          throw new RuntimeException("Couldn't ready body/entity from http request " + toString(), e);
        }
      }
    } finally {
      apacheResponse.close();
    }
  }

  static Map<String, List<String>> extractHeaders(org.apache.http.HttpResponse apacheResponse) {
    Map<String, List<String>> headers = new LinkedHashMap<>();
    Header[] allHeaders = apacheResponse.getAllHeaders();
    if (allHeaders != null) {
      for (Header header : allHeaders) {
        headers
          .computeIfAbsent(header.getName(), key -> new ArrayList<>())
          .add(header.getValue());
      }
    }
    return headers;
  }

  public ClientResponse log() {
    Log.logLines(HttpClient.log, toString(), "");
    return this;
  }

  @Override
  public String toString() {
    return toString(null);
  }

  public String toString(String prefix) {
    return toString(prefix, Integer.MAX_VALUE);
  }

  public String toString(String prefix, int maxBodyLength) {
    if (prefix == null) {
      prefix = "";
    }

    StringBuilder text = new StringBuilder();
    text.append(prefix);
    text.append("< ");
    text.append(apacheResponse.getStatusLine());
    if (headers != null) {
      for (String headerName : headers.keySet()) {
        if (headerName != null) {
          List<String> headerListValue = headers.get(headerName);
          String headerValue = headerListToString(headerListValue);
          text.append(NEWLINE);
          text.append(prefix);
          text.append("  ");
          text.append(headerName);
          text.append(": ");
          text.append(headerValue);
        }
      }
    } else {
      text.append(NEWLINE);
      text.append(prefix);
      text.append("< ");
      text.append(status);
    }
    if (body!=null) {
      text.append(NEWLINE);
      text.append(prefix);
      text.append("  ");
      String bodyCustomized = getString(body, prefix, maxBodyLength);
      text.append(bodyCustomized);
    }

    return text.toString();
  }

  static String getString(String bodyText, String prefix, int maxBodyLength) {
    return new BufferedReader(new StringReader(bodyText)).lines()
      .map(line -> (line.length() > maxBodyLength ? line.substring(0, maxBodyLength) + "..." : line))
      .collect(Collectors.joining("\n" + (prefix != null ? prefix + "  " : "  ")));
  }

  public static String headerListToString(List<String> headerListValue) {
    return headerListValue
      .stream()
      .collect(Collectors.joining(";"));
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public ClientResponse assertStatusOk() {
    return assertStatus(Http.ResponseCodes.OK_200);
  }
  public boolean isStatusOk() {
    return status==Http.ResponseCodes.OK_200;
  }

  public ClientResponse assertStatusBadRequest() {
    return assertStatus(Http.ResponseCodes.BAD_REQUEST_400);
  }
  public boolean isStatusBadRequest() {
    return status==Http.ResponseCodes.BAD_REQUEST_400;
  }

  public ClientResponse assertStatusNotFound() {
    return assertStatus(Http.ResponseCodes.NOT_FOUND_404);
  }
  public boolean isStatusNotFound() {
    return status==Http.ResponseCodes.NOT_FOUND_404;
  }

  public ClientResponse assertStatusInternalServerError() {
    return assertStatus(Http.ResponseCodes.INTERNAL_SERVER_ERROR_500);
  }
  public boolean isStatusInternalServerError() {
    return status==Http.ResponseCodes.INTERNAL_SERVER_ERROR_500;
  }

  public ClientResponse assertStatusCreated() {
    return assertStatus(Http.ResponseCodes.CREATED_201);
  }
  public boolean isStatusCreated() {
    return status==Http.ResponseCodes.CREATED_201;
  }

  public ClientResponse assertStatusNoContent() {
    return assertStatus(Http.ResponseCodes.NO_CONTENT_204);
  }
  public boolean isStatusNoContent() {
    return status==Http.ResponseCodes.NO_CONTENT_204;
  }

  public ClientResponse assertStatus(int expectedStatus) {
    if (status != expectedStatus) {
      throw createStatusException(expectedStatus);
    }
    return this;
  }

  protected AssertionError createStatusException(int expectedStatus) {
    return new AssertionError("Status was " + status + ", expected " + expectedStatus);
  }

  public String getBody() {
    return body;
  }

  public void writeBodyTo(Writer writer) {
    HttpEntity entity = apacheResponse.getEntity();
    if (entity != null) {
      try {
        String charset = getContentTypeCharset("UTF-8");
        InputStream content = entity.getContent();
        Io.readFrom(new InputStreamReader(content, getContentTypeCharset("UTF-8")))
          .description("read from response body")
          .read(reader->{
            Io.transfer(reader, writer);
          });
      } catch (Exception e) {
        throw new RuntimeException("Couldn't ready body/entity from http request " + toString(), e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getBodyAs(Type type, Type... genericTypeArgs) {
    Type combinedType = genericTypeArgs!=null && genericTypeArgs.length>0
      ? new ParameterType(type, genericTypeArgs)
      : type;
    String body = getBody();
    return (T) request
      .getHttpClient()
      .getSerializer()
      .deserialize(body, combinedType);
  }

  @SuppressWarnings("unchecked")
  public <T> T getBodyAs(Class<T> type) {
    return getBodyAs((Type) type);
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public boolean isContentTypeApplicationJson() {
    return headerContains(Http.Headers.CONTENT_TYPE, Http.ContentTypes.APPLICATION_JSON)
      || headerContains(Http.Headers.CONTENT_TYPE, Http.ContentTypes.APPLICATION_LD_JSON);
  }

  public boolean headerContains(String headerName, String headerValue) {
    if (headers == null) {
      return false;
    }
    List<String> headerValues = headers.get(headerName);
    if (headerValues == null) {
      return false;
    }
    for (String actualValue : headerValues) {
      if (actualValue.contains(headerValue)) {
        return true;
      }
    }
    return false;
  }

  public ClientRequest getRequest() {
    return request;
  }

  public CloseableHttpResponse getApacheResponse() {
    return apacheResponse;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public List<String> getHeader(String headerName) {
    if (headers != null) {
      return headers.get(headerName);
    }
    return null;
  }

  public String getContentTypeCharset(String defaultCharset) {
    List<String> values = getHeader(Http.Headers.CONTENT_TYPE);
    if (values != null) {
      for (String value : values) {
        HeaderElement[] headerElements = BasicHeaderValueParser.parseElements(value, (HeaderValueParser) null);
        if (headerElements != null && headerElements.length > 0) {
          NameValuePair charsetPair = headerElements[0].getParameterByName("charset");
          if (charsetPair != null) {
            return charsetPair.getValue();
          }
        }
      }
    }
    return defaultCharset;
  }
}