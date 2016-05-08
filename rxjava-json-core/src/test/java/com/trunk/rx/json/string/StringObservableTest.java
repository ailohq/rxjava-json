package com.trunk.rx.json.string;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.trunk.rx.string.StringObservable;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.testng.Assert.assertEquals;

public class StringObservableTest {

  @Test
  public void observableShouldBreakUpString() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    StringObservable.from("this is a string")
      .subscribe(t);
    t.assertNoErrors();
    t.assertCompleted();
    t.assertValues('t', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 's', 't', 'r', 'i', 'n', 'g');
  }

  @Test
  public void observableShouldStopEmittingOnUnsubscribe() throws Exception {
    List<Character> cs = new ArrayList<>();
    TestSubscriber<Character> t = new TestSubscriber<>();
    StringObservable.from("this is a string")
      .doOnNext(c -> cs.add(c))
      .take(5)
      .subscribe(t);
    t.assertNoErrors();
    t.assertCompleted();
    t.assertValues('t', 'h', 'i', 's', ' ');
    assertEquals(cs, ImmutableList.of('t', 'h', 'i', 's', ' '));
  }

  @Test
  public void observableShouldCompleteImmediatelyWithEmptyString() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    StringObservable.from("")
      .subscribe(t);
    t.assertNoErrors();
    t.assertCompleted();
    t.assertNoValues();
  }

  @Test
  public void operatorShouldCompleteImmediatelyWithEmptyString() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    Observable.just("")
      .lift(StringObservable.toCharacter())
      .subscribe(t);
    t.assertNoErrors();
    t.assertCompleted();
    t.assertNoValues();
  }

  @Test
  public void operatorShouldSkipOverEmptyString() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    Observable.just("", "a", "", "b", "")
      .lift(StringObservable.toCharacter())
      .subscribe(t);
    t.assertNoErrors();
    t.assertCompleted();
    t.assertValues('a', 'b');
  }

  @Test
  public void operatorShouldCompleteImmediatelyWithEmptyUpstream() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    Observable.<String>empty()
      .lift(StringObservable.toCharacter())
      .subscribe(t);
    t.assertNoErrors();
    t.assertCompleted();
    t.assertNoValues();
  }

  @Test
  public void operatorShouldBreakUpString() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    Observable.just("this is ", "a string")
      .lift(StringObservable.toCharacter())
      .subscribe(t);
    t.assertValues('t', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 's', 't', 'r', 'i', 'n', 'g');
  }

  @Test
  public void operatorShouldPropogateErrors() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    RuntimeException exception = new RuntimeException();
    Observable.just("this is ")
      .concatWith(Observable.error(exception))
      .concatWith(Observable.just("a string"))
      .lift(StringObservable.toCharacter())
      .subscribe(t);
    t.assertError(exception);
    t.assertValues('t', 'h', 'i', 's', ' ', 'i', 's', ' ');
  }

  @Test
  public void operatorShouldStopEmittingOnUnsubscribe() throws Exception {
    List<Character> cs = new ArrayList<>();
    TestSubscriber<Character> t = new TestSubscriber<>();
    Observable.just("this is ", "a string")
      .lift(StringObservable.toCharacter())
      .doOnNext(c -> cs.add(c))
      .take(5)
      .subscribe(t);
    t.assertValues('t', 'h', 'i', 's', ' ');
    assertEquals(cs, ImmutableList.of('t', 'h', 'i', 's', ' '));
  }

  @Test
  public void operatorShouldSendBackPressureUpstream() throws Exception {
    TestSubscriber<Character> t = new TestSubscriber<>();
    t.requestMore(0);

    int[] emitted = {0};

    Observable.just("t", "h", "", "i", "s is ", "a string")
      .doOnNext(ignore -> emitted[0] += 1)
      .lift(StringObservable.toCharacter())
      .subscribe(t);

    t.assertNoValues();

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 1);
    assertEquals(emitted[0], 1);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 2);
    assertEquals(emitted[0], 2);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 3);
    assertEquals(emitted[0], 4);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 4);
    assertEquals(emitted[0], 5);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 5);
    assertEquals(emitted[0], 5);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 6);
    assertEquals(emitted[0], 5);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 7);
    assertEquals(emitted[0], 5);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 8);
    assertEquals(emitted[0], 5);

    t.requestMore(1);
    assertEquals(t.getOnNextEvents().size(), 9);
    assertEquals(emitted[0], 6);

  }
}
