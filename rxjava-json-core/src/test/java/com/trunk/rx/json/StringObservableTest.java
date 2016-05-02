package com.trunk.rx.json;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.trunk.rx.StringObservable;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.testng.Assert.assertEquals;

public class StringObservableTest {
  @Test
  public void shouldBreakUpString() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    Observable.just("this is ", "a string").compose(StringObservable.toCharacter()).subscribe(t);
    t.assertValues('t', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 's', 't', 'r', 'i', 'n', 'g');
  }

  @Test
  public void shouldStopEmittingOnUnsubscribe() throws Exception {
    List<Character> cs = new ArrayList<>();
    TestSubscriber<Character> t = new TestSubscriber<>();
    Observable.just("this is ", "a string").compose(StringObservable.toCharacter())
      .doOnNext(c -> cs.add(c))
      .take(5)
      .subscribe(t);
    t.assertValues('t', 'h', 'i', 's', ' ');
    assertEquals(cs, ImmutableList.of('t', 'h', 'i', 's', ' '));
  }

  // TODO backpressure
}
