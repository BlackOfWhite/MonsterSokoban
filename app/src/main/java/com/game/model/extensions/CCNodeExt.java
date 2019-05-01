package com.game.model.extensions;

import org.cocos2d.nodes.CCNode;

/**
 * CCNode extension.
 *
 * @author niewinskip
 */
public class CCNodeExt extends CCNode {

  /**
   * Node text.
   */
  private String nodeText;

  /**
   * Constructor, extends CCNode.
   */
  public CCNodeExt() {
    super();
  }

  /**
   * Getter.
   *
   * @return String.
   */
  public final String getNodeText() {
    return this.nodeText;
  }

  /**
   * Setter.
   *
   * @param text CCNodeExt's text.
   */
  public final void setNodeText(final String text) {
    this.nodeText = text;
  }

}