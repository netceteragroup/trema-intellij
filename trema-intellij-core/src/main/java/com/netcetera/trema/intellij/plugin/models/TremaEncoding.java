package com.netcetera.trema.intellij.plugin.models;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Enum for displaying supported encodings for Trema.
 */
public enum TremaEncoding {
  /**
   * UTF-8.
   */
  UTF_8("UTF-8"),

  /**
   * CP1251.
   */
  Cp1251("CP1251"),

  /**
   * ISO-8859-1.
   */
  ISO_8859_1("ISO-8859-1"),

  /**
   * US-ASCII.
   */
  US_ASCII("US-ASCII"),

  /**
   * UTF-16.
   */
  UTF_16("UTF-16"),

  /**
   * UTF-16BE.
   */
  UTF_16BE("UTF-16BE"),

  /**
   * UTF-16LE.
   */
  UTF_16LE("UTF-16LE");

  @Getter
  private String encoding;

  /**
   * Constructor for the enum.
   * @param encoding the encoding
   */
  TremaEncoding(String encoding) {
    this.encoding = encoding;
  }

  public static List<TremaEncoding> getEncodings() {
    return Arrays.asList(values());
  }

}
