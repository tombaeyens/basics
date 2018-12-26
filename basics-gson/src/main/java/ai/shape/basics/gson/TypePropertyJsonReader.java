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
package ai.shape.basics.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.google.gson.stream.JsonToken.*;

/**
 * Helper for {@link TypePropertyStrategy} that provides access to the type property
 * before reading the other fields.  Since the type may not be the first property/field
 * in the object, the token stream leading up to the type property is cached.
 */
public class TypePropertyJsonReader extends JsonReader {

  // public static Logger log = LoggerFactory.getLogger(TypePropertyJsonReader.class);

  JsonReader in;
  String typePropertyName;
  List<TokenValue> cachedTokenValues;
  Integer cacheIndex = null;
  String typeName = null;

  /** because the super class is not designed for reuse and its
   * constructor requires a non-null reader */
  private static Reader BOGUS_NON_NULL_READER = new StringReader("");

  /** @param in is assumed to be in a position where it just has already
   *            begun reading the object: in.beginObject() already has been
   *            called.  And it is assumed that in.endObject() is also called
   *            by the caller. */
  public TypePropertyJsonReader(JsonReader in, String typePropertyName) {
    super(BOGUS_NON_NULL_READER);
    this.in = in;
    this.typePropertyName = typePropertyName;
  }

  public String readTypeName() {
    try {
      while (in.peek()!=JsonToken.END_OBJECT
        && nextPropertyNameIsNotType()) {
        cacheValueTokens();
      }
      return typeName;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't read json "+e.getMessage(), e);
    }
  }

  private boolean nextPropertyNameIsNotType() throws IOException {
    if (in.peek()==JsonToken.NAME) {
      String propertyName = in.nextName();
      if (typePropertyName.equals(propertyName)) {
        typeName = in.nextString();
        return false;
      } else {
        addTokenToCache(new TokenValue(NAME, propertyName));
      }
    }
    return true;
  }

  private static class TokenValue {
    JsonToken tokenType;
    Object tokenValue;
    public TokenValue(JsonToken tokenType, Object tokenValue) {
      this.tokenType = tokenType;
      this.tokenValue = tokenValue;
    }
    public JsonToken getTokenType() {
      return tokenType;
    }
    public Object getTokenValue() {
      return tokenValue;
    }
    public String toString() {
      return tokenType+" "+tokenValue;
    }
  }

  private void addTokenToCache(TokenValue tokenValue) {
    if (cachedTokenValues==null) {
      cachedTokenValues = new ArrayList<>();
    }
    cacheIndex = 0;
    cachedTokenValues.add(tokenValue);
    // log.debug("Added to cache "+tokenValue);
  }

  private void cacheValueTokens() throws IOException {
    JsonToken jsonToken = in.peek();
    switch (jsonToken) {
      case STRING:
        addTokenToCache(new TokenValue(jsonToken, in.nextString()));
        break;
      case BOOLEAN:
        addTokenToCache(new TokenValue(jsonToken, in.nextBoolean()));
        break;
      case NUMBER:
        addTokenToCache(new TokenValue(jsonToken, in.nextDouble()));
        break;
      case NULL:
        addTokenToCache(new TokenValue(jsonToken, null));
        break;
      case BEGIN_OBJECT:
        cacheObject();
        break;
      case BEGIN_ARRAY:
        cacheArray();
        break;
    }
  }

  private void cacheObject() throws IOException {
    in.beginObject();
    addTokenToCache(new TokenValue(BEGIN_OBJECT, null));
    while (in.peek()!=END_OBJECT) {
      addTokenToCache(new TokenValue(NAME, in.nextName()));
      cacheValueTokens();
    }
    in.endObject();
    addTokenToCache(new TokenValue(END_OBJECT, null));
  }

  private void cacheArray() throws IOException {
    in.beginArray();
    addTokenToCache(new TokenValue(BEGIN_ARRAY, null));
    while (in.peek()!=END_ARRAY) {
      cacheValueTokens();
    }
    in.endArray();
    addTokenToCache(new TokenValue(END_ARRAY, null));
  }

  @Override
  public JsonToken peek() throws IOException {
    if (cacheHasMoreTokens()) {
      return cachedTokenValues.get(cacheIndex).getTokenType();
    } else {
      return in.peek();
    }
  }

  /**
   * Returns true if the current array or object has another element.
   */
  @Override
  public boolean hasNext() throws IOException {
    if (cacheHasMoreTokens()) {
      JsonToken nextTokenType = peek();
      return nextTokenType!=END_ARRAY && nextTokenType!= END_OBJECT;
    } else {
      return in.hasNext();
    }
  }

  @Override
  public String nextName() throws IOException {
    if (cacheHasMoreTokens()) {
      return (String) consumeNextFromCache(NAME);
    } else {
      return in.nextName();
    }
  }

  @Override
  public String nextString() throws IOException {
    if (cacheHasMoreTokens()) {
      return (String) consumeNextFromCache(STRING);
    } else {
      return in.nextString();
    }
  }

  @Override
  public boolean nextBoolean() throws IOException {
    if (cacheHasMoreTokens()) {
      return (boolean) consumeNextFromCache(BOOLEAN);
    } else {
      return in.nextBoolean();
    }
  }

  @Override
  public void nextNull() throws IOException {
    if (cacheHasMoreTokens()) {
      consumeNextFromCache(NULL);
    } else {
      in.nextNull();
    }
  }

  @Override
  public double nextDouble() throws IOException {
    if (cacheHasMoreTokens()) {
      Number number = (Number) consumeNextFromCache(NUMBER);
      return number.doubleValue();
    } else {
      return in.nextDouble();
    }
  }

  @Override
  public long nextLong() throws IOException {
    if (cacheHasMoreTokens()) {
      Number number = (Number) consumeNextFromCache(NUMBER);
      return number.longValue();
    } else {
      return in.nextLong();
    }
  }

  @Override
  public int nextInt() throws IOException {
    if (cacheHasMoreTokens()) {
      Number number = (Number) consumeNextFromCache(NUMBER);
      return number.intValue();
    } else {
      return in.nextInt();
    }
  }

  @Override
  public void skipValue() throws IOException {
    if (cacheHasMoreTokens()) {
      consumeNextFromCache(null);
    } else {
      in.skipValue();
    }
  }

  @Override
  public void beginArray() throws IOException {
    if (cacheHasMoreTokens()) {
      consumeNextFromCache(BEGIN_ARRAY);
    } else {
      in.beginArray();
    }
  }

  @Override
  public void endArray() throws IOException {
    if (cacheHasMoreTokens()) {
      consumeNextFromCache(END_ARRAY);
    } else {
      in.endArray();
    }
  }

  @Override
  public void beginObject() throws IOException {
    if (cacheHasMoreTokens()) {
      consumeNextFromCache(BEGIN_OBJECT);
    } else {
      in.beginObject();
    }
  }

  @Override
  public void endObject() throws IOException {
    if (cacheHasMoreTokens()) {
      consumeNextFromCache(END_OBJECT);
    } else {
      in.endObject();
    }
  }

  private boolean cacheHasMoreTokens() {
    return cachedTokenValues!=null;
  }

  private Object consumeNextFromCache(JsonToken expectedTokenType) {
    TokenValue tokenValue = cachedTokenValues.get(cacheIndex);
    Object value = tokenValue.getTokenValue();
    if (expectedTokenType!=null && expectedTokenType!=tokenValue.getTokenType()) {
      throw new RuntimeException("Unexpected token encountered: Expected "+expectedTokenType+", but was "+tokenValue.getTokenType()+(value!=null ? "("+value+")" : ""));
    }
    // log.debug("Consumed from cache "+tokenValue);
    cacheIndex++;
    if (cacheIndex==cachedTokenValues.size()) {
      // Other methods rely on cacheIndex being set to null when the cache is empty;
      cacheIndex = null;
      cachedTokenValues = null;
    }
    return value;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()+"("+in.toString()+")";
  }

  @Override
  public String getPath() {
    throw new UnsupportedOperationException();
  }
}
