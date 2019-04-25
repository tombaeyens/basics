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

import ai.shape.com.google.gson.*;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;


/** Usage while building a Gson object
 *
 * Gson gson = new GsonBuilder()
 *   ...
 *   .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeConverter())
 *   ...
 *   .create()
 */
public class LocalDateTimeConverter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC").normalized();

  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(FORMATTER.format(localDateTime.atOffset(ZoneOffset.UTC)));
  }

  @Override
  public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String dateTimeString = json.getAsString();
    if (!dateTimeString.endsWith("Z")) {
      dateTimeString += "Z";
    }
    TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateTimeString);
    OffsetDateTime offsetDateTime = OffsetDateTime.from(temporalAccessor);
    ZonedDateTime zonedDateTime = offsetDateTime.atZoneSameInstant(UTC_ZONE_ID);
    return zonedDateTime.toLocalDateTime();
  }
}
