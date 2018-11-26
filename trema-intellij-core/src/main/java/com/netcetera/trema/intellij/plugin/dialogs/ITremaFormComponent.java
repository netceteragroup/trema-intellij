package com.netcetera.trema.intellij.plugin.dialogs;

import com.netcetera.trema.intellij.plugin.events.TremaModelChangedListener;

import javax.swing.JComponent;

/**
 * Interface used for identifying form components.
 * @param <T> the model that is returned
 */
public interface ITremaFormComponent<T> {
  /**
   * Gets the root component used in the Trema dialogs. This is generated by IDEA GUI Designer.
   * @return the root component {@link javax.swing.JComponent}.
   */
  JComponent $$$getRootComponent$$$();

  /**
   * Returns the title for the window where the form is displayed.
   * @return the title for the window
   */
  String getWindowTitle();

  /**
   * Compiles and returns the Trema data model.
   * @return {@link T} the form model.
   */
  T getDataModel();

  /**
   * Adds a listener for when the model changes.
   * @param listener the listener
   */
  void addModelChangedListener(TremaModelChangedListener listener);
}