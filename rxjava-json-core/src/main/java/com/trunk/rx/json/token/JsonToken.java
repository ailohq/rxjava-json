package com.trunk.rx.json.token;

public interface JsonToken {
  String value();

  boolean isArrayStart();

  boolean isArrayEnd();

  boolean isBoolean();

  boolean isName();

  boolean isNull();

  boolean isNumber();

  boolean isObjectStart();

  boolean isObjectEnd();

  boolean isString();

  boolean isDocumentEnd();
}
