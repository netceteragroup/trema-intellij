package com.netcetera.trema.intellij.plugin.helpers;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Helper for limiting the text field to some number of characters.
 */
public class TextFieldLimitHelper extends PlainDocument {
  private int limit;

  /**
   * Constructor.
   * @param limit limit the text field number of characters to the specified value
   */
  public TextFieldLimitHelper(int limit) {
    super();
    this.limit = limit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
    if (str == null) {
      return;
    }
    if ((getLength() + str.length()) <= limit) {
      super.insertString(offset, str, attr);
    }
  }
}
