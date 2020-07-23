package com.netcetera.trema.intellij.plugin.dialogs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.netcetera.trema.intellij.plugin.models.CreateNewTremaDbModel;
import com.netcetera.trema.intellij.plugin.models.TremaEncoding;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Dialog form for creating new Trema files.
 */
public class CreateTremaDbDialogForm extends AbstractTremaForm<CreateNewTremaDbModel> {

  private JPanel newTremaDbPanel;
  private JPanel titlePanel;
  private JLabel formTitleLabel;
  private JLabel formImagePlaceholder;
  private JLabel messageLabel;
  private JTextField foderDestinationPath;
  private JButton browseFolderBtn;
  private JTextField fileNameText;
  private JTextField masterLanguageText;
  private JComboBox<String> fileEncodingOption;
  private JTextField tremaSchemaLocation;
  private JPanel optionsPanel;

  private CreateNewTremaDbModel model;
  private AnActionEvent event;

  private static final String ERROR_INVALID_FOLDER = "The selected destination path is not a directory.";
  private static final String ERROR_INVALID_FILE = "The file name cannot be empty.";
  private static final String ERROR_EMPTY_MASTER_LANGUAGE = "The master language cannot be empty.";

  /**
   * Constructor for the dialog form.
   * @param event the event that is executed
   */
  public CreateTremaDbDialogForm(AnActionEvent event) {
    this.event = event;
    this.windowTitle = "New Trema XML database file";
    init();
  }

  private void init() {
    model = new CreateNewTremaDbModel();
    fileNameText.setText("texts.trm");
    masterLanguageText.setText("de");
    TremaEncoding.getEncodings().forEach(tremaEncoding -> fileEncodingOption.addItem(tremaEncoding.getEncoding()));
    fileEncodingOption.setSelectedIndex(0);

    browseFolderBtn.addActionListener(folderPickerListener);
    fileNameText.getDocument().addDocumentListener(textFieldChangedListener);
    masterLanguageText.getDocument().addDocumentListener(textFieldChangedListener);
  }

  private void validateFields() {
    // hide error field
    messageLabel.setVisible(false);
    String errorMessage = "";

    if (!Files.isDirectory(Paths.get(foderDestinationPath.getText()))) {
      errorMessage += ERROR_INVALID_FOLDER;
    }

    if (fileNameText.getText() == null || fileNameText.getText().isEmpty()) {
      errorMessage += ERROR_INVALID_FILE;
    }

    if (masterLanguageText.getText() == null || masterLanguageText.getText().isEmpty()) {
      errorMessage += ERROR_EMPTY_MASTER_LANGUAGE;
    }

    if (!errorMessage.isEmpty()) {
      // if there are some errors
      messageLabel.setVisible(true);
      messageLabel.setText(errorMessage);
      listener.modelStateValid(false);
    } else {
      listener.modelStateValid(true);
    }
  }

  private ActionListener folderPickerListener = new ActionListener() {

    @Override
    public void actionPerformed(ActionEvent e) {
      FileChooser.chooseFile(new FileChooserDescriptor(false, true, false, false, false, false), event.getProject(),
          null, virtualFiles -> {
            foderDestinationPath.setText(virtualFiles.getCanonicalPath());
            validateFields();
          });
    }
  };

  private DocumentListener textFieldChangedListener = new DocumentListener() {

    @Override
    public void insertUpdate(DocumentEvent e) {
      validateFields();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      validateFields();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      validateFields();
    }
  };

  @Override
  public JComponent $$$getRootComponent$$$() {
    return newTremaDbPanel;
  }

  @Override
  public CreateNewTremaDbModel getDataModel() {
    model.setFileName(fileNameText.getText());
    model.setFolderPath(foderDestinationPath.getText());
    Object selectedEncoding = fileEncodingOption.getSelectedItem();
    if (selectedEncoding == null) {
      model.setEncoding(fileEncodingOption.getItemAt(0));
    } else {
      model.setEncoding(selectedEncoding.toString());
    }
    model.setMasterLanguage(masterLanguageText.getText());
    model.setSchemaPath(tremaSchemaLocation.getText());
    return model;
  }
}
