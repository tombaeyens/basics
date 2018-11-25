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

package be.tombaeyens.magicless.httptest;

import be.tombaeyens.magicless.httpclient.ClientRequest;
import be.tombaeyens.magicless.httpclient.ClientResponse;

import java.io.IOException;

import static be.tombaeyens.magicless.httptest.HttpTest.latestServerException;

public class TestClientResponse extends ClientResponse {

  protected TestClientResponse(ClientRequest request) throws IOException {
    super(request);
  }

  @Override
  protected AssertionError createStatusException(int expectedStatus) {
    if (latestServerException!=null) {
      return new AssertionError(
        "Status was " +
        this.status +
        ", expected " +
        expectedStatus +
        (latestServerException!=null ? ": Server says: "+latestServerException.toString() : ""), latestServerException);
    } else {
      return super.createStatusException(expectedStatus);
    }
  }
}
