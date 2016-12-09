# rxjava-json
JSON stream handling for RxJava

TODO build and release lozenges

A library to let you stream JSON objects in [RxJava](https://github.com/ReactiveX/RxJava) with full
[backpressure](https://github.com/ReactiveX/RxJava/wiki/Backpressure) support.
Parsing uses a simplified [JsonPath](http://goessner.net/articles/JsonPath/) to find 
fragments. Writing is done by composing JsonElements, which can be 
subscribed to to emit JSON tokens.

The main driver for developing this library was to improve memory 
management when handling large list like JSON payloads. It allows us to keep the minimum number 
of objects in memory at any one time, while leveraging backpressure in
libraries like [RxNetty](https://github.com/ReactiveX/RxNetty).

## Building

`rxjava-json` uses [Gradle](http://gradle.org/) for build.

```bash
git clone https://github.com/Trunkplatform/rxjava-json.git
cd rxjava-json
./gradlew clean build
```


## rxjava-json-core

TBC maven/gradle

### Parsing

The core parser will extract a subset of JSON tokens from an Observable
of Strings by matching them against a set of given JsonPaths. This is
designed to feed into an un-marshalling process but is agnostic as to 
what that is. `rxjava-json-gson` provides an entry point for unmarshalling
using Gson.

```java
// our source JSON document
Observable<String> source = ...;

// all tokens, strict parsing
Observable<JsonPathEvent> o1 = source.compose(RxJson.from("$"));

// all links
Observable<JsonPathEvent> o1 = source.compose(RxJson.from("$._links"));


// all links, aggregated into objects
Observable<JsonObjectEvent> o1 = source.compose(RxJson.from("$._links").collectObjects());
```

#### Supported JsonPath

Only a subset of [JsonPath](http://goessner.net/articles/JsonPath/) is supported:

| JSONPath           | Description                                                                                                                                       |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `$`                | the root object/element                                                                                                                           |
| `.` or `[]`        | child operator                                                                                                                                    |
| `*`                | wildcard. All objects/elements regardless their names.                                                                                            |
| `[]`               | subscript operator. XPath uses it to iterate over element collections and for predicates. In Javascript and JSON it is the native array operator. Only positive positions are allowed. |
| `[,]`              | Union operator in XPath results in a combination of node sets. JSONPath allows alternate names or array indices as a set.                         |
| `[start:end:step]` | array slice operator borrowed from ES4. Only positive values are allowed |
| `$..*`             | all members of JSON structure. This is equivalent to `$` |

No operations that rely on knowledge outside of the current token are allowed, such as value queries, or cross element comparisons.
Where multiple matches can be made, only the most general will be returned (eg, `$.foo` will be prefered to `$.foo.bar`). 

See [JsonPathParserTest](https://github.com/Trunkplatform/rxjava-json/blob/master/rxjava-json-core/src/test/java/com/trunk/rx/json/path/JsonPathParserTest.java)
for examples.

### Writing

The core provides a set of JSON elements that can be composed to produce
an Observable of JSON tokens. These in turn can be converted to an
Observable of Strings. `JsonRaw` is provided to allow third-party JSON
marshalling libraries to produce raw output to put in the stream.

All the core JSON types are supported. Note that `JsonObject` _does not_ 
check for duplicate keys.

The recommended method for producing JSON streams is to construct the
root JSON entities using the core elements, then use a third party library such as Gson
to add values into these. See `rxjava-json-gson` below for an example of this.

```java
// create values
RxJson.valueBuilder().create("a string");
RxJson.valueBuilder().create(25);
RxJson.valueBuilder().create(false);
RxJson.valueBuilder().lenient().quoteLargeNumbers().create(BigInteger.valueOf("100000000000000000000000000000"));
RxJson.valueBuilder().Null();

// create an array
RxJson.newArray(); // an empty array
RxJson.newArray().add(RxJson.valueBuilder().create("a string"));
RxJson.newArray().addAll(Observable.just(RxJson.valueBuilder().create("a string")));

RxJson.newArray(RxJson.valueBuilder().create("a string"), RxJson.valueBuilder().create(1));
RxJson.newArray(Observable.just(RxJson.valueBuilder().create("a string"), RxJson.valueBuilder().create(1)));

// create an object and add to it
RxJson.newObject(); // an empty object
RxJson.newObject().add("key", RxJson.valueBuilder().create("a string"));
RxJson.newObject().addAll(Observable.just(JsonObject.entry("key", RxJson.valueBuilder().create("a string"))));

RxJson.newObject(JsonObject.entry("key 1", RxJson.valueBuilder().create("a string")), JsonObject.entry("key 2", RxJson.valueBuilder().create("a string")));
RxJson.newObject(Observable.just(JsonObject.entry("key 1", RxJson.valueBuilder().create("a string")), JsonObject.entry("key 2", RxJson.valueBuilder().create("a string"))));

// create a raw value
RxJson.newRaw("{\"key\":\"value\"}")

// convert to JSON
JsonElement e = ...;
e.compose(RxJson.toJson());
```


## rxjava-json-hal

TBC maven/gradle

A library to help building [HAL](http://stateless.co/hal_specification.html) objects in RxJavaJson.

```java
// {
//   "_links": {
//     "self": { "href": "/path", "title": "name" }, 
//     "next": { "href": "/path?page=2", "title": "name - page 2" } 
//   },
//   "_embedded": {
//     "child": [
//       {},
//       {}
//     ]
//   },
//   "foo": "bar",
//   "bar": "baz"
// }

HalObject.create()
  .self(HalLink.create(Uri.create("/path")).title("name"))
  .putLink("next", HalLink.create(Uri.create("/path?page=2")).title("name - page 2"))
  .appendEmbedded("child", HalObject.create())
  .appendEmbedded("child", HalObject.create())
  .appendData("foo", JsonValueBuilder.instance().create("bar"))
  .appendData(Observable.just(JsonObject.entry("bar", JsonValueBuilder.instance().create("baz")));
```

## rxjava-json-gson

TBC maven/gradle

### Parsing

`RxJavaGson` adds an additional layer that will return java objects created using Gson.
By default it uses the default Gson configuration and unmarshalls to an `Object`.


```
// our source JSON document
Observable<String> source = ...;

// root as an object
Observable<Object> o1 = source.compose(RxJsonGson.from("$"));

// links as an object
Observable<Object> o1 = source.compose(RxJsonGson.from("$._links"));

// links as an Link
Observable<Link> o1 = source.compose(
  RxJsonGson
    .from("$._links")
    .to(Link.class)
);

// links as an Link with lenient parsing
Observable<Link> o1 = source.compose(
  RxJsonGson
    .from("$._links")
    .using(new GsonBuilder.lenient().create())
    .lenient()
    .to(Link.class)
);

// links and embedded as HalObjects
Observable<HalObject> o1 = source.compose(
  RxJsonGson
    .from("$._links", "$._embedded")
    .using((jsonPath, jsonElement, gson) -> {
        if (jsonPath.equals(JsonPath.parse("$._links")) {
            return gson.fromJson(jsonElement, Link.class);
        }
        return gson.fromJson(jsonElement, Embedded.class);
    }
);
```

### Writing

`GsonJsonElementBuilder` produces `Observable<JsonToken>` using Gson. It
can use either the default or a custom Gson.

```
Observable<?> source = ...;

RxJson.newArray(source.compose(RxJsonGson.toJsonElements()));
```


## Performance

TBD

##License

Code ported from Gson is [copyright Google Inc](https://github.com/google/gson).

All other material copyright 2016 Trunk Platform.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
