package com.trunk.rx.json.gson.transformer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.trunk.rx.character.CharacterObservable;
import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.element.JsonArray;
import com.trunk.rx.json.gson.RxJsonGson;
import com.trunk.rx.json.transformer.TransformerJsonPath;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observables.StringObservable;
import rx.observers.TestSubscriber;

import java.util.Collections;

public class TransformerRxJsonGsonTest {
  @Test
  public void shouldMarshallDefaultObjects() throws Exception {
    TestSubscriber<Object> ts = new TestSubscriber<>();
    Observable.just("{\"a\":[1,2,3,{\"x\":[\"4\"]}]}")
      .compose(TransformerRxJsonGson.from("$.a[*]"))
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(1.0, 2.0, 3.0, ImmutableMap.of("x", ImmutableList.of("4")));
  }

  @Test
  public void shouldEmitComplexObjects() throws Exception {
    TestSubscriber<String> ts = new TestSubscriber<>();

    JsonArray.of(Observable.just("foo", Collections.singletonList("bar")).compose(RxJsonGson.toJsonElements()))
      .compose(RxJson.toJson())
      .compose(StringObservable::stringConcat)
      .subscribe(ts);

    ts.assertCompleted();
    ts.assertValue("[\"foo\",[\"bar\"]]");
  }

  @Test
  public void shouldParseNestedObject() throws Exception {
    TestSubscriber<Object> ts = new TestSubscriber<>();
    Observable.just("{\n" +
      "  \"Id\": \"380d2319-3ca1-472d-b3ab-5d64f253592c\",\n" +
      "  \"Status\": \"OK\",\n" +
      "  \"ProviderName\": \"Xero API Previewer\",\n" +
      "  \"DateTimeUTC\": \"\\/Date(1478066634716)\\/\",\n" +
      "  \"Organisations\": [\n" +
      "    {\n" +
      "      \"APIKey\": \"12345\",\n" +
      "      \"Name\": \"Ray White Bankstown\",\n" +
      "      \"LegalName\": \"Greenwood Property Group Pty Ltd\",\n" +
      "      \"PaysTax\": true,\n" +
      "      \"Version\": \"AU\",\n" +
      "      \"OrganisationType\": \"COMPANY\",\n" +
      "      \"BaseCurrency\": \"AUD\",\n" +
      "      \"CountryCode\": \"AU\",\n" +
      "      \"IsDemoCompany\": false,\n" +
      "      \"OrganisationStatus\": \"ACTIVE\",\n" +
      "      \"RegistrationNumber\": \"12345\",\n" +
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
      .compose(TransformerRxJsonGson.from("$.Organisations[0]"))
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(
      new Gson().fromJson(
        "{" +
          "  \"APIKey\": \"12345\",\n" +
          "  \"Name\": \"Ray White Bankstown\",\n" +
          "  \"LegalName\": \"Greenwood Property Group Pty Ltd\",\n" +
          "  \"PaysTax\": true,\n" +
          "  \"Version\": \"AU\",\n" +
          "  \"OrganisationType\": \"COMPANY\",\n" +
          "  \"BaseCurrency\": \"AUD\",\n" +
          "  \"CountryCode\": \"AU\",\n" +
          "  \"IsDemoCompany\": false,\n" +
          "  \"OrganisationStatus\": \"ACTIVE\",\n" +
          "  \"RegistrationNumber\": \"12345\",\n" +
          "  \"FinancialYearEndDay\": 30,\n" +
          "  \"FinancialYearEndMonth\": 6,\n" +
          "  \"SalesTaxBasis\": \"CASH\",\n" +
          "  \"SalesTaxPeriod\": \"MONTHLY\",\n" +
          "  \"DefaultSalesTax\": \"Tax Exclusive\",\n" +
          "  \"DefaultPurchasesTax\": \"Tax Inclusive\",\n" +
          "  \"PeriodLockDate\": \"\\/Date(1404086400000+0000)\\/\",\n" +
          "  \"EndOfYearLockDate\": \"\\/Date(1404086400000+0000)\\/\",\n" +
          "  \"CreatedDateUTC\": \"\\/Date(1465282900000)\\/\",\n" +
          "  \"OrganisationEntityType\": \"COMPANY\",\n" +
          "  \"Timezone\": \"AUSEASTERNSTANDARDTIME\",\n" +
          "  \"ShortCode\": \"!CrQMW\",\n" +
          "  \"Addresses\": [\n" +
          "    {\n" +
          "      \"AddressType\": \"STREET\",\n" +
          "      \"AddressLine1\": \"68 Marion St\",\n" +
          "      \"City\": \"BANKSTOWN\",\n" +
          "      \"Region\": \"NSW\",\n" +
          "      \"PostalCode\": \"2200\",\n" +
          "      \"Country\": \"Australia\",\n" +
          "      \"AttentionTo\": \"\"\n" +
          "    },\n" +
          "    {\n" +
          "      \"AddressType\": \"POBOX\",\n" +
          "      \"AddressLine1\": \"68 Marion St\",\n" +
          "      \"City\": \"BANKSTOWN\",\n" +
          "      \"Region\": \"NSW\",\n" +
          "      \"PostalCode\": \"2200\",\n" +
          "      \"Country\": \"Australia\",\n" +
          "      \"AttentionTo\": \"\"\n" +
          "    }\n" +
          "  ],\n" +
          "  \"Phones\": [\n" +
          "    {\n" +
          "      \"PhoneType\": \"OFFICE\",\n" +
          "      \"PhoneNumber\": \"(02) 9793 3333\",\n" +
          "      \"PhoneCountryCode\": \"61\"\n" +
          "    }\n" +
          "  ],\n" +
          "  \"ExternalLinks\": [],\n" +
          "  \"PaymentTerms\": {}\n" +
          "}",
        Object.class
      )
    );

  }
}
