package com.github.swagger.akka;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.models.parameters.*;
import io.swagger.util.Json;

import java.io.IOException;

public class ParameterDeserializer extends JsonDeserializer<Parameter> {
    @Override
    public Parameter deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Parameter result = null;

        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode sub = node.get("$ref");
        JsonNode inNode = node.get("in");

        if (sub != null) {
            result = io.swagger.util.Json.mapper().convertValue(sub, RefParameter.class);
        } else if (inNode != null) {
            String in = inNode.asText();
            if ("query".equals(in)) {
                result = io.swagger.util.Json.mapper().convertValue(node, QueryParameter.class);
            } else if ("header".equals(in)) {
                result = io.swagger.util.Json.mapper().convertValue(node, HeaderParameter.class);
            } else if ("path".equals(in)) {
                result = io.swagger.util.Json.mapper().convertValue(node, PathParameter.class);
            } else if ("formData".equals(in)) {
                result = io.swagger.util.Json.mapper().convertValue(node, FormParameter.class);
            } else if ("body".equals(in)) {
                result = io.swagger.util.Json.mapper().convertValue(node, BodyParameter.class);
            } else if ("cookie".equals(in)) {
                result = Json.mapper().convertValue(node, CookieParameter.class);
            }
        }

        return result;
    }
}
