package com.trunk.rx.json.transformer;

import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.JsonTokenEvent;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.path.NoopToken;
import com.trunk.rx.json.token.JsonDocumentEnd;
import rx.Observable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TransformerJsonPath implements Observable.Transformer<JsonTokenEvent, JsonPathEvent> {

  private final Collection<JsonPath> matchers;
  private final boolean lenient;

  public static TransformerJsonPath from(String... paths) {
    return from(Arrays.stream(paths).map(s -> JsonPath.parse(s)).collect(Collectors.toList()));
  }

  public static TransformerJsonPath from(JsonPath... matchers) {
    return from(Arrays.asList(matchers));
  }

  public static TransformerJsonPath from(Collection<JsonPath> matchers) {
    if (matchers.isEmpty()) {
      throw new IllegalArgumentException("One or more JsonPaths must be supplied");
    }
    return new TransformerJsonPath(matchers, false);
  }

  public TransformerJsonPath(Collection<JsonPath> matchers, boolean lenient) {
    this.lenient = lenient;
    this.matchers =  matchers;
  }


  @Override
  public Observable<JsonPathEvent> call(Observable<JsonTokenEvent> upstream) {
    ConcurrentHashMap<JsonPath, Integer> visitedMatchers = new ConcurrentHashMap<>();
    ConcurrentHashMap<JsonPath, Integer> completedMatchers = new ConcurrentHashMap<>();
    matchers.forEach(jsonPath -> {
      visitedMatchers.put(jsonPath, 0);
      completedMatchers.put(jsonPath, 0);
    });

    return upstream
      .takeUntil(ignore -> !lenient && allMatchersComplete(visitedMatchers, completedMatchers))
      .concatMap( // order is important
        jsonTokenEvent ->
          matches(jsonTokenEvent, visitedMatchers, completedMatchers)
            .concatWith(
              jsonTokenEvent.getToken() == JsonDocumentEnd.instance() ?
                Observable.just(new JsonPathEvent(NoopToken.instance(), jsonTokenEvent)) :
                Observable.empty()
            )
      );
  }

  public TransformerJsonPath strict() {
    return new TransformerJsonPath(matchers, false);
  }

  private boolean allMatchersComplete(Map<JsonPath, Integer> visitedMatchers, Map<JsonPath, Integer> completedMatchers) {
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

  private Observable<JsonPathEvent> matches(JsonTokenEvent jsonTokenEvent, Map<JsonPath, Integer> visitedMatchers, Map<JsonPath, Integer> completedMatchers) {
    return Observable.from(matchers)
      .flatMap(matcher -> matcher.match(jsonTokenEvent.getJsonPath()).map(matched -> new MatchWrapper(matcher, matched)))
      .doOnNext(wrapper -> {
        // mark visited matches (ie gave any result)
        if (!lenient) {
          visitedMatchers.put(wrapper.matcher, 1);
        }
      })
      .map(wrapper -> wrapper.matched)
      .reduce(NoopToken.instance(), (matchedPath, acc) -> matchedPath.length() < acc.length() ? matchedPath : acc)
      .doOnNext(t -> {
        // if we got no result, any visited matchers are complete
        if (!lenient && t == NoopToken.instance()) {
          visitedMatchers.entrySet().stream().filter(p -> p.getValue() == 1).forEach(p -> completedMatchers.put(p.getKey(), 1));
        }
      })
      .filter(t -> t != NoopToken.instance())
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
