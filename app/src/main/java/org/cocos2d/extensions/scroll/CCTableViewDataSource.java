package org.cocos2d.extensions.scroll;

import org.cocos2d.types.CGSize;

/**
 * Data source that governs table backend data.
 */
public interface CCTableViewDataSource {

  /**
   * cell height for a given table.
   *
   * @param table table to hold the instances of Class
   * @return cell size
   */
  CGSize cellSizeForTable(CCTableView table);

  /**
   * a cell instance at a given index
   *
   * @param idx index to search for a cell
   * @return cell found at idx
   */
  CCTableViewCell tableCellAtIndex(CCTableView table, int idx);

  /**
   * Returns number of cells in a given table view.
   *
   * @return number of cells
   */
  int numberOfCellsInTableView(CCTableView table);
}
