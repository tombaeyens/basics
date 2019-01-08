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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/** In contrast to the normal request handlers, a new OptionsHandler is
 * created for each OPTIONS request.  For other RequestHandler's, only
 * one object is used for all requests. */
public class OptionsHandler implements RequestHandler {

  protected List<String> allowedMethods = new ArrayList<>();

  public void addAllowedMethod(String method) {
    allowedMethods.add(method);
  }

  @Override
  public String method() {
    return Http.Methods.OPTIONS;
  }

  @Override
  public boolean pathMatches(ServerRequest request) {
    return true;
  }

  @Override
  public void handle(ServerRequest request, ServerResponse response) {
    // We assume that whatever headers the client requests is fine so we just copy all in the Access-Control-Allow-Headers
    String allowedHeaders = request.getHeader(Http.Headers.ACCESS_CONTROL_REQUEST_HEADERS);
    String allowedMethods = this.allowedMethods.stream().collect(Collectors.joining(", "));

    response
      .statusOk()
      .header(Http.Headers.ACCESS_CONTROL_ALLOW_METHODS, allowedMethods)
      .header(Http.Headers.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders)
      .headerContentType(Http.ContentTypes.TEXT_PLAIN);
  }
}
