package com.netcetera.trema.intellij.plugin.models;

import com.netcetera.trema.core.importing.Change;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Trema model used when importing from source.
 */
@NoArgsConstructor
@Getter
@Setter
public class TremaImportModel {
  private List<Change> conflictingChanges = new ArrayList<>();
  private List<Change> nonConflictingChanges = new ArrayList<>();
  private String importSource;
  private String encoding;
  private String separator;

}
