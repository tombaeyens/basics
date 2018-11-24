/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static ai.shape.magicless.app.util.Exceptions.exceptionWithCause;

public class Reflection {

  public static Field findFieldInObject(Object target, String fieldName) {
    if (target==null || fieldName==null) {
      return null;
    }
    return findFieldInClass(target.getClass(), fieldName);
  }

  public static Field findFieldInClass(Class<?> clazz, String fieldName) {
    if (clazz==null || fieldName==null) {
      return null;
    }
    for (Field field: clazz.getDeclaredFields()) {
      if (fieldName.equals(field.getName())) {
        return field;
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass!=null) {
      return findFieldInClass(superclass, fieldName);
    }
    return null;
  }


  public static Object getFieldValue(Field field, Object target) {
    try {
      field.setAccessible(true);
      return field.get(target);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Couldn't get field value with reflection: "+e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T instantiate(String className) {
    Class<T> clazz = null;
    try {
      clazz = (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw exceptionWithCause("find class "+(clazz!=null ? clazz.getName() : null), e);
    }
    return instantiate(clazz);
  }

  public static <T> T instantiate(Class<T> clazz) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      return constructor.newInstance();
    } catch (Exception e) {
      throw exceptionWithCause("instantiate class "+(clazz!=null ? clazz.getName() : null), e);
    }
  }

  public static boolean hasAnnotationRecursive(Class<?> clazz, Class<? extends Annotation> annotation) {
    if (clazz==null || annotation==null) {
      return false;
    }
    if (clazz.getAnnotation(annotation)!=null) {
      return true;
    }
    for (Field field: clazz.getDeclaredFields()) {
      if (field.getAnnotation(annotation)!=null) {
        return true;
      }
    }
    for (Method method: clazz.getDeclaredMethods()) {
      if (method.getAnnotation(annotation)!=null) {
        return true;
      }
    }
    for (Class<?> interfaze: clazz.getInterfaces()) {
      if (hasAnnotationRecursive(interfaze, annotation)) {
        return true;
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass!=Object.class) {
      return hasAnnotationRecursive(superclass, annotation);
    }
    return false;
  }
}
