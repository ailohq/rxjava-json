package com.trunk.rx.json.path;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JsonPathTest {
  @Test
  public void rootShouldBeEqual() throws Exception {
    assertEquals(JsonPath.from(RootToken.instance()), JsonPath.from(RootToken.instance()));
  }
  @Test
  public void rootShouldHaveSameHash() throws Exception {
    assertEquals(JsonPath.from(RootToken.instance()).hashCode(), JsonPath.from(RootToken.instance()).hashCode());
  }

  @Test
  public void allPathsShouldMatchRoot() throws Exception {
    assertEquals(JsonPath.parse("$..*").match(JsonPath.parse("$")).toBlocking().single(), RootToken.instance());
  }

  @Test
  public void recursiveShouldMatchShortest() throws Exception {
    assertEquals(JsonPath.parse("$.a..b").match(JsonPath.parse("$.a.b.b")).toBlocking().single(), JsonPath.parse("$.a.b"));
  }

  @Test
  public void recursiveShouldMatchOverValues() throws Exception {
    assertEquals(JsonPath.parse("$.a..b").match(JsonPath.parse("$.a.c.b")).toBlocking().single(), JsonPath.parse("$.a.c.b"));
  }

  @Test
  public void sliceShouldMatchInRange() throws Exception {
    JsonPath slice = JsonPath.parse("$[3:6]");
    assertEquals(slice.match(JsonPath.parse("$[3]")).toBlocking().single(), JsonPath.parse("$[3]"));
    assertEquals(slice.match(JsonPath.parse("$[4]")).toBlocking().single(), JsonPath.parse("$[4]"));
    assertEquals(slice.match(JsonPath.parse("$[5]")).toBlocking().single(), JsonPath.parse("$[5]"));
  }

  @Test
  public void sliceShouldNotMatchOutOfRange() throws Exception {
    JsonPath slice = JsonPath.parse("$[3:6]");
    assertTrue(slice.match(JsonPath.parse("$[2]")).isEmpty().toBlocking().single());
    assertTrue(slice.match(JsonPath.parse("$[6]")).isEmpty().toBlocking().single());
  }

  @Test
  public void sliceShouldNotMatchObject() throws Exception {
    JsonPath slice = JsonPath.parse("$[3:6]");
    assertTrue(slice.match(JsonPath.parse("$['3']")).isEmpty().toBlocking().single());
  }

  @Test
  public void sliceShouldDefaultToZeroOrMaxInt() throws Exception {
    assertEquals(JsonPath.parse("$[:6]").match(JsonPath.parse("$[0]")).toBlocking().single(), JsonPath.parse("$[0]"));
    assertEquals(JsonPath.parse("$[6:]").match(JsonPath.parse("$[" + Integer.MAX_VALUE + "]")).toBlocking().single(), JsonPath.parse("$[" + Integer.MAX_VALUE + "]"));
  }

  @Test
  public void indexShouldMatchIndex() throws Exception {
    assertEquals(JsonPath.parse("$[7]").match(JsonPath.parse("$[7]")).toBlocking().single(), JsonPath.parse("$[7]"));
  }

  @Test
  public void indexShouldNotMatchString() throws Exception {
    assertTrue(JsonPath.parse("$[7]").match(JsonPath.parse("$['7']")).isEmpty().toBlocking().single());
  }

  @Test
  public void indexShouldNotMatchDifferentIndex() throws Exception {
    assertTrue(JsonPath.parse("$[7]").match(JsonPath.parse("$[8]")).isEmpty().toBlocking().single());
  }

  @Test
  public void stepShouldMatchOnlyOnStep() throws Exception {
    JsonPath step = JsonPath.parse("$[6:21:3]");
    assertEquals(step.match(JsonPath.parse("$[6]")).toBlocking().single(), JsonPath.parse("$[6]"));
    assertEquals(step.match(JsonPath.parse("$[9]")).toBlocking().single(), JsonPath.parse("$[9]"));
    assertEquals(step.match(JsonPath.parse("$[12]")).toBlocking().single(), JsonPath.parse("$[12]"));
    assertEquals(step.match(JsonPath.parse("$[15]")).toBlocking().single(), JsonPath.parse("$[15]"));
    assertEquals(step.match(JsonPath.parse("$[18]")).toBlocking().single(), JsonPath.parse("$[18]"));
    assertTrue(step.match(JsonPath.parse("$[5]")).isEmpty().toBlocking().single());
    assertTrue(step.match(JsonPath.parse("$[7]")).isEmpty().toBlocking().single());
    assertTrue(step.match(JsonPath.parse("$[21]")).isEmpty().toBlocking().single());
    assertTrue(step.match(JsonPath.parse("$[24]")).isEmpty().toBlocking().single());
  }

  @Test
  public void emptyRangeStepShouldStartAtZeroEndAtMaxInt() throws Exception {
    JsonPath step = JsonPath.parse("$[::1]");
    assertEquals(step.match(JsonPath.parse("$[0]")).toBlocking().single(), JsonPath.parse("$[0]"));
    assertEquals(step.match(JsonPath.parse("$[" + Integer.MAX_VALUE + "]")).toBlocking().single(), JsonPath.parse("$[" + Integer.MAX_VALUE + "]"));
  }

  @Test
  public void objectShouldMatchObject() throws Exception {
    assertEquals(JsonPath.parse("$['a']").match(JsonPath.parse("$['a']")).toBlocking().single(), JsonPath.parse("$.a"));
    assertEquals(JsonPath.parse("$.a").match(JsonPath.parse("$['a']")).toBlocking().single(), JsonPath.parse("$.a"));
    assertEquals(JsonPath.parse("$.a").match(JsonPath.parse("$.a")).toBlocking().single(), JsonPath.parse("$.a"));
  }

  @Test
  public void objectShouldMatchObjectWithUnicode() throws Exception {
    assertEquals(JsonPath.parse("$['a\\t']").match(JsonPath.parse("$['a\\t']")).toBlocking().single(), JsonPath.parse("$['a\t']"));
  }

  @Test
  public void objectShouldNotMatchIndexWithEquivalentValue() throws Exception {
    assertTrue(JsonPath.parse("$['1']").match(JsonPath.parse("$[1]")).isEmpty().toBlocking().single());
  }

  @Test
  public void arrayWildcardShouldMatchAnyIndex() throws Exception {
    assertEquals(JsonPath.parse("$[*]").match(JsonPath.parse("$[1]")).toBlocking().single(), JsonPath.parse("$[1]"));
  }

  @Test
  public void objectWildcardShouldMatchAnyIndex() throws Exception {
    assertEquals(JsonPath.parse("$.*").match(JsonPath.parse("$[0]")).toBlocking().single(), JsonPath.parse("$[0]"));
    assertEquals(JsonPath.parse("$.*").match(JsonPath.parse("$[1]")).toBlocking().single(), JsonPath.parse("$[1]"));
    assertEquals(JsonPath.parse("$.*").match(JsonPath.parse("$[" + Integer.MAX_VALUE + "]")).toBlocking().single(), JsonPath.parse("$[" + Integer.MAX_VALUE + "]"));
  }

  @Test
  public void arrayWildcardShouldMatchAnyObject() throws Exception {
    assertEquals(JsonPath.parse("$[*]").match(JsonPath.parse("$.a")).toBlocking().single(), JsonPath.parse("$.a"));
    assertEquals(JsonPath.parse("$[*]").match(JsonPath.parse("$['a\t']")).toBlocking().single(), JsonPath.parse("$['a\t']"));
  }

  @Test
  public void objectWildcardShouldMatchAnyObject() throws Exception {
    assertEquals(JsonPath.parse("$.*").match(JsonPath.parse("$.a")).toBlocking().single(), JsonPath.parse("$.a"));
  }
}
