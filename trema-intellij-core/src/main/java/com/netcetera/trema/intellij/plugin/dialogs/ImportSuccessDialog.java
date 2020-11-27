package com.netcetera.trema.intellij.plugin.dialogs;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Dialog showing the result after an import was executed successfully.
 */
public class ImportSuccessDialog extends AbstractTremaForm {

  private JPanel successPanel;
  private JLabel titleLabel;
  private JTextArea messageTextArea;

  public ImportSuccessDialog(String windowTitle, String resultText) {
    this.windowTitle = windowTitle;
    this.titleLabel.setText("Import result");
    this.messageTextArea.setText(resultText);
  }

  @Override
  public JComponent $$$getRootComponent$$$() {
    return successPanel;
  }
}
