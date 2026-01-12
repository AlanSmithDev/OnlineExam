package com.fanshuaiko.backage.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * 将空字符串或null反序列化为null，避免数字字段绑定400
 */
public class EmptyStringToIntegerDeserializer extends JsonDeserializer<Integer> {
  @Override
  public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String text = p.getValueAsString();
    if (text == null || text.trim().isEmpty()) {
      return null;
    }
    try {
      return Integer.valueOf(text.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
