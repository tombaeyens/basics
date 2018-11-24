/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.container;

import ai.shape.magicless.app.util.Exceptions;
import ai.shape.magicless.app.util.Reflection;
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
    NOT_STARTED, STARTED
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

  ContainerState containerState = ContainerState.NOT_STARTED;

  LinkedList<Component> componentsToInitialize = null;
  LinkedList<Object> initializationPath = null;

  private static enum ComponentState {
    /** Ordering matters because .ordinal() is used on these enum values */
    NOT_CREATED, CREATED, INITIALIZING, INITIALIZED, STARTED
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
        object = factory.create();
      }
      componentState = ComponentState.CREATED;
      return object;
    }
    public boolean isInitialized() {
      return componentState.ordinal()>=ComponentState.INITIALIZED.ordinal();
    }
    public boolean isInstanceOf(Class<?> classFilter) {
      return (object!=null && classFilter.isAssignableFrom(object.getClass()))
             || (factoryObjectClass!=null && factoryObjectClass.isAssignableFrom(object.getClass()));
    }
    public String toString() {
      return object!=null ? object.getClass().getSimpleName() : factory!=null ? factory.getClass().getSimpleName() : "?";
    }
    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
      Class<?> clazz = object!=null ? object.getClass() : factory!=null ? factory.getClass() : null;
      return clazz!=null && Reflection.hasAnnotationRecursive(clazz, annotation);
    }
  }

  public void add(Object object) {
    if (object!=null) {
      Component component = new Component(object);
      addByClass(object.getClass(), component);
    }
  }

  public void add(String name, Object object) {
    if (name!=null) {
      add(name, new Component(object));
    }
  }

  /**
   * add a factory to create an object of a given clazz when it is retrieved from
   * the container
   */
  public void addFactory(Class<?> clazz, Factory factory) {
    if (clazz!=null && factory!=null) {
      addByClass(clazz, new Component(clazz, factory));
    }
  }

  public void addFactory(String name, Factory factory) {
    if (name!=null && factory!=null) {
      add(name, new Component(null, factory));
    }
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
          throw Exceptions.exceptionWithCause(" execute initialize method " + method, cause);
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
    return (T[]) filteredComponents.toArray(array);
  }

  public Object[] getAll() {
    return getAll(null);
  }

  public void start() {
    if (containerState == ContainerState.NOT_STARTED) {
      List<Component> startables = new ArrayList<>();
      for (Component component: components) {
        if (component.hasAnnotation(Start.class)) {
          getOpt(component, "startable("+component+")");
          startables.add(component);
        }
      }
      for (Component startable: startables) {
        start(startable);
      }
      containerState = ContainerState.STARTED;
    }
  }

  private void start(Component startable) {
    boolean initializationPathCreated = initializationPathAdd(startable);
    try {
      invoke(Start.class, startable.object);
      startable.componentState = ComponentState.STARTED;

    } finally {
      initializationPathRemove(initializationPathCreated);
    }
  }

  public void stop() {
    if (containerState == ContainerState.STARTED) {
      List<Object> stoppables = new ArrayList<>();
      for (Component component: components) {
        if (component.hasAnnotation(Stop.class)) {
          boolean initializationPathCreated = initializationPathAdd(component);
          try {
            invoke(Stop.class, component.object);
            component.componentState = ComponentState.INITIALIZED;

          } finally {
            initializationPathRemove(initializationPathCreated);
          }
        }
      }
      containerState = ContainerState.NOT_STARTED;
    }
  }
}
