package com.trunk.rx.json.token;

public class JsonQuote extends BaseToken {
  private static final class Holder {
    private static final JsonQuote INSTANCE = new JsonQuote();
  }

  public static JsonQuote instance() {
    return Holder.INSTANCE;
  }

  @Override
  public String value() {
    return "\"";
  }
}
