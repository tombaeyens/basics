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
import ai.shape.basics.util.Io;
import ai.shape.basics.util.Sets;

import java.util.Map;
import java.util.Set;

import static ai.shape.basics.util.Maps.entry;
import static ai.shape.basics.util.Maps.hashMap;


public class ResourceRequestHandler implements RequestHandler {

  private static final String REQUEST_CONTEXT_KEY_RESOURCE_PATH = "resourcePath";

  String basePath;
  Map<String, String> contentTypesByExtension;
  Set<String> indexFileNames;
  /** The resource returned when the path is not found (Optional).
   * This can be used in single page applications that want the index.html returned for URLs that don't match any resources.
   * When the path is not found and notFoundResourceName is specified, then the response status will be 200 OK.
   * When the path is not found and notFoundResourceName is not specified, this request handler will not match. */
  String notFoundResourceName;

  public ResourceRequestHandler(String basePath) {
    this(basePath, createDefaultExtensions(), createDefaultIndexFileNames());
  }

  public static Set<String> createDefaultIndexFileNames() {
    return Sets.hashSet(
      "index.html"
    );
  }

  public ResourceRequestHandler(String basePath, Map<String, String> contentTypesByExtension, Set<String> indexFileNames) {
    this.basePath = basePath;
    this.contentTypesByExtension = contentTypesByExtension;
    this.indexFileNames = indexFileNames;
  }

  public static Map<String, String> createDefaultExtensions() {
    return hashMap(
      entry("html", "text/html"),
      entry("css", "text/css"),
      entry("json", "application/json"),
      entry("svg", "image/svg+xml"),
      entry("jpg", "image/jpeg")
    );
  }

  @Override
  public String method() {
    return Http.Methods.GET;
  }

  @Override
  public boolean pathMatches(ServerRequest request) {
    String resourcePath = basePath + request.getPathInfo();
    if (resourcePath.endsWith("/")) {
      for (String indexFileName: indexFileNames) {
        String indexResourceName = resourcePath + indexFileName;
        if (Io.hasResource(indexResourceName)) {
          request.setContextObject(REQUEST_CONTEXT_KEY_RESOURCE_PATH, indexResourceName);
          return true;
        }
      }
    }
    if (Io.hasResource(resourcePath)) {
      request.setContextObject(REQUEST_CONTEXT_KEY_RESOURCE_PATH, resourcePath);
      return true;
    }
    if (notFoundResourceName!=null) {
      request.setContextObject(REQUEST_CONTEXT_KEY_RESOURCE_PATH, basePath+"/"+notFoundResourceName);
      return true;
    }
    return false;
  }

  @Override
  public void handle(ServerRequest request, ServerResponse response) {
    String resourcePath = request.getContextObject(REQUEST_CONTEXT_KEY_RESOURCE_PATH);
    response.headerContentType(getContentType(resourcePath));
    byte[] resourceBytes = Io.getResourceAsBytes(resourcePath);
    response.bodyBytes(resourceBytes);
  }

  private String getContentType(String resourcePath) {
    int lastDotIndex = resourcePath.lastIndexOf('.');
    if (lastDotIndex!=-1 && lastDotIndex<resourcePath.length()-2) {
      String extension = resourcePath.substring(lastDotIndex+1);
      return contentTypesByExtension.get(extension);
    }
    return null;
  }

  /** The resource returned when the path is not found (Optional).
   * This can be used in single page applications that want the index.html returned for URLs that don't match any resources.
   * When the path is not found and notFoundResourceName is specified, then the response status will be 200 OK.
   * When the path is not found and notFoundResourceName is not specified, this request handler will not match. */
  public ResourceRequestHandler notFoundResourceName(String notFoundResourceName) {
    this.notFoundResourceName = notFoundResourceName;
    return this;
  }
}
