package com.fanshuaiko.backage.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * 将空字符串或null反序列化为null，避免长整型字段绑定400
 */
public class EmptyStringToLongDeserializer extends JsonDeserializer<Long> {
  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String text = p.getValueAsString();
    if (text == null || text.trim().isEmpty()) {
      return null;
    }
    try {
      return Long.valueOf(text.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
