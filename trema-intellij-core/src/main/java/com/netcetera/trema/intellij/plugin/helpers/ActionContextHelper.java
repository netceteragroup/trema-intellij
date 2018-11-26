package com.netcetera.trema.intellij.plugin.helpers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.netcetera.trema.intellij.plugin.models.TremaFile;

import java.util.List;
import lombok.experimental.UtilityClass;

/**
 * ActionContextHelper sets the visibility of the context menu items depending on the file that is
 * interacted with.
 */
@UtilityClass
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ActionContextHelper {

  /**
   * Sets the visibility of the context item depending on the file that is interacted with. If the file
   * extension is in the supportedExtensions list, than the action can be executed.
   * @param event {@link com.intellij.openapi.actionSystem.AnActionEvent} IntelliJ event
   * @param supportedExtensions List<TremaFile> list that contain enum of supported extensions for Trema
   */
  public static void setVisibilityDependingOnContext(AnActionEvent event, List<TremaFile> supportedExtensions) {
    VirtualFile file = event.getDataContext().getData(DataKeys.VIRTUAL_FILE);
    Presentation presentation = event.getPresentation();

    presentation.setVisible(isSupportedTremaFile(file, supportedExtensions));

  }

  private static boolean isSupportedTremaFile(VirtualFile file, List<TremaFile> supportedExtensions) {
      return file != null && supportedExtensions.contains(TremaFile.fromExtension(file.getExtension()));
  }
}
