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
package ai.shape.basics.routerservlet;

import ai.shape.basics.util.Http;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Usage: Create a subclass, and configure it by
 * overriding {@link HttpServlet#init(ServletConfig)}
 * and invoking {@link #requestHandler(RequestHandler)},
 * and {@link #defaultResponseHeader(String, String)}. */
public class RouterServlet extends HttpServlet {

  private static final long serialVersionUID = 5826674732887125068L;

  /** maps methods to list of request paths */
  private List<RequestHandler> requestHandlers = new ArrayList<>();
  private Map<String,List<String>> defaultResponseHeaders;
  protected ExceptionListener exceptionListener;

  @Override
  protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    ServerRequest request = new ServerRequest(servletRequest);
    ServerResponse response = new ServerResponse(request, servletResponse);

    RequestHandler requestHandler = findRequestHandler(request);
    if (requestHandler!=null) {
      request.setRequestHandler(requestHandler);
      try {
        request.logRequest();
        applyDefaultResponseHeaders(response);
        requestHandler.handle(request, response);
      } catch (HttpException e) {
        response.status(e.getStatusCode());
        response.bodyString("{\"message\":\"" + e.getMessage() + "\"}");
        if (exceptionListener!=null) {
          exceptionListener.exception(request, response, e);
        }
      } catch (Throwable e) {
        HttpLogger.log.debug("Problem by "+requestHandler.getClass().getSimpleName()+" for request "+request.getPathInfo(), e);
        response.statusInternalServerError();
        response.bodyString("{\"message\":\"See the server logs for more details\"}");
        if (exceptionListener!=null) {
          exceptionListener.exception(request, response, e);
        }
      }
    } else {
      HttpLogger.log.debug("No handler found for "+request.getPathInfo());
      response.statusNotFound();
    }
    if (HttpLogger.log.isDebugEnabled()) response.logTo(HttpLogger.log);
  }

  private RequestHandler findRequestHandler(ServerRequest request) {
    if (requestHandlers!=null) {
      if (Http.Methods.OPTIONS.equals(request.getMethod())) {
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/OPTIONS
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
        return createOptionsRequestHandler(request);
      } else {
        return findFirstMatchingRequestHandler(request);
      }
    }
    return null;
  }

  private RequestHandler createOptionsRequestHandler(ServerRequest request) {
    OptionsHandler optionsHandler = new OptionsHandler();
    for (RequestHandler requestHandler: requestHandlers) {
      if (requestHandler.pathMatches(request)) {
        optionsHandler.addAllowedMethod(requestHandler.method());
      }
    }
    return optionsHandler;
  }

  private RequestHandler findFirstMatchingRequestHandler(ServerRequest request) {
    for (RequestHandler requestHandler: requestHandlers) {
      if (requestHandler.method().equals(request.getMethod())
        && requestHandler.pathMatches(request)) {
        return requestHandler;
      }
    }
    return null;
  }

  public RouterServlet requestHandler(RequestHandler requestHandler) {
    if (requestHandler!=null) {
      requestHandlers.add(requestHandler);

    }
    return this;
  }

  /** default response headers are added after the request handler has
   * returned without throwing exceptions */
  public RouterServlet defaultResponseHeader(String name, String value) {
    if (defaultResponseHeaders==null) {
      defaultResponseHeaders = new LinkedHashMap<>();
    }
    List<String> values = defaultResponseHeaders
      .computeIfAbsent(name, key->new ArrayList<String>());
    values.add(value);
    return this;
  }

  public Map<String,List<String>> getDefaultResponseHeaders() {
    return defaultResponseHeaders;
  }

  private void applyDefaultResponseHeaders(ServerResponse response) {
    if (defaultResponseHeaders!=null) {
      defaultResponseHeaders.forEach((name,values)->{
        if (values!=null) {
          values.forEach(value->{
            response.header(name, value);
          });
        }
      });
    }
  }

  public ExceptionListener getExceptionListener() {
    return this.exceptionListener;
  }
  public void setExceptionListener(ExceptionListener exceptionListener) {
    this.exceptionListener = exceptionListener;
  }
  public RouterServlet exceptionListener(ExceptionListener exceptionListener) {
    this.exceptionListener = exceptionListener;
    return this;
  }
}
