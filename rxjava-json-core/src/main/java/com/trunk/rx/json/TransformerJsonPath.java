package com.trunk.rx.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.path.NoopToken;
import com.trunk.rx.json.token.JsonDocumentEnd;

import rx.Observable;

public class TransformerJsonPath implements Observable.Transformer<JsonTokenEvent, JsonPathEvent> {

  private final Observable<JsonPath> matchers;
  private final boolean lenient;

  private final ConcurrentHashMap<JsonPath, Integer> visitedMatchers = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<JsonPath, Integer> completedMatchers = new ConcurrentHashMap<>();

  public static TransformerJsonPath from(JsonPath... matchers) {
    return from(Arrays.asList(matchers));
  }

  public static TransformerJsonPath from(Collection<JsonPath> matchers) {
    if (matchers.isEmpty()) {
      throw new IllegalArgumentException("One or more matchers must be supplied");
    }
    return new TransformerJsonPath(Observable.from(matchers), false);
  }

  private TransformerJsonPath(Observable<JsonPath> matchers, boolean lenient) {
    this.lenient = lenient;
    this.matchers = matchers;
    matchers.toBlocking().forEach(jsonPath -> {
      visitedMatchers.put(jsonPath, 0);
      completedMatchers.put(jsonPath, 0);
    });
  }


  @Override
  public Observable<JsonPathEvent> call(Observable<JsonTokenEvent> upstream) {
    return upstream
      .takeUntil(ignore -> !lenient && allMatchersComplete())
      .concatMap( // order is important
        jsonTokenEvent ->
          matches(jsonTokenEvent)
            .concatWith(
              jsonTokenEvent.getToken() == JsonDocumentEnd.INSTANCE ?
                Observable.just(new JsonPathEvent(NoopToken.INSTANCE, jsonTokenEvent)) :
                Observable.empty()
            )
      );
  }

  private boolean allMatchersComplete() {
    for (Entry<JsonPath, Integer> p : visitedMatchers.entrySet()) {
      if (p.getValue() == 0 || completedMatchers.get(p.getKey()) == 0) {
        return false;
      }
    }
    return true;
  }

  public TransformerJsonPath lenient() {
    return new TransformerJsonPath(matchers, true);
  }

  private Observable<JsonPathEvent> matches(JsonTokenEvent jsonTokenEvent) {
    return matchers
      .flatMap(matcher -> matcher.match(jsonTokenEvent.getJsonPath()).map(matched -> new MatchWrapper(matcher, matched)))
      .doOnNext(wrapper -> {
        // mark visited matches (ie gave any result)
        if (!lenient) {
          visitedMatchers.put(wrapper.matcher, 1);
        }
      })
      .map(wrapper -> wrapper.matched)
      .reduce(NoopToken.INSTANCE, (matchedPath, acc) -> matchedPath.length() < acc.length() ? matchedPath : acc)
      .doOnNext(t -> {
        // if we got no result, any visited matchers are complete
        if (!lenient && t == NoopToken.INSTANCE) {
          visitedMatchers.entrySet().stream().filter(p -> p.getValue() == 1).forEach(p -> completedMatchers.put(p.getKey(), 1));
        }
      })
      .filter(t -> t != NoopToken.INSTANCE)
      .map(shortestMatchedPath -> new JsonPathEvent(shortestMatchedPath, jsonTokenEvent));
  }

  private class MatchWrapper {
    final JsonPath matcher;
    final JsonPath matched;

    private MatchWrapper(JsonPath matcher, JsonPath matched) {
      this.matcher = matcher;
      this.matched = matched;
    }
  }
}
