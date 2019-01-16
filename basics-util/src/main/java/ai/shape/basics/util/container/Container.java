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
package ai.shape.basics.util.container;

import ai.shape.basics.util.Exceptions;
import ai.shape.basics.util.Reflection;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Container for making application context components configurable and
 * accessible.
 *
 * Lifecycle: (instantiate)--> creating --.initialize()--> initialized
 * --.start()--> started --.stop()--> stopped
 *
 * The container can only once be started and stopped.
 *
 * Usage: Make your application context inherit from Container. Eg MyAppContext
 * extends Container Then in the constructor, use the add and addFactory methods
 * to add components and factories to the container and at the end of the
 * constructor, call initialize();
 */
@SuppressWarnings("unchecked")
public class Container {

  private static final Logger log = getLogger(Container.class.getName());

  private static enum ContainerState {
    NOT_STARTED, STARTING, STARTED, STOPPING, STOPPED
  }

  /**
   * list of components by name. multiple names might point to the same component.
   */
  Map<String, Component> componentsByName = new LinkedHashMap<>();

  /**
   * list of unique components, so you can get a list of components in the order
   * as they are added.
   */
  List<Component> components = new ArrayList<>();
  List<Component> startables = new ArrayList<>();
  List<Component> stoppables = new ArrayList<>();

  ContainerState containerState = ContainerState.NOT_STARTED;

  LinkedList<Component> componentsToInitialize = null;
  LinkedList<Object> initializationPath = null;

  private static enum ComponentState {
    /** Ordering matters because .ordinal() is used on these enum values */
    NOT_CREATED, CREATED, INITIALIZING, INITIALIZED, STARTING, STARTED, STOPPING, STOPPED
  }

  private class Component {
    /** class of the object that the factory produces */
    Class<?> factoryObjectClass;
    Factory factory;
    Object object;
    ComponentState componentState;
    public Component(Object object) {
      this.object = object;
      this.componentState = ComponentState.CREATED;
    }
    public Component(Class<?> objectClass, Factory factory) {
      this.factoryObjectClass = objectClass;
      this.factory = factory;
      this.componentState = ComponentState.NOT_CREATED;
    }
    private Object getCreated() {
      if (componentState==ComponentState.NOT_CREATED) {
        object = factory.create(Container.this);
      }
      componentState = ComponentState.CREATED;
      return object;
    }
    public boolean isInitialized() {
      return componentState.ordinal()>=ComponentState.INITIALIZED.ordinal();
    }
    public boolean isInstanceOf(Class<?> classFilter) {
      return (object!=null && classFilter.isAssignableFrom(object.getClass()))
             || (factoryObjectClass!=null && classFilter.isAssignableFrom(factoryObjectClass));
    }
    public String toString() {
      return object!=null ? object.getClass().getSimpleName() : factory!=null ? factory.getClass().getSimpleName() : "?";
    }
    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
      Class<?> clazz = object!=null ? object.getClass() : factory!=null ? factory.getClass() : null;
      return clazz!=null && Reflection.hasAnnotationRecursive(clazz, annotation);
    }
  }

  public Container add(Object object) {
    if (object!=null) {
      Component component = new Component(object);
      addByClass(object.getClass(), component);
    }
    return this;
  }

  public Container add(String name, Object object) {
    if (name!=null) {
      add(name, new Component(object));
    }
    return this;
  }

  public Container addModule(Module module) {
    module.add(this);
    return this;
  }

  /**
   * add a factory to create an object of a given clazz when it is retrieved from
   * the container
   */
  public Container addFactory(Class<?> clazz, Factory factory) {
    if (clazz!=null && factory!=null) {
      addByClass(clazz, new Component(clazz, factory));
    }
    return this;
  }

  public Container addFactory(String name, Factory factory) {
    if (name!=null && factory!=null) {
      add(name, new Component(null, factory));
    }
    return this;
  }

  private void addByClass(Class<?> clazz, Component component) {
    add(clazz.getName(), component);
    for (Class<?> interfaze : clazz.getInterfaces()) {
      addByClass(interfaze, component);
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != Object.class && superclass != null) {
      addByClass(superclass, component);
    }
  }

  private void add(String name, Component component) {
    componentsByName.put(name, component);
    addComponent(component);
  }

  /**
   * true if the component is already in the list of objects using == (not
   * .equals()).
   */
  private void addComponent(Component component) {
    if (!containsComponent(component)) {
      components.add(component);

      if (component.hasAnnotation(Start.class)) {
        startables.add(component);
      }
      if (component.hasAnnotation(Stop.class)) {
        stoppables.add(component);
      }

      if (containerState== ContainerState.STARTED) {
        if (component.hasAnnotation(Start.class)) {
          getOpt(component, component.toString());
          start(component);
        }
      }
    }
  }

  private boolean containsComponent(Component componentToAdd) {
    for (Component component : components) {
      if (component == componentToAdd) {
        return true;
      }
    }
    return false;
  }

  public <T> T getOpt(Class<?> clazz) {
    if (clazz==null) {
      return null;
    }
    return getOpt(clazz.getName());
  }

  public <T> T getOpt(String name) {
    if (name==null) {
      return null;
    }
    Component component = componentsByName.get(name);
    if (component==null) {
      return null;
    }
    return getOpt(component, name);
  }

  private <T> T getOpt(Component component, Object pathElement) {
    if (component.componentState.ordinal()>= ComponentState.INITIALIZED.ordinal()) {
      return (T) component.object;
    }
    Object object = component.getCreated();
    if (component.componentState== ComponentState.INITIALIZING) {
      throw new RuntimeException("Circular dependency: "+getInitializationPathString());
    }

    boolean initializationPathCreated = initializationPathAdd(pathElement);
    try {
      if (componentsToInitialize!=null) {
        // log.debug("Adding to initialize "+component);
        componentsToInitialize.add(component);

      } else {
        componentsToInitialize = new LinkedList<Component>();
        componentsToInitialize.add(component);

        while (!componentsToInitialize.isEmpty()) {
          Component nextToInitialize = componentsToInitialize.removeFirst();
          initializeComponent(nextToInitialize);
        }
        componentsToInitialize = null;
      }

    } finally {
      initializationPathRemove(initializationPathCreated);
    }


    return (T) object;
  }

  /** performs injections & calls @Initialize methods on the given object.
   * The object is not added to the container. @Start methods are not invoked. */
  public void initialize(Object object) {
    initializeComponent(new Component(object));
  }

  /** returns true if the initialization path was initialized */
  private boolean initializationPathAdd(Object pathElement) {
    boolean initializationPathCreated = initializationPath==null;
    if (initializationPathCreated) {
      initializationPath = new LinkedList<>();
    }
    initializationPath.add(pathElement);
    return initializationPathCreated;
  }

  private void initializationPathRemove(boolean initializationPathCreated) {
    if (initializationPathCreated) {
      initializationPath = null;
    }
  }

  private String getInitializationPathString() {
    return initializationPath
      .stream()
      .map(o->o.toString())
      .collect(Collectors.joining(" -> "));
  }

  private Object getCreated(Class<?> clazz) {
    return getCreated(clazz.getName());
  }

  private Object getCreated(String name) {
    Component component = componentsByName.get(name);
    return component!=null ? component.getCreated() : null;
  }

  private void initializeComponent(Component component) {
    // log.debug("Initializing "+component);
    component.componentState = ComponentState.INITIALIZING;
    Object object = component.getCreated();
    inject(object, object.getClass());
    invoke(Initialize.class, object);
    component.componentState = ComponentState.INITIALIZED;
    // log.debug("Initializing "+component+" done");
  }

  @SuppressWarnings("deprecation")
  private void inject(Object object, Class<?> clazz) {
    for (Field field : clazz.getDeclaredFields()) {
      Object dependency = null;
      Inject inject = field.getAnnotation(Inject.class);
      if (inject != null) {
        String dependencyName = inject.value();
        boolean required = inject.required();
        if (!"".equals(dependencyName)) {
          dependency = getOpt(dependencyName);
        } else {
          dependency = getOpt(field.getType());
          dependencyName = field.toString();
        }
        if (dependency==null && required) {
          initializationPath.add(dependencyName);
          throw new RuntimeException("Required dependency not found: "+getInitializationPathString());
        }
        try {
          if (!field.isAccessible()) {
            field.setAccessible(true);
          }
          // log.debug("Injecting "+field+" = "+dependency);
          field.set(object, dependency);
        } catch (IllegalAccessException e) {
          throw Exceptions.exceptionWithCause("inject field " + field, e);
        }
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != Object.class) {
      inject(object, superclass);
    }
  }

  private <T extends Annotation> void invoke(Class<T> annotationClass, Object object) {
    invoke(annotationClass, object, object.getClass());
  }

  @SuppressWarnings("deprecation")
  private <T extends Annotation> void invoke(Class<T> annotationClass, Object object, Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.getAnnotation(annotationClass) != null) {
        Object[] args = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length>0) {
          args = new Object[parameterTypes.length];
          for (int i=0; i< parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            try {
              initializationPath.add(method.toString()+"(arg "+i+" "+parameterType.getSimpleName()+")");
              args[i] = getOpt(parameterType);
            } finally {
              initializationPath.removeLast();
            }
          }
        }

        try {
          if (!method.isAccessible()) {
            method.setAccessible(true);
          }
          // log.debug("Invoking "+annotationClass.getSimpleName()+" method "+method);
          method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
          Throwable cause =  e instanceof InvocationTargetException
            ? ((InvocationTargetException)e).getTargetException()
            : e;
          if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          }
          throw Exceptions.exceptionWithCause(" write initialize method " + method, cause);
        }
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != Object.class) {
      invoke(annotationClass, object, superclass);
    }
  }

  public <T> T get(String name) {
    T component = getOpt(name);
    if (component == null) {
      throw new RuntimeException("Component " + name + " not found");
    }
    return component;
  }

  public <T> T get(Class<T> clazz) {
    T component = getOpt(clazz);
    if (component == null) {
      throw new RuntimeException("Component " + clazz.getName() + " not found:\n"+
        componentsByName.keySet().stream().collect(Collectors.joining("\n")));
    }
    return component;
  }

  public <T> T[] getAll(Class<T> classFilter) {
    List<T> filteredComponents = new ArrayList<>();
    for (Component component : components) {
      if (classFilter==null || component.isInstanceOf(classFilter)) {
        Object object = getOpt(component, "getAll("+(classFilter!=null ? classFilter.getSimpleName() : "*")+")");
        filteredComponents.add((T) object);
      }
    }
    Class<?> arrayType = classFilter != null ? classFilter : Object.class;
    T[] array = (T[]) Array.newInstance(arrayType, filteredComponents.size());
    return filteredComponents.toArray(array);
  }

  public Object[] getAll() {
    return getAll(null);
  }

  public void start() {
    if (containerState!=ContainerState.STARTED) {
      containerState = ContainerState.STARTING;
      try {
        List<Component> componentsToStart = new ArrayList<>();
        for (Component startable: startables) {
          if (startable.componentState!=ComponentState.STARTED) {
            // ensures startables are initialized
            getOpt(startable, "startable("+startable+")");
            componentsToStart.add(startable);
          }
        }
        for (Component componentToStart: componentsToStart) {
          start(componentToStart);
        }
        containerState = ContainerState.STARTED;
      } catch (RuntimeException e) {
        stop();
        throw e;
      }
    }
  }

  private void start(Component startable) {
    if (startable.componentState!=ComponentState.STARTED) {
      boolean initializationPathStarted = initializationPathAdd(startable);
      try {
        startable.componentState = ComponentState.STARTING;
        invoke(Start.class, startable.object);
        startable.componentState = ComponentState.STARTED;

      } finally {
        initializationPathRemove(initializationPathStarted);
      }
    }
  }

  public void stop() {
    if (containerState != ContainerState.STOPPED) {
      containerState = ContainerState.STOPPING;
      for (Component stoppable: stoppables) {
        if (stoppable.componentState==ComponentState.STARTED
            || stoppable.componentState==ComponentState.STARTING) {
          stop(stoppable);
        }
      }
      containerState = ContainerState.STOPPED;
      log.debug("Stopped the container");
    }
  }

  private void stop(Component stoppable) {
    boolean initializationPathCreated = initializationPathAdd(stoppable);
    try {
      invoke(Stop.class, stoppable.object);
      stoppable.componentState = ComponentState.STOPPED;

    } finally {
      initializationPathRemove(initializationPathCreated);
    }
  }

}
