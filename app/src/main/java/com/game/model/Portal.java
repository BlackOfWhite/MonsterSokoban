package com.game.model;

/**
 * Representation of portal object.
 *
 * @author niewinskip
 */
public class Portal {

  /**
   * Portal's position.
   */
  private final int position;

  /**
   * Portal's destination.
   */
  private final int destination;

  /**
   * Constructor.
   *
   * @param pos Portal's position.
   * @param dest Portal's destination.
   */
  public Portal(final int pos, final int dest) {
    this.position = pos;
    this.destination = dest;
  }

  /**
   * Getter.
   *
   * @return Portal's position.
   */
  public final int getPos() {
    return position;
  }

  /**
   * Getter.
   *
   * @return Portal's destination.
   */
  public final int getDestination() {
    return destination;
  }

}
