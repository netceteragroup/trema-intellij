package com.netcetera.trema.intellij.plugin.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * DialogWrapper used to display Trema forms.
 */
public class TremaDialogWrapper extends DialogWrapper {

  private ITremaFormComponent component;

  /**
   * TremaDialogWrapper constructor.
   * @param project {@link Project} the project that is currently active in IntelliJ
   * @param component {@link ITremaFormComponent} the
   */
  public TremaDialogWrapper(@Nullable Project project, ITremaFormComponent component) {
    super(project);
    this.component = component;
    setTitle(this.component.getWindowTitle());
    init();
    this.component.addModelChangedListener(this::setOKActionEnabled);
  }

  public ITremaFormComponent getComponent() {
    return component;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
        return component.$$$getRootComponent$$$();
    }
}
