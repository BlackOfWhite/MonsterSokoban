package com.game.model;

import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.nodes.CCSprite;

/**
 * Class for keeping Sprite and its Animations Sequence in one object. Useful
 * with calling functions in animations and sequences.
 *
 * @author niewinskip
 */
public class SpriteAnimationBinder {

  /**
   * CCSequence.
   */
  private final CCSequence seq;

  /**
   * CCSprite.
   */
  private final CCSprite sprite;

  /**
   * Constructor.
   *
   * @param seq CCSequence.
   * @param sprite CCSprite.
   */
  public SpriteAnimationBinder(final CCSequence seq, final CCSprite sprite) {
    this.seq = seq;
    this.sprite = sprite;
  }

  /**
   * Getter.
   *
   * @return CCSequence.
   */
  public final CCSequence getSequence() {
    return seq;
  }

  /**
   * Getter.
   *
   * @return CCSprite.
   */
  public final CCSprite getSprite() {
    return sprite;
  }

}
