package helloscala.common.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.helloscala.HelloscalaModule;
import helloscala.util.TimeUtils;
import helloscala.util.TimeUtils$;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

/**
 * Jackson全局配置
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-14.
 */
public class Jackson {
    public static final ObjectMapper defaultObjectMapper = getObjectMapper();

    public static ObjectNode createObjectNode() {
        return defaultObjectMapper.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return defaultObjectMapper.createArrayNode();
    }

    private static ObjectMapper getObjectMapper() {
        TimeUtils timeUtils = TimeUtils$.MODULE$;
        JavaTimeModule jtm = new JavaTimeModule();
        jtm.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(timeUtils.formatterDateTime()));
        jtm.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(timeUtils.formatterDateTime()));

        return new ObjectMapper()
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

