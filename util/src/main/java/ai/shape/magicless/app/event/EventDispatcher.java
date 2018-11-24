/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */

package ai.shape.magicless.app.event;

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
    eventListeners.add(new EventFilter(eventListener));
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
