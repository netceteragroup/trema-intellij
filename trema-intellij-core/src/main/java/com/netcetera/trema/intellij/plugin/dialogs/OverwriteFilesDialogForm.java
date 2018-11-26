package com.netcetera.trema.intellij.plugin.dialogs;

import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.netcetera.trema.intellij.plugin.events.TremaModelChangedListener;
import com.netcetera.trema.intellij.plugin.models.LanguageAndFileName;
import com.netcetera.trema.intellij.plugin.models.TremaConflictExportFileModel;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Form used to display list of conflicted files. This form displays files that are already existing in the file system.
 */
public class OverwriteFilesDialogForm extends AbstractTremaForm<TremaConflictExportFileModel> {
  private JPanel parent;
  private JLabel overwriteFilesMessageLabel;
  private JPanel labelPanel;
  private JPanel overwriteFilesPanel;

  /**
   * Constructor for the form used to display list of conflicted files. This form displays files that are already
   * existing in the file system.
   * @param conflictedFiles {@link List<LanguageAndFileName>} list of files that exist in the file system
   */
  public OverwriteFilesDialogForm(List<LanguageAndFileName> conflictedFiles) {
    this.windowTitle = "Overwrite files";
    listener.modelStateValid(true);

    overwriteFilesPanel.setLayout(new GridLayoutManager(conflictedFiles.size(), 1, JBUI.emptyInsets(), -1, -1));

    for (int i = 0; i < conflictedFiles.size(); i++) {
      JCheckBox checkBox = new JCheckBox(conflictedFiles.get(i).toString(), true);
      checkBox.setBackground(JBColor.WHITE);
      overwriteFilesPanel.add(checkBox, getGridPosition(i));
    }
  }

  @Override
  public JComponent $$$getRootComponent$$$() {
    return parent;
  }

  @Override
  public TremaConflictExportFileModel getDataModel() {
    List<String> doNotOverrideFilePaths = Arrays.stream(overwriteFilesPanel.getComponents())
        .filter(c -> c instanceof JCheckBox && ((JCheckBox) c).isSelected())
        .map(c -> ((JCheckBox) c).getText())
        .collect(Collectors.toList());

    return new TremaConflictExportFileModel(doNotOverrideFilePaths);
  }

  private GridConstraints getGridPosition(int row) {
    return new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
        null, null, null, 0, false);
  }

  /**
   * @noinspection ALL
   */
  private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
    if (currentFont == null)
      return null;
    String resultName;
    if (fontName == null) {
      resultName = currentFont.getName();
    } else {
      Font testFont = new Font(fontName, Font.PLAIN, 10);
      if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
        resultName = fontName;
      } else {
        resultName = currentFont.getName();
      }
    }
    return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
  }

}
