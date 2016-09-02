package com.trunk.rx.json.transformer;

import com.trunk.rx.json.token.JsonToken;
import rx.Observable;

public class TransformerJsonTokenToString implements Observable.Transformer<JsonToken, String> {

  private static final class Holder {
    private static final TransformerJsonTokenToString INSTANCE = new TransformerJsonTokenToString();
  }

  public static TransformerJsonTokenToString instance() {
    return Holder.INSTANCE;
  }

  @Override
  public Observable<String> call(Observable<JsonToken> jsonTokenObservable) {
    return jsonTokenObservable.map(token -> {
      if (token.isString() || token.isName()) {
        return escape(token.value());
      }
      return token.value();
    });
  }

  private String escape(String value) {
    return value
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\b", "\\b")
      .replace("\f", "\\f")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }
}
