package com.trunk.rx.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.trunk.rx.StringObservable;
import com.trunk.rx.json.exception.MalformedJsonException;
import com.trunk.rx.json.token.JsonArray;
import com.trunk.rx.json.token.JsonArrayEnd;
import com.trunk.rx.json.token.JsonArrayStart;
import com.trunk.rx.json.token.JsonBoolean;
import com.trunk.rx.json.token.JsonName;
import com.trunk.rx.json.token.JsonNull;
import com.trunk.rx.json.token.JsonNumber;
import com.trunk.rx.json.token.JsonObject;
import com.trunk.rx.json.token.JsonObjectStart;
import com.trunk.rx.json.token.JsonString;
import com.trunk.rx.json.token.JsonToken;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.testng.Assert.assertEquals;

public class JsonTokenOperatorTest {

  private static final JsonTokenOperator BASE_PARSER = new JsonTokenOperator();
  public static final JsonTokenOperator LENIENT_PARSER = BASE_PARSER.lenient();

  @Test
  public void shouldReturnNoTokensForEmptyUpstream() throws Exception {
    should("return no tokens for empty upstream")
      .given(BASE_PARSER)
      .when()
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldReturnWithoutErrorForEmptyUpstreamWhenLenient() throws Exception {
    should("return without error for empty upstream when lenient")
      .given(LENIENT_PARSER)
      .when()
      .then()
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnNoTokensForEmptyString() throws Exception {
    should("return no tokens for empty string")
      .given(BASE_PARSER)
      .when("")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldReturnNoTokensForEmptyStringWhenLenient() throws Exception {
    should("return no tokens for empty string when lenient")
      .given(LENIENT_PARSER)
      .when("")
      .then()
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnNoTokensForWhiteSpaceOnly() throws Exception {
    // white space only
    should("return no tokens for whitespace only")
      .given(BASE_PARSER)
      .when("\n \t\r")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldReturnNoTokensForWhitespaceOnlyWhenLenient() throws Exception {
    should("return no tokens for whitespace only when lenient")
      .given(LENIENT_PARSER)
      .when("\n \t\r")
      .then()
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnEmptyObject() throws Exception {
    should("return empty object")
      .given(BASE_PARSER)
      .when("{}")
      .then(JsonObject.start(), JsonObject.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnEmptyObjectWithWhitespace() throws Exception {
    should("return empty object with whitespace")
      .given(BASE_PARSER)
      .when(" { } ")
      .then(JsonObject.start(), JsonObject.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnEmptyArray() throws Exception {
    should("return empty array")
      .given(BASE_PARSER)
      .when("[]")
      .then(JsonArray.start(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnEmptyArrayWithWhitespace() throws Exception {
    should("return empty array with whitespace")
      .given(BASE_PARSER)
      .when(" [ ] ")
      .then(JsonArray.start(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnSimpleNumber() throws Exception {
    should("return simple number")
      .given(BASE_PARSER)
      .when("1")
      .then(JsonNumber.of("1"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnDecimalNumber() throws Exception {
    should("return decimal number")
      .given(BASE_PARSER)
      .when("1.1")
      .then(JsonNumber.of("1.1"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnExponentialNumber() throws Exception {
    should("return exponential number")
      .given(BASE_PARSER)
      .when("1e2")
      .then(JsonNumber.of("1e2"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnExponentialNumberWithPositiveSign() throws Exception {
    should("return exponential number with positive sign")
      .given(BASE_PARSER)
      .when("1e+2")
      .then(JsonNumber.of("1e+2"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnExponentialNumberWithNegativeSign() throws Exception {
    should("return exponential number with negative sign")
      .given(BASE_PARSER)
      .when("1e-2")
      .then(JsonNumber.of("1e-2"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnExponentialNumberWithDecimal() throws Exception {
    should("return exponential number with decimal")
      .given(BASE_PARSER)
      .when("1.1e2")
      .then(JsonNumber.of("1.1e2"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnExponentialNumberWithDecimalAndPositiveSign() throws Exception {
    should("return exponential number with decimal and positive sign")
      .given(BASE_PARSER)
      .when("1.1e+2")
      .then(JsonNumber.of("1.1e+2"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnExponentialNumberWithDecimalAndNegativeSign() throws Exception {
    should("return exponential number with decimal and negative sign")
      .given(BASE_PARSER)
      .when("1.1e-2")
      .then(JsonNumber.of("1.1e-2"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnVeryLongNumber() throws Exception {
    should("return very long number")
      .given(BASE_PARSER)
      .when(longString("1", 100_000))
      .then(JsonNumber.of(longString("1", 100_000)))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectNotaNumberWhenStrict() throws Exception {
    should("reject not-a-number when strict")
      .given(BASE_PARSER)
      .when("[NaN]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectInfinityWhenStrict() throws Exception {
    should("reject not-a-number when strict")
      .given(BASE_PARSER)
      .when("[Infinity]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectNegativeInfinityWhenStrict() throws Exception {
    should("reject not-a-number when strict")
      .given(BASE_PARSER)
      .when("[-Infinity]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAcceptNotaNumberAndInfinityWhenLenient() throws Exception {
    should("accept not-a-number when lenient")
      .given(LENIENT_PARSER)
      .when("[NaN, -Infinity, Infinity]")
      .then(
        JsonArray.start(),
        JsonNumber.of("NaN"),
        JsonNumber.of("-Infinity"),
        JsonNumber.of("Infinity"),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectOctalPrefix() throws Exception {
    should("reject octal prefix")
      .given(BASE_PARSER)
      .when("[03]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldConvertOctalPrefixToStringWhenLenient() throws Exception {
    should("convert octal prefix to string when lenient")
      .given(LENIENT_PARSER)
      .when("[03]")
      .then(JsonArray.start(), JsonString.of("03"), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectMalformedNumbers() throws Exception {
    shouldRejectMalformedNumber("-").run();
    shouldRejectMalformedNumber(".").run();
    shouldRejectMalformedNumber("e").run();
    shouldRejectMalformedNumber("0e").run();
    shouldRejectMalformedNumber(".e").run();
    shouldRejectMalformedNumber("0.e").run();
    shouldRejectMalformedNumber("-.0e").run();
    shouldRejectMalformedNumber("e1").run();
    shouldRejectMalformedNumber(".e1").run();
    shouldRejectMalformedNumber("-e1").run();
    shouldRejectMalformedNumber("1x").run();
    shouldRejectMalformedNumber("1.1x").run();
    shouldRejectMalformedNumber("1e1x").run();
    shouldRejectMalformedNumber("1ex").run();
    shouldRejectMalformedNumber("1.1ex").run();
    shouldRejectMalformedNumber("1.1e1x").run();
    shouldRejectMalformedNumber("0.").run();
    shouldRejectMalformedNumber("-0.").run();
    shouldRejectMalformedNumber("0.e1").run();
    shouldRejectMalformedNumber("-0.e1").run();
    shouldRejectMalformedNumber(".0").run();
    shouldRejectMalformedNumber("-.0").run();
    shouldRejectMalformedNumber(".0e1").run();
    shouldRejectMalformedNumber("-.0e1").run();
  }

  @Test
  public void shouldConvertMalformedNumbersToStringWhenLenient() throws Exception {
    shouldConvertMalformedNumberToStringWhenLenient("-").run();
    shouldConvertMalformedNumberToStringWhenLenient(".").run();
    shouldConvertMalformedNumberToStringWhenLenient("e").run();
    shouldConvertMalformedNumberToStringWhenLenient("0e").run();
    shouldConvertMalformedNumberToStringWhenLenient(".e").run();
    shouldConvertMalformedNumberToStringWhenLenient("0.e").run();
    shouldConvertMalformedNumberToStringWhenLenient("-.0e").run();
    shouldConvertMalformedNumberToStringWhenLenient("e1").run();
    shouldConvertMalformedNumberToStringWhenLenient(".e1").run();
    shouldConvertMalformedNumberToStringWhenLenient("-e1").run();
    shouldConvertMalformedNumberToStringWhenLenient("1x").run();
    shouldConvertMalformedNumberToStringWhenLenient("1.1x").run();
    shouldConvertMalformedNumberToStringWhenLenient("1e1x").run();
    shouldConvertMalformedNumberToStringWhenLenient("1ex").run();
    shouldConvertMalformedNumberToStringWhenLenient("1.1ex").run();
    shouldConvertMalformedNumberToStringWhenLenient("1.1e1x").run();
    shouldConvertMalformedNumberToStringWhenLenient("0.").run();
    shouldConvertMalformedNumberToStringWhenLenient("-0.").run();
    shouldConvertMalformedNumberToStringWhenLenient("0.e1").run();
    shouldConvertMalformedNumberToStringWhenLenient("-0.e1").run();
    shouldConvertMalformedNumberToStringWhenLenient(".0").run();
    shouldConvertMalformedNumberToStringWhenLenient("-.0").run();
    shouldConvertMalformedNumberToStringWhenLenient(".0e1").run();
    shouldConvertMalformedNumberToStringWhenLenient("-.0e1").run();
  }

  @Test
  public void shouldReturnSimpleString() throws Exception {
    should("return simple string")
      .given(BASE_PARSER)
      .when("\"s\"")
      .then(JsonString.of("s"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnNumberInStringAsString() throws Exception {
    should("return number in string as string")
      .given(BASE_PARSER)
      .when("\"1\"")
      .then(JsonString.of("1"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnVeryLongString() throws Exception {
    String s = longString("a", 10_000);
    should("return very long string")
      .given(BASE_PARSER)
      .when("\"" + s + "\"")
      .then(JsonString.of(s))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldIgnoreSlashCommentsInString() throws Exception {
    should("ignore // comments in string")
      .given(BASE_PARSER)
      .when("\"// comment\"")
      .then(JsonString.of("// comment"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldIgnoreHashCommentsInString() throws Exception {
    should("ignore # comments in string")
      .given(BASE_PARSER)
      .when("\"# comment\"")
      .then(JsonString.of("# comment"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldNotAllowUnquotedStrings() throws Exception {
    should("not allow unquoted strings")
      .given(BASE_PARSER)
      .when("a")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowUnquotedStringsInArrays() throws Exception {
    should("not allow unquoted strings")
      .given(BASE_PARSER)
      .when("[a]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowSingleQuotedStringsInArrays() throws Exception {
    should("not allow single quoted strings")
      .given(BASE_PARSER)
      .when("['a']")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowSingleQuotedStrings() throws Exception {
    should("not allow single quoted strings")
      .given(BASE_PARSER)
      .when("'a'")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowUnquotedStringsWhenLenient() throws Exception {
    should("allow unquoted strings when lenient")
      .given(LENIENT_PARSER)
      .when("[a]")
      .then(JsonArray.start(), JsonString.of("a"), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowSingleQuotedStringsWhenLenient() throws Exception {
    should("allow single quoted strings when lenient")
      .given(LENIENT_PARSER)
      .when("['a']")
      .then(JsonArray.start(), JsonString.of("a"), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectUnterminatedStrings() throws Exception {
    should("reject unterminated strings")
      .given(LENIENT_PARSER)
      .when("\"a")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldUnescapeCharacters() throws Exception {
    should("unescape characters")
      .given(BASE_PARSER)
      .when("[\"a\","
              + "\"a\\\"\","
              + "\"\\\"\","
              + "\":\","
              + "\",\","
              + "\"\\b\","
              + "\"\\f\","
              + "\"\\n\","
              + "\"\\r\","
              + "\"\\t\","
              + "\" \","
              + "\"\\\\\","
              + "\"{\","
              + "\"}\","
              + "\"[\","
              + "\"]\","
              + "\"\\u0000\","
              + "\"\\u0019\","
              + "\"\\u20AC\""
              + "]")
      .then(
        JsonArray.start(),
        JsonString.of("a"),
        JsonString.of("a\""),
        JsonString.of("\""),
        JsonString.of(":"),
        JsonString.of(","),
        JsonString.of("\b"),
        JsonString.of("\f"),
        JsonString.of("\n"),
        JsonString.of("\r"),
        JsonString.of("\t"),
        JsonString.of(" "),
        JsonString.of("\\"),
        JsonString.of("{"),
        JsonString.of("}"),
        JsonString.of("["),
        JsonString.of("]"),
        JsonString.of("\0"),
        JsonString.of("\u0019"),
        JsonString.of("\u20AC"),
        JsonArray.end()
      )
      .run();
  }

  @Test
  public void shouldErrorOnInvalidEscapedCharacter() throws Exception {
    should("error on invalid escaped character")
      .given(BASE_PARSER)
      .when("[\"\\u000g\"]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldErrorOnTruncatedEscapedCharacter() throws Exception {
    should("error on truncated escaped character")
      .given(BASE_PARSER)
      .when("[\"\\u000")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldErrorOnTruncatedEscapeSequence() throws Exception {
    should("error on truncated escape sequence")
      .given(BASE_PARSER)
      .when("[\"\\")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldErrorOnTruncatedEscapeSequenceWhenLenient() throws Exception {
    should("error on truncated escape sequence")
      .given(LENIENT_PARSER)
      .when("[\"\\")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldErrorOnBareEscapeCharWhenLenient() throws Exception {
    should("error on truncated escape sequence")
      .given(LENIENT_PARSER)
      .when("\\")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldErrorOnBareEscapeCharInArrayWhenLenient() throws Exception {
    should("error on truncated escape sequence")
      .given(LENIENT_PARSER)
      .when("[\\")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldReturnBooleanTrue() throws Exception {
    should("return boolean true")
      .given(BASE_PARSER)
      .when("true")
      .then(JsonBoolean.True())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnBooleanFalse() throws Exception {
    should("return boolean false")
      .given(BASE_PARSER)
      .when("false")
      .then(JsonBoolean.False())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnNull() throws Exception {
    should("return null")
      .given(BASE_PARSER)
      .when("null")
      .then(JsonNull.INSTANCE)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnMixedCaseLiterals() throws Exception {
    should("return mixed case literals")
      .given(BASE_PARSER)
      .when("[True,TruE,False,FALSE,NULL,nulL]")
      .then(
        JsonArray.start(),
        JsonBoolean.True(),
        JsonBoolean.True(),
        JsonBoolean.False(),
        JsonBoolean.False(),
        JsonNull.INSTANCE,
        JsonNull.INSTANCE,
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnStringsForQuotedLiterals() throws Exception {
    should("return strings for quoted literals")
      .given(BASE_PARSER)
      .when("[\"true\",\"false\",\"null\"]")
      .then(
        JsonArray.start(),
        JsonString.of("true"),
        JsonString.of("false"),
        JsonString.of("null"),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnStringsForQuotedLiteralsWhenLenient() throws Exception {
    should("return strings for quoted literals")
      .given(LENIENT_PARSER)
      .when("[\"true\",\"false\",\"null\"]")
      .then(
        JsonArray.start(),
        JsonString.of("true"),
        JsonString.of("false"),
        JsonString.of("null"),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnSimpleObject() throws Exception {
    should("return simple object")
      .given(BASE_PARSER)
      .when("{\"hello\":\"world\"}")
      .then(
        JsonObject.start(),
        JsonName.of("hello"),
        JsonString.of("world"),
        JsonObject.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectAlternateNameValueSeparatorEquals() throws Exception {
    should("reject alternate name value separator =")
      .given(BASE_PARSER)
      .when("{\"a\"=\"b\"}")
      .then(
        JsonObject.start(),
        JsonName.of("a")
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectAlternateNameValueSeparatorEqualsArrow() throws Exception {
    should("reject alternate name value separator =>")
      .given(BASE_PARSER)
      .when("{\"a\"=>\"b\"}")
      .then(
        JsonObject.start(),
        JsonName.of("a")
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAcceptAlternateNameValueSeparatorEqualsWhenLenient() throws Exception {
    should("accept alternate name value separator = when lenient")
      .given(LENIENT_PARSER)
      .when("{\"a\"=\"b\"}")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b"),
        JsonObject.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAcceptAlternateNameValueSeparatorEqualsArrowWhenLenient() throws Exception {
    should("accept alternate name value separator => when lenient")
      .given(LENIENT_PARSER)
      .when("{\"a\"=>\"b\"}")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b"),
        JsonObject.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldNotAllowUnquotedNames() throws Exception {
    should("not allow unquoted names")
      .given(BASE_PARSER)
      .when("{a:\"b\"}")
      .then(JsonObject.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowUnquotedNamesWhenLenient() throws Exception {
    should("allow unquoted names when lenient")
      .given(LENIENT_PARSER)
      .when("{a:\"b\"}")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b"),
        JsonObject.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldNotAllowSingleQuotedNames() throws Exception {
    should("not allow single quoted names")
      .given(BASE_PARSER)
      .when("{'a':\"b\"}")
      .then(JsonObject.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowSingleQuotedNamesWhenLenient() throws Exception {
    should("allow single quoted names when lenient")
      .given(LENIENT_PARSER)
      .when("{'a':\"b\"}")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b"),
        JsonObject.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldNotAllowSemicolonObjectListSeparator() throws Exception {
    should("not allow semicolon object list separator")
      .given(BASE_PARSER)
      .when("{\"a\":true;\"b\":true}")
      .then(JsonObject.start(), JsonName.of("a"), JsonBoolean.True())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowSemicolonObjectListSeparatorWhenLenient() throws Exception {
    should("allow semicolon object list separator when lenient")
      .given(LENIENT_PARSER)
      .when("{\"a\":true;\"b\":true}")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonBoolean.True(),
        JsonName.of("b"),
        JsonBoolean.True(),
        JsonObject.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldReturnSimpleArray() throws Exception {
    should("return simple array")
      .given(BASE_PARSER)
      .when("[true, false]")
      .then(
        JsonArray.start(),
        JsonBoolean.True(),
        JsonBoolean.False(),
        JsonArray.end()
      ).run();
  }

  @Test
  public void shouldNotAllowSemicolonSeparatorForArray() throws Exception {
    should("not allow semicolon separator for array")
      .given(BASE_PARSER)
      .when("[true;true]")
      .then(JsonArray.start(), JsonBoolean.True())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowSemicolonSeparatorForArrayWhenLenient() throws Exception {
    should("allow semicolon separator for array when lenient")
      .given(LENIENT_PARSER)
      .when("[true;true]")
      .then(
        JsonArray.start(),
        JsonBoolean.True(),
        JsonBoolean.True(),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldNotAllowDuplicateSeparatorForArray() throws Exception {
    should("not allow duplicate separator for array")
      .given(BASE_PARSER)
      .when("[true,,true]")
      .then(JsonArray.start(), JsonBoolean.True())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowTrailingSeparatorForArray() throws Exception {
    should("not allow trailing separator for array")
      .given(BASE_PARSER)
      .when("[true,]")
      .then(JsonArray.start(), JsonBoolean.True())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowLeadingSeparatorForArray() throws Exception {
    should("not allow leading separator for array")
      .given(BASE_PARSER)
      .when("[,true]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowSeparatorOnlyArray() throws Exception {
    should("not allow separator only array")
      .given(BASE_PARSER)
      .when("[,]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldInsertNullForDuplicateSeparatorForArrayWhenLenient() throws Exception {
    should("insert null for duplicate separator for array when lenient")
      .given(LENIENT_PARSER)
      .when("[true,,true]")
      .then(
        JsonArray.start(),
        JsonBoolean.True(),
        JsonNull.INSTANCE,
        JsonBoolean.True(),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldInsertNullForTrailingSeparatorForArrayWhenLenient() throws Exception {
    should("insert null for trailing separator for array when lenient")
      .given(LENIENT_PARSER)
      .when("[true,]")
      .then(
        JsonArray.start(),
        JsonBoolean.True(),
        JsonNull.INSTANCE,
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldInsertNullForLeadingSeparatorForArrayWhenLenient() throws Exception {
    should("insert null for leading separator for array when lenient")
      .given(LENIENT_PARSER)
      .when("[,true]")
      .then(
        JsonArray.start(),
        JsonNull.INSTANCE,
        JsonBoolean.True(),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldInsertNullForSeparatorOnlyArrayWhenLenient() throws Exception {
    should("insert null for separator only array when lenient")
      .given(LENIENT_PARSER)
      .when("[,]")
      .then(
        JsonArray.start(),
        JsonNull.INSTANCE,
        JsonNull.INSTANCE,
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectObjectMissingValue() throws Exception {
    should("reject object missing value")
      .given(BASE_PARSER)
      .when("{\"a\":}")
      .then(
        JsonObject.start(),
        JsonName.of("a")
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectObjectMissingValueWhenLenient() throws Exception {
    should("reject object missing value")
      .given(LENIENT_PARSER)
      .when("{\"a\":}")
      .then(
        JsonObject.start(),
        JsonName.of("a")
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectPrematureEndOfObject() throws Exception {
    should("reject premature end of object")
      .given(BASE_PARSER)
      .when("{\"a\":true,")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonBoolean.True()
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectPrematureEndOfObjectWhenLenient() throws Exception {
    should("reject premature end of object when lenient")
      .given(LENIENT_PARSER)
      .when("{\"a\":true,")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonBoolean.True()
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectTrailingCommaInObject() throws Exception {
    should("reject trailing comma in object")
      .given(BASE_PARSER)
      .when("{\"a\":\"b\",}")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b")
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectTrailingCommaInObjectWhenLenient() throws Exception {
    should("reject trailing comma in object when lenient")
      .given(LENIENT_PARSER)
      .when("{\"a\":\"b\",}")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b")
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowSlashComments() throws Exception {
    should("not allow // comments")
      .given(BASE_PARSER)
      .when("[// comment\n true]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowHashComments() throws Exception {
    should("not allow # comments")
      .given(BASE_PARSER)
      .when("[# comment\n true]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldNotAllowCStyleComments() throws Exception {
    should("not allow /**/ comments")
      .given(BASE_PARSER)
      .when("[/* comment */ true]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowStripLeadingWhitespaceSlashCommentsWhenLenient() throws Exception {
    should("allow // comments when lenient")
      .given(LENIENT_PARSER)
      .when("[ // comment\n true]")
      .then(JsonArray.start(), JsonBoolean.True(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowSlashCommentsWhenLenient() throws Exception {
    should("allow // comments when lenient")
      .given(LENIENT_PARSER)
      .when("[foo,// comment\n bar //comment\n]")
      .then(JsonArray.start(), JsonString.of("foo"), JsonString.of("bar"), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowSlashCommentsEverywhereWhenLenient() throws Exception {
    should("allow /**/ comments when lenient")
      .given(LENIENT_PARSER)
      .when("// c \n[// c \nfoo// c \n, // c \n {// c \na// c \n:// c \nb// c \n,\"x\"// c \n:// c \n\"y\"// c \n}// c \n]// c \n")
      .then(
        JsonArray.start(),
        JsonString.of("foo"),
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b"),
        JsonName.of("x"),
        JsonString.of("y"),
        JsonObject.end(),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowStripLeadingWhitespaceHashCommentsWhenLenient() throws Exception {
    should("allow # comments when lenient")
      .given(LENIENT_PARSER)
      .when("[ # comment\n true]")
      .then(JsonArray.start(), JsonBoolean.True(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowHashCommentsWhenLenient() throws Exception {
    should("allow # comments when lenient")
      .given(LENIENT_PARSER)
      .when("[foo # comment\n, bar]")
      .then(JsonArray.start(), JsonString.of("foo"), JsonString.of("bar"), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowHashCommentsEverywhereWhenLenient() throws Exception {
    should("allow /**/ comments when lenient")
      .given(LENIENT_PARSER)
      .when("# c \n[# c \nfoo# c \n, # c \n {# c \na# c \n:# c \nb# c \n,\"x\"# c \n:# c \n\"y\"# c \n}# c \n]# c \n")
      .then(
        JsonArray.start(),
        JsonString.of("foo"),
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b"),
        JsonName.of("x"),
        JsonString.of("y"),
        JsonObject.end(),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowStripLeadingWhitespaceCStyleCommentsWhenLenient() throws Exception {
    should("allow /**/ comments when lenient")
      .given(LENIENT_PARSER)
      .when("[ /* comment */ true]")
      .then(JsonArray.start(), JsonBoolean.True(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowCStyleCommentsWhenLenient() throws Exception {
    should("allow /**/ comments when lenient")
      .given(LENIENT_PARSER)
      .when("[foo, /* comment */ bar]")
      .then(JsonArray.start(), JsonString.of("foo"), JsonString.of("bar"), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowCStyleCommentsEverywhereWhenLenient() throws Exception {
    should("allow /**/ comments when lenient")
      .given(LENIENT_PARSER)
      .when("/* c */[/* c */foo/* c */, /* c */ {/* c */a/* c */:/* c */b/* c */,\"x\"/* c */:/* c */\"y\"/* c */}/* c */]/* c */")
      .then(
        JsonArray.start(),
        JsonString.of("foo"),
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("b"),
        JsonName.of("x"),
        JsonString.of("y"),
        JsonObject.end(),
        JsonArray.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectMultipleTopLevelValues() throws Exception {
    should("reject multiple top level values")
      .given(BASE_PARSER)
      .when("[][]")
      .then(JsonArray.start(), JsonArray.end())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowMultipleTopLevelValuesWhenLenient() throws Exception {
    should("allow multiple top level values when lenient")
      .given(LENIENT_PARSER)
      .when("[] true {}")
      .then(
        JsonArray.start(),
        JsonArray.end(),
        JsonBoolean.True(),
        JsonObject.start(),
        JsonObject.end()
      )
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectNonExecutePrefix() throws Exception {
    should("reject non execute prefix")
      .given(BASE_PARSER)
      .when(")]}'\n []")
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectPartialNonExecutePrefix() throws Exception {
    should("reject partial non execute prefix")
      .given(BASE_PARSER)
      .when(")]}' []")
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectNonExecutePrefixWithLeadingWhitespace() throws Exception {
    should("reject non execute prefix with leading whitespace")
      .given(BASE_PARSER)
      .when("\r\n \t)]}'\n []")
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowNonExecutePrefixWhenLenient() throws Exception {
    should("allow non execute prefix when lenient")
      .given(LENIENT_PARSER)
      .when(")]}'\n []")
      .then(JsonArray.start(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectPartialNonExecutePrefixWhenLenient() throws Exception {
    should("reject partial non execute prefix when lenient")
      .given(LENIENT_PARSER)
      .when(")]}' []")
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowNonExecutePrefixWithLeadingWhitespaceWhenLenient() throws Exception {
    should("allow non execute prefix with leading whitespace when lenient")
      .given(LENIENT_PARSER)
      .when("\r\n \t)]}'\n []")
      .then(JsonArray.start(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldIgnoreBomAsFirstCharacter() throws Exception {
    should("ignore Bom as first character")
      .given(BASE_PARSER)
      .when("\ufeff[]")
      .then(JsonArray.start(), JsonArray.end())
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldRejectBomAsSecondCharacter() throws Exception {
    should("ignore Bom as first character")
      .given(BASE_PARSER)
      .when(" \ufeff[]")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectBomBareCharacter() throws Exception {
    should("reject Bom bare character")
      .given(BASE_PARSER)
      .when("[\ufeff]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldFailWithPosition() throws Exception {
    should("fail with position")
      .given(LENIENT_PARSER)
      .when("[\n\n\n\n\n\"a\",}]")
      .then("Expected value at line 6 column 5 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionGreaterThanBufferSize() throws Exception {
    should("fail with position greater than buffer size")
      .given(LENIENT_PARSER)
      .when("[\n\n" + longString(" ", 8192) + "\n\n\n\"a\",}]")
      .then("Expected value at line 6 column 5 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionOverSlashOfEOLComment() throws Exception {
    should("fail with position over slash of EOL comment")
      .given(LENIENT_PARSER)
      .when("\n// foo\n\n//bar\r\n[\"a\",}")
      .then("Expected value at line 5 column 6 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionOverSlashOfHashComment() throws Exception {
    should("fail with position over slash of hash comment")
      .given(LENIENT_PARSER)
      .when("\n# foo\n\n#bar\r\n[\"a\",}")
      .then("Expected value at line 5 column 6 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionOverSlashOfCComment() throws Exception {
    should("fail with position over slash of C comment")
      .given(LENIENT_PARSER)
      .when("\n\n/* foo\n*\n*\r\nbar */[\"a\",}")
      .then("Expected value at line 6 column 12 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionOverQuotedString() throws Exception {
    should("fail with position over quoted string")
      .given(LENIENT_PARSER)
      .when("[\"foo\nbar\r\nbaz\n\",\n  }")
      .then("Expected value at line 5 column 3 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionOverUnquotedString() throws Exception {
    should("fail with position over unquoted string")
      .given(LENIENT_PARSER)
      .when("[\n\nabcd\n\n,}")
      .then("Expected value at line 5 column 2 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionEscapedNewlineCharacter() throws Exception {
    should("fail with position escaped newline character")
      .given(LENIENT_PARSER)
      .when("[\n\n\"\\\n\n\",}")
      .then("Expected value at line 5 column 3 path $[1]").run();
  }

  @Test
  public void shouldFailWithPositionEscapedNewlineCharacterAfterBom() throws Exception {
    should("fail with position escaped newline character after Bom")
      .given(LENIENT_PARSER)
      .when("\ufeff[\"a\",}]")
      .then("Expected value at line 1 column 6 path $[1]").run();
  }

  @Test
  public void shouldFailWithNestedPath() throws Exception {
    should("fail with nested path")
      .given(LENIENT_PARSER)
      .when("[1,{\"a\":[2,3,}")
      .then("Expected value at line 1 column 14 path $[1].a[2]").run();
  }

  @Test
  public void shouldAllowVeryDeepObjectNesting() throws Exception {
    should("allow very deep object nesting")
      .given(BASE_PARSER)
      .when(bigObject())
      .then(bigObjectTokens()).run();
  }

  @Test
  public void shouldAllowVeryDeepArrayNesting() throws Exception {
    should("allow very deep array nesting")
      .given(BASE_PARSER)
      .when("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]")
      .then(
        ImmutableList.builder()
          .addAll(repeat(JsonArray.start(), 40))
          .addAll(repeat(JsonArray.end(), 40))
          .build().toArray(new JsonToken[0])
      )
      .run();
  }

  @Test
  public void shouldRejectSingleSlash() throws Exception {
    should("reject single slash https://github.com/google/gson/issues/409")
      .given(LENIENT_PARSER)
      .when("/")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectLeadingSingleSlash() throws Exception {
    should("reject leading single slash https://github.com/google/gson/issues/409")
      .given(LENIENT_PARSER)
      .when("/x")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectSingleSlashAfterComment() throws Exception {
    should("reject single slash after comment https://github.com/google/gson/issues/409")
      .given(LENIENT_PARSER)
      .when("/* foo *//")
      .then()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectJunkDataInObjectWhenLenient() throws Exception {
    should("reject junk data in object when lenient")
      .given(LENIENT_PARSER)
      .when("{\"a\":\"android\"x")
      .then(
        JsonObject.start(),
        JsonName.of("a"),
        JsonString.of("android")
      )
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldRejectUnterminatedUnquotedStringWhenLenient() throws Exception {
    should("reject unterminated unquoted string when lenient")
      .given(LENIENT_PARSER)
      .when("[" + longString("x", 1024 * 16))
      .then(JsonArray.start(), JsonString.of(longString("x", 1024 * 16)))
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldAllowTopLevelUnquotedStringWhenLenient() throws Exception {
    should("allow top level unquoted string when lenient")
      .given(LENIENT_PARSER)
      .when(longString("x", 1024 * 16))
      .then(JsonString.of(longString("x", 1024 * 16)))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldAllowEmptyNameWhenLenient() throws Exception {
    should("allow empty name when lenient")
      .given(LENIENT_PARSER)
      .when("{\"\":true}")
      .then(
        JsonObject.start(),
        JsonName.of(""),
        JsonBoolean.True(),
        JsonObject.end()
      )
      .then(Is.COMPLETED).run();
  }

  @Test
  public void shouldTestRegressions() throws Exception {
    Stream.of(
      given(LENIENT_PARSER).when("{]").thenType(JsonObjectStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{,").thenType(JsonObjectStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{{").thenType(JsonObjectStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{[").thenType(JsonObjectStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{:").thenType(JsonObjectStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\",").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\",").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":}").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"::").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":,").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"=}").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"=>}").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"=>\"string\":").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"=>\"string\"=").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"=>\"string\"=>").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"=>\"string\",").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"=>\"string\",\"name\"").thenType(JsonObjectStart.class, JsonName.class, JsonString.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("[}").thenType(JsonArrayStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("[,]").thenType(JsonArrayStart.class, JsonNull.class, JsonNull.class, JsonArrayEnd.class).then(Is.COMPLETED),
      given(LENIENT_PARSER).when("{").thenType(JsonObjectStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\"").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\",").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{'name'").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{'name',").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{name").thenType(JsonObjectStart.class, JsonName.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("[").thenType(JsonArrayStart.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("[string").thenType(JsonArrayStart.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("[\"string\"").thenType(JsonArrayStart.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("['string'").thenType(JsonArrayStart.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("[123").thenType(JsonArrayStart.class, JsonNumber.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("[123,").thenType(JsonArrayStart.class, JsonNumber.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":123").thenType(JsonObjectStart.class, JsonName.class, JsonNumber.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":123,").thenType(JsonObjectStart.class, JsonName.class, JsonNumber.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":\"string\"").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":\"string\",").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":'string'").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":'string',").thenType(JsonObjectStart.class, JsonName.class, JsonString.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":false").thenType(JsonObjectStart.class, JsonName.class, JsonBoolean.class).then(MalformedJsonException.class),
      given(LENIENT_PARSER).when("{\"name\":false,,").thenType(JsonObjectStart.class, JsonName.class, JsonBoolean.class).then(MalformedJsonException.class)
    )
      .forEach(testItem -> testItem.run());
  }

  @Test
  public void shouldReturnPathWIthEachEvent() throws Exception {
    TestSubscriber<JsonTokenEvent> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .compose(StringObservable.toCharacter())
      .lift(BASE_PARSER)
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(
      new JsonTokenEvent(JsonObject.start(), "$"),

      new JsonTokenEvent(JsonName.of("a"), "$"),
      new JsonTokenEvent(JsonNumber.of("1234"), "$.a"),

      new JsonTokenEvent(JsonName.of("b"), "$"),
      new JsonTokenEvent(JsonArray.start(), "$.b"),
      new JsonTokenEvent(JsonNumber.of("1"), "$.b[0]"),
      new JsonTokenEvent(JsonNumber.of("2"), "$.b[1]"),
      new JsonTokenEvent(JsonNumber.of("3"), "$.b[2]"),
      new JsonTokenEvent(JsonNumber.of("4"), "$.b[3]"),
      new JsonTokenEvent(JsonArray.end(), "$.b"),

      new JsonTokenEvent(JsonName.of("c"), "$"),
      new JsonTokenEvent(JsonObject.start(), "$.c"),
      new JsonTokenEvent(JsonName.of("w"), "$.c"),
      new JsonTokenEvent(JsonArray.start(), "$.c.w"),
      new JsonTokenEvent(JsonNumber.of("5"), "$.c.w[0]"),
      new JsonTokenEvent(JsonNumber.of("6"), "$.c.w[1]"),
      new JsonTokenEvent(JsonNumber.of("7"), "$.c.w[2]"),
      new JsonTokenEvent(JsonNumber.of("8"), "$.c.w[3]"),
      new JsonTokenEvent(JsonArray.end(), "$.c.w"),
      new JsonTokenEvent(JsonName.of("x"), "$.c"),
      new JsonTokenEvent(JsonBoolean.True(), "$.c.x"),
      new JsonTokenEvent(JsonName.of("y"), "$.c"),
      new JsonTokenEvent(JsonBoolean.False(), "$.c.y"),
      new JsonTokenEvent(JsonName.of("z"), "$.c"),
      new JsonTokenEvent(JsonNull.INSTANCE, "$.c.z"),
      new JsonTokenEvent(JsonObject.end(), "$.c"),

      new JsonTokenEvent(JsonName.of("d"), "$"),
      new JsonTokenEvent(JsonArray.start(), "$.d"),
      new JsonTokenEvent(JsonObject.start(), "$.d[0]"),
      new JsonTokenEvent(JsonName.of("1"), "$.d[0]"),
      new JsonTokenEvent(JsonString.of("1"), "$.d[0].1"),
      new JsonTokenEvent(JsonObject.end(), "$.d[0]"),
      new JsonTokenEvent(JsonArray.end(), "$.d"),

      new JsonTokenEvent(JsonObject.end(), "$")
    );
  }

  @Test
  public void shouldReturnCorrectPathForMultipleDocumentsWhenLenient() throws Exception {
    TestSubscriber<JsonTokenEvent> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234} true [1,2,3] false")
      .compose(StringObservable.toCharacter())
      .lift(LENIENT_PARSER)
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(
      new JsonTokenEvent(JsonObject.start(), "$"),

      new JsonTokenEvent(JsonName.of("a"), "$"),
      new JsonTokenEvent(JsonNumber.of("1234"), "$.a"),
      new JsonTokenEvent(JsonObject.end(), "$"),

      new JsonTokenEvent(JsonBoolean.True(), "$"),

      new JsonTokenEvent(JsonArray.start(), "$"),
      new JsonTokenEvent(JsonNumber.of("1"), "$[0]"),
      new JsonTokenEvent(JsonNumber.of("2"), "$[1]"),
      new JsonTokenEvent(JsonNumber.of("3"), "$[2]"),
      new JsonTokenEvent(JsonArray.end(), "$"),

      new JsonTokenEvent(JsonBoolean.False(), "$")
    );
  }

  private JsonToken[] bigObjectTokens() {
    ImmutableList.Builder<JsonToken> builder = ImmutableList.builder();

    for (int i = 0; i < 40; i++) {
      builder.add(JsonObject.start());
      builder.add(JsonName.of("a"));
    }
    builder.add(JsonBoolean.True());
    for (int i = 0; i < 40; i++) {
      builder.add(JsonObject.end());
    }
    return builder.build().toArray(new JsonToken[0]);
  }

  private String bigObject() {
    String array = "{\"a\":%s}";
    String json = "true";
    for (int i = 0; i < 40; i++) {
      json = String.format(array, json);
    }
    return json;
  }

  private <T> Collection<T> repeat(T t, int count) {
    Collection<T> c = new ArrayList<>();
    for (int i = 0; i < count; ++i) {
      c.add(t);
    }
    return c;
  }

  private TestItem shouldRejectMalformedNumber(String number) {
    return should("reject malformed numbers " + number)
      .given(BASE_PARSER)
      .when("[" + number + "]")
      .then(JsonArray.start())
      .then(MalformedJsonException.class);
  }

  private TestItem shouldConvertMalformedNumberToStringWhenLenient(String number) {
    return should("reject malformed numbers " + number)
      .given(LENIENT_PARSER)
      .when("[" + number + "]")
      .then(JsonArray.start(), JsonString.of(number), JsonArray.end())
      .then(Is.COMPLETED);
  }

  private String longString(String s, int length) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; ++i) {
      builder.append(s);
    }
    return builder.toString();
  }

  public static final String[] EMPTY_ARRAY = new String[0];

  public static TestItem should(String description) {
    return new TestItem(
      EMPTY_ARRAY,
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      description,
      null
    );
  }

  public static TestItem given(JsonTokenOperator jsonTokenOperator) {
    return new TestItem(
      EMPTY_ARRAY,
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      "",
      jsonTokenOperator
    );
  }

  private enum Is {
    COMPLETED,
    NOT_COMPLETED
  }

  private static class TestItem {

    private final String[] jsonFragments;
    private final Optional<JsonToken[]> expectedTokens;
    private final Optional<Class<? extends JsonToken>[]> expectedTokenTypes;
    private final Optional<Class<? extends Throwable>> error;
    private final Optional<String> message;
    private final Optional<Is> completed;
    private final JsonTokenOperator jsonTokenOperator;
    private final String description;

    private TestItem(String[] jsonFragments,
                     Optional<JsonToken[]> expectedTokens,
                     Optional<Class<? extends JsonToken>[]> expectedTokenTypes,
                     Optional<Class<? extends Throwable>> error,
                     Optional<String> message,
                     Optional<Is> completed,
                     String description,

                     JsonTokenOperator jsonTokenOperator) {
      this.jsonFragments = jsonFragments;
      this.expectedTokens = expectedTokens;
      this.expectedTokenTypes = expectedTokenTypes;
      this.error = error;
      this.message = message;
      this.completed = completed;
      this.description = description;
      this.jsonTokenOperator = jsonTokenOperator;
    }

    public TestItem given(JsonTokenOperator jsonTokenOperator) {
      return new TestItem(jsonFragments, expectedTokens, expectedTokenTypes, error, message, completed, description, jsonTokenOperator);
    }

    public TestItem when(String... jsonFragments) {
      return new TestItem(jsonFragments, expectedTokens, expectedTokenTypes, error, message, completed, description, jsonTokenOperator);
    }

    public TestItem then(JsonToken... expectedTokens) {
      return new TestItem(jsonFragments, Optional.of(expectedTokens), expectedTokenTypes, error, message, completed, description, jsonTokenOperator);
    }

    public TestItem thenType(Class<? extends JsonToken>... expectedTokenTypes) {
      return new TestItem(jsonFragments, expectedTokens, Optional.of(expectedTokenTypes), error, message, completed, description, jsonTokenOperator);
    }

    public TestItem then(Class<? extends Throwable> error) {
      return new TestItem(jsonFragments, expectedTokens, expectedTokenTypes, Optional.of(error), message, completed, description, jsonTokenOperator);
    }

    public TestItem then(Is completed) {
      return new TestItem(jsonFragments, expectedTokens, expectedTokenTypes, error, message, Optional.of(completed), description, jsonTokenOperator);
    }

    public TestItem then(String message) {
      return new TestItem(jsonFragments, expectedTokens, expectedTokenTypes, error, Optional.of(message), completed, description, jsonTokenOperator);
    }

    public void run() {
      try {
        TestSubscriber<JsonToken> ts = new TestSubscriber<>();
        Observable.from(jsonFragments)
          .compose(StringObservable.toCharacter())
          .lift(jsonTokenOperator)
          .map(e -> e.getToken())
          .subscribe(ts);

        ts.awaitTerminalEvent(2, TimeUnit.SECONDS);

        error.ifPresent(e -> ts.assertError(e));
        message.ifPresent(
          m ->
            assertEquals(ts.getOnErrorEvents().get(0).getMessage(), m)
        );
        completed.ifPresent(c -> {
          if (c == Is.COMPLETED) {
            ts.assertNoErrors();
            ts.assertCompleted();
          } else {
            ts.assertNotCompleted();
          }
        });
        expectedTokens.ifPresent(t -> ts.assertValues(t));
        expectedTokenTypes.ifPresent(
          t ->
            assertEquals(
              ts.getOnNextEvents().stream().map(jsonToken -> jsonToken.getClass()).collect(Collectors.toList()),
              ImmutableList.copyOf(t)
            )
        );
      } catch (Throwable t) {
        String json = String.join("", jsonFragments);
        Assert.fail((description.isEmpty() ? "" : "should " + description + " ") + "'" + json.substring(0, Math.min(json.length(), 120)) + "'", t);
      }
    }
  }
}
