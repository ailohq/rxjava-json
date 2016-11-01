package com.trunk.rx.json.hal;

import com.trunk.rx.json.element.JsonElement;
import com.trunk.rx.json.element.JsonObject;
import com.trunk.rx.json.element.JsonValueBuilder;
import rx.Observable;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An immutable HAL link object.
 */
public class HalLink extends JsonElement {

  private final Optional<String> name;
  private final URI href;
  private final Optional<Boolean> templated;
  private final Optional<String> type;
  private final Optional<String> deprecation;
  private final Optional<URI> profile;
  private final Optional<Locale> hreflang;
  private final Optional<String> title;

  public static HalLink create(URI href) {
    return new HalLink(Optional.empty(), href, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
  }

  public static HalLink create(String href) {
    return create(URI.create(href));
  }

  protected HalLink(Optional<String> name, URI href, Optional<Boolean> templated, Optional<String> type, Optional<String> deprecation, Optional<URI> profile, Optional<Locale> hreflang, Optional<String> title) {
    super(
      JsonObject.of(
        Observable.from(
          Stream.<Optional<JsonObject.Entry<JsonElement>>>builder()
            .add(deprecation.map(d -> JsonObject.entry("deprecation", JsonValueBuilder.instance().create(d))))
            .add(Optional.of(JsonObject.entry("href", JsonValueBuilder.instance().create(href.toASCIIString()))))
            .add(hreflang.map(h -> JsonObject.entry("hreflang", JsonValueBuilder.instance().create(h.toString()))))
            .add(name.map(n -> JsonObject.entry("name", JsonValueBuilder.instance().create(n))))
            .add(profile.map(p -> JsonObject.entry("profile", JsonValueBuilder.instance().create(p.toASCIIString()))))
            .add(templated.map(t -> JsonObject.entry("templated", JsonValueBuilder.instance().create(t))))
            .add(title.map(t -> JsonObject.entry("title", JsonValueBuilder.instance().create(t))))
            .add(type.map(t -> JsonObject.entry("type", JsonValueBuilder.instance().create(t))))
            .build()
            .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
            .collect(Collectors.toList())
        )
      )
    );
    this.name = name;
    this.href = href;
    this.templated = templated;
    this.type = type;
    this.deprecation = deprecation;
    this.profile = profile;
    this.hreflang = hreflang;
    this.title = title;
  }

  /**
   * Its value MAY be used as a secondary key for selecting Link Objects
   * which share the same relation type.
   */
  public HalLink name(String name) {
    return new HalLink(Optional.ofNullable(name), href, templated, type, deprecation, profile, hreflang, title);
  }

  /**
   * Its value is boolean and SHOULD be true when the Link Object's "href"
   * property is a URI Template.
   *
   * Its value SHOULD be considered false if it is undefined or any other
   * value than true.
   */
  public HalLink templated(boolean templated) {
    return new HalLink(name, href, Optional.ofNullable(templated), type, deprecation, profile, hreflang, title);
  }

  /**
   * Its value is a string used as a hint to indicate the media type
   * expected when dereferencing the target resource.
   */
  public HalLink type(String type) {
    return new HalLink(name, href, templated, Optional.ofNullable(type), deprecation, profile, hreflang, title);
  }

  /**
   * Its presence indicates that the link is to be deprecated (i.e.
   * removed) at a future date.  Its value is a URL that SHOULD provide
   * further information about the deprecation.

   * A client SHOULD provide some notification (for example, by logging a
   * warning message) whenever it traverses over a link that has this
   * property.  The notification SHOULD include the deprecation property's
   * value so that a client maintainer can easily find information about
   * the deprecation.
   */
  public HalLink deprecation(String deprecation) {
    return new HalLink(name, href, templated, type, Optional.ofNullable(deprecation), profile, hreflang, title);
  }

  /**
   * Its value is a string which is a URI that hints about the profile (as
   * defined by <a href="https://tools.ietf.org/html/draft-kelly-json-hal-07#ref-I-D.wilde-profile-link">[I-D.wilde-profile-link]</a>)
   * of the target resource.
   */
  public HalLink profile(URI profile) {
    return new HalLink(name, href, templated, type, deprecation, Optional.ofNullable(profile), hreflang, title);
  }

  /**
   * Its value is a string and is intended for labelling the link with a
   * human-readable identifier (as defined by <a href="https://tools.ietf.org/html/rfc5988">[RFC5988]</a>).
   */
  public HalLink title(String title) {
    return new HalLink(name, href, templated, type, deprecation, profile, hreflang, Optional.ofNullable(title));
  }

  /**
   * Its value is a string and is intended for indicating the language of
   * the target resource (as defined by <a href="https://tools.ietf.org/html/rfc5988">[RFC5988]</a>).
   */
  public HalLink hreflang(Locale hreflang) {
    return new HalLink(name, href, templated, type, deprecation, profile, Optional.ofNullable(hreflang), title);
  }
}
