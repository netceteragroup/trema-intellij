package com.netcetera.trema.intellij.plugin.dialogs;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.Font;
import java.awt.event.ActionListener;

/**
 * Form that is used for displaying exceptions.
 */
public class ExceptionDialogForm extends AbstractTremaForm {

  private JLabel messageLabel;
  private JTextArea exceptionStackTrace;
  private JPanel exceptionPanel;
  private JLabel exceptionMessageLabel;
  private JButton showMoreButton;

  /**
   * Constructor for the exception form.
   *
   * @param message          {@link String} a short message to be displayed.
   * @param exceptionMessage {@link String} the exception message
   * @param stackTrace       {@link String} the stack trace
   */
  public ExceptionDialogForm(String message, String exceptionMessage, String stackTrace) {
    this.messageLabel.setText(message);
    this.exceptionMessageLabel.setText(exceptionMessage);
    this.exceptionStackTrace.setText(stackTrace);
    this.windowTitle = "Trema Exception";
    this.showMoreButton.addActionListener(toggleShowStackTraceListener);
  }

  /**
   * Constructor for the exception form.
   *
   * @param windowTitle      {@link String} set the window title.
   * @param message          {@link String} a short message to be displayed.
   * @param exceptionMessage {@link String} the exception message
   * @param stackTrace       {@link String} the stack trace
   */
  public ExceptionDialogForm(String windowTitle, String message, String exceptionMessage, String stackTrace) {
    this(message, exceptionMessage, stackTrace);
    this.windowTitle = windowTitle;
  }

  @Override
  public JComponent $$$getRootComponent$$$() {
    return exceptionPanel;
  }
  private ActionListener toggleShowStackTraceListener = e -> {
    String buttonText = showMoreButton.getText();

    if (buttonText.equals("Show more")) {
      exceptionStackTrace.setRows(15);
      showMoreButton.setText("Show less");
    } else {
      exceptionStackTrace.setRows(3);
      showMoreButton.setText("Show more");
    }
  };
}
