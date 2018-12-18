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


public class NotFoundException extends HttpException {

  private static final long serialVersionUID = 1L;

  public NotFoundException() {
    super();
  }

  public static void throwIfNull(Object o, String message, Object... args) {
    if (o==null) {
      throw new NotFoundException(String.format(message, args));
    }
  }

  public static void throwIf(boolean condition, String message, Object... args) {
    if (condition) {
      throw new NotFoundException(String.format(message, args));
    }
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(Throwable cause) {
    super(cause);
  }

  @Override
  public int getStatusCode() {
    return 404;
  }
}
