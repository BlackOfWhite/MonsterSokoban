package com.game.layers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.game.MonsterSokoban.R;
import com.game.helpers.AchievementHelper;
import com.game.helpers.InGameHelper;
import com.game.helpers.LevelParser;
import com.game.helpers.ParticleHelper;
import com.game.logger.Logger;
import com.game.model.MoveBackup;
import com.game.model.Portal;
import com.game.model.Skull;
import com.game.model.SpriteAnimationBinder;
import com.game.model.extensions.CCNodeExt;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.game.preferences.SharedPreferencesKeys;
import com.game.preferences.SpritePreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.cocos2d.actions.instant.CCCallFunc;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.instant.CCCallFuncND;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCScaleTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.transitions.CCFadeTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.utils.CCFormatter;

/**
 * Class for managing and displaying  process.
 *
 * @author niewinskip
 */
public class GameLayer extends CCLayer {

  /**
   * Z order.
   */
  private static final int Z0 = 0, Z1 = 1, Z2 = 2, Z3 = 3, Z4 = 4, Z5 = 5;
  /**
   * Ice tile.
   */
  private static final int SLIDE = GlobalPreferences.SLIDE;
  /**
   * Floor tile.
   */
  private static final int FLOOR = GlobalPreferences.FLOOR;
  /**
   * Destination tile.
   */
  private static final int DESTY = GlobalPreferences.DESTY;
  /**
   * Portal tile.
   */
  private static final int TELEP = GlobalPreferences.TELEP;
  /**
   * Block tile.
   */
  private static final int BLOCK = GlobalPreferences.BLOCK;
  /**
   * Empty tile.
   */
  private static final int EMPTY = GlobalPreferences.EMPTY;
  /**
   * Tag of each tile.
   */
  private static final int TILE_NODE_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of pushes label.
   */
  private static final int PUSHES_LABEL_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of skulls label.
   */
  private static final int SKULLS_LABEL_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of hero sprite.
   */
  private static final int HERO_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag.
   */
  private static final int RESET_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag.
   */
  private static final int REVERT_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of winning  color overlay.
   */
  private static final int WIN_OVERLAY_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of winning  'next' button. Used with touch listener.
   */
  private static final int WIN_NEXT_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of winning  'retry' button. Used with touch listener.
   */
  private static final int WIN_RETRY_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of winning  'back' button. Used with touch listener.
   */
  private static final int WIN_BACK_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag of first skull sprite. Next skulls' tags are equal to
   * [FIRST_SKULL_TAG + its' position].
   */
  private static final int FIRST_SKULL_TAG = GlobalPreferences.FIRST_SKULL_TAG;
  /**
   * Fonts used.
   */
  private static final String myFont = GlobalPreferences.FONT;
  /**
   * Font color.
   */
  private static final ccColor3B fontColor = ccColor3B.ccc3(255, 255, 255);
  /**
   * Reference to the android context variable.
   */
  private static Context appContext;
  /**
   * Number of the current level.
   */
  private static int levelNumber;
  /**
   * Tag of currently moved skull sprite.
   */
  private static int skullCurrentTag;
  /**
   * Position in table of currently moving skull sprite.
   */
  private static int skullCurrentPosition;
  /**
   * Calculated size of each tile.
   */
  private static float tileSquareSize;
  /**
   * Number of  board's rows.
   */
  private static int numberOfRows;
  /**
   * Number of  board's columns.
   */
  private static int numberOfColumns;
  /**
   * Game board size. Should be equal to columns * rows.
   */
  private static int tabSize;
  /**
   * Single move time multiplier. Should be greater than 1.
   */
  private final float moveBoost = 1.75f;
  /**
   * Click.
   */
  private CGPoint startLocation;
  /**
   * Number of skulls to collect in current level.
   */
  private int skullsToCollect = 100;
  /**
   * Number of skulls collected in current level.
   */
  private int numSkullsCollected = 0;
  /**
   * Scale for text labels. Keep original scale.
   */
  private float pushesLabelScale;
  /**
   * Scale for text labels. Keep original scale.
   */
  private float skullsLabelScale;
  /**
   * First coordinate to start laying tiles. Starting from top - left.
   */
  private int startY;
  /**
   * First coordinate to start laying tiles. Starting from top - left.
   */
  private int startX;
  /**
   * Flag. Check if all skulls were collected and if should display
   * LevelSummaryScreen.
   */
  private boolean levelCompleted = false;
  /**
   * Device's screen size.
   */
  private final CGSize screenSize;
  /**
   * Game screen scale. Ensure that looks nice on different screen sizes.
   */
  private final float generalScaleFactor;
  /**
   * Scale for tiles.
   */
  private float scaleFactor;
  /**
   * Remember sequence for  hero.
   */
  private CCSequence heroCurrSeq;
  /**
   * Blocks' sprites resolution. Available are 256 & 128. If calculated size
   * for block is less than 128, than set resolution to 128. Better image
   * quality.
   */
  private float spritesResolution;
  /**
   * Check if any moved was stored in list of the previous moves. Used for
   * reverting moves.
   */
  private boolean anyMoveBackedUp;
  /**
   * Hero previous position. Used for reverting moves.
   */
  private int heroPreviousPosition;
  /**
   * Hero current position. Used for reverting moves.
   */
  private int heroCurrentPosition;
  /**
   * Number of move reverts.
   */
  private int revertsNumber;
  /**
   * Number of pushes.
   */
  private int pushes;
  /**
   * Minimum number of pushes.
   */
  private int minimumNumberOfPushes = 100;
  /**
   * Game map.
   */
  private int[] gameMap;
  /**
   * Hero existence map.
   */
  private boolean[] heroTable;
  /**
   * Skulls existence map.
   */
  private boolean[] skullsTable;
  /**
   * List of skulls.
   */
  private List<Skull> skullsList;
  /**
   * List of portals.
   */
  private List<Portal> portalList;
  /**
   * List of backed up moves.
   */
  private List<MoveBackup> prevMovesList;
  /**
   * CCNode to hold all tiles.
   */
  private CCNode tilesNode;
  /**
   * Chill out step counter.
   */
  private int chillOutSteps = 0;

  /**
   * Constructor.
   *
   * @param level Current level.
   */
  public GameLayer(final int level) {
    levelNumber = level;
    this.pushes = 0;
    this.revertsNumber = 0;
    this.anyMoveBackedUp = false;
    this.spritesResolution = 256.0f;

    screenSize = DevicePreferences.screenSize;
    generalScaleFactor = DevicePreferences.generalScaleFactor;
    appContext = CCDirector.sharedDirector().getActivity();

    // Run function in scheduled thread.
    // schedule("updateTimeLabel", 1f);
    addSpritesBefore();
    calcGameParams();
    loadTiles();
    addSpritesAfter();
    checkAllSkullsCollected();
    addChild(ParticleHelper.fireflies(screenSize.width, screenSize.height), Z5);
    preloadSoundEffects();
    InGameHelper.turnAllSensorsOn(this);
  }

  /**
   * Start GameLayer scene.
   *
   * @param levelNumber Create  layer, pass level number to constructor.
   * @return Scene.
   */
  public static CCScene scene(final int levelNumber) {
    CCScene scene = CCScene.node();
    CCLayer layer = new GameLayer(levelNumber);
    scene.addChild(layer);
    return scene;
  }

  /**
   * Add sprites. Execute after calculating  parameters and loading tiles.
   */
  private void addSpritesAfter() {
    // Item panel.
    CCSprite itemPanel = CCSprite.sprite(SpritePreferences.ITEM_PANEL);
    itemPanel.setScale(screenSize.height / 3 / itemPanel.getContentSize().height);
    itemPanel.setPosition(screenSize.width * 0.9f, screenSize.height / 2.0f);
    addChild(itemPanel, Z1);

    // Item panel: skull.
    CCSprite skullItem;
    if (screenSize.height * 0.15f > 128) {
      skullItem = CCSprite.sprite(SpritePreferences.WHITE_SKULL_BLOCK_256);
    } else {
      skullItem = CCSprite.sprite(SpritePreferences.WHITE_SKULL_BLOCK_128);
    }
    skullItem.setScale(screenSize.height * 0.15f / skullItem.getContentSize().height);
    skullItem.setPosition(screenSize.width * 0.9f, screenSize.height * 0.54f);
    addChild(skullItem, itemPanel.getZOrder() + 1);

    // Add skulls Label to track number of skulls collected
    CCBitmapFontAtlas skullsLabel = CCBitmapFontAtlas.bitmapFontAtlas("0/" + skullsToCollect, myFont);
    skullsLabelScale = screenSize.height * 0.07f / skullsLabel.getContentSize().height;
    skullsLabel.setScale(skullsLabelScale);
    skullsLabel.setColor(fontColor);
    skullsLabel.setPosition(screenSize.width * 0.9f, screenSize.height * 0.405f);
    addChild(skullsLabel, itemPanel.getZOrder() + 1, SKULLS_LABEL_TAG);
  }

  /**
   * Add sprites. Execute before calculating  parameters and loading
   * tiles. Divide screen height into 10 parts. 2 most right are for side
   * panels.
   */
  private void addSpritesBefore() {
    // Load sprite for background.
    CCSprite bg = InGameHelper.getBackground(screenSize);
    addChild(bg, Z0);

    // Side panel energy
    CCSprite itemPanel = CCSprite.sprite(SpritePreferences.ITEM_PANEL);
    itemPanel.setScale(screenSize.height / 3 / itemPanel.getContentSize().height);
    itemPanel.setPosition(screenSize.width * 0.9f, screenSize.height * 5 / 6f);
    addChild(itemPanel, Z1);

    // Item panel: energy.
    CCSprite energyItem = CCSprite.sprite(SpritePreferences.I_ENERGY);
    energyItem.setScale(screenSize.height * 0.15f / energyItem.getContentSize().height);
    energyItem.setPosition(screenSize.width * 0.9f, screenSize.height * 0.87f);
    addChild(energyItem, itemPanel.getZOrder() + 1);

    // Add pushes label to track number of pushes
    CCBitmapFontAtlas pushesLabel = CCBitmapFontAtlas.bitmapFontAtlas("000", myFont);
    pushesLabelScale = screenSize.height * 0.07f / pushesLabel.getContentSize().height;
    pushesLabel.setScale(pushesLabelScale);
    pushesLabel.setColor(fontColor);
    pushesLabel.setPosition(screenSize.width * 0.9f, screenSize.height * 0.74f);
    addChild(pushesLabel, itemPanel.getZOrder() + 1, PUSHES_LABEL_TAG);

    // Add Reset Button
    CCSprite revert = CCSprite.sprite(SpritePreferences.B_WIDE_DARK);
    revert.setScale(
        InGameHelper.getPreferredScale(screenSize.width / 5, screenSize.height / 6, revert.getContentSize()));
    revert.setPosition(screenSize.width * 0.9f, screenSize.height / 4.0f);
    addChild(revert, Z1, REVERT_BUTTON_TAG);

    // Add text to button.
    InGameHelper.addTextToSprite(revert, "Revert");

    // Add Revert Button
    CCSprite reset = CCSprite.sprite(SpritePreferences.B_WIDE_DARK);
    reset.setScale(revert.getScale());
    reset.setPosition(screenSize.width * 0.9f, screenSize.height / 12.0f);
    addChild(reset, Z1, RESET_BUTTON_TAG);

    // Add text to button.
    InGameHelper.addTextToSprite(reset, "Reset");
  }

  /**
   * Calculate fundamental  parameters.
   */
  private void calcGameParams() {
    // Create a node to hold all elements.
    tilesNode = CCNode.node();
    tilesNode.setTag(TILE_NODE_TAG);
    addChild(tilesNode);

    initGame(levelNumber);

    /**
     * Available width and height. Should be calculated.
     */
    int usableWidth = (int) (screenSize.width * 0.8f);
    int usableHeight = (int) (screenSize.height);
    tileSquareSize = Math.min((usableHeight / numberOfRows), (usableWidth / numberOfColumns));

    // Position of first tile. Should be top Y.
    startY = (int) (tileSquareSize * numberOfRows - tileSquareSize / 2.0f);
    startY = (int) (startY + (usableHeight - tileSquareSize * numberOfRows) / 2.0f);

    // Position of first tile. Should be left X.
    startX = (int) (tileSquareSize / 2.0f);
    startX = (int) (startX + (usableWidth - tileSquareSize * numberOfColumns) / 2.0f);

    // Decide which resolution should be used for blocks' sprites.
    spritesResolution = tileSquareSize <= 128 ? 128 : 256;
    // set scale for tiles. original size is 256.
    scaleFactor = tileSquareSize / spritesResolution;
    Logger.log("Calculated  parameters. Resolution: " + spritesResolution + "\nScale factor: "
        + scaleFactor
        + "\nRaw tile square size is: " + tileSquareSize);
  }

  /**
   * Update  state, load all tiles sprites.
   */
  private void loadTiles() {
    int tI = 0;
    CCSprite character;
    tilesNode.removeAllChildren(true);

    for (int j = startY; j > startY - (tileSquareSize * numberOfRows); j -= tileSquareSize) {
      for (int i = startX; i < startX + (tileSquareSize * numberOfColumns); i += tileSquareSize) {
        if (tI >= (numberOfRows * numberOfColumns)) {
          break;
        }
        CCSprite tile;
        if (spritesResolution == 256) {
          tile = CCSprite.sprite(SpritePreferences.FLOOR_BLOCK_256);
        } else {
          tile = CCSprite.sprite(SpritePreferences.FLOOR_BLOCK_128);
        }
        // tile = getFloorSprite(true, gameMap, tI);
        CCNodeExt eachNode = new CCNodeExt();
        CCNodeExt heroNode = new CCNodeExt();

        int x = i; // i - j;
        int y = j; // (i + j) / 2;

        // Bottom tile.
        if (gameMap[tI] == SLIDE) {
          if (spritesResolution == 256) {
            tile = CCSprite.sprite(SpritePreferences.ICE_BLOCK_256);
          } else {
            tile = CCSprite.sprite(SpritePreferences.ICE_BLOCK_128);
          }
        } else if (gameMap[tI] == TELEP) {
          // Keep empty block.
        } else if (gameMap[tI] == EMPTY) {
          tile = CCSprite.sprite(SpritePreferences.EMPTY_BLOCK);
        }

        CCNode bottomTile = new CCNodeExt();
        bottomTile.setContentSize(tile.getContentSize());
        bottomTile.addChild(tile, -1, 1);
        bottomTile.setPosition(x, y);
        bottomTile.setScale(scaleFactor);
        tilesNode.addChild(bottomTile, Z2);

        // Hero tile.
        if (heroTable[tI]) {
          character = InGameHelper.getHeroSprite(appContext, spritesResolution);
          heroNode.setContentSize(character.getContentSize());
          heroNode.addChild(character, Z1);
          heroNode.setPosition(x, y);
          heroNode.setScale(scaleFactor);
          tilesNode.addChild(heroNode, Z4, HERO_TAG);
        } else if (skullsTable[tI]) {
          if (spritesResolution == 256) {
            character = CCSprite.sprite(SpritePreferences.WHITE_SKULL_BLOCK_256);
          } else {
            character = CCSprite.sprite(SpritePreferences.WHITE_SKULL_BLOCK_128);
          }
          heroNode.setContentSize(character.getContentSize());
          heroNode.addChild(character, Z1);
          heroNode.setPosition(x, y);
          heroNode.setScale(scaleFactor);
          tilesNode.addChild(heroNode, Z4, FIRST_SKULL_TAG + tI);
        }

        boolean addGas = false;
        // Front tile
        if (gameMap[tI] == TELEP) {
          addChild(ParticleHelper.portalEmitter(x, y, tileSquareSize), Z5);
        } else if (gameMap[tI] == DESTY) {
          if (spritesResolution == 256) {
            tile = CCSprite.sprite(SpritePreferences.DESTINATION_BLOCK_256);
          } else {
            tile = CCSprite.sprite(SpritePreferences.DESTINATION_BLOCK_128);
          }
          eachNode.setContentSize(tile.getContentSize());
          eachNode.addChild(tile, Z3);
          addGas = true;
        } else if (gameMap[tI] == BLOCK) {
          if (spritesResolution == 256) {
            tile = CCSprite.sprite(SpritePreferences.BLOCK_BLOCK_256);
          } else {
            tile = CCSprite.sprite(SpritePreferences.BLOCK_BLOCK_128);
          }
          eachNode.setContentSize(tile.getContentSize());
          eachNode.addChild(tile, Z3);
        }
        eachNode.setPosition(x, y);
        eachNode.setScale(scaleFactor);
        tilesNode.addChild(eachNode, Z3, tI);
        if (addGas) {
          addChild(ParticleHelper.gasEmitter(x, y,
              eachNode.getContentSize().width * eachNode.getScale() / 2.0f), Z5);
        }
        tI++;
      }
    }
  }

  @Override
  public final boolean ccTouchesBegan(final MotionEvent event) {
    startLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    return true;
  }

  @Override
  public final boolean ccTouchesEnded(final MotionEvent event) {
    CGPoint endLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    CCSprite bsReset = (CCSprite) getChildByTag(RESET_BUTTON_TAG);
    CCSprite bsRevert = (CCSprite) getChildByTag(REVERT_BUTTON_TAG);

    if (InGameHelper.detectClick(startLocation, endLocation)) {
      // Buttons in winOverlay.
      if (levelCompleted) {
        CCColorLayer winOverlay = (CCColorLayer) getChildByTag(WIN_OVERLAY_TAG);
        CCSprite bsBack = (CCSprite) winOverlay.getChildByTag(WIN_BACK_BUTTON_TAG);
        CCSprite bsReset2 = (CCSprite) winOverlay.getChildByTag(WIN_RETRY_BUTTON_TAG);
        CCSprite bsNext = (CCSprite) winOverlay.getChildByTag(WIN_NEXT_BUTTON_TAG);
        if (InGameHelper.spriteClicked(bsBack, startLocation, endLocation)) {
          startDifficultySelectionScene();
        } else if (InGameHelper.spriteClicked(bsReset2, startLocation, endLocation)) {
          AchievementHelper.graveMistake();
          resetCallback();
        } else if (InGameHelper.spriteClicked(bsNext, startLocation, endLocation)) {
          startNextLevel();
        }
        // In- buttons.
      } else if (InGameHelper.spriteClicked(bsReset, startLocation, endLocation)) {
        AchievementHelper.graveMistake();
        resetCallback();
      } else if (InGameHelper.spriteClicked(bsRevert, startLocation, endLocation) && !prevMovesList.isEmpty()) {
        AchievementHelper.backInTime();
        revertMove();
      }
    } else if (!levelCompleted) {
      /*
       * Issue #65. Do not animate if one the sprites is still in move. If
       * slide was done very quickly, than skull or hero may be null!
       */
      CCNodeExt skullNode = (CCNodeExt) tilesNode.getChildByTag(skullCurrentTag);
      CCNodeExt heroNode = (CCNodeExt) tilesNode.getChildByTag(HERO_TAG);
      if (heroNode != null) {
        if (heroNode.numberOfRunningActions() == 0) {
          if (skullNode == null) {
            slideHero(this, InGameHelper.detectMove(startLocation, endLocation));
          } else if (skullNode.numberOfRunningActions() == 0) {
            slideHero(this, InGameHelper.detectMove(startLocation, endLocation));
          }
        }
      }
    }
    return true;
  }

  /**
   * Override and handle the event called in MainActivity. It ensures that
   * event is always called in OpenGl thread.
   */
  @Override
  public final boolean ccKeyDown(final int id, final KeyEvent ke) {
    Logger.log("Clicked back button in GameLayer");
    InGameHelper.turnAllSensorsOff(this);
    InGameHelper.popAndReplaceSceneWithTag();
    return true;
  }

  /**
   * Calculate hero move parameters.
   *
   * @param sender Sender callback.
   * @param moveOb String object, should be one of 4 constants.
   */
  public void slideHero(Object sender, Object moveOb) {
    String move = (String) moveOb;
    int portTo = -1;
    int heroToPush = -1;
    /*
     * UP
     */
    if (move.equals(GlobalPreferences.MOVE_UP)) {
      for (int y = 0; y < tabSize; y++) {
        if (heroTable[y]) {
          int moveTo = y;
          for (int f = y - numberOfColumns; f >= 0; f -= numberOfColumns) {
            if (skullsTable[f]) {
              if (gameMap[f + numberOfColumns] == SLIDE && f + numberOfColumns != y) {
              } else {
                heroToPush = f;
                moveTo = f;
              }
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              chillOutSteps++;
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              AchievementHelper.intoxicated();
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          if (moveTo != y || heroToPush > -1) {
            // y->f
            animateHero(y, moveTo, portTo, move, heroToPush);
          }
          break;
        }
      }
    }

    /*
     * DOWN
     */
    if (move.equals(GlobalPreferences.MOVE_DOWN)) {
      for (int y = 0; y < tabSize; y++) {
        if (heroTable[y]) {
          int moveTo = y;
          for (int f = y + numberOfColumns; f < tabSize; f += numberOfColumns) {
            if (skullsTable[f]) {
              if (gameMap[f - numberOfColumns] == SLIDE && f - numberOfColumns != y) {

              } else {
                heroToPush = f;
                moveTo = f;
              }
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              chillOutSteps++;
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              AchievementHelper.intoxicated();
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          if (moveTo != y || heroToPush > -1) {
            // y->f
            animateHero(y, moveTo, portTo, move, heroToPush);
          }
          break;
        }
      }
    }

    /*
     * LEFT
     */
    if (move.equals(GlobalPreferences.MOVE_LEFT)) {
      for (int y = 0; y < tabSize; y++) {
        if (heroTable[y]) {
          int numberOfMoves = y % numberOfColumns;
          int moveTo = y;
          for (int f = y - 1; f >= y - numberOfMoves; f--) {
            if (skullsTable[f]) {
              if (gameMap[f + 1] == SLIDE && f + 1 != y) {

              } else {
                heroToPush = f;
                moveTo = f;
              }
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              chillOutSteps++;
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              AchievementHelper.intoxicated();
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          if (moveTo != y || heroToPush > -1) {
            // y->f
            animateHero(y, moveTo, portTo, move, heroToPush);
          }
          break;
        }
      }
    }

    /*
     * RIGHT
     */
    if (move.equals(GlobalPreferences.MOVE_RIGHT)) {
      for (int y = 0; y < tabSize; y++) {
        if (heroTable[y]) {
          int moveTo = y;
          for (int f = y + 1; f <= (y / numberOfColumns) * numberOfColumns + numberOfColumns - 1; f++) {
            if (skullsTable[f]) {
              if (gameMap[f - 1] == SLIDE && f - 1 != y) {

              } else {
                heroToPush = f;
                moveTo = f;
              }
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              chillOutSteps++;
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              AchievementHelper.intoxicated();
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          if (moveTo != y || heroToPush > -1) {
            // y->f
            animateHero(y, moveTo, portTo, move, heroToPush);
          }
          break;
        }
      }
    }
    if (chillOutSteps >= 45) {
      AchievementHelper.chillOut();
    }
  }

  /**
   * Calculate skull move parameters.
   *
   * @param sender Sender callback.
   * @param moveOb Move direction, should be one of 4 constants.
   */
  public void slideSkull(Object sender, Object moveOb) {
    String move = (String) moveOb;
    int portTo = -1;
    /*
     * UP
     */
    if (move.equals(GlobalPreferences.MOVE_UP)) {
      for (int y = 0; y < tabSize; y++) {
        if (y == skullCurrentPosition) {
          int moveTo = y;
          for (int f = y - numberOfColumns; f >= 0; f -= numberOfColumns) {
            if (skullsTable[f] || heroTable[f]) {
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          // if (moveTo != y)
          // y->f
          animateSkull(y, moveTo, portTo, move);
          break;
        }
      }
    }

    /*
     * DOWN
     */
    if (move.equals(GlobalPreferences.MOVE_DOWN)) {
      for (int y = 0; y < tabSize; y++) {
        if (y == skullCurrentPosition) {
          int moveTo = y;
          for (int f = y + numberOfColumns; f < tabSize; f += numberOfColumns) {
            if (skullsTable[f] || heroTable[f]) {
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          // if (moveTo!=y)
          // y->f
          animateSkull(y, moveTo, portTo, move);
          break;
        }
      }
    }

    /*
     * LEFT
     */
    if (move.equals(GlobalPreferences.MOVE_LEFT)) {
      for (int y = 0; y < tabSize; y++) {
        if (y == skullCurrentPosition) {
          int numberOfMoves = y % numberOfColumns;
          int moveTo = y;
          for (int f = y - 1; f >= y - numberOfMoves; f--) {
            if (skullsTable[f] || heroTable[f]) {
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          // if (moveTo!=y)
          // y->f
          animateSkull(y, moveTo, portTo, move);
          break;
        }
      }
    }

    /*
     * RIGHT
     */
    if (move.equals(GlobalPreferences.MOVE_RIGHT)) {
      for (int y = 0; y < tabSize; y++) {
        if (y == skullCurrentPosition) {
          int moveTo = y;
          for (int f = y + 1; f <= (y / numberOfColumns) * numberOfColumns + numberOfColumns - 1; f++) {
            if (skullsTable[f] || heroTable[f]) {
              break;
            } else if (gameMap[f] == FLOOR) {
              moveTo = f;
              break;
            } else if (gameMap[f] == SLIDE) {
              moveTo = f;
            } else if (gameMap[f] == DESTY) {
              moveTo = f;
              break;
            } else if (gameMap[f] == TELEP) {
              for (Portal t : portalList) {
                if (f == t.getPos()) {
                  moveTo = f;
                  portTo = t.getDestination();
                  break;
                }
              }
              break;
            } else if (gameMap[f] >= BLOCK) {
              break;
            }
          }
          // if (moveTo!=y)
          // y->f
          animateSkull(y, moveTo, portTo, move);
          break;
        }
      }
    }
  }

  /**
   * Move and animate hero.
   *
   * @param from Current hero position (in map).
   * @param to Hero wanted position.
   * @param destination Portal destination. Will not be considered if value is set
   * to -1.
   * @param move Direction of move.
   * @param skullToPush Skull's position in boolean tab. This skull will be moved
   * later. Will not be considered if value is set to -1.
   */
  public void animateHero(int from, int to, int destination, String move, int skullToPush) {
    // Get position of node to which we should move hero.
    CGPoint moveTo = tilesNode.getChildByTag(to).getPosition();

    // Create animation sequence.
    float time = calcTime(from, to);
    CCDelayTime delay = CCDelayTime.action(time);
    CCMoveTo moveTile = CCMoveTo.action(time * moveBoost, moveTo);
    CCCallFunc win = CCCallFuncN.action(this, "winCallback");

    // Create current move MoveBackup and add it to previousMovesList.
    // Hero is always moved first, so don't add it while executing skull
    // move.
    heroPreviousPosition = from;
    heroCurrentPosition = to;

    // First we assume that white hero will not be moved.
    if (!anyMoveBackedUp) {
      prevMovesList.add(new MoveBackup(heroPreviousPosition, -1, -1));
      anyMoveBackedUp = true;
    }

    // Animate.
    CCSequence se;
    // If it was first and the last move (if no skulls were pushed).
    if (destination == -1 && skullToPush == -1) {
      if (!levelCompleted) {
        se = CCSequence.actions(moveTile);
      } else {
        se = CCSequence.actions(moveTile, delay, win);
      }
      tilesNode.getChildByTag(HERO_TAG).runAction(se);
      heroTable[to] = true;
      heroTable[from] = false;
      anyMoveBackedUp = false;
    }
    // If hero entered portal, than start new move for hero.
    else if (destination != -1 && skullToPush == -1) {
      CCCallFuncND newSlide = CCCallFuncND.action(this, "slideHero", move);
      CCDelayTime portalDelay = CCDelayTime.action(0.1f);
      CCScaleTo shrink = CCScaleTo.action(0.05f, 0.01f * scaleFactor);
      CCScaleTo grow = CCScaleTo.action(0.05f, scaleFactor);
      CGPoint newPos = tilesNode.getChildByTag(destination).getPosition();
      CCMoveTo portalMove = CCMoveTo.action(0.1f, newPos);
      se = CCSequence.actions(moveTile, shrink, portalMove, portalDelay, grow, newSlide);
      playPortalSound();
      tilesNode.getChildByTag(HERO_TAG).runAction(se);
      heroTable[from] = false;
      heroTable[to] = false;
      heroTable[destination] = true;
      AchievementHelper.looper();
    }
    // If skull was pushed, than start new move for skull.
    // It is the last move for hero, update map with position and tag of
    // pushed skull.
    else if (destination == -1 && skullToPush != -1) {
      skullCurrentPosition = skullToPush;
      for (Skull skull : skullsList) {
        if (skull.getPosition() == skullCurrentPosition) {
          skullCurrentTag = skull.getSkullTag();
          break;
        }
      }
      prevMovesList.get(prevMovesList.size() - 1).setSkullPosition(skullCurrentPosition);
      prevMovesList.get(prevMovesList.size() - 1).setSkullTag(skullCurrentTag);
      // Remember move sequence for hero.
      heroCurrSeq = CCSequence.actions(moveTile);
      slideSkull(this, move);
    }
  }

  /**
   * Move and animate skull.
   *
   * @param from Current skull position (in map).
   * @param to Skull wanted position.
   * @param portalTo Portal destination. Will not be considered if value is set
   * to -1.
   * @param move Direction of move.
   */
  public void animateSkull(int from, int to, int portalTo, final String move) {
    // Get position of node to which we want to push skull.
    CGPoint moveTo = tilesNode.getChildByTag(to).getPosition();

    // Create animation.
    float time = calcTime(from, to);
    CCDelayTime delay = CCDelayTime.action(time);
    CCMoveTo moveTile = CCMoveTo.action(time * moveBoost, moveTo);
    CCCallFunc win = CCCallFuncN.action(this, "winCallback");
    CCSequence se;

    // Animate HERO.
    // Check if skull should be really moved.
    if (to != from) {
      tilesNode.getChildByTag(HERO_TAG).runAction(heroCurrSeq);
      heroTable[heroPreviousPosition] = false;
      heroTable[heroCurrentPosition] = true;
      // If skull was not moved but hero was moved. Rare bug fix.
    } else if (heroCurrentPosition != heroPreviousPosition) {
      int newMoveDestination = 0;
      boolean shouldMoveHero = false;
      if (move.equals(GlobalPreferences.MOVE_LEFT) && (heroCurrentPosition + 1) != heroPreviousPosition) {
        newMoveDestination = heroCurrentPosition + 1;
        shouldMoveHero = true;
      } else if (move.equals(GlobalPreferences.MOVE_RIGHT) && (heroCurrentPosition - 1) != heroPreviousPosition) {
        newMoveDestination = heroCurrentPosition - 1;
        shouldMoveHero = true;
      } else if (move.equals(GlobalPreferences.MOVE_UP)
          && (heroCurrentPosition + numberOfColumns) != heroPreviousPosition) {
        newMoveDestination = heroCurrentPosition + numberOfColumns;
        shouldMoveHero = true;
      } else if (move.equals(GlobalPreferences.MOVE_DOWN)
          && (heroCurrentPosition - numberOfColumns) != heroPreviousPosition) {
        newMoveDestination = heroCurrentPosition - numberOfColumns;
        shouldMoveHero = true;
      }
      if (shouldMoveHero) {
        CGPoint position = tilesNode.getChildByTag(newMoveDestination).getPosition();
        heroCurrSeq = CCSequence.actions(CCMoveTo.action(time, position));
        tilesNode.getChildByTag(HERO_TAG).runAction(heroCurrSeq);
        heroTable[heroPreviousPosition] = false;
        heroTable[newMoveDestination] = true;
      }
    }

    // Animate skull, this is the last move.
    if (portalTo == -1 && from != to) {
      // Update number of pushes.
      Thread t = new Thread() {
        public void run() {
          updatePushes(1);
        }
      };
      t.start();

      // Update maps.
      anyMoveBackedUp = false;
      skullsTable[from] = false;
      skullsTable[to] = true;
      skullCurrentPosition = to;
      for (Skull skull : skullsList) {
        if (skull.getPosition() == from) {
          skull.setPosition(to);
          break;
        }
      }

      // Check if all skulls collected
      levelCompleted = checkAllSkullsCollected();
      if (!levelCompleted) {
        se = CCSequence.actions(moveTile);
      } else {
        se = CCSequence.actions(moveTile, delay, win);
      }
      tilesNode.getChildByTag(skullCurrentTag).runAction(se);
      playPushSound();
    }
    // Start new move for skull because it entered portal.
    else if (portalTo != -1) {
      SoundEngine.sharedEngine().playEffect(appContext, R.raw.teleport);
      AchievementHelper.looper();

      // Create animation.
      CCCallFuncND newSlide = CCCallFuncND.action(this, "slideSkull", move);
      CCDelayTime tpDelay = CCDelayTime.action(0.1f);
      CCScaleTo shrink = CCScaleTo.action(0.15f, 0.05f * scaleFactor);
      CCScaleTo grow = CCScaleTo.action(0.15f, 1f * scaleFactor);
      CGPoint newPos = tilesNode.getChildByTag(portalTo).getPosition();
      CCMoveTo portal = CCMoveTo.action(0.1f, newPos);
      se = CCSequence.actions(moveTile, shrink, portal, tpDelay, grow, newSlide);

      tilesNode.getChildByTag(skullCurrentTag).runAction(se);

      // Update maps.
      skullsTable[from] = false;
      skullsTable[to] = false;
      skullsTable[portalTo] = true;
      skullCurrentPosition = portalTo;
      for (Skull skull : skullsList) {
        if (skull.getPosition() == from) {
          skull.setPosition(portalTo);
          break;
        }
      }
    }
  }

  /**
   * Calculate time per move. each tile is 0.05f seconds.
   *
   * @param from Staring position.
   * @param to Destination position.
   * @return Calculated move time.
   */
  public final float calcTime(final int from, final int to) {
    float time;
    int length;
    int abs = Math.abs(from - to);
    if (abs < numberOfColumns) {
      length = abs;
    } else {
      length = abs / numberOfColumns;
    }
    length++;
    time = length * 0.05f;
    return time;
  }

  /**
   * Update number of pushes. Should be 1 or -1.
   *
   * @param toAdd Number of pushes to add, can be negative.
   */
  public final void updatePushes(final int toAdd) {
    pushes += toAdd;
    // Negative number of pushes bug.
    if (pushes < 0) {
      pushes = 0;
      return;
    }
    // Animate pushes
    CCBitmapFontAtlas pushesLabel = (CCBitmapFontAtlas) getChildByTag(PUSHES_LABEL_TAG);
    pushesLabel.runAction(CCSequence.actions(CCScaleTo.action(0.2f, pushesLabelScale * 1.5f),
        CCScaleTo.action(0.2f, pushesLabelScale)));
    pushesLabel.setString(CCFormatter.format("%03d", pushes));
  }

  /**
   * Check if all heroes have been collected. Also check and update heroLabel
   *
   * @return True if all skulls collected, false if not.
   */
  public final boolean checkAllSkullsCollected() {
    int i = 0;
    for (Skull skull : skullsList) {
      if (gameMap[skull.getPosition()] == DESTY) {
        i++;
      }
    }
    if (i != numSkullsCollected) {
      numSkullsCollected = i;
      updateSkulls(numSkullsCollected);
    }
    return i == skullsToCollect;
  }

  /**
   * Update number of skulls.
   */
  public final void updateSkulls(final int n) {
    CCBitmapFontAtlas skullsLabel = (CCBitmapFontAtlas) getChildByTag(SKULLS_LABEL_TAG);
    skullsLabel.runAction(CCSequence.actions(CCScaleTo.action(0.2f, skullsLabelScale * 1.5f),
        CCScaleTo.action(0.2f, skullsLabelScale)));
    skullsLabel.setString(n + "/" + skullsToCollect);
  }

  /**
   * Calculate number of diamonds awarded.
   *
   * @return Number of diamonds.
   */
  public final int getDiamonds() {
    Random r = new Random();
    int d = r.nextInt(100) + 1;
    if (d <= 4) {
      return 5;
    } else if (d <= 12) {
      return 4;
    } else if (d <= 24) {
      return 3;
    } else if (d <= 50) {
      return 2;
    }
    return 1;
  }

  /**
   * Initiate table. Each time new table.
   */
  public final void initGame(final int levelId) {
    LevelParser levelParser = new LevelParser(levelId);
    // Initialize  parameters.
    numberOfColumns = levelParser.getNumberOfColumns();
    numberOfRows = levelParser.getNumberOfRows();
    tabSize = numberOfColumns * numberOfRows;
    minimumNumberOfPushes = levelParser.getMinimumPushes();
    skullsToCollect = levelParser.getNumberOfSkullsToCollect();
    // Get all  tables.
    gameMap = levelParser.getGameBoard();
    skullsTable = levelParser.getSkullsTable();
    skullsList = levelParser.getSkullsList();
    portalList = levelParser.getPortalsList();
    // Initialize hero table and set first position to true.
    heroTable = new boolean[tabSize + 1];
    heroTable[levelParser.getHeroFirstPosition()] = true;
    // Initialize previous moves list.
    prevMovesList = new ArrayList<>();
  }

  /**
   * Start win callback. Should be executed when level is finished.
   *
   * @param sender Sender callback.
   */
  public final void winCallback(final Object sender) {
    // Create a dark semi-transparent layer.
    CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 255));
    pauseOverlay.setOpacity(200);
    pauseOverlay.setPosition(0f, 0f);
    pauseOverlay.setAnchorPoint(0f, 0f);
    pauseOverlay.setIsTouchEnabled(true);
    addChild(pauseOverlay, 200, WIN_OVERLAY_TAG);

    // Summary panel
    CCSprite summaryPanel = CCSprite.sprite(SpritePreferences.SUMMARY_PANEL);
    summaryPanel.setAnchorPoint(0.5f, 0f);
    summaryPanel.setScale(InGameHelper.getPreferredScale(screenSize.width * 0.9f, screenSize.height * 0.9f,
        summaryPanel.getContentSize()));
    summaryPanel.setPosition(screenSize.width / 2.0f, 0);
    pauseOverlay.addChild(summaryPanel);

    float panelW = summaryPanel.getContentSize().width * summaryPanel.getScale();
    float panelH = summaryPanel.getContentSize().height * summaryPanel.getScale();
    float panelX = (screenSize.width - panelW) / 2.0f;

    // Completed.
    CCBitmapFontAtlas completed = CCBitmapFontAtlas.bitmapFontAtlas("Level " + levelNumber + " complete", myFont);
    completed.setScale(panelH * 0.1f / completed.getContentSize().height);
    completed.setPosition(panelX + panelW / 2.0f, panelH * 0.83f);
    pauseOverlay.addChild(completed);

    // Pushes.
    CCBitmapFontAtlas pushesTitle = CCBitmapFontAtlas.bitmapFontAtlas("Pushes", myFont);
    pushesTitle.setScale(panelH * 0.1f / pushesTitle.getContentSize().height);
    pushesTitle.setPosition(panelX + panelW * 0.22f, panelH * 0.43f);
    pauseOverlay.addChild(pushesTitle);

    CCBitmapFontAtlas pushesLabel = CCBitmapFontAtlas.bitmapFontAtlas(pushes + "/" + minimumNumberOfPushes, myFont);
    pushesLabel.setScale(panelH * 0.09f / pushesLabel.getContentSize().height);
    pushesLabel.setPosition(panelX + panelW * 0.6f, panelH * 0.43f);
    pauseOverlay.addChild(pushesLabel);

    // Reverts.
    CCBitmapFontAtlas revertsTitle = CCBitmapFontAtlas.bitmapFontAtlas("Reverts", myFont);
    revertsTitle.setScale(panelH * 0.1f / revertsTitle.getContentSize().height);
    revertsTitle.setPosition(panelX + panelW * 0.22f, panelH * 0.27f);
    pauseOverlay.addChild(revertsTitle);

    CCBitmapFontAtlas revertsLabel = CCBitmapFontAtlas.bitmapFontAtlas("" + revertsNumber, myFont);
    revertsLabel.setScale(panelH * 0.09f / revertsLabel.getContentSize().height);
    revertsLabel.setPosition(panelX + panelW * 0.6f, panelH * 0.27f);
    pauseOverlay.addChild(revertsLabel);

    // Show skulls.
    float skullY = panelH * 0.64f;
    float panelSpacing = panelW * 0.28f;
    float panelMid = panelX + panelW / 2.0f;

    String path = SpritePreferences.I_SKULL_256;
    if (panelW * 0.23f <= 128) {
      path = SpritePreferences.I_SKULL_128;
    }

    CCSprite goldenSkull2 = null, goldenSkull3 = null;
    CCSequence se1, se2 = null, se3 = null;
    CCSprite goldenSkull1 = CCSprite.sprite(path);
    goldenSkull1.setPosition(panelMid - panelSpacing, skullY);
    float skullS = panelW * 0.27f / goldenSkull1.getContentSize().width;
    float skullIntroS = skullS * 0.001f;
    goldenSkull1.setScale(skullIntroS);
    pauseOverlay.addChild(goldenSkull1, Z5);
    se1 = InGameHelper.getSpriteIntroEffect(goldenSkull1, skullS);

    int awards = 1;
    if (pushes <= minimumNumberOfPushes && revertsNumber <= 0) {
      goldenSkull2 = CCSprite.sprite(path);
      goldenSkull2.setScale(skullIntroS);
      goldenSkull2.setPosition(panelMid, skullY);
      pauseOverlay.addChild(goldenSkull2, Z5);
      goldenSkull3 = CCSprite.sprite(path);
      goldenSkull3.setScale(skullIntroS);
      goldenSkull3.setPosition(panelMid + panelSpacing, skullY);
      pauseOverlay.addChild(goldenSkull3, Z5);
      se2 = InGameHelper.getSpriteIntroEffect(goldenSkull2, skullS);
      se3 = InGameHelper.getSpriteIntroEffect(goldenSkull3, skullS);
      awards = 3;
    } else if (pushes <= minimumNumberOfPushes || revertsNumber <= 0) {
      goldenSkull2 = CCSprite.sprite(path);
      goldenSkull2.setScale(skullIntroS);
      goldenSkull2.setPosition(panelMid, skullY);
      pauseOverlay.addChild(goldenSkull2, Z5);
      se2 = InGameHelper.getSpriteIntroEffect(goldenSkull2, skullS);
      awards = 2;
    }

    // Update  preferences.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
    // Unlock new level if necessary.
    prefs.edit().putBoolean(SharedPreferencesKeys.LEVEL_UNLOCKED_INFO_KEY + (levelNumber + 1), true).apply();
    // Update level awards.
    int awardsInfo = prefs.getInt(SharedPreferencesKeys.LEVEL_INFO_KEY + levelNumber, 0);
    if (awardsInfo < awards) {
      prefs.edit().putInt(SharedPreferencesKeys.LEVEL_INFO_KEY + levelNumber, awards).apply();
    }
    // Add diamonds if level was completed for the first time.
    if (awardsInfo == 0) {
      // Show diamonds awarded
      int diamonds = getDiamonds();
      CCBitmapFontAtlas d = CCBitmapFontAtlas.bitmapFontAtlas("+ " + diamonds, GlobalPreferences.FONT);
      d.setAnchorPoint(1f, 0.5f);
      d.setScale((screenSize.height - panelH) / d.getContentSize().height);
      d.setPosition(screenSize.width / 2.0f - 10 * generalScaleFactor, screenSize.height * 0.93f);
      pauseOverlay.addChild(d, 300);

      CCSprite diamond = CCSprite.sprite(SpritePreferences.I_DIAMOND);
      diamond.setAnchorPoint(0f, 0.5f);
      diamond.setScale((screenSize.height - panelH) / diamond.getContentSize().height);
      diamond.setPosition(screenSize.width / 2.0f + 10 * generalScaleFactor, screenSize.height * 0.93f);
      pauseOverlay.addChild(diamond, 300);

      diamonds += prefs.getInt(SharedPreferencesKeys.TOTAL_DIAMONDS_COLLECTED, 0);
      prefs.edit().putInt(SharedPreferencesKeys.TOTAL_DIAMONDS_COLLECTED, diamonds).apply();
      AchievementHelper.mrScrooge(diamonds);
    }

    // Buttons.
    float posY = panelH * 0.09f;
    float scaleX = panelH * 0.17f;
    // Exit button.
    CCSprite back = CCSprite.sprite(SpritePreferences.B_MENU);
    back.setScale(scaleX / back.getContentSize().height);
    back.setPosition(panelMid - panelSpacing, posY);
    pauseOverlay.addChild(back, 301, WIN_BACK_BUTTON_TAG);

    // Reset button.
    CCSprite retry = CCSprite.sprite(SpritePreferences.B_RESET);
    retry.setScale(scaleX / retry.getContentSize().height);
    retry.setPosition(panelMid, posY);
    pauseOverlay.addChild(retry, 301, WIN_RETRY_BUTTON_TAG);

    // Next button.
    CCSprite next = CCSprite.sprite(SpritePreferences.B_NEXT);
    next.setScale(scaleX / next.getContentSize().height);
    next.setPosition(panelMid + panelSpacing, posY);
    pauseOverlay.addChild(next, 301, WIN_NEXT_BUTTON_TAG);

    // Animate skulls.
    CCDelayTime delay = CCDelayTime.action(0.2f);
    CCCallFunc skull1 = CCCallFuncND.action(this, "animateSkullsWin",
        new SpriteAnimationBinder(se1, goldenSkull1));
    CCSequence seq;
    if (se2 == null) {
      seq = CCSequence.actions(skull1);
    } else {
      CCCallFunc skull2 = CCCallFuncND.action(this, "animateSkullsWin",
          new SpriteAnimationBinder(se2, goldenSkull2));
      if (se3 == null) {
        seq = CCSequence.actions(skull1, delay, skull2);
      } else {
        CCCallFunc skull3 = CCCallFuncND.action(this, "animateSkullsWin",
            new SpriteAnimationBinder(se3, goldenSkull3));
        seq = CCSequence.actions(skull1, delay, skull2, delay, skull3);
      }
    }
    pauseOverlay.runAction(seq);
    updateAchievements(levelNumber);
  }

  /**
   * Update achievements accessible from GameLayer.
   *
   * @param level Current level.
   */
  private void updateAchievements(final int level) {
    Logger.log("Level: " + levelNumber);
    if (levelNumber == 5) {
      AchievementHelper.gettingStarted();
    }
    if (levelNumber == 30) {
      AchievementHelper.beginner();
    }
    if (levelNumber == 55) {
      AchievementHelper.icelyDone();
    }
    if (levelNumber == 42) {
      AchievementHelper.marathon();
    }
    if (levelNumber == 45) {
      AchievementHelper.advanced();
    }
    if (levelNumber == 60) {
      AchievementHelper.theMaze();
    }
    if (levelNumber == 60) {
      AchievementHelper.expert();
    }
  }

  /**
   * Function only for purpose of animating skulls. Allows to arrange
   * animations in a sequence. Has to be PUBLIC.
   *
   * @param sender Sender function.
   * @param spriteAnimationWrapper Object containing information about sprite to animate and
   * sequence itself.
   */
  public final void animateSkullsWin(final Object sender, final Object spriteAnimationWrapper) {
    if (spriteAnimationWrapper instanceof SpriteAnimationBinder) {
      SpriteAnimationBinder saw = (SpriteAnimationBinder) spriteAnimationWrapper;
      saw.getSprite().runAction(saw.getSequence());
    }
  }

  /**
   * Start next level, check if it is possible (if more levels exist).
   */
  private void startNextLevel() {
    InGameHelper.turnAllSensorsOff(this);
    if (levelNumber < GlobalPreferences.TOTAL_NUMBER_OF_LEVELS) {
      InGameHelper.turnAllSensorsOff(this);
      CCScene scene = GameLayer.scene(levelNumber + 1);
      scene.setTag(GlobalPreferences.GAME_LAYER_TAG);
      CCFadeTransition fade = CCFadeTransition.transition(1f, scene);
      CCDirector.sharedDirector().replaceScene(fade);
    } else {
      startDifficultySelectionScene();
    }
  }

  /**
   * Reset scene.
   */
  private void resetCallback() {
    InGameHelper.turnAllSensorsOff(this);
    CCScene scene = GameLayer.scene(levelNumber);
    scene.setTag(GlobalPreferences.GAME_LAYER_TAG);
    CCFadeTransition fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }

  /**
   * Start level selection scene.
   */
  private void startDifficultySelectionScene() {
    InGameHelper.turnAllSensorsOff(this);
    CCScene scene = DifficultyLayer.scene();
    scene.setTag(GlobalPreferences.DIFFICULTY_LAYER_TAG);
    CCFadeTransition fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }

  /**
   * Preload sounds used in this layer.
   */
  private void preloadSoundEffects() {
    if (DevicePreferences.SOUND_MODE % 2 == 1) {
      SoundEngine.sharedEngine().preloadEffect(appContext, R.raw.teleport);
      SoundEngine.sharedEngine().preloadEffect(appContext, R.raw.pushsound);
    }
  }

  /**
   * Play portal sound.
   *
   * @return True if effect was played.
   */
  private void playPortalSound() {
    if (DevicePreferences.SOUND_MODE % 2 == 0) {
      return;
    }
    SoundEngine.sharedEngine().playEffect(appContext, R.raw.teleport);
  }

  /**
   * Play skull pushing sound.
   *
   * @return True if effect was played.
   */
  private boolean playPushSound() {
    if (DevicePreferences.SOUND_MODE % 2 == 0) {
      return false;
    }
    SoundEngine.sharedEngine().playEffect(appContext, R.raw.pushsound);
    return true;
  }

  /**
   * Revert hero and skull.
   */
  private void revertMove() {
    revertsNumber++;
    // Revert HERO.
    // Set default values.
    int heroCurrPos = 1;
    int heroPrevPos = prevMovesList.get(prevMovesList.size() - 1).getHeroPosition();
    for (int x = 0; x < numberOfColumns * numberOfRows; x++) {
      if (heroTable[x]) {
        heroCurrPos = x;
        break;
      }
    }
    float time = calcTime(heroCurrPos, heroPrevPos);
    CGPoint heroCGPoint = tilesNode.getChildByTag(heroPrevPos).getPosition();
    CCMoveTo move = CCMoveTo.action(time, heroCGPoint);
    CCNodeExt heroNode = (CCNodeExt) tilesNode.getChildByTag(HERO_TAG);
    heroNode.runAction(move);
    heroTable[heroCurrPos] = false;
    heroTable[heroPrevPos] = true;

    // Revert SKULL.
    int p = prevMovesList.get(prevMovesList.size() - 1).getSkullPosition();
    int t = prevMovesList.get(prevMovesList.size() - 1).getSkullTag();
    // Check if skull was actually moved.
    if (t != -1 && p != -1) {
      updatePushes(-1);

      // Animate.
      CGPoint skullPrevPos = tilesNode.getChildByTag(p).getPosition();
      move = CCMoveTo.action(time, skullPrevPos);
      tilesNode.getChildByTag(t).runAction(move);

      // Fix positions.
      int skullCurrPos = 0;
      for (Skull skull : skullsList) {
        if (skull.getSkullTag() == t) {
          skullCurrPos = skull.getPosition();
          break;
        }
      }
      skullsTable[skullCurrPos] = false;
      skullsTable[p] = true;
      for (Skull skull : skullsList) {
        if (skull.getPosition() == skullCurrPos) {
          skull.setPosition(p);
          break;
        }
      }
    }
    prevMovesList.remove(prevMovesList.size() - 1);
    checkAllSkullsCollected();
  }

  /**
   * Choose correct tile for floor.
   */
  private CCSprite getFloorSprite(boolean largeMode, int[] map, int currentPos) {
    int leftPos = currentPos - 1;
    int rightPos = currentPos + 1;
    int topPos = currentPos - numberOfColumns;
    int bottomPos = currentPos + numberOfColumns;
    StringBuilder mode = new StringBuilder("Blocks/");
    if (currentPos > numberOfColumns && currentPos < (numberOfRows * numberOfColumns - numberOfColumns)) {
      if (map[leftPos] == BLOCK && ((currentPos - 1) % numberOfColumns != 0)) {
        mode.append("L");
      }
      if (map[topPos] == BLOCK) {
        mode.append("T");
      }
      if (map[rightPos] == BLOCK && ((currentPos + 2) % numberOfColumns != 0)) {
        mode.append("R");
      }
      if (map[bottomPos] == BLOCK) {
        mode.append("B");
      }
    }
    if (mode.toString().equals("Blocks/")) {
      mode.append("floor_128");
    }
    mode.append(".png");
    Logger.log(mode.toString());
    return CCSprite.sprite(mode.toString());
  }
}