/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helloscala.common.jackson;

import scalapb.GeneratedMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.module.helloscala.HelloscalaModule;
import helloscala.common.util.TimeUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class ZonedDateTimeDeserializer extends JSR310DateTimeDeserializerBase<ZonedDateTime> {
    public ZonedDateTimeDeserializer() {
        super(ZonedDateTime.class, TimeUtils.formatterDateTime());
    }

//    public ZonedDateTimeDeserializer(DateTimeFormatter f) {
//        this(ZonedDateTime.class, f);
//    }

//    public ZonedDateTimeDeserializer(Class<ZonedDateTime> supportedType, DateTimeFormatter f) {
//        super(supportedType, f);
//    }

    @Override
    protected JsonDeserializer<ZonedDateTime> withDateFormat(DateTimeFormatter dtf) {
        return null;
    }

    @Override
    public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        if (parser.hasTokenId(JsonTokenId.ID_STRING)) {
            String string = parser.getText().trim();
            if (string.length() == 0) {
                return null;
            }

            try {
                return TimeUtils.toZonedDateTime(string);
            } catch (DateTimeException e) {
//                _rethrowDateTimeException(parser, context, e, string);
                throw new JsonParseException(parser, string, e);
            }
        }
        if (parser.isExpectedStartArrayToken()) {
            JsonToken t = parser.nextToken();
            if (t == JsonToken.END_ARRAY) {
                return null;
            }
            if ((t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT)
                    && context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
                final ZonedDateTime parsed = deserialize(parser, context);
                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    handleMissingEndArrayForSingle(parser, context);
                }
                return parsed;
            }
            if (t == JsonToken.VALUE_NUMBER_INT) {
                ZonedDateTime result;

                int year = parser.getIntValue();
                int month = parser.nextIntValue(-1);
                int day = parser.nextIntValue(-1);
                int hour = parser.nextIntValue(-1);
                int minute = parser.nextIntValue(-1);

                t = parser.nextToken();
                if (t == JsonToken.END_ARRAY) {
                    result = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, TimeUtils.ZONE_CHINA_OFFSET());
                } else {
                    int second = parser.getIntValue();
                    t = parser.nextToken();
                    if (t == JsonToken.END_ARRAY) {
                        result = ZonedDateTime.of(year, month, day, hour, minute, second, 0, TimeUtils.ZONE_CHINA_OFFSET());
                    } else {
                        int partialSecond = parser.getIntValue();
                        if (partialSecond < 1_000 &&
                                !context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS))
                            partialSecond *= 1_000_000; // value is milliseconds, convert it to nanoseconds
                        if (parser.nextToken() != JsonToken.END_ARRAY) {
                            throw context.wrongTokenException(parser, handledType(), JsonToken.END_ARRAY,
                                    "Expected array to end");
                        }
                        result = ZonedDateTime.of(year, month, day, hour, minute, second, partialSecond, TimeUtils.ZONE_CHINA_OFFSET());
                    }
                }
                return result;
            }
            context.reportInputMismatch(handledType(),
                    "Unexpected token (%s) within Array, expected VALUE_NUMBER_INT",
                    t);
        }
        if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
            return (ZonedDateTime) parser.getEmbeddedObject();
        }
        throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_STRING,
                "Expected array or string.");
    }
}

/**
 * Jackson全局配置
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-14.
 */
public class Jackson {
    public static final Class<GeneratedMessage> FILTER_ID_CLASS = GeneratedMessage.class;

    public static final ObjectMapper defaultObjectMapper = createObjectMapper();

    public static ObjectNode createObjectNode() {
        return defaultObjectMapper.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return defaultObjectMapper.createArrayNode();
    }

    public static String stringify(Object value) throws JsonProcessingException {
        return defaultObjectMapper.writeValueAsString(value);
    }

    public static String prettyStringify(Object value) throws JsonProcessingException {
        return defaultObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    public static ObjectMapper createObjectMapper() {
        return createObjectMapper(null);
    }

    public static ObjectMapper createObjectMapper(JsonFactory jf) {
        JavaTimeModule jtm = new JavaTimeModule();
        jtm.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(TimeUtils.formatterDateTime()));
        jtm.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(TimeUtils.formatterDateTime()));
        jtm.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(TimeUtils.formatterDateTime()));
        jtm.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());

        return new ObjectMapper(jf)
                .setFilterProvider(
                        new SimpleFilterProvider()
                                .addFilter(FILTER_ID_CLASS.getName(), SimpleBeanPropertyFilter.serializeAllExcept("allFields")))
                .setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                    @Override
                    public Object findFilterId(Annotated a) {
                        if (FILTER_ID_CLASS.isAssignableFrom(a.getRawType())) {
                            return FILTER_ID_CLASS.getName();
                        }
                        return super.findFilterId(a);
                    }
                })
                .findAndRegisterModules()
                .registerModule(new HelloscalaModule())
                .registerModule(jtm)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
//                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}

