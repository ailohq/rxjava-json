package com.trunk.rx.json.transformer;

import com.trunk.rx.character.CharacterObservable;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.JsonTokenEvent;
import com.trunk.rx.json.operator.OperatorJsonToken;
import com.trunk.rx.json.operator.OperatorJsonTokenTest;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.path.NoopToken;
import com.trunk.rx.json.path.RootToken;
import com.trunk.rx.json.token.JsonArray;
import com.trunk.rx.json.token.JsonDocumentEnd;
import com.trunk.rx.json.token.JsonObject;
import com.trunk.rx.json.token.JsonString;
import com.trunk.rx.json.token.JsonToken;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.testng.Assert.assertEquals;

public class TransformerJsonPathTest {

  private static final OperatorJsonToken STRICT_PARSER = new OperatorJsonToken();
  private static final OperatorJsonToken LENIENT_PARSER = STRICT_PARSER.lenient();

  @Test
  public void shouldSkipAllWhenNoMatches() throws Exception {
    TestSubscriber<JsonPathEvent> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.zzz")))
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(new JsonPathEvent(NoopToken.instance(), new JsonTokenEvent(JsonDocumentEnd.instance(), NoopToken.instance())));
  }

  @Test
  public void shouldReturnSingleGroupForRootPath() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$")))
      .map(JsonPathEvent::getMatchedPathFragment)
      .distinctUntilChanged()
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(RootToken.instance(), NoopToken.instance());
  }

  @Test
  public void shouldReturnAllElementsForRootPath() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$")))
      .map(e -> e.getTokenEvent().getToken())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    assertEquals(
      ts.getOnNextEvents(),
      Observable.just(OperatorJsonTokenTest.bigObject())
        .lift(CharacterObservable.toCharacter())
        .lift(STRICT_PARSER)
        .map(JsonTokenEvent::getToken)
        .toList()
        .toBlocking()
        .single()
    );
  }

  @Test
  public void shouldReturnSingleGroupForAllPath() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(JsonPathEvent::getMatchedPathFragment)
      .distinctUntilChanged()
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(RootToken.instance(), NoopToken.instance());
  }

  @Test
  public void shouldReturnAllElementsForAllPath() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(e -> e.getTokenEvent().getToken())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    assertEquals(
      ts.getOnNextEvents(),
      CharacterObservable.from(OperatorJsonTokenTest.bigObject())
        .lift(STRICT_PARSER)
        .map(JsonTokenEvent::getToken)
        .toList()
        .toBlocking()
        .single()
    );
  }

  @Test
  public void shouldReturnGroupForSingleMatch() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":{\"b\":[1,2,3,4,5,6]}}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.a.b")))
      .map(JsonPathEvent::getMatchedPathFragment)
      .distinctUntilChanged()
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(JsonPath.parse("$.a.b"));
  }

  @Test
  public void shouldReturnManyMatches() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")))
      .map(JsonPathEvent::getMatchedPathFragment)
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(JsonPath.parse("$.b[0]"), JsonPath.parse("$.b[1]"), JsonPath.parse("$.b[2]"), JsonPath.parse("$.b[3]"));
  }

  @Test
  public void shouldReturnCompleteCorrectlyOnManyMatches() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")))
      .map(JsonPathEvent::getMatchedPathFragment)
      .take(1)
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(JsonPath.parse("$.b[0]"));
  }

  @Test
  public void shouldExitEarlyWhenLastPossibleMatchMade() throws Exception {
    StringBuilder buf = new StringBuilder();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(CharacterObservable.toCharacter())
      .doOnNext(buf::append)
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")))
      .map(JsonPathEvent::getMatchedPathFragment)
      .subscribe();

    assertEquals(buf.toString(), "{\"a\":1234,\"b\":[1,2,3,4],\"c\"");
  }

  @Test
  public void shouldNotExitEarlyWhenLenient() throws Exception {
    StringBuilder buf = new StringBuilder();
    String value = "{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}";
    Observable.just(value)
      .lift(CharacterObservable.toCharacter())
      .doOnNext(buf::append)
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")).lenient())
      .map(JsonPathEvent::getMatchedPathFragment)
      .subscribe();

    assertEquals(buf.toString(), value);
  }

  @Test
  public void shouldTestExitOnFirstDocumentDocumentsWhenStrict() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    String value = "{}{}";
    Observable.just(value)
      .lift(CharacterObservable.toCharacter())
      .lift(LENIENT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(JsonPathEvent::getMatchedPathFragment)
      .subscribe(ts);

    ts.assertValues(
      JsonPath.parse("$"), JsonPath.parse("$"), NoopToken.instance()
    );
  }

  @Test
  public void shouldTestMultipleDocumentsWhenLenient() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    String value = "{\"a\":{\"b\":[1,2,3,4,5,6]}} {\"a\":{\"b\":[1,2,3,4,5,6]}} {\"a\":{\"b\":[1,2,3,4,5,6]}}";
    Observable.just(value)
      .lift(CharacterObservable.toCharacter())
      .lift(LENIENT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.a.b[1]")).lenient())
      .map(JsonPathEvent::getMatchedPathFragment)
      .subscribe(ts);

    ts.assertValues(
      JsonPath.parse("$.a.b[1]"), NoopToken.instance(),
      JsonPath.parse("$.a.b[1]"), NoopToken.instance(),
      JsonPath.parse("$.a.b[1]"), NoopToken.instance()
    );
  }

  @Test
  public void backPressureShouldWorkCorrectly() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    ts.requestMore(0);

    int[] emitted = { 0 };

    Observable.just(
      new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonArray.end(), JsonPath.parse("$")),
      new JsonTokenEvent(JsonDocumentEnd.instance(), NoopToken.instance())
    )
      .doOnNext(t -> emitted[0] = emitted[0] + 1)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(e -> e.getTokenEvent().getToken())
      .subscribe(ts);

    ts.assertNoValues();

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 1);
    assertEquals(emitted[0], 4); // concatMap is eager on the next element

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 2);
    assertEquals(emitted[0], 5);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 3);
    assertEquals(emitted[0], 5);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 4);
    assertEquals(emitted[0], 5);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 5);
    assertEquals(emitted[0], 5);

    ts.assertCompleted();
  }

  @Test
  public void backPressureShouldWorkCorrectlyOnPathGroups() throws Exception {
    TestSubscriber<JsonPathEvent> ts = new TestSubscriber<>();
    ts.requestMore(0);

    int[] emitted = { 0 };

    Observable.just(
      new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[1]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[1]")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[2]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[2]")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[3]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[3]")),
      new JsonTokenEvent(JsonArray.end(), JsonPath.parse("$"))
    ).concatWith(Observable.just(new JsonTokenEvent(JsonDocumentEnd.instance(), NoopToken.instance())))
      .doOnNext(t -> emitted[0] = emitted[0] + 1)
      .compose(TransformerJsonPath.from(JsonPath.parse("$[*]")))
      .subscribe(ts);

    ts.assertNoValues();

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 1);
    assertEquals(emitted[0], 5); // concatMap is eager on the next element

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 2);
    assertEquals(emitted[0], 6);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 3);
    assertEquals(emitted[0], 7);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 4);
    assertEquals(emitted[0], 8);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 5);
    assertEquals(emitted[0], 9);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 6);
    assertEquals(emitted[0], 10);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 7);
    assertEquals(emitted[0], 11);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 8);
    assertEquals(emitted[0], 11);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 9);
    assertEquals(emitted[0], 11);

    ts.assertCompleted();
  }

  @Test
  public void shouldBeAbleToReuse() throws Exception {
    TestSubscriber<String> ts = new TestSubscriber<>();

    TransformerJsonPath transformerJsonPath = TransformerJsonPath.from(JsonPath.parse("$.a.b.*"));

    Observable.just("{\"a\":{\"b\":[1,2,3,4,5,6]}}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(transformerJsonPath)
      .map(e -> e.getTokenEvent().getToken())
      .subscribe();

    Observable.just("{\"a\":{\"b\":[1,2,3,4,5,6]}}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(transformerJsonPath)
      .map(e -> e.getTokenEvent().getToken().value())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues("1","2","3","4","5","6");
  }

  @Test
  public void shouldParseMultipleNestedWildcardPaths() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just("{\n" +
      "    \"name\": \"John Smth\",\n" +
      "    \"roleName\": \"PM Team Lead\",\n" +
      "    \"status\": \"Terminated\",\n" +
      "    \"_links\": {\n" +
      "        \"self\": {\"href\": \"/groups/1/settings/staff/100\"},\n" +
      "        \"employmentHistories\": { \n" +
      "            \"href\": \"/groups/1/settings/staff/100/employmentHistories\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"_embedded\":{\n" +
      "        \"xeroEmployee\": {\n" +
      "            \"name\": \"John H. Smith\",\n" +
      "            \"_links\": {\n" +
      "                \"self\": {\"href\":\"/groups/1/xeroEmployees/123\"}\n" +
      "            }\n" +
      "        },\n" +
      "        \"employmentHistories\": [\n" +
      "            {\n" +
      "                \"from\": \"2016-01-01\",\n" +
      "                \"to\": null,\n" +
      "                \"fullTimeEquivalent\": \"1.0\",\n" +
      "                \"_links\": {\n" +
      "                    \"self\": {\"href\": \"/groups/1/settings/staff/100/employmentHistories/5\"}\n" +
      "                },\n" +
      "                \"_embedded\": {\n" +
      "                    \"role\": {\n" +
      "                        \"name\": \"PM Team Lead\",\n" +
      "                        \"_links\": {\n" +
      "                            \"self\": {\"href\": \"/roles/7\"}\n" +
      "                        }\n" +
      "                    },\n" +
      "                    \"trackingCategory\": {\n" +
      "                        \"name\": \"Property Management\",\n" +
      "                        \"_links\": {\n" +
      "                            \"self\": {\"href\": \"/groups/1/trackingcategories/9\"},\n" +
      "                            \"channel\": {\"href\": \"/channels/3\"}\n" +
      "                        }\n" +
      "                    }\n" +
      "                }\n" +
      "            }\n" +
      "        ]\n" +
      "    }\n" +
      "}"
    )
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from("$.._links..href"))
      .map(e -> e.getTokenEvent().getToken())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(
      JsonString.of("/groups/1/settings/staff/100"),
      JsonString.of("/groups/1/settings/staff/100/employmentHistories"),
      JsonString.of("/groups/1/xeroEmployees/123"),
      JsonString.of("/groups/1/settings/staff/100/employmentHistories/5"),
      JsonString.of("/roles/7"),
      JsonString.of("/groups/1/trackingcategories/9"),
      JsonString.of("/channels/3"),
      JsonDocumentEnd.instance()
    );
  }

  @Test
  public void shouldParseInArray() throws Exception {
    TestSubscriber<String> ts = new TestSubscriber<>();
    Observable.just("{\n" +
      "  \"Id\": \"380d2319-3ca1-472d-b3ab-5d64f253592c\",\n" +
      "  \"Status\": \"OK\",\n" +
      "  \"ProviderName\": \"Xero API Previewer\",\n" +
      "  \"DateTimeUTC\": \"\\/Date(1478066634716)\\/\",\n" +
      "  \"Organisations\": [\n" +
      "    {\n" +
      "      \"APIKey\": \"UEZYTWCZLFLQGEVYD3USDAILJM65IS\",\n" +
      "      \"Name\": \"Ray White Bankstown\",\n" +
      "      \"LegalName\": \"Greenwood Property Group Pty Ltd\",\n" +
      "      \"PaysTax\": true,\n" +
      "      \"Version\": \"AU\",\n" +
      "      \"OrganisationType\": \"COMPANY\",\n" +
      "      \"BaseCurrency\": \"AUD\",\n" +
      "      \"CountryCode\": \"AU\",\n" +
      "      \"IsDemoCompany\": false,\n" +
      "      \"OrganisationStatus\": \"ACTIVE\",\n" +
      "      \"RegistrationNumber\": \"50127183760\",\n" +
      "      \"FinancialYearEndDay\": 30,\n" +
      "      \"FinancialYearEndMonth\": 6,\n" +
      "      \"SalesTaxBasis\": \"CASH\",\n" +
      "      \"SalesTaxPeriod\": \"MONTHLY\",\n" +
      "      \"DefaultSalesTax\": \"Tax Exclusive\",\n" +
      "      \"DefaultPurchasesTax\": \"Tax Inclusive\",\n" +
      "      \"PeriodLockDate\": \"\\/Date(1404086400000+0000)\\/\",\n" +
      "      \"EndOfYearLockDate\": \"\\/Date(1404086400000+0000)\\/\",\n" +
      "      \"CreatedDateUTC\": \"\\/Date(1465282900000)\\/\",\n" +
      "      \"OrganisationEntityType\": \"COMPANY\",\n" +
      "      \"Timezone\": \"AUSEASTERNSTANDARDTIME\",\n" +
      "      \"ShortCode\": \"!CrQMW\",\n" +
      "      \"Addresses\": [\n" +
      "        {\n" +
      "          \"AddressType\": \"STREET\",\n" +
      "          \"AddressLine1\": \"68 Marion St\",\n" +
      "          \"City\": \"BANKSTOWN\",\n" +
      "          \"Region\": \"NSW\",\n" +
      "          \"PostalCode\": \"2200\",\n" +
      "          \"Country\": \"Australia\",\n" +
      "          \"AttentionTo\": \"\"\n" +
      "        },\n" +
      "        {\n" +
      "          \"AddressType\": \"POBOX\",\n" +
      "          \"AddressLine1\": \"68 Marion St\",\n" +
      "          \"City\": \"BANKSTOWN\",\n" +
      "          \"Region\": \"NSW\",\n" +
      "          \"PostalCode\": \"2200\",\n" +
      "          \"Country\": \"Australia\",\n" +
      "          \"AttentionTo\": \"\"\n" +
      "        }\n" +
      "      ],\n" +
      "      \"Phones\": [\n" +
      "        {\n" +
      "          \"PhoneType\": \"OFFICE\",\n" +
      "          \"PhoneNumber\": \"(02) 9793 3333\",\n" +
      "          \"PhoneCountryCode\": \"61\"\n" +
      "        }\n" +
      "      ],\n" +
      "      \"ExternalLinks\": [],\n" +
      "      \"PaymentTerms\": {}\n" +
      "    }\n" +
      "  ]\n" +
      "}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from("$.Organisations[0]"))
      .map(e -> e.getTokenEvent().getToken().value())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValueCount(100);
  }
}
