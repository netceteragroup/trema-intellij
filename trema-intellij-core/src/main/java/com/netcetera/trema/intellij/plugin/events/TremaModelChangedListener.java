package com.netcetera.trema.intellij.plugin.events;

/**
 * Event listener when the trema model has changed.
 */
public interface TremaModelChangedListener {

  /**
   * Event fied when the trema model has changed.
   * @param tremaValidationState the state of the trema model.
   *                             True  -> model is valid
   *                             False -> model is invalid
   */
  void modelStateValid(boolean tremaValidationState);
}
