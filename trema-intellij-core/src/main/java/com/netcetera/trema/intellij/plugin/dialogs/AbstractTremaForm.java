package com.netcetera.trema.intellij.plugin.dialogs;

import com.netcetera.trema.intellij.plugin.events.TremaModelChangedListener;

public abstract class AbstractTremaForm<T> implements ITremaFormComponent<T> {
  protected TremaModelChangedListener listener;
  protected String windowTitle;

  @Override
  public void addModelChangedListener(TremaModelChangedListener listener) {
    this.listener = listener;
    this.listener.modelStateValid(false);
  }

  @Override
  public String getWindowTitle() {
    return windowTitle;
  }

  @Override
  public T getDataModel() {
    return null;
  }
}
