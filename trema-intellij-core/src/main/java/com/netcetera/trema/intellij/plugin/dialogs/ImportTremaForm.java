package com.netcetera.trema.intellij.plugin.dialogs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.netcetera.trema.core.ParseException;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.IImportSource;
import com.netcetera.trema.core.importing.Change;
import com.netcetera.trema.core.importing.ChangesAnalyzer;
import com.netcetera.trema.intellij.plugin.helpers.TextFieldLimitHelper;
import com.netcetera.trema.intellij.plugin.helpers.TremaUtil;
import com.netcetera.trema.intellij.plugin.models.TremaEncoding;
import com.netcetera.trema.intellij.plugin.models.TremaImportModel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Form that is used when importing from Trema compatible import file.
 */
public class ImportTremaForm extends AbstractTremaForm<TremaImportModel> {
  private static final String DEFAULT_CSV_SEPARATOR = ";";
  private static final String TEXT_LANGUAGE = "Language: ";
  private static final String TEXT_MASTER_LANGUAGE = "Master Language: ";
  private static final String TEXT_RECORDS = "Records: ";
  private static final String TEXT_CONFLICTING = "Conflicting: ";
  private static final String TEXT_NONCONFLICTING = "Non-Conflicting: ";
  private static final String COLUMN_KEY = "Key";
  private static final String COLUMN_CHANGE = "Change";
  private static final String COLUMN_VALUE_TO_ACCEPT = "Value to accept";
  private static final String COLUMN_STATUS_TO_ACCEPT = "Status to accept";
  private static final String NONCONFLICTING_CHANGES = "NonConflictingChanges";
  private static final String CONFLICTING_CHANGES = "ConflictingChanges";
  private static final String ERROR_INVALID_SEPARATOR_CHAR = "Please insert a valid separator char.";
  private static final String ERROR_INVALID_IMPORT_PATH = "Please select a valid import source file.";

  private JPanel importPanel;
  private JPanel titlePanel;
  private JLabel formTitleLabel;
  private JLabel messageLabel;
  private JLabel formImagePlaceholder;
  private JPanel fileImportLocationPanel;
  private JLabel importFileLabel;
  private JTextField importFilePath;
  private JButton openBrowseBtn;
  private JLabel encodingLabel;
  private JComboBox<String> encodingOption;
  private JLabel separatorLabel;
  private JTextField separatorChar;
  private JPanel fileImportOptionsPanel;
  private JLabel importSourceLabel;
  private JLabel databaseLabel;
  private JLabel changesLabel;
  private JLabel languageLabel;
  private JLabel recordsLabel;
  private JLabel databaseMasterLanguageLabel;
  private JLabel databaseRecordsLabel;
  private JLabel conflictingChangesLabel;
  private JLabel nonConflictingChangesLabel;
  private JPanel conflictingChangesPanel;
  private JPanel statisticsPanel;
  private JPanel nonConflictingChangesPanel;
  private JTable conflictingRecordsTable;
  private JButton selectAllConflictingBtn;
  private JButton deselectAllConflictingBtn;
  private JTable nonConflictingRecordsTable;
  private JButton selectAllNonConflictingBtn;
  private JButton deselectAllNonConflictingBtn;

  private AnActionEvent event;
  private ChangesAnalyzer analyzer;
  private IImportSource importSource;
  private IDatabase db;
  private Map<String, List<Change>> importChanges = new HashMap<>();
  private TremaImportModel tremaImportModel;

  /**
   * Constructor for the form that is used when importing from Trema compatible import file.
   *
   * @param event {@link AnActionEvent} event that is triggered when interacting with
   *              IntelliJ
   * @param db    {@link IDatabase} the Trema database model that represents the Trema source.
   */
  public ImportTremaForm(AnActionEvent event, IDatabase db) {
    this.event = event;
    this.db = db;
    this.windowTitle = "Import from XLS/CSV";
    init();
  }

  /**
   * Initializes the model and sets the views.
   */
  private void init() {
    this.tremaImportModel = new TremaImportModel();
    setPanelEnabled(fileImportOptionsPanel, false);
    initFileSelectPanel();
    initImportPanel();
  }

  /**
   * Sets the action event for the Browse button for finding import sources. Initializes the encoding options.
   */
  private void initFileSelectPanel() {
    openBrowseBtn.addActionListener(folderPickerListener);
    TremaEncoding.getEncodings().forEach(tremaEncoding -> encodingOption.addItem(tremaEncoding.getEncoding()));
    encodingOption.setSelectedIndex(0);
    separatorChar.setDocument(new TextFieldLimitHelper(1));
    separatorChar.setText(DEFAULT_CSV_SEPARATOR);
    separatorChar.getDocument().addDocumentListener(charSeparatorListener);
  }

  /**
   * Sets the second panel, where after selecing the import source, it selects the conflicted keys when merging the changes.
   */
  private void initImportPanel() {
    TremaDataModel conflictingTableModel = new TremaDataModel();
    conflictingTableModel.addColumn("");
    conflictingTableModel.addColumn(COLUMN_KEY);
    conflictingTableModel.addColumn(COLUMN_CHANGE);
    conflictingTableModel.addColumn(COLUMN_VALUE_TO_ACCEPT);
    conflictingTableModel.addColumn(COLUMN_STATUS_TO_ACCEPT);
    conflictingRecordsTable.setModel(conflictingTableModel);

    TremaDataModel nonConflictingTableModel = new TremaDataModel();
    nonConflictingTableModel.addColumn("");
    nonConflictingTableModel.addColumn(COLUMN_KEY);
    nonConflictingTableModel.addColumn(COLUMN_CHANGE);
    nonConflictingTableModel.addColumn(COLUMN_VALUE_TO_ACCEPT);
    nonConflictingTableModel.addColumn(COLUMN_STATUS_TO_ACCEPT);
    nonConflictingRecordsTable.setModel(nonConflictingTableModel);

    selectAllConflictingBtn.addActionListener(e -> updateStatusAllRows(conflictingRecordsTable, true));
    deselectAllConflictingBtn.addActionListener(e -> updateStatusAllRows(conflictingRecordsTable, false));
    selectAllNonConflictingBtn.addActionListener(e -> updateStatusAllRows(nonConflictingRecordsTable, true));
    deselectAllNonConflictingBtn.addActionListener(e -> updateStatusAllRows(nonConflictingRecordsTable, false));
  }

  @Override
  public JComponent $$$getRootComponent$$$() {
    return importPanel;
  }

  /**
   * Retuns the import changes.
   * @return model that contains import changes.
   */
  @Override
  public TremaImportModel getDataModel() {
    Object encodingSelectedItem = encodingOption.getSelectedItem();
    if (encodingSelectedItem != null) {
      tremaImportModel.setEncoding(encodingOption.getSelectedItem().toString());
    } else {
      tremaImportModel.setEncoding(encodingOption.getItemAt(0));
    }
    tremaImportModel.setSeparator(separatorChar.getText());
    tremaImportModel.setImportSource(importFilePath.getText());
    // process conflicting records
    updateChangesModel(conflictingRecordsTable, CONFLICTING_CHANGES);
    tremaImportModel.getConflictingChanges().addAll(importChanges.get(CONFLICTING_CHANGES));
    // process non-conflicting records
    updateChangesModel(nonConflictingRecordsTable, NONCONFLICTING_CHANGES);
    tremaImportModel.getNonConflictingChanges().addAll(importChanges.get(NONCONFLICTING_CHANGES));
    return tremaImportModel;
  }


  /**
   * Updates the changes for the conflicting and non-conflicting tables.
   * @param table The table that needs to change.
   * @param changesType The type of the change (conflicting/nonconflicting).
   */
  private void updateChangesModel(JTable table, String changesType) {
    TableModel tableModel = table.getModel();

    List<Change> changes = importChanges.get(changesType);

    for (int i = 0; i < tableModel.getRowCount(); i++) {
      Change change = changes.get(i);
      if (tableModel.getValueAt(i, 0) != null && (Boolean) tableModel.getValueAt(i, 0)) {
        change.setAccept(true);
      } else {
        change.setAccept(false);
      }
    }
  }

  /**
   * Sets a panel enabled or disabled.
   * @param panel panel to change
   * @param enabled is enabled
   */
  private void setPanelEnabled(JPanel panel, boolean enabled) {
    panel.setEnabled(enabled);
    Arrays.stream(panel.getComponents()).forEach(component -> {
      if (component instanceof JPanel) {
        setPanelEnabled((JPanel) component, enabled);
      } else {
        component.setEnabled(enabled);
      }
    });
  }

  private ActionListener folderPickerListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      FileChooser.chooseFile(fileChooserDescriptor, event.getProject(), null, virtualFiles -> {
        importFilePath.setText(virtualFiles.getCanonicalPath());
        validateFields();
      });
    }
  };

  private DocumentListener charSeparatorListener = new DocumentListener() {
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

  private void validateFields() {
    messageLabel.setText("");
    messageLabel.setVisible(false);
    this.listener.modelStateValid(true);
    String errorMessage = "";
    if (separatorChar.getText().length() != 1) {
      errorMessage = ERROR_INVALID_SEPARATOR_CHAR;
    }

    if (!Files.isRegularFile(Paths.get(importFilePath.getText()))) {
      errorMessage = ERROR_INVALID_IMPORT_PATH;
    }

    if (!errorMessage.isEmpty()) {
      messageLabel.setText(errorMessage);
      messageLabel.setVisible(true);
      this.listener.modelStateValid(false);
      return;
    }

    try {
      validateImportData();
    } catch (ParseException ignored) { }
  }

  private void validateImportData() throws ParseException {
    try {
      Object selectedEncodingOption = encodingOption.getSelectedItem();
      String selectedEncoding;
      if (selectedEncodingOption == null) {
        selectedEncoding = encodingOption.getItemAt(0);
      } else {
        selectedEncoding = encodingOption.getSelectedItem().toString();
      }
      this.importSource = TremaUtil.getImportSource(importFilePath.getText(),
          selectedEncoding,
          separatorChar.getText().toCharArray()[0]);
      if (importSource == null) {
        throw new ParseException("Could not read the import source.");
      }
      analyzer = new ChangesAnalyzer(this.importSource, this.db);
      analyzer.setUseMasterValueFromFile(true);
      analyzer.analyze();
      importChanges.put(CONFLICTING_CHANGES, analyzer.getConflictingChangesAsList());
      importChanges.put(NONCONFLICTING_CHANGES, analyzer.getNonConflictingChangesAsList());
      displayData();
    } catch (IOException | ParseException e) {
      throw new ParseException("Could not validate the import data");
    }
  }

  private void displayData() {
    displayStatistics();
    displayTableData(conflictingRecordsTable, importChanges.get(CONFLICTING_CHANGES));
    displayTableData(nonConflictingRecordsTable, importChanges.get(NONCONFLICTING_CHANGES));
  }

  private void displayTableData(JTable table, List<Change> changes) {
    if (table == null || table.getModel() == null || changes == null) {
      return;
    }
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.setNumRows(0);

    for (Change change : changes) {
      Vector v = new Vector();
      v.add(change.isAcceptable() ? true : null);
      v.add(change.getKey());
      v.add(change.getImportedValue());
      v.add(change.getAcceptValue());
      v.add(change.getAcceptStatus());
      model.addRow(v);
    }
    table.setModel(model);
  }

  private void displayStatistics() {
    languageLabel.setText(TEXT_LANGUAGE + importSource.getLanguage());
    recordsLabel.setText(TEXT_RECORDS + importSource.getSize());
    databaseMasterLanguageLabel.setText(TEXT_MASTER_LANGUAGE + db.getMasterLanguage());
    databaseRecordsLabel.setText(TEXT_RECORDS + db.getSize());
    conflictingChangesLabel.setText(TEXT_CONFLICTING + analyzer.getConflictingChangesAsList().size());
    nonConflictingChangesLabel.setText(TEXT_NONCONFLICTING + analyzer.getNonConflictingChangesAsList().size());
  }

  private void updateStatusAllRows(JTable table, boolean status) {
    TableModel model = table.getModel();
    for (int i = 0; i < model.getRowCount(); i++) {
      model.setValueAt(status, i, 0);
    }
  }

  private FileChooserDescriptor fileChooserDescriptor =
      new FileChooserDescriptor(true, false, false, false, false, false) {

        @Override
        public boolean isFileSelectable(VirtualFile file) {
          return file != null
              && file.getExtension() != null
              && (file.getExtension().equals("xls") || file.getExtension().equals("csv"));
        }
      };

  private static class TremaDataModel extends DefaultTableModel {

    @Override
    public Class<?> getColumnClass(int i) {
      return super.getValueAt(0, i).getClass();
    }

  }
}
