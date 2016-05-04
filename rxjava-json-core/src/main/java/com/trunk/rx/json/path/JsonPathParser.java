package com.trunk.rx.json.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.trunk.rx.json.exception.MalformedPathException;
import com.trunk.rx.json.impl.CharacterIndex;

import static com.trunk.rx.json.impl.JsonParser.getEscapedUnicode;
import static java.lang.Character.isWhitespace;

public class JsonPathParser {

  private static final String NAME_START = "[a-zA-Z_$]";

  public List<JsonPath> parse(String p) {
    if (p.isEmpty()) {
      throw new MalformedPathException("Empty path");
    }
    CharacterIndex path = new CharacterIndex(p);

    readWhitespace(path);

    List<JsonPath> tokens = new ArrayList<>();
    readRootObject(path, tokens);

    while(path.inBounds()) {
      readNextToken(path, tokens);
    }

    return tokens;
  }

  private void readNextToken(CharacterIndex path, List<JsonPath> tokens) {
    if (path.currentCharIs('.')) {
      readDotToken(path, tokens);
    } else if (path.currentCharIs('[')) {
      readBracketToken(path, tokens);
    } else {
      illegalCharacter(path, ".", "[");
    }
  }

  private void readBracketToken(CharacterIndex path, List<JsonPath> tokens) {
    path.incrementPosition(1);
    if (path.currentCharIs('*')) {
      path.incrementPosition(1);
      if (path.currentCharIs(']')) {
        tokens.add(WildcardToken.array());
        path.incrementPosition(1);
      } else {
        illegalCharacter(path, "]");
      }
    } else if (path.currentCharIs('"') || path.currentCharIs('\'')) {
      readStringName(path, tokens);
    } else if (isNumber(path.currentChar()) || path.currentCharIs(':')) {
      readArray(path, tokens);
    } else {
      illegalCharacter(path, "[0-9]", "'", "\"", ":");
    }
  }

  private void readArray(CharacterIndex path, List<JsonPath> tokens) {
    List<ArrayToken> arrayElements = new ArrayList<>();
    List<Integer> integerElements = new ArrayList<>();

    String currentNumber = "";

    while (path.inBounds()) {
      if (path.currentCharIs(']') || path.currentCharIs(',')) {
        integerElements.add(asInteger(currentNumber));
        currentNumber = "";
        if (
          (integerElements.size() == 1 && integerElements.get(0) == null) ||
          (integerElements.size() == 2 && integerElements.get(0) == null && integerElements.get(1) == null) ||
          (integerElements.size() == 3 && integerElements.get(2) == null)
        ){
          throw new MalformedPathException("Illegal array accessor at position " + path.position());
        }
        arrayElements.add(ArrayToken.of(integerElements));
        integerElements.clear();
        if (path.currentCharIs(']')) {
          tokens.add(ArrayUnionToken.using(arrayElements));
          path.incrementPosition(1);
          return;
        }
        path.incrementPosition(1);
      } else if (path.currentCharIs(':')) {
        integerElements.add(asInteger(currentNumber));
        currentNumber = "";
        path.incrementPosition(1);
      } else if (isNumber(path.currentChar())) {
        currentNumber = currentNumber + path.currentChar();
        path.incrementPosition(1);
      } else {
        illegalCharacter(path, "[0-9]", ":", ",", "]");
      }
    }

    throw new MalformedPathException("Unclosed array expression at position " + path.position());
  }

  private Integer asInteger(String s) {
    return s.isEmpty() ? null : Integer.parseInt(s);
  }

  private void readStringName(CharacterIndex path, List<JsonPath> tokens) {
    char quoteChar = path.currentChar();
    path.incrementPosition(1);

    int start = path.position();
    int end;

    while (path.inBounds()) {
      if (path.currentCharIs('\\')) {
        path.incrementPosition(2);
      } else if (path.currentCharIs(quoteChar)) {
        end = path.position();
        path.incrementPosition(1);
        if (!path.inBounds()) {
          throw new MalformedPathException("Unclosed object name expression at position " + path.position());
        } else if (path.currentCharIs(']')) {
          tokens.add(ObjectToken.of(unescape(path.subSequence(start, end))));
          path.incrementPosition(1);
          return;
        } else {
          illegalCharacter(path, "]");
        }
      } else {
        path.incrementPosition(1);
      }
    }
    if (path.inBounds()) {
      illegalCharacter(path, Character.toString(quoteChar));
    } else {
      throw new MalformedPathException("Unclosed string expression at position " + path.position());
    }
  }

  private void readDotToken(CharacterIndex path, List<JsonPath> tokens) {
    if (!path.hasMoreCharacters()) {
      unexpectedEndOfPath(path);
    } else if (path.currentCharIs('.') && path.nextCharIs('.')) {
      path.incrementPosition(2);
      if (path.inBounds() && path.currentCharIs('*')) {
        // treat '$..*' as '$'
        if (tokens.size() == 1 && !path.hasMoreCharacters()) {
          path.incrementPosition(1);
          return;
        } else {
          throw new MalformedPathException("All elements can only occur after root and must be the last token at position " + path.position());
        }
      }
      tokens.add(RecursiveToken.INSTANCE);
      if (!path.inBounds()) {
        unexpectedEndOfPath(path);
      } else if (path.currentCharIs('[')) {
        readBracketToken(path, tokens);
        return;
      }
    } else {
      path.incrementPosition(1);
    }

    if (!path.inBounds()) {
      unexpectedEndOfPath(path);
    } else if (path.currentCharIs('*')) {
      tokens.add(WildcardToken.object());
      path.incrementPosition(1);
    } else if (isNameStart(path.currentChar())) {
      int start = path.position();
      int end = start;
      while(path.inBounds() && isName(path.currentChar())) {
        end = path.position();
        path.incrementPosition(1);
      }
      tokens.add(ObjectToken.of(path.subSequence(start, end + 1).toString()));
    } else {
      illegalCharacter(path, "*", NAME_START);
    }
  }

  private String unescape(CharSequence charSequence) {
    return replaceUnicodeEscape(
      charSequence.toString()
      .replace("\\t", "\t")
      .replace("\\b", "\b")
      .replace("\\n", "\n")
      .replace("\\r", "\r")
      .replace("\\f", "\f")
      .replace("\\'", "\'")
      .replace("\\\"", "\"")
      .replace("\\\\", "\\")
      .replace("\\\n", "\n")
    );
  }

  private String replaceUnicodeEscape(String s) {
    boolean inEscape = false;
    boolean inUnicodeEscape = false;
    char[] unicodeBuffer = new char[4];
    int bufferOffset = 0;
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      if (inEscape) {
        if (c == 'u') {
          inUnicodeEscape = true;
          inEscape = false;
          bufferOffset = 0;
        } else if (c == 't') {
          result.append('\t');
        } else if (c == 'b') {
          result.append('\b');
        } else if (c == 'n') {
          result.append('\n');
        } else if (c == 'r') {
          result.append('\r');
        } else if (c == 'f') {
          result.append('\f');
        } else if (c == '\'') {
          result.append('\'');
        } else if (c == '"') {
          result.append('"');
        } else if (c == '\\') {
          result.append('\\');
        } else if (c == '\n') {
          result.append('\n');
        } else {
          result.append(c);
        }
      } else if (inUnicodeEscape) {
        if (isHex(c)) {
          unicodeBuffer[bufferOffset] = c;
          ++bufferOffset;
        } else {
          break;
        }
        if (bufferOffset == 4) {
          result.append(getEscapedUnicode(unicodeBuffer));
          inUnicodeEscape = false;
        }
      } else if (c == '\\') {
        inEscape = true;
      } else {
        result.append(c);
      }
    }
    if (inUnicodeEscape) {
      throw new MalformedPathException("Invalid unicode escape sequence");
    }

    return result.toString();
  }

  private boolean isHex(char c) {
    return isNumber(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  private boolean isNumber(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isName(char c) {
    return isNameStart(c) || isNumber(c);
  }

  private boolean isNameStart(char c) {
    return c == '$' || c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  private void readRootObject(CharacterIndex path, List<JsonPath> tokens) {
    if (!path.currentCharIs('$')) {
      illegalCharacter(path, "$");
    }
    path.incrementPosition(1);
    tokens.add(RootToken.INSTANCE);
  }

  private void readWhitespace(CharacterIndex path) {
    while(path.inBounds()) {
      if (!isWhitespace(path.currentChar())) {
        return;
      }
      path.incrementPosition(1);
    }
  }

  private void unexpectedEndOfPath(CharacterIndex path) {
    throw new MalformedPathException("Unexpected end of path");
  }

  private void illegalCharacter(CharacterIndex path, String... expected) {
    throw new MalformedPathException("Illegal character '" + path.currentChar() + "' at position " + path.position() + ", expected " + String.join(" or ", Arrays.asList(expected).stream().map(c -> "'" + c + "'").collect(Collectors.toList())));
  }
}
