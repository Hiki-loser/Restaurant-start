package com.sky.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.InputStream;
import java.util.Set;

public class JsonSchemaValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final JsonSchema schema;

    public JsonSchemaValidator(InputStream schemaInputStream) throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonNode schemaNode = MAPPER.readTree(schemaInputStream);
        this.schema = factory.getSchema(schemaNode);
    }

    /**
     * 校验 JSON 字符串是否符合 schema
     * @return set of validation messages (empty => valid)
     */
    public Set<ValidationMessage> validate(String json) throws Exception {
        JsonNode node = MAPPER.readTree(json);
        return schema.validate(node);
    }

    public static JsonSchemaValidator fromResource(String resourcePath) throws Exception {
        InputStream is = JsonSchemaValidator.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) throw new IllegalStateException("schema resource not found: " + resourcePath);
        return new JsonSchemaValidator(is);
    }
}
