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
package ai.shape.magicless.app.event;

import ai.shape.magicless.app.util.Lists;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static ai.shape.magicless.app.util.Exceptions.assertTrue;

public class EventDispatcher {

  List<EventListener<Event>> eventListeners = new ArrayList<>();

  /** usage: adding an object that
   * implements EventListener<MyEventClass> will
   * automatically filter on MyEventClass */
  public EventDispatcher add(EventListener<?> eventListener) {
    // We make a copy because we don't want to change the
    // exising eventListeners list.  It's possible that this occurs
    // inside a dispatch if some event handler adds a listener.
    // And without the copy, we'ld get a ConcurrentModificationException.
    // For performance reasons, we make the copy here instead of in the
    // dispatch method itself.
    this.eventListeners = new ArrayList<>(eventListeners);
    this.eventListeners.add(new EventFilter(eventListener));
    return this;
  }

  public void dispatch(Event event) {
    for (EventListener<Event> eventListener: eventListeners) {
      eventListener.event(event);
    }
  }

  static class EventFilter implements EventListener<Event> {
    Class<?> eventType;
    EventListener<?> eventListener;
    public EventFilter(EventListener<?> eventListener) {
      this.eventType = findEventType(eventListener.getClass());
      this.eventListener = eventListener;
    }
    Class<?> findEventType(Class<?> clazz) {
      for (Type interfaze: clazz.getGenericInterfaces()) {
        if (interfaze instanceof ParameterizedType) {
          ParameterizedType parameterizedType = (ParameterizedType) interfaze;
          Type rawType = parameterizedType.getRawType();
          if (rawType==EventListener.class) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type actualTypeArgument = actualTypeArguments[0];
            assertTrue(actualTypeArgument instanceof Class, "EventListener generic type arg must be a class");
            return (Class<?>)actualTypeArgument;
          }
        }
      }
      Class<?> superclass = clazz.getSuperclass();
      return superclass!=Object.class ? findEventType(superclass) : null;
    }
    @Override
    public void event(Event event) {
      if (eventType.isAssignableFrom(event.getClass())) {
        eventListener.event(castEvent(event));
      }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> T castEvent(Event event) {
      return (T) event;
    }
  }
}
