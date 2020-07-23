package com.netcetera.trema.intellij.plugin.helpers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.netcetera.trema.intellij.plugin.models.TremaFile;

import java.util.List;

/**
 * ActionContextHelper sets the visibility of the context menu items depending on the file that is
 * interacted with.
 */
public final class ActionContextHelper {

  private ActionContextHelper() {
  }

  /**
   * Sets the visibility of the context item depending on the file that is interacted with. If the file
   * extension is in the supportedExtensions list, than the action can be executed.
   *
   * @param event the IntelliJ event
   * @param supportedExtensions list that contains the supported extensions for Trema
   */
  public static void setVisibilityDependingOnContext(AnActionEvent event, List<TremaFile> supportedExtensions) {
    VirtualFile file = event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
    Presentation presentation = event.getPresentation();

    presentation.setVisible(isSupportedTremaFile(file, supportedExtensions));

  }

  private static boolean isSupportedTremaFile(VirtualFile file, List<TremaFile> supportedExtensions) {
      return file != null && supportedExtensions.contains(TremaFile.fromExtension(file.getExtension()));
  }
}
