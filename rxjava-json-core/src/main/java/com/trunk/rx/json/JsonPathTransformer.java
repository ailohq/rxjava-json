package com.trunk.rx.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.path.NoopToken;
import com.trunk.rx.json.token.JsonDocumentEnd;

import rx.Observable;

public class JsonPathTransformer implements Observable.Transformer<JsonTokenEvent, JsonPathEvent> {

  private final Observable<JsonPath> matchers;
  private final boolean lenient;

  private final ConcurrentHashMap<JsonPath, Integer> visitedMatchers = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<JsonPath, Integer> completedMatchers = new ConcurrentHashMap<>();

  public static JsonPathTransformer from(JsonPath... matchers) {
    return from(Arrays.asList(matchers));
  }

  public static JsonPathTransformer from(Collection<JsonPath> matchers) {
    if (matchers.isEmpty()) {
      throw new IllegalArgumentException("One or more matchers must be supplied");
    }
    return new JsonPathTransformer(Observable.from(matchers), false);
  }

  private JsonPathTransformer(Observable<JsonPath> matchers, boolean lenient) {
    this.lenient = lenient;
    this.matchers = matchers;
    matchers.toBlocking().forEach(jsonPath -> {
      visitedMatchers.put(jsonPath, 0);
      completedMatchers.put(jsonPath, 0);
    });
  }


  @Override
  public Observable<JsonPathEvent> call(Observable<JsonTokenEvent> upstream) {
    AtomicInteger documentIndex = new AtomicInteger(0);
    return upstream
      .takeUntil(ignore -> !lenient && allMatchersComplete())
      .flatMap(
        jsonTokenEvent ->
          matches(jsonTokenEvent)
            .concatWith(
              jsonTokenEvent.getToken() == JsonDocumentEnd.INSTANCE ?
                Observable.just(new PathMatch(jsonTokenEvent, NoopToken.INSTANCE)) :
                Observable.empty()
            )

      )
      .doOnNext(pathMatch -> {
        if (isDocumentEnd(pathMatch)) {
          documentIndex.incrementAndGet();
        }
      })
      .groupBy(
        pathMatch -> new PartitionKey(pathMatch.matchedPathFragment, documentIndex.get()),
        pathMatch -> pathMatch.jsonPathEvent.getToken()
      )
      .map(
        group ->
          new JsonPathEvent(
            group.getKey().jsonPath,
            group
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

  private boolean isDocumentEnd(PathMatch pathMatch) {return pathMatch.jsonPathEvent.getToken() == JsonDocumentEnd.INSTANCE;}

  public JsonPathTransformer lenient() {
    return new JsonPathTransformer(matchers, true);
  }

  private Observable<PathMatch> matches(JsonTokenEvent jsonTokenEvent) {
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
      .map(shortestMatchedPath -> new PathMatch(jsonTokenEvent, shortestMatchedPath));
  }

  private class PathMatch {
    final JsonTokenEvent jsonPathEvent;
    final JsonPath matchedPathFragment;

    private PathMatch(JsonTokenEvent jsonPathEvent, JsonPath matchedPathFragment) {
      this.jsonPathEvent = jsonPathEvent;
      this.matchedPathFragment = matchedPathFragment;
    }
  }

  private class MatchWrapper {
    final JsonPath matcher;
    final JsonPath matched;

    private MatchWrapper(JsonPath matcher, JsonPath matched) {
      this.matcher = matcher;
      this.matched = matched;
    }
  }

  private class PartitionKey {
    final JsonPath jsonPath;
    final int documentIndex;

    private PartitionKey(JsonPath jsonPath, int documentIndex) {
      this.jsonPath = jsonPath;
      this.documentIndex = documentIndex;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PartitionKey that = (PartitionKey) o;
      return documentIndex == that.documentIndex &&
        Objects.equals(jsonPath, that.jsonPath);
    }

    @Override
    public int hashCode() {
      return Objects.hash(jsonPath, documentIndex);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("PartitionKey{");
      sb.append("documentIndex=").append(documentIndex);
      sb.append(", jsonPath=").append(jsonPath);
      sb.append('}');
      return sb.toString();
    }
  }
}
