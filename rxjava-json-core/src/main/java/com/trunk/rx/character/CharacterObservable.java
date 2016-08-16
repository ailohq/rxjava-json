package com.trunk.rx.character;

import com.trunk.rx.character.operator.OnSubscribeStringToChar;
import com.trunk.rx.character.operator.OperatorStringToChar;
import rx.Observable;

public class CharacterObservable {

  public static Observable<Character> from(String s) {
    return Observable.create(new OnSubscribeStringToChar(s));
  }

  public static Observable.Operator<Character, String> toCharacter() {
    return new OperatorStringToChar();
  }
}
