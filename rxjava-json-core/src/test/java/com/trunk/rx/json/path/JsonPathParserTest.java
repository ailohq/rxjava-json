package com.trunk.rx.json.path;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.trunk.rx.json.exception.MalformedPathException;

import static org.testng.Assert.assertEquals;

public class JsonPathParserTest {

  public static final JsonPathParser PARSER = new JsonPathParser();

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectEmptyString() throws Exception {
    PARSER.parse("");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectMissingRoot() throws Exception {
    PARSER.parse(".foo");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectEndsWithSingleDot() throws Exception {
    PARSER.parse("$.a.");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectRecursionThenObjectDot() throws Exception {
    PARSER.parse("$.a...b");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectEndsWithRecursive() throws Exception {
    PARSER.parse("$.a..");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectMissingCloseBracketArray() throws Exception {
    PARSER.parse("$.a[1");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectMissingCloseBracketString() throws Exception {
    PARSER.parse("$.a['1'");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectArrayFilter() throws Exception {
    PARSER.parse("$.a[?(@.isbn)]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectNegativeInSliceStart() throws Exception {
    PARSER.parse("$.a[-1:]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectNegativeIndex() throws Exception {
    PARSER.parse("$.a[-1]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectNegativeInSliceEnd() throws Exception {
    PARSER.parse("$.a[:-2]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectNegativeStep() throws Exception {
    PARSER.parse("$.a[1:4:-2]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectOutOfOrderStep() throws Exception {
    PARSER.parse("$.a[4:1:2]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectMissingStep() throws Exception {
    PARSER.parse("$.a[4:1:]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectSliceOutOfOrder() throws Exception {
    PARSER.parse("$.a[4:1]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectSliceWithNoValues() throws Exception {
    PARSER.parse("$.a[4:1]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectUnionWithNoValues() throws Exception {
    PARSER.parse("$.a[,]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectUnionWithNoInitialValue() throws Exception {
    PARSER.parse("$.a[,1]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectUnionWithNoFinalValue() throws Exception {
    PARSER.parse("$.a[1,]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectScriptExpression() throws Exception {
    PARSER.parse("$.a[(@.length-1)]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectArrayAfterObjectDot() throws Exception {
    PARSER.parse("$.[*]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectUnclosedSingleQuote() throws Exception {
    PARSER.parse("$['foo]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectUnclosedDoubleQuote() throws Exception {
    PARSER.parse("$[\"foo]");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectUnclosedEscape() throws Exception {
    PARSER.parse("$['\\']");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectShortUnicodeEscape() throws Exception {
    PARSER.parse("$['\\u001 foo']");
  }

  @Test(expectedExceptions = MalformedPathException.class)
  public void shouldRejectAllElementsExceptAfterRoot() throws Exception {
    PARSER.parse("$.a..*");
  }

  @Test
  public void shouldAllowRootOnly() throws Exception {
    assertEquals(PARSER.parse("$"), ImmutableList.of(RootToken.INSTANCE));
  }

  @Test
  public void shouldConvertAllElementsToRoot() throws Exception {
    assertEquals(PARSER.parse("$..*"), ImmutableList.of(RootToken.INSTANCE));
  }

  @Test
  public void shouldAllowObjectBareName() throws Exception {
    assertEquals(PARSER.parse("$.a"), ImmutableList.of(RootToken.INSTANCE, ObjectToken.of("a")));
  }

  @Test
  public void shouldAllowRecursiveThenObjectBareName() throws Exception {
    assertEquals(PARSER.parse("$..a"), ImmutableList.of(RootToken.INSTANCE, RecursiveToken.INSTANCE, ObjectToken.of("a")));
  }

  @Test
  public void shouldAllowRecursiveThenObjectQuotedName() throws Exception {
    assertEquals(PARSER.parse("$..['a']"), ImmutableList.of(RootToken.INSTANCE, RecursiveToken.INSTANCE, ObjectToken.of("a")));
  }

  @Test
  public void shouldAllowRecursiveThenArray() throws Exception {
    assertEquals(PARSER.parse("$..[1]"), ImmutableList.of(RootToken.INSTANCE, RecursiveToken.INSTANCE, ArrayIndexToken.of(1)));
  }

  @Test
  public void shouldAllowObjectStringNameSingleQuotes() throws Exception {
    assertEquals(PARSER.parse("$['a']"), ImmutableList.of(RootToken.INSTANCE, ObjectToken.of("a")));
  }

  @Test
  public void shouldAllowObjectStringNameDoubleQuotes() throws Exception {
    assertEquals(PARSER.parse("$[\"a\"]"), ImmutableList.of(RootToken.INSTANCE, ObjectToken.of("a")));
  }

  @Test
  public void shouldJsonDecodeStringName() throws Exception {
    assertEquals(PARSER.parse("$['\\t\\b\\n\\r\\f\\'\\\"\\n\\u0019']"), ImmutableList.of(RootToken.INSTANCE, ObjectToken.of("\t\b\n\r\f\'\"\n\u0019")));
  }

  @Test
  public void shouldAllowObjectWildCard() throws Exception {
    assertEquals(PARSER.parse("$.*"), ImmutableList.of(RootToken.INSTANCE, WildcardToken.object()));
  }

  @Test
  public void shouldAllowArrayIndex() throws Exception {
    assertEquals(PARSER.parse("$[1]"), ImmutableList.of(RootToken.INSTANCE, ArrayIndexToken.of(1)));
  }

  @Test
  public void shouldAllowArrayWildCard() throws Exception {
    assertEquals(PARSER.parse("$[*]"), ImmutableList.of(RootToken.INSTANCE, WildcardToken.array()));
  }

  @Test
  public void shouldAllowSlice() throws Exception {
    assertEquals(PARSER.parse("$[1:2]"), ImmutableList.of(RootToken.INSTANCE, ArraySliceToken.of(1, 2)));
  }

  @Test
  public void shouldAllowSliceWithStartOnly() throws Exception {
    assertEquals(PARSER.parse("$[1:]"), ImmutableList.of(RootToken.INSTANCE, ArraySliceToken.of(1, null)));
  }

  @Test
  public void shouldAllowSliceWithEndOnly() throws Exception {
    assertEquals(PARSER.parse("$[:2]"), ImmutableList.of(RootToken.INSTANCE, ArraySliceToken.of(null, 2)));
  }

  @Test
  public void shouldAllowArrayWithSteps() throws Exception {
    assertEquals(PARSER.parse("$[0:10:2]"), ImmutableList.of(RootToken.INSTANCE, ArrayStepToken.of(0, 10, 2)));
  }

  @Test
  public void shouldAllowArrayWithStepsWithNoEnd() throws Exception {
    assertEquals(PARSER.parse("$[0::2]"), ImmutableList.of(RootToken.INSTANCE, ArrayStepToken.of(0, null, 2)));
  }

  @Test
  public void shouldAllowArrayWithStepsNoStart() throws Exception {
    assertEquals(PARSER.parse("$[:10:2]"), ImmutableList.of(RootToken.INSTANCE, ArrayStepToken.of(null, 10, 2)));
  }

  @Test
  public void shouldAllowArrayWithStepsNoStartOrEnd() throws Exception {
    assertEquals(PARSER.parse("$[::2]"), ImmutableList.of(RootToken.INSTANCE, ArrayStepToken.of(null, null, 2)));
  }

  @Test
  public void shouldAllowArrayUnion() throws Exception {
    assertEquals(
      PARSER.parse("$[1,3:5,0:10:2]"),
      ImmutableList.of(
        RootToken.INSTANCE,
        ArrayUnionToken.using(
          ArrayIndexToken.of(1),
          ArraySliceToken.of(3, 5),
          ArrayStepToken.of(0, 10, 2)
        )
      )
    );
  }

  @Test
  public void shouldMatchDeepPath() throws Exception {
    assertEquals(
      PARSER.parse("$.foo['b a r'][*].*..bim.baz[5]"),
      ImmutableList.of(
        RootToken.INSTANCE,
        ObjectToken.of("foo"),
        ObjectToken.of("b a r"),
        WildcardToken.array(),
        WildcardToken.object(),
        RecursiveToken.INSTANCE,
        ObjectToken.of("bim"),
        ObjectToken.of("baz"),
        ArrayIndexToken.of(5)
      )
    );
  }
}
