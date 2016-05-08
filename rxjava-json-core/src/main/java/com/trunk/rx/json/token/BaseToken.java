package com.trunk.rx.json.token;

public abstract class BaseToken implements JsonToken {
  @Override
  public boolean isArrayEnd() {
    return false;
  }

  @Override
  public boolean isArrayStart() {
    return false;
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  @Override
  public boolean isDocumentEnd() {
    return false;
  }

  @Override
  public boolean isName() {
    return false;
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isObjectEnd() {
    return false;
  }

  @Override
  public boolean isObjectStart() {
    return false;
  }

  @Override
  public boolean isString() {
    return false;
  }
}
