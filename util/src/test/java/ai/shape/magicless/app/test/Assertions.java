/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.test;

import java.util.Collection;

import static org.junit.Assert.fail;

public class Assertions {

  public static <C extends Collection<E>, E> C assertOneElement(C collection) {
    return assertOneElement(collection, 1);
  }

  public static <C extends Collection<E>, E> C assertOneElement(C collection, int size) {
    if (collection==null) {
      fail("Collection is null");
    }
    if (collection.size()!=size) {
      fail("Expected collection size "+size+", but was "+collection.size());
    }
    return collection;
  }
}
