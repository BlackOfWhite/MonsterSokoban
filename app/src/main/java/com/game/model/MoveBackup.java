package com.game.model;

/**
 * Representation of the last move. Save core properties.
 *
 * @author niewinskip
 */
public class MoveBackup {

  /**
   * Hero position.
   */
  private final int heroPosition;

  /**
   * Skull position.
   */
  private int skullTag;

  /**
   * Skull tag.
   */
  private int skullPosition;

  /**
   * Constructor.
   *
   * @param heroPosition Hero position.
   * @param skullTag Skull tag.
   * @param skullPosition Skull position.
   */
  public MoveBackup(final int heroPosition, final int skullTag, final int skullPosition) {
    this.heroPosition = heroPosition;
    this.skullTag = skullTag;
    this.skullPosition = skullPosition;
  }

  /**
   * Get hero position.
   *
   * @return Hero position.
   */
  public final int getHeroPosition() {
    return heroPosition;
  }

  /**
   * Get skull position.
   *
   * @return Skull position.
   */
  public final int getSkullPosition() {
    return skullPosition;
  }

  /**
   * Setter.
   *
   * @param position Skull position.
   */
  public final void setSkullPosition(final int position) {
    this.skullPosition = position;
  }

  /**
   * Get skull tag.
   *
   * @return Skull tag.
   */
  public final int getSkullTag() {
    return skullTag;
  }

  /**
   * Setter.
   *
   * @param tag Skull tag.
   */
  public final void setSkullTag(final int tag) {
    this.skullTag = tag;
  }
}
