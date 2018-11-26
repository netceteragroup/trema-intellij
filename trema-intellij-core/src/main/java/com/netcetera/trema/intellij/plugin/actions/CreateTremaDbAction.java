package com.netcetera.trema.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.netcetera.trema.intellij.plugin.dialogs.CreateTremaDbDialogForm;
import com.netcetera.trema.intellij.plugin.dialogs.TremaDialogWrapper;
import com.netcetera.trema.intellij.plugin.models.CreateNewTremaDbModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Action for creating a new Trema file.
 */
public class CreateTremaDbAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    CreateTremaDbDialogForm dialog = new CreateTremaDbDialogForm(event);
    TremaDialogWrapper createNewDbModel = new TremaDialogWrapper(event.getProject(), dialog);
    if (createNewDbModel.showAndGet()) {
      try {
        CreateNewTremaDbModel model = dialog.getDataModel();
        InputStream initialContents = getInitialContents(dialog.getDataModel());

        Files.copy(initialContents,
            Paths.get(model.getFolderPath(), model.getFileName()).toAbsolutePath(),
            StandardCopyOption.REPLACE_EXISTING);

        initialContents.close();

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
  * Creates an input stream with the initial contents for an empty
  * Trema XML database.
  * @param model Model containing all data needed for the initial database structure
  * @return the initial contents for an empty Trema XML database
  * @throws UnsupportedEncodingException if the given encoding is not
  * supported
  */
  private InputStream getInitialContents(CreateNewTremaDbModel model) throws UnsupportedEncodingException {
    StringBuilder contents = new StringBuilder(1024);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss O yyyy");
    String lineSeparator = "\n";
    contents.append("<?xml version=\"1.0\" encoding=\"").append(model.getEncoding()).append("\"?>");
    contents.append(lineSeparator);
    contents.append("<!-- generated on ").append(ZonedDateTime.now().format(formatter)).append(" -->");
    contents.append(lineSeparator);
    contents.append("<trema masterLang=\"").append(model.getMasterLanguage()).append("\"");
    if (model.getSchemaPath() != null && model.getSchemaPath().length() > 0) {
      contents.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      contents.append(" xsi:noNamespaceSchemaLocation=\"").append(model.getSchemaPath()).append("\"");
    }
    contents.append("/>");
    return new ByteArrayInputStream(contents.toString().getBytes(model.getEncoding()));
  }
}