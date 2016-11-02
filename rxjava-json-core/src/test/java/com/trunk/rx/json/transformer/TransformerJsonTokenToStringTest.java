package com.trunk.rx.json.transformer;

import com.trunk.rx.json.RxJson;
import org.testng.annotations.Test;

import static com.trunk.rx.json.Assert.assertEquals;

public class TransformerJsonTokenToStringTest {
  @Test
  public void shouldParseNull() throws Exception {
    assertEquals(
      RxJson.valueBuilder().Null(),
      "null"
    );
  }

  @Test
  public void shouldParseNullInObject() throws Exception {
    assertEquals(
      RxJson.newObject().add("a", RxJson.valueBuilder().Null()),
      "{\"a\":null}"
    );
  }
}