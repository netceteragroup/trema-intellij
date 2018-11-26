package com.netcetera.trema.intellij.plugin.dialogs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Dialog wrapper for exception dialogs.
 */
public class TremaExceptionDialogWrapper extends TremaDialogWrapper {
  /**
   * Exception message when catching IOException exceptions.
   */
  public static final String IO_EXCEPTION = "Ooops, some IOException occurred. Here is a stack trace.";

  /**
   * Exception message when catching ParseException exceptions.
   */
  public static final String PARSE_EXCEPTION = "There was a problem with the trema file.";

  /**
   * Exception message when catching ExportException exceptions.
   */
  public static final String EXPORT_EXCEPTION = "Now that's embarrassing. The plugin cannot export the trema content.";

  /**
   * TremaExceptionDialogWrapper constructor.
   *
   * @param project   {@link Project} the project that is currently active in IntelliJ
   * @param component {@link ITremaFormComponent} the
   */
  public TremaExceptionDialogWrapper(@Nullable Project project, ITremaFormComponent component) {
    super(project, component);
  }

  /**
   * Creates a dialog for displaying exceptions.
   * @param title the title of the window.
   * @param ex the exception that has occurred
   * @param event the event that was triggered
   * @return {@link TremaDialogWrapper} dialog wrapper to display the exception mesasge
   */
  public static TremaDialogWrapper createExceptionDialog(String title, Exception ex, AnActionEvent event) {
    ExceptionDialogForm exceptionForm = new ExceptionDialogForm(title,
        ex.getMessage(),
        ExceptionUtils.getStackTrace(ex));
    exceptionForm.listener.modelStateValid(true);
    return new TremaDialogWrapper(event.getProject(), exceptionForm);
  }
}
