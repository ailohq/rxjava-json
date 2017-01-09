package com.trunk.rx.json.hal;

import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.element.JsonArray;
import com.trunk.rx.json.element.JsonElement;
import com.trunk.rx.json.element.JsonObject;
import rx.Observable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable <a href="http://stateless.co/hal_specification.html">HAL</a> object.
 * <p>
 * This is the top level object returned or embedded in another HalObject.
 */
public final class HalObject extends JsonElement {

  private static final class Holder {
    private static final String SELF = "self";
    private static final String LINKS = "_links";
    private static final String EMBEDDED = "_embedded";
    private static final String DATA_CANNOT_USE_RESERVED_PROPERTY = "Data cannot use reserved property '%s'";
    private static final String LINK_SELF_CAN_ONLY_BE_SET_USING_SELF = "Rel 'self' can only be set using #self";
    private static final HalObject EMPTY_HAL_OBJECT =
      new HalObject(
        Optional.empty(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        Observable.empty(),
        false,
        Order.LinksEmbeddedData,
        false
      );
  }

  private final Optional<HalLink> self;
  private final Map<String, Observable<HalLink>> singletonLinks;
  private final Map<String, Observable<HalLink>> arrayLinks;
  private final Map<String, Observable<HalObject>> singletonEmbedded;
  private final Map<String, Observable<HalObject>> arrayEmbedded;
  private final Observable<JsonObject.Entry<JsonElement>> data;
  private final boolean lenient;
  private final Order order;
  private final boolean suppressNulls;

  /**
   * @return an immutable empty HalObject
   */
  public static HalObject create() {
    return Holder.EMPTY_HAL_OBJECT;
  }

  private HalObject(
    Optional<HalLink> self,
    Map<String, Observable<HalLink>> singletonLinks,
    Map<String, Observable<HalLink>> arrayLinks,
    Map<String, Observable<HalObject>> singletonEmbedded,
    Map<String, Observable<HalObject>> arrayEmbedded,
    Observable<JsonObject.Entry<JsonElement>> data,
    boolean lenient,
    Order order,
    boolean suppressNulls
  ) {
    super(
      JsonObject.of(
        getOrderedElements(self, singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, data, lenient, order, suppressNulls)
      )
        .suppressNulls(suppressNulls)
    );

    this.self = self;
    this.singletonLinks = Collections.unmodifiableMap(singletonLinks);
    this.arrayLinks = Collections.unmodifiableMap(arrayLinks);
    this.singletonEmbedded = Collections.unmodifiableMap(singletonEmbedded);
    this.arrayEmbedded = Collections.unmodifiableMap(arrayEmbedded);
    this.data = data;
    this.lenient = lenient;
    this.order = order;
    this.suppressNulls = suppressNulls;
  }

  /**
   * By default using reserved keys will return an exception to the subscriber. In lenient mode these
   * are silently removed from the stream.
   *
   * @return an new HalObject
   */
  public HalObject lenient() {
    return lenient ? this : new HalObject(self, singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, data, true, order, suppressNulls);
  }

  /**
   * By default using reserved keys will return an exception to the subscriber. This will re-enable the default functionality.
   *
   * @return an new HalObject
   */
  public HalObject strict() {
    return !lenient ? this : new HalObject(self, singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, data, false, order, suppressNulls);
  }

  /**
   * Set the self rel. This means people can't accidentally specify an array of self rels.
   *
   * @param self the self link
   * @return a new HalObject with the self link set to the given value
   */
  public HalObject self(HalLink self) {
    return new HalObject(Optional.of(self), singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, data, lenient, order, suppressNulls);
  }

  /**
   * Replace the given rel with a single value link.
   *
   * @param rel the relation name
   * @param link the relation link
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject putLink(String rel, HalLink link) {
    Objects.requireNonNull(rel, "putLink requires a non-null rel");
    Objects.requireNonNull(link, "putLink requires a non-null link");

    return putLink(rel, Observable.just(link));
  }


  /**
   * Replace the given rel with a single value link, using the first value returned
   * or <code>null</code> if the observable is empty.
   *
   * @param rel the relation name
   * @param link the relation link
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject putLink(String rel, Observable<HalLink> link) {
    Objects.requireNonNull(rel, "putLink requires a non-null rel");
    Objects.requireNonNull(link, "putLink requires a non-null link");
    requireRelNotSelf(rel);

    Map<String, Observable<HalLink>> newSingleton = new HashMap<>(singletonLinks);
    Map<String, Observable<HalLink>> newArray = new HashMap<>(arrayLinks);
    newSingleton.put(rel, link.take(1));
    newArray.remove(rel);
    return new HalObject(self, newSingleton, newArray, singletonEmbedded, arrayEmbedded, data, lenient, order, suppressNulls);
  }

  /**
   * Replace the given rel with an array of links.
   *
   * @param rel the relation name
   * @param links the relation links
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject putAllLinks(String rel, Observable<HalLink> links) {
    Objects.requireNonNull(rel, "putAllLinks requires a non-null rel");
    Objects.requireNonNull(links, "putAllLinks requires a non-null links");
    requireRelNotSelf(rel);

    Map<String, Observable<HalLink>> newSingleton = new HashMap<>(singletonLinks);
    Map<String, Observable<HalLink>> newArray = new HashMap<>(arrayLinks);
    newSingleton.remove(rel);
    newArray.put(rel, links);
    return new HalObject(self, newSingleton, newArray, singletonEmbedded, arrayEmbedded, data, lenient, order, suppressNulls);
  }

  /**
   * Replace the given rel with an array of links.
   *
   * @param rel the relation name
   * @param links the relation links
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject putAllLinks(String rel, Iterable<HalLink> links) {
    Objects.requireNonNull(rel, "putAllLinks requires a non-null rel");
    Objects.requireNonNull(links, "putAllLinks requires a non-null links");
    requireRelNotSelf(rel);

    return putAllLinks(rel, Observable.from(links));
  }

  /**
   * Replace the given rel with an array of links.
   *
   * @param rel the relation name
   * @param links the relation links
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject putAllLinks(String rel, HalLink... links) {
    Objects.requireNonNull(rel, "putAllLinks requires a non-null rel");
    Objects.requireNonNull(links, "putAllLinks requires a non-null links");
    requireRelNotSelf(rel);

    return putAllLinks(rel, Observable.from(links));
  }

  /**
   * Append a link to the given rel. If the rel already contains single value link
   * this will be converted to an array of links.
   *
   * @param rel the relation name
   * @param link the relation link
   * @return a new HalObject with the link added as part of an array
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject appendLink(String rel, HalLink link) {
    Objects.requireNonNull(rel, "appendLink requires a non-null rel");
    Objects.requireNonNull(link, "appendLink requires a non-null link");
    requireRelNotSelf(rel);

    return appendLinks(rel, Observable.just(link));
  }

  /**
   * Append links to the given rel. If the rel contains single value link
   * this will be converted to an array of links.
   *
   * @param rel the relation name
   * @param links the relation links
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject appendLinks(String rel, Observable<HalLink> links) {
    Objects.requireNonNull(rel, "appendLinks requires a non-null rel");
    Objects.requireNonNull(links, "appendLinks requires a non-null links");
    requireRelNotSelf(rel);

    Map<String, Observable<HalLink>> newSingleton = new HashMap<>(singletonLinks);
    Map<String, Observable<HalLink>> newArray = new HashMap<>(arrayLinks);
    Observable<HalLink> newValue = newArray.containsKey(rel) ?
      newArray.get(rel).concatWith(links) :
      (
        newSingleton.containsKey(rel) ?
          newSingleton.get(rel).concatWith(links) :
          links
      );
    newSingleton.remove(rel);
    newArray.put(rel, newValue);
    return new HalObject(self, newSingleton, newArray, singletonEmbedded, arrayEmbedded, data, lenient, order, suppressNulls);
  }

  /**
   * Append links to the given rel. If the rel contains single value link
   * this will be converted to an array of links.
   *
   * @param rel the relation name
   * @param links the relation links
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject appendLinks(String rel, Iterable<HalLink> links) {
    Objects.requireNonNull(rel, "appendLinks requires a non-null rel");
    Objects.requireNonNull(links, "appendLinks requires a non-null links");
    requireRelNotSelf(rel);

    return appendLinks(rel, Observable.from(links));
  }

  /**
   * Append links to the given rel. If the rel contains single value link
   * this will be converted to an array of links.
   *
   * @param rel the relation name
   * @param links the relation links
   * @return a new HalObject
   * @throws HalKeyException if the rel 'self' is specified
   */
  public HalObject appendLinks(String rel, HalLink... links) {
    Objects.requireNonNull(rel, "appendLinks requires a non-null rel");
    Objects.requireNonNull(links, "appendLinks requires a non-null links");
    requireRelNotSelf(rel);

    return appendLinks(rel, Observable.from(links));
  }

  /**
   * Replace the given embedded with a single value HalObject.
   *
   * @param key the object name
   * @param embedded the embedded object
   * @return a new HalObject
   */
  public HalObject putEmbedded(String key, HalObject embedded) {
    Objects.requireNonNull(key, "putAllEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "putAllEmbedded requires a non-null embedded");

    return putEmbedded(key, Observable.just(embedded));
  }

  /**
   * Replace the given embedded with a single value HalObject, using the first value returned
   * or <code>null</code> if the observable is empty.
   *
   * @param key the object name
   * @param embedded the embedded object
   * @return a new HalObject
   */
  public HalObject putEmbedded(String key, Observable<HalObject> embedded) {
    Objects.requireNonNull(key, "putAllEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "putAllEmbedded requires a non-null embedded");

    Map<String, Observable<HalObject>> newSingleton = new HashMap<>(singletonEmbedded);
    Map<String, Observable<HalObject>> newArray = new HashMap<>(arrayEmbedded);
    newSingleton.put(key, embedded.take(1));
    newArray.remove(key);
    return new HalObject(self, singletonLinks, arrayLinks, newSingleton, newArray, data, lenient, order, suppressNulls);
  }

  /**
   * Replace the given embedded with an array of HalObjects.
   *
   * @param key the object name
   * @param embedded the embedded objects
   * @return a new HalObject
   */
  public HalObject putAllEmbedded(String key, Observable<HalObject> embedded) {
    Objects.requireNonNull(key, "putAllEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "putAllEmbedded requires a non-null embedded");

    Map<String, Observable<HalObject>> newSingleton = new HashMap<>(singletonEmbedded);
    Map<String, Observable<HalObject>> newArray = new HashMap<>(arrayEmbedded);
    newSingleton.remove(key);
    newArray.put(key, embedded);
    return new HalObject(self, singletonLinks, arrayLinks, newSingleton, newArray, data, lenient, order, suppressNulls);
  }

  /**
   * Replace the given embedded with an array of HalObjects.
   *
   * @param key the object name
   * @param embedded the embedded objects
   * @return a new HalObject
   */
  public HalObject putAllEmbedded(String key, Iterable<HalObject> embedded) {
    Objects.requireNonNull(key, "putAllEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "putAllEmbedded requires a non-null embedded");

    return putAllEmbedded(key, Observable.from(embedded));
  }

  /**
   * Replace the given embedded with an array of HalObjects.
   *
   * @param key the object name
   * @param embedded the embedded objects
   * @return a new HalObject
   */
  public HalObject putAllEmbedded(String key, HalObject... embedded) {
    Objects.requireNonNull(key, "putAllEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "putAllEmbedded requires a non-null embedded");

    return putAllEmbedded(key, Observable.from(embedded));
  }

  /**
   * Append a HalObject to the given key. If the key contains single value object
   * this will be converted to an array of objects.
   *
   * @param key the object name
   * @param embedded the embedded objects
   * @return a new HalObject with the object added as part of an array
   */
  public HalObject appendEmbedded(String key, HalObject embedded) {
    Objects.requireNonNull(key, "appendEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "appendEmbedded requires a non-null embedded");

    return appendEmbedded(key, Observable.just(embedded));
  }

  /**
   * Append HalObjects to the given key. If the key contains single value object
   * this will be converted to an array of objects.
   *
   * @param key the object name
   * @param embedded the embedded objects
   * @return a new HalObject
   */
  public HalObject appendEmbedded(String key, Observable<HalObject> embedded) {
    Objects.requireNonNull(key, "appendEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "appendEmbedded requires a non-null embedded");

    Map<String, Observable<HalObject>> newSingleton = new HashMap<>(singletonEmbedded);
    Map<String, Observable<HalObject>> newArray = new HashMap<>(arrayEmbedded);
    Observable<HalObject> newValue = newArray.containsKey(key) ?
        newArray.get(key).concatWith(embedded) :
        (
            newSingleton.containsKey(key) ?
                newSingleton.get(key).concatWith(embedded) :
                embedded
        );
    newSingleton.remove(key);
    newArray.put(key, newValue);
    return new HalObject(self, singletonLinks, arrayLinks, newSingleton, newArray, data, lenient, order, suppressNulls);
  }

  /**
   * Append HalObjects to the given key. If the key contains single value object
   * this will be converted to an array of objects.
   *
   * @param key the object name
   * @param embedded the embedded objects
   * @return a new HalObject
   */
  public HalObject appendEmbedded(String key, Iterable<HalObject> embedded) {
    Objects.requireNonNull(key, "appendEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "appendEmbedded requires a non-null embedded");

    return appendEmbedded(key, Observable.from(embedded));
  }

  /**
   * Append HalObjects to the given key. If the key contains single value object
   * this will be converted to an array of objects.
   *
   * @param key the object name
   * @param embedded the embedded objects
   * @return a new HalObject
   */
  public HalObject appendEmbedded(String key, HalObject... embedded) {
    Objects.requireNonNull(key, "appendEmbedded requires a non-null key");
    Objects.requireNonNull(embedded, "appendEmbedded requires a non-null embedded");

    return appendEmbedded(key, Observable.from(embedded));
  }

  /**
   * Append data to the HalObject. This does not check for duplicate key names.
   *
   * @param key the data key name
   * @param data the data to append
   * @return a new HalObject
   * @throws HalKeyException if HAL reserved keys are used
   */
  public HalObject appendData(String key, JsonElement data) {
    Objects.requireNonNull(key, "appendData requires a non-null key");
    Objects.requireNonNull(data, "appendData requires a non-null data");
    requireKeyNotReserved(key);

    return appendData(JsonObject.entry(key, data));
  }

  /**
   * Append data to the HalObject. This does not check for duplicate key names.
   *
   * @param key the data key name
   * @param data the data to append
   * @return a new HalObject
   * @throws HalKeyException if HAL reserved keys are used
   */
  public HalObject appendData(String key, Observable<JsonElement> data) {
    Objects.requireNonNull(key, "appendData requires a non-null key");
    Objects.requireNonNull(data, "appendData requires a non-null data");
    requireKeyNotReserved(key);

    return appendData(JsonObject.entry(key, data));
  }

  /**
   * Append data to the HalObject. This does not check for duplicate key names.
   *
   * @param data the data to append as JsonObject Entries
   * @return a new HalObject
   */
  public HalObject appendData(Iterable<JsonObject.Entry<JsonElement>> data) {
    Objects.requireNonNull(data, "appendData requires a non-null data");

    return appendData(Observable.from(data));
  }

  /**
   * Append data to the HalObject. This does not check for duplicate key names.
   *
   * @param data the data to append as JsonObject Entries
   * @return a new HalObject
   */
  @SafeVarargs
  public final HalObject appendData(JsonObject.Entry<JsonElement>... data) {
    Objects.requireNonNull(data, "appendData requires a non-null data");

    return appendData(Observable.from(data));
  }

  /**
   * Append data to the HalObject. This does not check for duplicate key names.
   *
   * @param data the data to append as JsonObject Entries
   * @return a new HalObject
   */
  public HalObject appendData(Observable<JsonObject.Entry<JsonElement>> data) {
    Objects.requireNonNull(data, "appendData requires a non-null data");

    return new HalObject(self, singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, this.data.concatWith(data), lenient, order, suppressNulls);
  }

  /**
   * Set a specific ordering for the emission of _links, _embedded and the data.
   * This can be used to optimise streaming where the a shared observable emits
   * to more than one of _links, _embedded or the data.
   *
   * @param order the order of emission
   * @return a new HalObject
   */
  public HalObject withOrder(Order order) {
    Objects.requireNonNull(order, "withOrder requires a non-null order");

    return new HalObject(self, singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, data, lenient, order, suppressNulls);
  }

  public HalObject suppressNulls() {
    return new HalObject(self, singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, data, lenient, order, true);
  }

  public HalObject suppressNulls(boolean suppress) {
    return new HalObject(self, singletonLinks, arrayLinks, singletonEmbedded, arrayEmbedded, data, lenient, order, suppress);
  }

  private void requireRelNotSelf(String rel) {
    if (Holder.SELF.equals(rel)) {
      throw new HalKeyException(Holder.LINK_SELF_CAN_ONLY_BE_SET_USING_SELF);
    }
  }

  private void requireKeyNotReserved(String key) {
    if (Holder.LINKS.equals(key) || Holder.EMBEDDED.equals(key)) {
      throw new HalKeyException(String.format(Holder.DATA_CANNOT_USE_RESERVED_PROPERTY, key));
    }
  }

  private static Observable<JsonObject.Entry<JsonElement>> getOrderedElements(
    Optional<HalLink> self,
    Map<String, Observable<HalLink>> singletonLinks,
    Map<String, Observable<HalLink>> arrayLinks,
    Map<String, Observable<HalObject>> singletonEmbedded,
    Map<String, Observable<HalObject>> arrayEmbedded,
    Observable<JsonObject.Entry<JsonElement>> data,
    boolean lenient,
    Order order,
    boolean suppressNulls
  ) {
    Observable<JsonObject.Entry<JsonElement>> links = getLinks(self, singletonLinks, arrayLinks, lenient, suppressNulls);
    Observable<JsonObject.Entry<JsonElement>> embedded = getEmbedded(singletonEmbedded, arrayEmbedded, suppressNulls);
    Observable<JsonObject.Entry<JsonElement>> _data = getData(lenient, data);

    switch (order) {
      case LinksEmbeddedData:
        return links.concatWith(embedded).concatWith(_data);
      case LinksDataEmbedded:
        return links.concatWith(_data).concatWith(embedded);
      case EmbeddedLinksData:
        return embedded.concatWith(links).concatWith(_data);
      case EmbeddedDataLinks:
        return embedded.concatWith(_data).concatWith(links);
      case DataLinksEmbedded:
        return _data.concatWith(links).concatWith(embedded);
      case DataEmbeddedLinks:
        return _data.concatWith(embedded).concatWith(links);
    }
    return Observable.error(new RuntimeException());
  }

  private static Observable<JsonObject.Entry<JsonElement>> getData(
    boolean lenient,
    Observable<JsonObject.Entry<JsonElement>> data
  ) {
    return data.concatMap(e -> {
      if (e.getKey().equals(Holder.LINKS) || e.getKey().equals(Holder.EMBEDDED)) {
        if (lenient) {
          return Observable.empty();
        }
        return Observable.error(new HalKeyException(String.format(Holder.DATA_CANNOT_USE_RESERVED_PROPERTY, e.getKey())));
      }
      return Observable.just(e);
    });
  }

  private static Observable<JsonObject.Entry<JsonElement>> getEmbedded(
    Map<String, Observable<HalObject>> singletonEmbedded,
    Map<String, Observable<HalObject>> arrayEmbedded,
    boolean suppressNulls
  ) {
    return !singletonEmbedded.isEmpty() || !arrayEmbedded.isEmpty() ?
      Observable.just(
        JsonObject.entry(
          Holder.EMBEDDED,
          JsonObject.of(
            Observable.from(singletonEmbedded.entrySet())
              .concatMap(
                e ->
                  e.getValue()
                    .map(v -> JsonObject.<JsonElement>entry(e.getKey(), v))
                    .switchIfEmpty(
                      suppressNulls ?
                        Observable.empty() :
                        Observable.just(JsonObject.<JsonElement>entry(e.getKey(), RxJson.valueBuilder().Null()))
                    )
              )
              .concatWith(
                Observable.from(arrayEmbedded.entrySet())
                  .map(e -> JsonObject.entry(e.getKey(), JsonArray.of(e.getValue())))
              )
          )
        )
      ) :
      Observable.empty();
  }

  private static Observable<JsonObject.Entry<JsonElement>> getLinks(
    Optional<HalLink> self,
    Map<String, Observable<HalLink>> singletonLinks,
    Map<String, Observable<HalLink>> arrayLinks,
    boolean lenient,
    boolean suppressNulls
  ) {
    return self.isPresent() || !singletonLinks.isEmpty() || !arrayLinks.isEmpty() ?
      Observable.just(
        JsonObject.entry(
          Holder.LINKS,
          JsonObject.of(
            self.map(s -> Observable.<JsonObject.Entry<JsonElement>>just(JsonObject.entry(Holder.SELF, s))).orElse(Observable.empty())
              .concatWith(
                Observable.from(singletonLinks.entrySet())
                  .concatMap(
                    e ->
                      e.getValue()
                        .map(v -> JsonObject.<JsonElement>entry(e.getKey(), v))
                        .switchIfEmpty(
                          suppressNulls ?
                            Observable.empty() :
                            Observable.just(JsonObject.<JsonElement>entry(e.getKey(), RxJson.valueBuilder().Null()))
                        )
                  )
                  .concatWith(
                    Observable.from(arrayLinks.entrySet())
                      .map(e -> JsonObject.entry(e.getKey(), JsonArray.of(e.getValue())))
                  )
                  .concatMap(e -> {
                    if (e.getKey().equals(Holder.SELF)) {
                      if (lenient) {
                        return Observable.empty();
                      }
                      return Observable.error(new HalKeyException(Holder.LINK_SELF_CAN_ONLY_BE_SET_USING_SELF));
                    }
                    return Observable.just(e);
                  })
              )
          )
        )
      ) :
      Observable.empty();
  }

  public enum Order {
    LinksEmbeddedData,
    LinksDataEmbedded,
    EmbeddedLinksData,
    EmbeddedDataLinks,
    DataLinksEmbedded,
    DataEmbeddedLinks
  }
}
