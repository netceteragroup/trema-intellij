package com.netcetera.trema.intellij.plugin.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model for creating a new Trema file.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateNewTremaDbModel {

  private String folderPath;
  private String fileName;
  private String encoding;
  private String masterLanguage;
  private String schemaPath;

}
