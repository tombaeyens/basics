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

public abstract class PathRequestHandler implements RequestHandler {

  public static final String GET = Http.Methods.GET;
  public static final String POST = Http.Methods.POST;
  public static final String PUT = Http.Methods.PUT;
  public static final String DELETE = Http.Methods.DELETE;
  public static final String OPTIONS = Http.Methods.OPTIONS;

  private String method;
  private Path path;

  /** Use method constants {@link #GET}, {@link #PUT}, {@link #POST} and {@link #DELETE}.
   * pathTemplate examples:
   * "/"
   * "/hello"
   * "/products/{productId} -> productId is made available through {@link ServerRequest#getPathParameter(String)} */
  protected PathRequestHandler(String method, String pathTemplate) {
    this.method = method;
    this.path = new Path(pathTemplate);
  }

  @Override
  public String method() {
    return method;
  }

  @Override
  public boolean pathMatches(ServerRequest request) {
    return path.matches(request);
  }
}
