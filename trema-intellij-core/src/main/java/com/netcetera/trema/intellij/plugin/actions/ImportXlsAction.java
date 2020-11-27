package com.netcetera.trema.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.netcetera.trema.core.ParseException;
import com.netcetera.trema.core.XMLDatabase;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.importing.Change;
import com.netcetera.trema.core.importing.ChangesAnalyzer;
import com.netcetera.trema.intellij.plugin.dialogs.ImportSuccessDialog;
import com.netcetera.trema.intellij.plugin.dialogs.ImportTremaForm;
import com.netcetera.trema.intellij.plugin.dialogs.TremaDialogWrapper;
import com.netcetera.trema.intellij.plugin.dialogs.TremaExceptionDialogWrapper;
import com.netcetera.trema.intellij.plugin.helpers.ActionContextHelper;
import com.netcetera.trema.intellij.plugin.helpers.TremaUtil;
import com.netcetera.trema.intellij.plugin.models.TremaFile;
import com.netcetera.trema.intellij.plugin.models.TremaImportModel;

import java.io.IOException;
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
      e.printStackTrace();
      exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog(TremaExceptionDialogWrapper.IO_EXCEPTION,
          e, event);
    } catch (ParseException e) {
      e.printStackTrace();
      exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog(TremaExceptionDialogWrapper.PARSE_EXCEPTION,
          e, event);
    } catch (Exception e) {
      e.printStackTrace();
      exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog("Exception", e, event);
    } finally {
      if (exceptionDialog != null) {
        exceptionDialog.show();
      }
    }
  }

  private void performImport(AnActionEvent event) throws IOException, ParseException {

    VirtualFile importFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
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
      String importLog = "Accepted conflicting changes (" + appliedConflicting.size() + "):"
        + formatAsList(appliedConflicting)
        + "\n\nDenied conflicting changes (" + deniedConflicting.size() + "):"
        + formatAsList(deniedConflicting)
        + "\n\nAccepted non-conflicting changes (" + appliedNonConflicting.size() + "):"
        + formatAsList(appliedNonConflicting)
        + "\n\nDenied non-conflicting changes (" + deniedNonConflicting.size() + "):"
        + formatAsList(deniedNonConflicting);

      TremaDialogWrapper wrapper = new TremaDialogWrapper(event.getProject(),
        new ImportSuccessDialog("Import Log", importLog));
      wrapper.show();
    }
  }

  @Override
  public void update(AnActionEvent event) {
    super.update(event);
    ActionContextHelper.setVisibilityDependingOnContext(event, supportedExtensions);
  }

  private static String formatAsList(List<String> elements) {
    if (elements.isEmpty()) {
      return "";
    }
    return "\n - " + String.join("\n - ", elements);
  }
}
