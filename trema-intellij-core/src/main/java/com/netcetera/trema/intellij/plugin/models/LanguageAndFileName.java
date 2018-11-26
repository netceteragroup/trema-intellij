package com.netcetera.trema.intellij.plugin.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Model to represent the export language and the file path for the export.
 */
@AllArgsConstructor
@Getter
public class LanguageAndFileName {
  private String language;
  private String fileName;

  /**
   * {@inheritDoc}
  */
  public String toString() {
    return getFileName() + " (" + getLanguage() + ")";
  }
}
