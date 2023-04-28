package ru.practicum.shareit;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.*;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomJsonPatch implements JsonSerializable, Patch {
    private static final MessageBundle BUNDLE = MessageBundles.getBundle(JsonPatchMessages.class);
    private final List<JsonPatchOperation> operations;
    private final Map<String, String> properties;

    @JsonAnyGetter
    public Map<String, String> getMap() {
        return properties;
    }

    @JsonAnySetter
    public void setMap(String key, String value) {
        this.properties.put(key, value);
    }

    @JsonCreator
    public CustomJsonPatch(Map<String, String> properties) throws JsonPointerException {
        this.properties = properties;
        this.operations = new ArrayList<>();
        init(properties);
    }

    public static CustomJsonPatch fromJson(JsonNode node) throws IOException {
        BUNDLE.checkNotNull(node, "customJsonPatch.nullInput");
        return JacksonUtils.getReader().forType(CustomJsonPatch.class).readValue(node);
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        for (JsonPatchOperation op : this.operations) {
            op.serialize(gen, serializers);
        }
        gen.writeEndArray();
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        this.serialize(gen, serializers);
    }

    @Override
    public JsonNode apply(JsonNode node) throws JsonPatchException {
        BUNDLE.checkNotNull(node, "customJsonPatch.nullInput");
        JsonNode ret = node;

        JsonPatchOperation operation;
        for(Iterator var3 = this.operations.iterator(); var3.hasNext(); ret = operation.apply(ret)) {
            operation = (JsonPatchOperation)var3.next();
        }

        return ret;
    }

    public String toString() {
        return this.operations.toString();
    }

    private void init(Map<String, String> properties) throws JsonPointerException {
        for (Map.Entry entry : properties.entrySet()) {
            operations.add(
                    new ReplaceOperation(
                            new JsonPointer(String.format("/%s", entry.getKey())),
                            new TextNode(entry.getValue().toString())
                    )
            );
        }
    }
}