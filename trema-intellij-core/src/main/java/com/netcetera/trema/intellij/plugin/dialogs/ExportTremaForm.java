package com.netcetera.trema.intellij.plugin.dialogs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.netcetera.trema.common.TremaCoreUtil;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.exporting.ExportException;
import com.netcetera.trema.intellij.plugin.helpers.TextFieldLimitHelper;
import com.netcetera.trema.intellij.plugin.models.LanguageAndFileName;
import com.netcetera.trema.intellij.plugin.models.TremaEncoding;
import com.netcetera.trema.intellij.plugin.models.TremaExportModel;
import com.netcetera.trema.intellij.plugin.models.TremaExportType;
import com.netcetera.trema.intellij.plugin.models.TremaFile;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.intellij.openapi.fileChooser.FileChooser.chooseFile;

/**
 * Form that is used when exporting the Trema file.
 */
public class ExportTremaForm extends AbstractTremaForm<TremaExportModel> {

  private static final String DEFAULT_CSV_SEPARATOR = ";";
  private static final String ERROR_CSV_SEPARATOR_CHAR = "Please enter a non-whitespace separator character.";
  private static final String ERROR_LANGUAGES_TO_EXPORT = "Please select at least one language to export";
  private static final String ERROR_STATUSES_TO_EXPORT = "Please select at least one status to export";
  private static final String ERROR_INVALID_DESTINATION_PATH = "Please select a valid destination path";
  private static final String ERROR_INVALID_FILE_NAME = "File name should not be empty";
  private JPanel exportPanel;
  private JPanel titlePanel;
  private JLabel formTitleLabel;
  private JPanel exportTileTypePanel;
  private JPanel exportDatabaseTypePanel;
  private JPanel languagesToExportPanel;
  private JPanel statusToExportPanel;
  private JPanel exportDestinationPanel;
  private JPanel csvOptionsPanel;
  private JPanel propertiesOptionsPanel;
  private JRadioButton xlsExportOption;
  private JRadioButton csvExportOption;
  private JRadioButton propertiesExportOption;
  private JRadioButton exportWholeDatabaseOption;
  private JRadioButton exportCurrentSelectionOption;
  private JLabel formImagePlaceholder;
  private JLabel messageLabel;
  private JLabel csvEncodingLabel;
  private JLabel csvSeparatorLabel;
  private JTextField csvSeparatorChar;
  private JComboBox<String> csvEncodingOptions;
  private JCheckBox propertiesEscapeQuotesCheckbox;
  private JLabel exportDestinationFolderLabel;
  private JLabel exportDestinationBaseNameLabel;
  private JTextField exportDestinationFolderPath;
  private JTextField exportDestinationBaseName;
  private JLabel exportDestinationBaseNameExt;
  private JButton exportDestinationBrowsePathButton;

  private TremaExportModel exportModel;
  private AnActionEvent event;
  private IDatabase db;

  /**
   * Form constructor.
   *
   * @param event {@link AnActionEvent} event that is triggered when interacting with
   *              IntelliJ
   * @param db    {@link IDatabase} the Trema database model that represents the Trema source.
   */
  public ExportTremaForm(AnActionEvent event, IDatabase db) throws ExportException {
    this.event = event;
    this.db = db;
    this.windowTitle = "Trema CSV/Properties Export Wiazard";
    init();
  }

  @Override
  public JComponent $$$getRootComponent$$$() {
    return exportPanel;
  }

  @Override
  public TremaExportModel getDataModel() {
    exportModel.setExportFileType(getSelectedExportFileType());
    setSelectedExportType();
    setLanguagesToExport();
    setStatusesToExport();
    setExportDestinationPath();
    setOptionsToExport();
    return exportModel;
  }

  private void init() throws ExportException {
    exportModel = new TremaExportModel();
    xlsExportOption.addChangeListener(typeChangedListener);
    csvExportOption.addChangeListener(typeChangedListener);
    exportDestinationBrowsePathButton.addActionListener(folderPickerListener);
    propertiesExportOption.addChangeListener(typeChangedListener);
    VirtualFile exportFile = event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);

    if (exportFile == null) {
      throw new ExportException("Cannot access the export source file.");
    }
    VirtualFile exportFileParent = exportFile.getParent();
    if (exportFileParent == null) {
      throw new ExportException("Cannot access the parent of the export source file.");
    }
    String exportFilePath = exportFileParent.getCanonicalPath();
    if (exportFilePath == null) {
      throw new ExportException("Cannot access the location of the export source file.");
    }
    exportDestinationFolderPath.setText(
        Paths.get(exportFilePath).toString());
    exportDestinationBaseName.setText(exportFile.getNameWithoutExtension());

    exportWholeDatabaseOption.setText(
        String.format("Whole database (%s record%s)",
            db.getSize(),
            db.getSize() == 1 ? "" : "s"));

    exportWholeDatabaseOption.setSelected(true);

    initLanguagesCheckBox();
    initStatusesCheckBox();
    initCsvEncodingOptions();
    typeChanged();
  }

  private void setLanguagesToExport() {
    String basePath = exportDestinationFolderPath.getText();
    String baseFileName = exportDestinationBaseName.getText();

    Arrays.stream(languagesToExportPanel.getComponents())
      .filter(c -> c instanceof JCheckBox && ((JCheckBox) c).isSelected())
      .forEach(c -> {
        String lang = ((JCheckBox) c).getText();
        String path = Paths.get(basePath, baseFileName + "_" + lang + "."
            + getSelectedExportFileType().getExtension())
            .toAbsolutePath().toString();
        exportModel.getLanguagesToExport().add(new LanguageAndFileName(lang, path));
      });
  }

  private void setStatusesToExport() {
    Arrays.stream(statusToExportPanel.getComponents())
        .filter(c -> c instanceof JCheckBox && ((JCheckBox) c).isSelected())
        .forEach(c -> exportModel.getStatusesToExport().add(Status.valueOf(((JCheckBox) c).getText())));
  }

  private void setExportDestinationPath() {
    if (exportDestinationFolderPath.getText().isEmpty()) {
      throw new InvalidPathException("", "Invalid destination folder path");
    }
    if (exportDestinationBaseName.getText().isEmpty()) {
      throw new InvalidPathException("", "Invalid destination file name");
    }

    Path filePath = Paths.get(exportDestinationFolderPath.getText(),
        exportDestinationBaseName.getText()).toAbsolutePath();
    this.exportModel.setExportFilePath(filePath.toString());

  }

  private void setSelectedExportType() {
    if (exportWholeDatabaseOption.isSelected()) {
      exportModel.setExportType(TremaExportType.WHOLE_DATABASE);
    }
    if (exportCurrentSelectionOption.isSelected()) {
      exportModel.setExportType(TremaExportType.CURRENT_SELECTION);
    }
  }

  private void setOptionsToExport() {
    TremaFile exportFileTypeOption = getSelectedExportFileType();
    if (TremaFile.CSV == exportFileTypeOption) {
      Object selectedEncodingOption = csvEncodingOptions.getSelectedItem();
      if (selectedEncodingOption != null) {
        exportModel.setCsvOptionsEncoding(csvEncodingOptions.getSelectedItem().toString());
      }
      exportModel.setCsvOptionsSeparator(csvSeparatorChar.getText().charAt(0));
    }
    if (TremaFile.PROPS == exportFileTypeOption) {
      exportModel.setPropertiesOptionsEscapeQuotes(propertiesEscapeQuotesCheckbox.isSelected());
    }
  }

  private TremaFile getSelectedExportFileType() {
    if (xlsExportOption.isSelected()) {
      return TremaFile.XLS;
    }
    if (csvExportOption.isSelected()) {
      return TremaFile.CSV;
    }
    if (propertiesExportOption.isSelected()) {
      return TremaFile.PROPS;
    }
    return TremaFile.XLS;
  }

  private ChangeListener typeChangedListener = e -> {
    typeChanged();
    validateFields();
  };

  private DocumentListener charChangedListener = new DocumentListener() {

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

  private ActionListener folderPickerListener = (ActionEvent e) ->
    chooseFile(new FileChooserDescriptor(false, true, false, false, false, false), event.getProject(),
      null, (VirtualFile virtualFiles) -> {
        exportDestinationFolderPath.setText(virtualFiles.getCanonicalPath());
        validateFields();
      });

  private void typeChanged() {
    TremaFile selectedExportFileType = getSelectedExportFileType();
    setCSVOptionsEnabled(selectedExportFileType == TremaFile.CSV);
    setPropertiesOptionsEnabled(selectedExportFileType == TremaFile.PROPS);
    if (selectedExportFileType == TremaFile.CSV) {
      exportDestinationBaseNameExt.setText("(\"_<language>.csv\" will be appended)");
    } else if (selectedExportFileType == TremaFile.XLS) {
      exportDestinationBaseNameExt.setText("(\"_<language>.xls\" will be appended)");
    } else if (selectedExportFileType == TremaFile.PROPS) {
      exportDestinationBaseNameExt.setText("(\"_<language>.properties\" will be appended)");
    }
  }

  private void setCSVOptionsEnabled(Boolean enabled) {
    csvSeparatorChar.setEnabled(enabled);
    csvEncodingOptions.setEnabled(enabled);
    csvEncodingLabel.setEnabled(enabled);
    csvSeparatorLabel.setEnabled(enabled);
  }

  private void setPropertiesOptionsEnabled(Boolean enabled) {
    propertiesEscapeQuotesCheckbox.setEnabled(enabled);
  }

  /**
   * COMPONENT INITIALIZATION
   */
  private void initLanguagesCheckBox() {
    Set<String> languageSet = TremaCoreUtil.getLanguages(db.getTextNodes());
    if (languageSet == null || languageSet.size() == 0) {
      return;
    }
    String[] languages = languageSet.toArray(new String[languageSet.size()]);
    languagesToExportPanel.setLayout(new GridLayoutManager(languageSet.size(), 1, JBUI.emptyInsets(), -1, -1));
    for (int i = 0; i < languages.length; i++) {
      JCheckBox lang = new JCheckBox(languages[i], null, true);
      lang.addChangeListener(typeChangedListener);
      languagesToExportPanel.add(lang, getGridPosition(i));
    }
  }

  private void initStatusesCheckBox() {
    List<Status> statuses = Arrays.asList(Status.getAvailableStatus());
    statusToExportPanel.setLayout(new GridLayoutManager(statuses.size(), 1, JBUI.emptyInsets(), -1, -1));
    statuses.forEach((Status status) -> {
      JCheckBox stat = new JCheckBox(status.getName(), null, true);
      stat.addChangeListener(typeChangedListener);
      statusToExportPanel.add(stat, getGridPosition(status.getPosition()));
    });
  }

  private void initCsvEncodingOptions() {
    TremaEncoding.getEncodings().forEach((TremaEncoding tremaEncoding) ->
      csvEncodingOptions.addItem(tremaEncoding.getEncoding()));
    csvEncodingOptions.setSelectedIndex(0);
    csvSeparatorChar.setDocument(new TextFieldLimitHelper(1));
    csvSeparatorChar.setText(DEFAULT_CSV_SEPARATOR);
    csvSeparatorChar.getDocument().addDocumentListener(charChangedListener);
    exportDestinationBaseName.getDocument().addDocumentListener(charChangedListener);
  }

  private GridConstraints getGridPosition(int row) {
    return new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
        null, null, null, 0, false);
  }

  private void validateFields() {
    // hide error field
    messageLabel.setVisible(false);
    String errorMessage = "";
    // check languages to export
    boolean isLanguageExportInvalid = Arrays.stream(languagesToExportPanel.getComponents())
        .noneMatch(component -> ((JCheckBox) component).isSelected());
    if (isLanguageExportInvalid) {
      errorMessage = ERROR_LANGUAGES_TO_EXPORT;
    }

    // check statuses to export
    boolean isStatusesExportInvalid = Arrays.stream(statusToExportPanel.getComponents())
        .noneMatch(component -> ((JCheckBox) component).isSelected());
    if (isStatusesExportInvalid) {
      errorMessage = ERROR_STATUSES_TO_EXPORT;
    }

    //is encoding valid
    if (csvExportOption.isSelected() && csvSeparatorChar.getText().isEmpty()) {
      errorMessage = ERROR_CSV_SEPARATOR_CHAR;
    }

    if (exportDestinationFolderPath.getText().isEmpty()
        || !Files.isDirectory(Paths.get(exportDestinationFolderPath.getText()))) {
      errorMessage = ERROR_INVALID_DESTINATION_PATH;
    }

    if (exportDestinationBaseName.getText().isEmpty()) {
      errorMessage = ERROR_INVALID_FILE_NAME;
    }

    if (!errorMessage.isEmpty()) {
      messageLabel.setVisible(true);
      messageLabel.setText(errorMessage);
      listener.modelStateValid(false);
    } else {
      listener.modelStateValid(true);
    }
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
