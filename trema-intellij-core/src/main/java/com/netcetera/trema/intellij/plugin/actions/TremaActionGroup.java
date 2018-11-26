package com.netcetera.trema.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.netcetera.trema.intellij.plugin.helpers.ActionContextHelper;
import com.netcetera.trema.intellij.plugin.models.TremaFile;

import java.util.Arrays;
import java.util.List;

/**
 * Action group where the other Trema actions will be presented.
 */
public class TremaActionGroup extends DefaultActionGroup {
  private List<TremaFile> supportedExtensions = Arrays.asList(TremaFile.values());

  @Override
  public void update(AnActionEvent event) {
    super.update(event);
    ActionContextHelper.setVisibilityDependingOnContext(event, supportedExtensions);
  }
}
