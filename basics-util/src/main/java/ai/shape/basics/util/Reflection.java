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
package ai.shape.basics.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static ai.shape.basics.util.Exceptions.exceptionWithCause;

public class Reflection {

  public static Method findMethodInClass(Class<?> clazz, String methodName) {
    if (clazz==null || methodName==null) {
      return null;
    }
    for (Method method: clazz.getDeclaredMethods()) {
      if (methodName.equals(method.getName())) {
        return method;
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass!=null) {
      return findMethodInClass(superclass, methodName);
    }
    return null;

  }

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
