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
package be.tombaeyens.magicless.routerservlet;

import ai.shape.magicless.app.util.Http;
import org.slf4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

public class ServerResponse {

  ServerRequest serverRequest;
  HttpServletResponse response;
  String bodyLog;

  public ServerResponse(ServerRequest serverRequest, HttpServletResponse response) {
    this.serverRequest = serverRequest;
    this.response = response;
  }

  public ServerResponse statusOk() {
    return status(200);
  }

  public ServerResponse statusNotFound() {
    return status(404);
  }

  public ServerResponse statusInternalServerError() {
    return status(500);
  }

  public ServerResponse status(int status) {
    this.response.setStatus(status);
    return this;
  }

  public ServerResponse bodyBytes(byte[] bytes) {
    if (bytes!=null) {
      bodyBytes(bytes, "..." + bytes.length + " bytes...");
    }
    return this;
  }

  public ServerResponse bodyBytes(byte[] bytes, String bodyLog) {
    if (bytes!=null) {
      writeBodyBytes(bytes);
    }
    setBodyLog(bodyLog);
    return this;
  }

  private void writeBodyBytes(byte[] bytes) {
    try {
      headerContentLength(bytes.length);
      ServletOutputStream out = response.getOutputStream();
      out.write(bytes);
      out.flush();
    } catch (IOException e) {
      throw new RuntimeException("Couldn't send body: "+e.getMessage(), e);
    }
  }

  public ServerResponse bodyString(String responseBody) {
    return bodyString(responseBody, responseBody);
  }

  public ServerResponse bodyString(String responseBody, String bodyLog) {
    byte[] bytes = responseBody.getBytes(Charset.forName("UTF-8"));
    setBodyLog(bodyLog);
    writeBodyBytes(bytes);
    return this;
  }

  public ServerResponse bodyJsonString(String responseBody) {
    headerContentTypeApplicationJson();
    bodyString(responseBody);
    return this;
  }

  public void setBodyLog(String bodyLog) {
    this.bodyLog = bodyLog;
  }

  public ServerResponse header(String name, String value) {
    if (response.isCommitted()) {
      throw new RuntimeException("Bug: headers need to be set before the body is sent");
    }
    if (name!=null && value!=null) {
      response.addHeader(name, value);
    }
    return this;
  }

  public ServerResponse headerContentLength(Number value) {
    return header(Http.Headers.CONTENT_LENGTH, value!=null ? value.toString() : null);
  }

  public ServerResponse headerContentType(String value) {
    return header(Http.Headers.CONTENT_TYPE, value);
  }

  public ServerResponse headerContentTypeTextPlain() {
    return header(Http.Headers.CONTENT_TYPE, Http.ContentTypes.TEXT_PLAIN);
  }

  public ServerResponse headerContentTypeApplicationJson() {
    return header(Http.Headers.CONTENT_TYPE, Http.ContentTypes.APPLICATION_JSON);
  }

  public ServerResponse headerContentTypeTextHtml() {
    return header(Http.Headers.CONTENT_TYPE, Http.ContentTypes.TEXT_HTML);
  }

  /** invoked at the end of {@link RouterServlet#service(HttpServletRequest, HttpServletResponse)} */
  public void logTo(Logger log) {
    // Log status line
    log.debug("< " + serverRequest.getRequest().getProtocol() + " " +
              response.getStatus() + " " +
              Http.ResponseCodes.getText(response.getStatus()));

//    // Log response headers
//    if (response.getHeaderNames()!=null && !response.getHeaderNames().isEmpty()) {
//      // Log headers
//      response.getHeaderNames().stream()
//        .forEach(headerName->{
//          response.getHeaders(headerName).stream()
//            .forEach(headerValue->{
//              log.debug("  "+headerName+": "+headerValue);
//            });
//        });
//    }

    // Log body
    if (bodyLog!=null) {
      BufferedReader reader = new BufferedReader(new StringReader(bodyLog));
      reader.lines().forEach(line->log.debug("  "+line));
    }
  }

  public void sendRedirect(String location) {
    try {
      response.sendRedirect(location);
    } catch (IOException e) {
      throw new InternalServerException();
    }
  }

  public ServletResponse getResponse() {
    return response;
  }

  /**
   * @param maxAge an integer specifying the maximum age of the cookie in seconds
   */
  public void setCookie(String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setMaxAge(maxAge); // expiration
    response.addCookie(cookie);
  }
}
