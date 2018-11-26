package com.netcetera.trema.intellij.plugin.models;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Enum for supported Trema file extenstions.
 */
public enum TremaFile {
  /**
   * CSV file extension.
   */
  CSV("csv"),

  /**
   * Microsoft Excel OpenXML format file extension.
   */
  XLS("xls"),

  /**
   * Java proerties format file extension.
   */
  PROPS("properties"),

  /**
   * Trema file extension.
   */
  TRM("trm");

  @Getter
  private String extension;

  /**
   *
   * @param extension {@link String} extension value.
   */
  TremaFile(String extension) {
      this.extension = extension;
  }

  /**
   * Get a {@link TremaFile} enum object from extension type.
   * @param extension {@link String} the file extension
   * @return {@link TremaFile} the enum from the extension. Returns null if the extension is not in the enum
   */
  public static TremaFile fromExtension(String extension) {
    if (StringUtils.isNotBlank(extension)) {
      for (TremaFile trm: TremaFile.values()) {
        if (trm.getExtension().equals(extension)) {
         return trm;
        }
      }
    }
    return null;
  }

}
