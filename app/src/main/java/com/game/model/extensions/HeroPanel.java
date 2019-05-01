package com.game.model.extensions;

import org.cocos2d.nodes.CCSprite;

/**
 * Create object representing game hero available in shop.
 *
 * @author niewinskip
 */
public class HeroPanel {

  /**
   * Price in gems.
   */
  private final int price;

  /**
   * Hero display name.
   */
  private final String name;

  /**
   * Path to hero's sprite.
   */
  private final CCSprite spritePath;

  /**
   * Constructor.
   *
   * @param name Hero/item name.
   * @param spritePath Sprite path.
   * @param price Sprite (diamonds).
   */
  public HeroPanel(final String name, final String spritePath, final int price) {
    this.name = name;
    this.spritePath = CCSprite.sprite(spritePath);
    this.price = price;
  }

  /**
   * Access price.
   *
   * @return Price
   */
  public final int getPrice() {
    return price;
  }

  /**
   * Access name.
   *
   * @return Name
   */
  public final String getName() {
    return name;
  }

  /**
   * Access sprite path.
   *
   * @return Sprite's path
   */
  public final CCSprite getSprite() {
    return spritePath;
  }
}
