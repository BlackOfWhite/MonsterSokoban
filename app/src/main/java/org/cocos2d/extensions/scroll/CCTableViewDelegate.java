package org.cocos2d.extensions.scroll;

/**
 * Sole purpose of this delegate is to single touch event in this version.
 */
public interface CCTableViewDelegate {

  /**
   * Delegate to respond touch event
   *
   * @param table table contains the given cell
   * @param cell cell that is touched
   */
  void tableCellTouched(CCTableView table, CCTableViewCell cell);
}
