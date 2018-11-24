/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.test;

import ai.shape.magicless.app.container.Container;
import ai.shape.magicless.app.container.Inject;
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
