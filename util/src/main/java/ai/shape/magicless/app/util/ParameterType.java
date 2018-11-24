/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */

package ai.shape.magicless.app.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ParameterType implements Type, ParameterizedType {

  Type rawType;
  Type[] actualTypeArguments;
  Type ownerType;

  public ParameterType(Type rawType, Type... actualTypeArguments) {
    this.rawType = rawType;
    this.actualTypeArguments = actualTypeArguments;
  }

  public ParameterType ownerType(Type ownerType) {
    this.ownerType = ownerType;
    return this;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  @Override
  public String getTypeName() {
    return rawType.getTypeName() +
      "<" +
      Arrays.stream(actualTypeArguments)
        .map(actualTypeArgument->actualTypeArgument.getTypeName())
        .collect(Collectors.joining(",")) +
      ">";
  }
}
