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
package ai.shape.magicless.app.test;

import ai.shape.basics.util.container.Container;
import ai.shape.basics.util.container.Inject;
import org.hamcrest.core.IsSame;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class InjectTest {

  public static class TestComponentA {
    @Inject
    TestComponentB b;
  }

  public static class TestComponentB {
    @Inject
    TestComponentA a;
  }

  @Test
  public void testPlainObjectRetrievalByType() {
    Container container = new Container() {
      {
        add(new TestComponentA());
        add(new TestComponentB());
      }
    };
    assertThat(container.get(TestComponentA.class).b, is(notNullValue()));
    assertThat(container.get(TestComponentA.class).b, new IsSame<>(container.get(TestComponentB.class)));
    assertThat(container.get(TestComponentB.class).a, is(notNullValue()));
    assertThat(container.get(TestComponentB.class).a, new IsSame<>(container.get(TestComponentA.class)));
  }

}
