package com.game.preferences;

import com.game.helpers.InGameHelper;
import com.game.model.extensions.HeroPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class containing global constant. LEVEL NUMERATION STARTS WITH 1!
 *
 * @author Piotr Niewinski.
 */
public final class GlobalPreferences {

  /**
   * Is logger logging.
   */
  public final static boolean IS_LOGGER_ON = true;
  /**
   * Number of levels created with easy difficulty.
   */
  public static final int NUMBER_OF_EASY_LEVELS_CREATED = 30;
  /**
   * Number of levels created with medium difficulty.
   */
  public static final int NUMBER_OF_MEDIUM_LEVELS_CREATED = 15;
  /**
   * Number of levels created with hard difficulty.
   */
  public static final int NUMBER_OF_HARD_LEVELS_CREATED = 15;
  /**
   * Total number of levels.
   */
  public static final int TOTAL_NUMBER_OF_LEVELS = NUMBER_OF_EASY_LEVELS_CREATED + NUMBER_OF_HARD_LEVELS_CREATED
      + NUMBER_OF_MEDIUM_LEVELS_CREATED;
  /**
   * Levels per row per page.
   */
  public static final int LEVELS_PER_ROW = 5;
  /**
   * Levels per columns per page.
   */
  public static final int LEVELS_PER_COLUMN = 3;
  /**
   * Levels per page in level view.
   */
  public static final int LEVELS_PER_PAGE = LEVELS_PER_COLUMN * LEVELS_PER_ROW;
  /**
   * Number of heroes available in shop.
   */
  public static final int NUMBER_OF_HEROES_IN_SHOP = 6;
  /**
   * Portal tile.
   */
  public static final int TELEP = -1;
  /**
   * Sliding tile.
   */
  public static final int SLIDE = -2;
  /**
   * Normal floor tile.
   */
  public static final int FLOOR = 0;
  /**
   * Skull destination tile.
   */
  public static final int DESTY = 1;
  /**
   * Blocker tile.
   */
  public static final int BLOCK = 2;
  /**
   * Empty tile.
   */
  public static final int EMPTY = 3;
  /**
   * Font path.
   */
  public static final String FONT = "Fonts/font.fnt";
  /**
   * Tag for the first skull. Next one should be tag+1, tag+2 etc. Reserve
   * tags for next skulls.
   */
  public static final int FIRST_SKULL_TAG = InGameHelper.generateUniqueTag();
  /**
   * Number of items available in shop.
   */
  public static final int TOTAL_ITEMS_IN_SHOP = 6;
  /**
   * Available moves, left.
   */
  public static final String MOVE_LEFT = "MOVE_LEFT";
  /**
   * Available moves, left.
   */
  public static final String MOVE_RIGHT = "MOVE_RIGHT";
  /**
   * Available moves, left.
   */
  public static final String MOVE_UP = "MOVE_UP";
  /**
   * Available moves, down.
   */
  public static final String MOVE_DOWN = "MOVE_DOWN";
  /**
   * No available moves.
   */
  public static final String NO_MOVE = "NO_MOVE";
  /**
   * Chosen difficulty.
   */
  public static final String LEVEL_EASY = "DIFF_EASY";
  /**
   * Chosen difficulty.
   */
  public static final String LEVEL_MEDIUM = "DIFF_MEDIUM";
  /**
   * Chosen difficulty.
   */
  public static final String LEVEL_HARD = "DIFF_HARD";
  /**
   * Main layer tag.
   */
  public static final int MAIN_LAYER_TAG = 901;
  /**
   * Shop layer tag.
   */
  public static final int SHOP_LAYER_TAG = 902;
  /**
   * Difficulty choose layer tag.
   */
  public static final int DIFFICULTY_LAYER_TAG = 903;
  /**
   * Level selection layer tag.
   */
  public static final int LEVEL_SELECTION_LAYER_TAG = 904;
  /**
   * Game layer tag.
   */
  public static final int GAME_LAYER_TAG = 905;
  /**
   * Credits layer tag.
   */
  public static final int CREDITS_LAYER_TAG = 906;
  /**
   * List of heroes available to buy or select in shop (256x256).
   */
  public static final List<HeroPanel> heroList_256 = new ArrayList<>(
      Arrays.asList(new HeroPanel("Vlad", SpritePreferences.VLAD_256, 0),
          new HeroPanel("Frankie", SpritePreferences.FRANKIE_256, 15),
          new HeroPanel("Otzi", SpritePreferences.OTZI_256, 25),
          new HeroPanel("Bones", SpritePreferences.BONES_256, 40),
          new HeroPanel("Jack", SpritePreferences.JACK_256, 60),
          new HeroPanel("The Zombie", SpritePreferences.ZOMBIE_256, 75)));
  /**
   * List of heroes available to buy or select in shop (128x128).
   */
  public static final List<HeroPanel> heroList_128 = new ArrayList<>(
      Arrays.asList(new HeroPanel("Vlad", SpritePreferences.VLAD_128, 0),
          new HeroPanel("Frankie", SpritePreferences.FRANKIE_128, 15),
          new HeroPanel("Otzi", SpritePreferences.OTZI_128, 25),
          new HeroPanel("Bones", SpritePreferences.BONES_128, 40),
          new HeroPanel("Jack", SpritePreferences.JACK_128, 60),
          new HeroPanel("The Zombie", SpritePreferences.ZOMBIE_128, 75)));
  /**
   * Current/Last selected difficulty.
   */
  public static String CURRENT_LEVEL_DIFFICULTY = LEVEL_EASY;

  /**
   * Constructor.
   */
  private GlobalPreferences() {

  }
}
