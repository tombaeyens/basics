/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.test;

import ai.shape.magicless.app.container.Container;
import ai.shape.magicless.app.container.Initialize;
import ai.shape.magicless.app.container.Start;
import ai.shape.magicless.app.container.Stop;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ContainerTest {

  @SuppressWarnings("rawtypes")
  public static class TestComponent {
    int initializedCount = 0;
    int startedCount = 0;
    int stoppedCount = 0;
    @Initialize
    public void initialize() {
      initializedCount++;
    }
    @Start
    public void start() {
      startedCount++;
    }
    @Stop
    public void stop() {
      stoppedCount++;
    }
  }

  @Test
  public void testPlainObjectRetrievalByType() {
    final LinkedHashMap<Object, Object> component = new LinkedHashMap<>();
    Container container = new Container() {
      {
        add(component);
      }
    };
    assertThat(container.getOpt(LinkedHashMap.class), sameInstance(component));
    assertThat(container.getOpt(HashMap.class), sameInstance(component));
    assertThat(container.getOpt(Map.class), sameInstance(component));
    assertThat(container.getOpt(Cloneable.class), sameInstance(component));
  }

  @Test
  public void testPlainObjectRetrievalByName() {
    final Map<String,Object> linked = new LinkedHashMap<>();
    final Map<String,Object> tree = new TreeMap<>();
    Container container = new Container() {
      {
        add("linked", linked);
        add("tree", tree);
      }
    };
    assertThat(container.getOpt("linked"), sameInstance(linked));
    assertThat(container.getOpt("tree"), sameInstance(tree));
    assertThat(container.getAll(LinkedHashMap.class), is(new Object[]{linked}));
    assertThat(container.getAll(TreeMap.class), is(new Object[]{tree}));
    assertThat(container.getAll(Map.class), is(new Object[]{tree, linked}));
    assertThat(container.getAll(), is(new Object[]{tree, linked}));
  }

  @Test
  public void testContainerLifecycleCallbacks() {
    final TestComponent testComponent = new TestComponent();
      Container container = new Container() {
      {
        add(testComponent);
        assertThat(testComponent.initializedCount, is(0));
        assertThat(testComponent.startedCount,     is(0));
        assertThat(testComponent.stoppedCount,     is(0));
      }
    };

    // This initializes the test component
    container.get(TestComponent.class);

    assertThat(testComponent.initializedCount, is(1));
    assertThat(testComponent.startedCount,     is(0));
    assertThat(testComponent.stoppedCount,     is(0));

    container.start();

    assertThat(testComponent.initializedCount, is(1));
    assertThat(testComponent.startedCount,     is(1));
    assertThat(testComponent.stoppedCount,     is(0));

    container.stop();

    assertThat(testComponent.initializedCount, is(1));
    assertThat(testComponent.startedCount,     is(1));
    assertThat(testComponent.stoppedCount,     is(1));
  }

  @Test
  public void testUnavailableObject() {
    Container container = new Container();
    container.getOpt("cleanplanet");
  }
}
