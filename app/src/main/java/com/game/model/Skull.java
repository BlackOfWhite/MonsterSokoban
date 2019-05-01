package com.game.model;

import com.game.preferences.GlobalPreferences;

/**
 * Representation of skull object.
 *
 * @author niewinskip
 */
public class Skull {

  /**
   * Skull position.
   */
  private int position;

  /**
   * Skull tag.
   */
  private int tag;

  /**
   * Constructor.
   *
   * @param pos Skull's position.
   */
  public Skull(final int pos) {
    setPosition(pos);
    setTag(GlobalPreferences.FIRST_SKULL_TAG + pos);
  }

  /**
   * Getter.
   *
   * @return Skull's tag.
   */
  public final int getSkullTag() {
    return tag;
  }

  /**
   * Getter.
   *
   * @return Skull's position.
   */
  public final int getPosition() {
    return position;
  }

  /**
   * Setter.
   *
   * @param pos Skull's position.
   */
  public final void setPosition(final int pos) {
    this.position = pos;
  }

  /**
   * Setter.
   *
   * @param tag Skull's tag.
   */
  public final void setTag(final int tag) {
    this.tag = tag;
  }
}
