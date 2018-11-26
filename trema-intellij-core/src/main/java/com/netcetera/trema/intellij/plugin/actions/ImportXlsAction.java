package com.netcetera.trema.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.netcetera.trema.core.ParseException;
import com.netcetera.trema.core.XMLDatabase;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.importing.Change;
import com.netcetera.trema.core.importing.ChangesAnalyzer;
import com.netcetera.trema.intellij.plugin.dialogs.ExceptionDialogForm;
import com.netcetera.trema.intellij.plugin.dialogs.ImportTremaForm;
import com.netcetera.trema.intellij.plugin.dialogs.TremaDialogWrapper;
import com.netcetera.trema.intellij.plugin.dialogs.TremaExceptionDialogWrapper;
import com.netcetera.trema.intellij.plugin.helpers.ActionContextHelper;
import com.netcetera.trema.intellij.plugin.helpers.TremaUtil;
import com.netcetera.trema.intellij.plugin.models.TremaFile;
import com.netcetera.trema.intellij.plugin.models.TremaImportModel;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Action for importing the Trema compatible source to the selected Trema file.
 */
public class ImportXlsAction extends AnAction {

  private List<TremaFile> supportedExtensions = Collections.singletonList(TremaFile.TRM);

  @Override
  public void actionPerformed(AnActionEvent event) {
    TremaDialogWrapper exceptionDialog = null;
    try {
      performImport(event);
    } catch (IOException e) {
      exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog(TremaExceptionDialogWrapper.IO_EXCEPTION,
          e, event);
    } catch (ParseException e) {
      exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog(TremaExceptionDialogWrapper.PARSE_EXCEPTION,
          e, event);
    } catch (Exception e) {
      exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog("Exception", e, event);
    } finally {
      if (exceptionDialog != null) {
        exceptionDialog.show();
      }
    }
  }

  private void performImport(AnActionEvent event) throws IOException, ParseException {

    VirtualFile importFile = event.getData(DataKeys.VIRTUAL_FILE);
    if (importFile == null) {
      throw new IOException("Cannot access the import source or does not exist.");
    }
    String filePath = importFile.getPath();
    IDatabase db = TremaUtil.getXMLDatabase(filePath);

    ImportTremaForm importForm = new ImportTremaForm(event, db);
    TremaDialogWrapper importDialog = new TremaDialogWrapper(event.getProject(), importForm);
    // if OK was clicked
    if (importDialog.showAndGet()) {
      TremaImportModel model = importForm.getDataModel();
      if (model == null) {
        throw new ParseException("Model is null");
      }

      // lists used for logging...
      List<String> appliedConflicting = new ArrayList<>(model.getConflictingChanges().size());
      List<String> deniedConflicting = new ArrayList<>(model.getConflictingChanges().size());
      List<String> appliedNonConflicting = new ArrayList<>(model.getNonConflictingChanges().size());
      List<String> deniedNonConflicting = new ArrayList<>(model.getNonConflictingChanges().size());

      for (Change change: model.getConflictingChanges()) {
        if (ChangesAnalyzer.isApplicable(change)) {
          appliedConflicting.add(change.getKey());
        } else {
          deniedConflicting.add(change.getKey());
        }
        ChangesAnalyzer.applyChange(db, change);
      }

      for (Change change: model.getNonConflictingChanges()) {
        if (ChangesAnalyzer.isApplicable(change)) {
          appliedNonConflicting.add(change.getKey());
        } else {
          deniedNonConflicting.add(change.getKey());
        }
        ChangesAnalyzer.applyChange(db, change);
      }
      //write database to file and refresh the file in the editor
      TremaUtil.doImport((XMLDatabase) db, event, model);

      // create import log
      StringBuilder builder = new StringBuilder();
      String importLog;
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss O yyyy");
      builder.append(ZonedDateTime.now().format(formatter)).append("\nImport log: \n\n");
      builder.append("Accepted conflicting changes (").append(appliedConflicting.size()).append("):\n");
      builder.append(String.join("\n", appliedConflicting)).append("\n\n");
      builder.append("Denied conflicting changes (").append(deniedConflicting.size()).append("):\n");
      builder.append(String.join("\n", deniedConflicting)).append("\n\n");
      builder.append("Accepted non-conflicting changes (").append(appliedNonConflicting.size()).append("):\n");
      builder.append(String.join("\n", appliedNonConflicting)).append("\n\n");
      builder.append("Denied non-conflicting changes (").append(deniedNonConflicting.size()).append("):\n");
      builder.append(String.join("\n", deniedNonConflicting)).append("\n\n");

      importLog = builder.toString();
      TremaDialogWrapper logWrapper = new TremaDialogWrapper(event.getProject(),
          new ExceptionDialogForm("ImportLog", "Import Log", "", importLog));
      logWrapper.show();
    }
  }

  @Override
  public void update(AnActionEvent event) {
    super.update(event);
    ActionContextHelper.setVisibilityDependingOnContext(event, supportedExtensions);
  }
}
