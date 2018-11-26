package com.netcetera.trema.intellij.plugin.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.netcetera.trema.core.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Trema model used when exporting the Trema source.
 */
@NoArgsConstructor
@Getter
@Setter
public class TremaExportModel {
  private TremaFile exportFileType;
  private TremaExportType exportType;
  private List<LanguageAndFileName> languagesToExport = new ArrayList<>();
  private List<Status> statusesToExport = new ArrayList<>();
  private String exportFilePath;
  private String csvOptionsEncoding;
  private char csvOptionsSeparator;
  private Boolean propertiesOptionsEscapeQuotes;
}
