package com.netcetera.trema.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.netcetera.trema.core.ParseException;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.exporting.ExportException;
import com.netcetera.trema.intellij.plugin.dialogs.ExportTremaForm;
import com.netcetera.trema.intellij.plugin.dialogs.OverwriteFilesDialogForm;
import com.netcetera.trema.intellij.plugin.dialogs.TremaDialogWrapper;
import com.netcetera.trema.intellij.plugin.dialogs.TremaExceptionDialogWrapper;
import com.netcetera.trema.intellij.plugin.helpers.ActionContextHelper;
import com.netcetera.trema.intellij.plugin.helpers.TremaUtil;


import com.netcetera.trema.intellij.plugin.models.LanguageAndFileName;
import com.netcetera.trema.intellij.plugin.models.TremaConflictExportFileModel;
import com.netcetera.trema.intellij.plugin.models.TremaExportModel;
import com.netcetera.trema.intellij.plugin.models.TremaFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Action for exporting the Trema source to XLS/CSV/Properties.
 */
public class ExportXlsAction extends AnAction {

    private List<TremaFile> supportedExtensions = Collections.singletonList(TremaFile.TRM);

    @Override
    public void actionPerformed(AnActionEvent event) {
      TremaDialogWrapper exceptionDialog = null;

      try {
        performExport(event);
      } catch (IOException e) {
          exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog(TremaExceptionDialogWrapper.IO_EXCEPTION,
              e, event);
      } catch (ParseException e) {
        exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog(TremaExceptionDialogWrapper.PARSE_EXCEPTION,
            e, event);
      } catch (ExportException e) {
        exceptionDialog = TremaExceptionDialogWrapper.createExceptionDialog(
            TremaExceptionDialogWrapper.EXPORT_EXCEPTION,
            e, event);
      } finally {
        if (exceptionDialog != null) {
          exceptionDialog.show();
        }
      }
    }

    private void performExport(AnActionEvent event) throws IOException, ExportException, ParseException {
      VirtualFile exportFile = event.getData(DataKeys.VIRTUAL_FILE);
      if (exportFile == null) {
        throw new ExportException("The source path does not exist or cannot access it.");
      }
      String filePath = exportFile.getPath();
      IDatabase db = TremaUtil.getXMLDatabase(filePath);
      ExportTremaForm exportForm = new ExportTremaForm(event, db);
      TremaDialogWrapper exportDialog = new TremaDialogWrapper(event.getProject(), exportForm);


      // if OK was clicked
      if (exportDialog.showAndGet()) {

        TremaExportModel model = exportForm.getDataModel();
        if (model == null) {
          throw new ParseException("Model is null");
        }

        List<LanguageAndFileName> conflictedFiles = checkIfExportFilesExist(model.getLanguagesToExport());

        if (conflictedFiles.size() > 0) {

          TremaDialogWrapper conflictFilesDialog = createExportConflictedFilesDialog(event,
              model.getLanguagesToExport());

          if (conflictFilesDialog.showAndGet()) {

            TremaConflictExportFileModel doNotOverwriteFiles = (TremaConflictExportFileModel) conflictFilesDialog
                .getComponent().getDataModel();
            model.getLanguagesToExport().removeIf(languageAndFileName ->
                doNotOverwriteFiles.getConflictPaths().contains(languageAndFileName.toString()));
          } else {
            // cancel was clicked, remove conflicted files from the export
            model.getLanguagesToExport().removeAll(conflictedFiles);
          }
        }

        if (model.getExportFileType() == TremaFile.XLS) {
          TremaUtil.doXlsExport(model, db);
        } else if (model.getExportFileType() == TremaFile.CSV) {
          TremaUtil.doCsvExport(model, db);
        } else if (model.getExportFileType() == TremaFile.PROPS) {
          TremaUtil.doPropertiesExport(model, db);
        } else {
          throw new ExportException("Invalid export extension");
        }
      }
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        ActionContextHelper.setVisibilityDependingOnContext(event, supportedExtensions);
    }

    private List<LanguageAndFileName> checkIfExportFilesExist(List<LanguageAndFileName> paths) {
      List<LanguageAndFileName> filesThatExist = new ArrayList<>();
      for (LanguageAndFileName langAndFileName: paths) {
        File file = new File(langAndFileName.getFileName());
        if (file.exists()) {
          filesThatExist.add(langAndFileName);
        }
      }
      return filesThatExist;
    }

  private TremaDialogWrapper createExportConflictedFilesDialog(AnActionEvent event,
                                                               List<LanguageAndFileName> languages) {
    OverwriteFilesDialogForm conflictModel = new OverwriteFilesDialogForm(languages);
    return new TremaDialogWrapper(event.getProject(), conflictModel);
  }
}
