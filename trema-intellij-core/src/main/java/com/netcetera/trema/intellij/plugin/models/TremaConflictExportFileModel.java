package com.netcetera.trema.intellij.plugin.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Trema model containing paths to conflicting sources. This mean that the sources in the list already exist in the file
 * system.
 */
@AllArgsConstructor
@Getter
public class TremaConflictExportFileModel {
  private List<String> conflictPaths = new ArrayList<>();
}
