package com.game.layers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.game.helpers.InGameHelper;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.game.preferences.SharedPreferencesKeys;
import com.game.preferences.SpritePreferences;
import org.cocos2d.actions.instant.CCCallFunc;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.extensions.scroll.CCClipNode;
import org.cocos2d.extensions.scroll.CCScrollView;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.transitions.CCFadeTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;

/**
 * Class for managing and displaying all available levels.
 *
 * @author niewinskip
 */
public class LevelMenuLayer extends CCLayer {

  /**
   * Z order.
   */
  private static final int Z0 = 0, Z1 = 1, Z2 = 2, Z3 = 3, Z4 = 4;
  /**
   * Tag for scroll view.
   */
  private static final int SCROLLVIEW_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag for indicators. Reserve 100 next tags for future indicators.
   */
  private static final int INDICATORS_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag for cancel button.
   */
  private static final int CANCEL_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Next item button.
   */
  private static final int NEXT_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Previous item button.
   */
  private static final int PREVIOUS_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Font path.
   */
  private static final String FONT = GlobalPreferences.FONT;
  /**
   * Font color.
   */
  private static final ccColor3B FONT_COLOR = ccColor3B.ccWHITE;
  /**
   * Time after which indicators will be refreshed/updated.
   */
  private static final float INDICATORS_REFRESH_DELAY = 0.25f;
  /**
   * Application context.
   */
  private static Context appContext;
  /**
   * Chosen difficulty.
   */
  private final String difficulty;
  /**
   * Global device scaling factor.
   */
  private final float generalScaleFactor = DevicePreferences.generalScaleFactor;
  /**
   * Screen size.
   */
  private final CGSize screenSize = DevicePreferences.screenSize;
  /**
   * Center of the screen's width.
   */
  private final float halfW = screenSize.width / 2.0f;
  /**
   * Center of the screen's height.
   */
  private final float halfH = screenSize.height / 2.0f;
  /**
   * Scroll view.
   */
  private CCScrollView scrollView;
  /**
   * Finger first and last location.
   */
  private CGPoint startLocation;
  /**
   * Scale for all tiles.
   */
  private float tileScale = 0.0f;
  private float panelHeight, panelTopY;
  /**
   * Total number of pages to display in scrollView.
   */
  private int numberOfPages;

  /**
   * First level displayed.
   */
  private int levelToStartFrom;

  /**
   * Last level displayed.
   */
  private int levelToEndWith;

  /**
   * Constructor.
   *
   * @param difficulty Chosen difficulty level.
   */
  public LevelMenuLayer(final String difficulty) {
    appContext = CCDirector.sharedDirector().getActivity();
    this.difficulty = difficulty;
    GlobalPreferences.CURRENT_LEVEL_DIFFICULTY = difficulty;
    makeGUI();
    InGameHelper.turnAllSensorsOn(this);
  }

  /**
   * Start Level Menu Layer.
   *
   * @return scene.
   */
  public static CCScene scene(final String difficulty) {
    CCScene scene = CCScene.node();
    CCLayer layer = new LevelMenuLayer(difficulty);
    scene.addChild(layer);
    return scene;
  }

  /**
   * Prepare GUI.
   */
  private void makeGUI() {
    // Load sprite for background.
    // numOfLevels HAS to be the last TAGc
    CCSprite bg = InGameHelper.getBackground(screenSize);
    // wtf
    addChild(bg, Z0, GlobalPreferences.TOTAL_NUMBER_OF_LEVELS);

    // Load sprite for panel.
    CCSprite panel = InGameHelper.getMainPanel(SpritePreferences.LEVEL_PANEL, screenSize);
    addChild(panel, Z1);

    // Create title
    CCBitmapFontAtlas panelText = InGameHelper.getPanelTitle(panel, getDifficulty(difficulty));
    addChild(panelText, Z2);

    // Cancel button.
    CCSprite cancel = CCSprite.sprite(SpritePreferences.B_CANCEL);
    cancel.setScale(screenSize.width * 0.12f / cancel.getContentSize().width);
    cancel.setPosition(halfW + panel.getBoundingBox().size.width / 2.0f,
        halfH + panel.getBoundingBox().size.height * 0.37f);
    addChild(cancel, Z2, CANCEL_BUTTON_TAG);

    // Next button.
    CCSprite next = CCSprite.sprite(SpritePreferences.B_NEXT);
    next.setScale(screenSize.width * 0.13f / next.getContentSize().width);
    next.setPosition(halfW + panel.getBoundingBox().size.width / 2.0f, halfH);
    addChild(next, Z4, NEXT_BUTTON_TAG);

    // Previous button.
    CCSprite previous = CCSprite.sprite(SpritePreferences.B_PREVIOUS);
    previous.setScale(screenSize.width * 0.13f / previous.getContentSize().width);
    previous.setPosition(halfW - panel.getBoundingBox().size.width / 2.0f, halfH);
    addChild(previous, Z4, PREVIOUS_BUTTON_TAG);

    scrollView = CCScrollView.view(CGSize.zero());
    scrollView.bounces = true;
    scrollView.setClipToBounds(true);
    scrollView.direction = 1; // for horizontal scrolling.
    scrollView.setPagingEnabled(true);
    addChild(scrollView, Z2, SCROLLVIEW_TAG);

    int numRows = GlobalPreferences.LEVELS_PER_COLUMN;
    int numColumns = GlobalPreferences.LEVELS_PER_ROW;

    float margin = 10 * generalScaleFactor;
    float panelWidth = panel.getContentSize().width * panel.getScale();
    panelHeight = panel.getContentSize().height * panel.getScale();
    float panelLeftX = (screenSize.width - panelWidth) / 2.0f;
    panelTopY = screenSize.height - (screenSize.height - panelHeight) / 2.0f;

    int usableWidth = (int) (panelWidth * 0.8f - margin * (numColumns - 1));
    int usableHeight = (int) (panelHeight * 0.6f - margin * (numRows - 1));
    /**
     * Tile's side size.
     */
    float tileSquareSize = Math.min((usableHeight / numRows), (usableWidth / numColumns));
    int startLeft = (int) (panelLeftX + panelWidth * 0.1f + margin / 2.0f);
    int startTop = (int) (panelTopY - panelHeight * 0.2f - tileSquareSize);

    tileScale = tileSquareSize / 256.0f;

    // Load number of unlocked levels.
    // Display sprite for each level.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

    levelToStartFrom = 0;
    levelToEndWith = GlobalPreferences.NUMBER_OF_EASY_LEVELS_CREATED;
    if (difficulty.equals(GlobalPreferences.LEVEL_MEDIUM)) {
      levelToStartFrom = GlobalPreferences.NUMBER_OF_EASY_LEVELS_CREATED;
      levelToEndWith = levelToStartFrom + GlobalPreferences.NUMBER_OF_MEDIUM_LEVELS_CREATED;
    } else if (difficulty.equals(GlobalPreferences.LEVEL_HARD)) {
      levelToStartFrom = GlobalPreferences.NUMBER_OF_EASY_LEVELS_CREATED
          + GlobalPreferences.NUMBER_OF_MEDIUM_LEVELS_CREATED;
      levelToEndWith = levelToStartFrom + GlobalPreferences.NUMBER_OF_HARD_LEVELS_CREATED;
    }
    numberOfPages = (levelToEndWith - levelToStartFrom) / GlobalPreferences.LEVELS_PER_PAGE;
    Logger.log("Level to start from: " + levelToStartFrom + ". LevelToEndWith: " + levelToEndWith
        + " NumberOfPages: " + numberOfPages);

    int totalSkulls = 0;
    int tI = 0;
    for (int p = 1; p <= numberOfPages; p++) {
      int topX = (int) (startLeft + (p - 1) * screenSize.width);
      for (int j = startTop; j > startTop - (tileSquareSize * numRows); j -= (tileSquareSize + margin)) {
        for (int i = topX; i < topX + (tileSquareSize * numColumns); i += tileSquareSize + margin) {
          if (tI - ((p - 1) * GlobalPreferences.LEVELS_PER_PAGE) >= GlobalPreferences.LEVELS_PER_PAGE) {
            break;
          }
          // Start with 1, not 0.
          int realLevelNumber = tI + 1 + levelToStartFrom;
//					Logger.log("Real lvl number: " + realLevelNumber);
          if (realLevelNumber > levelToEndWith) {
            Logger.log("Real lvl number: " + realLevelNumber);
            break;
          }
          // Assume that level is locked.
          CCSprite tileBox = CCSprite.sprite(SpritePreferences.LEVEL_LOCKED);
          tileBox.setPosition(i, j);
          tileBox.setScale(tileScale);
          // Display level number.
          CCBitmapFontAtlas tileNumber = CCBitmapFontAtlas.bitmapFontAtlas("00", FONT);
          tileNumber.setString((tI + 1) + "");
          tileNumber.setScale(1.5f);
          tileNumber.setColor(FONT_COLOR);
          tileNumber.setAnchorPoint(0.5f, 0f);
          tileNumber.setPosition(tileNumber.getPosition().x + tileBox.getContentSize().width / 2,
              tileNumber.getPosition().y + tileBox.getContentSize().height / 2);

          // Get number of awards for a given level and display it.
          int awards = prefs.getInt(SharedPreferencesKeys.LEVEL_INFO_KEY + realLevelNumber, 0);
          totalSkulls += awards;

          boolean unlocked = isLevelUnlocked(realLevelNumber, prefs);
          if (unlocked) {
            String imgPath;
            switch (awards) {
              case 0:
                imgPath = SpritePreferences.AWARDS_0;
                break;
              case 1:
                imgPath = SpritePreferences.AWARDS_1;
                break;
              case 2:
                imgPath = SpritePreferences.AWARDS_2;
                break;
              case 3:
                imgPath = SpritePreferences.AWARDS_3;
                break;
              default:
                imgPath = SpritePreferences.AWARDS_0;
                break;
            }
            tileBox.setTexture(CCTextureCache.sharedTextureCache().addImage(imgPath));
            tileBox.addChild(tileNumber, Z2);
          }
          scrollView.addChild(tileBox, Z2, tI);
          tI++;
        }
      }
    }

    // Display total number of awards (skulls) collected.
    float height = screenSize.height * 0.1f;
    float posY = screenSize.height * 0.095f;
    CCSprite overlay;
    // 64, because h = 64 and w = 128
    if (height <= 64) {
      overlay = CCSprite.sprite(SpritePreferences.TEXT_PANEL_128);
    } else {
      overlay = CCSprite.sprite(SpritePreferences.TEXT_PANEL_256);
    }
    overlay.setScale(1.3f * height / overlay.getContentSize().height);
    overlay.setPosition(halfW, posY);
    addChild(overlay, Z2);

    CCSprite skull;
    if (height > 128) {
      skull = CCSprite.sprite(SpritePreferences.I_SKULL_256);
    } else {
      skull = CCSprite.sprite(SpritePreferences.I_SKULL_128);
    }
    skull.setAnchorPoint(0f, 0.5f);
    skull.setScale(0.78f * height / skull.getContentSize().height);
    skull.setPosition(halfW + margin / 2.0f, posY);
    addChild(skull, Z3);

    CCBitmapFontAtlas numberOfSkulls = CCBitmapFontAtlas.bitmapFontAtlas(totalSkulls + "", FONT);
    numberOfSkulls.setAnchorPoint(1f, 0.5f);
    numberOfSkulls.setScale(0.78f * height / numberOfSkulls.getContentSize().height);
    numberOfSkulls.setPosition(halfW - margin / 2.0f, posY);
    addChild(numberOfSkulls, Z3);

    // Load and display level page indicators.
    loadIndicators();

    // Initialize scroll view.
    scrollView.setViewSize(CGSize.make(screenSize.width, screenSize.height));
    scrollView.setContentSize(CGSize.make(((numberOfPages) * screenSize.width), screenSize.height));
  }

  /**
   * Get position of first finger touch.
   */
  @Override
  public final boolean ccTouchesBegan(final MotionEvent event) {
    startLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    return true;
  }

  /**
   * Get position of last finger touch. Detect which button was clicked and
   * start appropriate level scene.
   */
  @Override
  public final boolean ccTouchesEnded(final MotionEvent event) {
    CGPoint endLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    boolean updateIndicators = false;
    CCScrollView tilesNode = (CCScrollView) getChildByTag(SCROLLVIEW_TAG);

    if (InGameHelper.detectClick(startLocation, endLocation)) {
      CCSprite cancel = (CCSprite) getChildByTag(CANCEL_BUTTON_TAG);
      CCSprite next = (CCSprite) getChildByTag(NEXT_BUTTON_TAG);
      CCSprite previous = (CCSprite) getChildByTag(PREVIOUS_BUTTON_TAG);
      if (InGameHelper.spriteClicked(cancel, startLocation, endLocation)) {
        InGameHelper.turnAllSensorsOff(this);
        InGameHelper.popAndReplaceSceneWithTag();
      } else if (InGameHelper.spriteClicked(next, startLocation, endLocation)) {
        updateIndicators = true;
        scrollView.scrollToNextPage(screenSize.width, numberOfPages);
      } else if (InGameHelper.spriteClicked(previous, startLocation, endLocation)) {
        updateIndicators = true;
        scrollView.scrollToPreviousPage(screenSize.width);
      } else {
        // Get container of all children (levels).
        CCClipNode container = (CCClipNode) tilesNode.getChildren().get(0);
        int cPage = scrollView.getCurrentPageNum();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        for (int i = 0; i < ((container.getChildren().size())); i++) {
          int realLevelNumber = i + 1 + levelToStartFrom;
          if (i == levelToEndWith || !isLevelUnlocked(realLevelNumber, prefs)
              || realLevelNumber > GlobalPreferences.TOTAL_NUMBER_OF_LEVELS) {
            break;
          }
          if (i >= (cPage * GlobalPreferences.LEVELS_PER_PAGE)
              && i < ((cPage + 1) * GlobalPreferences.LEVELS_PER_PAGE)) {
            // Check if clicked location was within level
            // sprite.
            CCSprite backSprite = (CCSprite) container.getChildByTag(i);
            CGRect spritePos = CGRect.make(backSprite.getPosition().x, backSprite.getPosition().y,
                backSprite.getContentSize().width * tileScale,
                backSprite.getContentSize().height * tileScale);
            if (spritePos.contains(endLocation.x + cPage * screenSize.width, endLocation.y)) {
              backSprite.runAction(InGameHelper.getClickEffect(backSprite));
              startNewLevel(realLevelNumber);
            }
          }
        }
      }
    } else {
      // Finger swipe detected.
      updateIndicators = true;
    }

    // Schedule indicators update. Function should be lunched after some
    // delay. Time should be equal to 2x bounce duration. Possible bugs.
    if (updateIndicators) {
      Logger.log("Scheduling indicators update");
      CCCallFunc lunch = CCCallFunc.action(this, "updateIndicators");
      CCSequence se = CCSequence.actions(CCDelayTime.action(INDICATORS_REFRESH_DELAY), lunch);
      this.runAction(se);
    }
    return true;
  }

  /**
   * Override and handle the event called in MainActivity. It ensures that
   * event is always called in OpenGl thread.
   */
  @Override
  public final boolean ccKeyDown(final int id, final KeyEvent ke) {
    Logger.log("Clicked back button in LevelMenuLayer");
    InGameHelper.turnAllSensorsOff(this);
    InGameHelper.popAndReplaceSceneWithTag();
    return true;
  }

  /**
   * Load indicators for level pages. Each indicator should represent one
   * page.
   */
  public final void loadIndicators() {
    CCSprite indicator = CCSprite.sprite(SpritePreferences.INDICATOR_OFF);
    indicator.setScale(panelHeight * 0.05f / indicator.getContentSize().height);
    float indicatorWidth = indicator.getContentSize().width * indicator.getScale();
    float margin = indicatorWidth / 4;
    int numOfIndicators = numberOfPages;
    float posy = panelTopY - panelHeight * 0.81f;
    float posX = screenSize.width / 2.0f + indicatorWidth / 2.0f;
    if (numOfIndicators % 2 == 0) {
      posX -= ((numOfIndicators / 2) * indicatorWidth);
      posX -= (((float) (numOfIndicators - 1) / 2) * margin);
    } else {
      posX = screenSize.width / 2.0f - ((float) (numOfIndicators / 2) * indicatorWidth)
          - ((numOfIndicators / 2) * margin);
    }
    for (int x = 1; x <= numOfIndicators; x++) {
      // Load sprite for indicator.
      if ((x - 1) == scrollView.getCurrentPageNum()) {
        indicator = CCSprite.sprite(SpritePreferences.INDICATOR_ON);
      } else {
        indicator = CCSprite.sprite(SpritePreferences.INDICATOR_OFF);
      }
      indicator.setScale(panelHeight * 0.05f / indicator.getContentSize().height);
      indicator.setPosition(posX, posy);
      posX += indicatorWidth + margin;
      addChild(indicator, Z3, INDICATORS_TAG + x);
    }
  }

  /**
   * Update indicators.
   */
  public final void updateIndicators() {
    CCSprite head;
    for (int x = 1; x <= numberOfPages; x++) {
      head = (CCSprite) getChildByTag(INDICATORS_TAG + x);
      if ((x - 1) == scrollView.getCurrentPageNum()) {
        head.setTexture(CCTextureCache.sharedTextureCache().addImage(SpritePreferences.INDICATOR_ON));
      } else {
        head.setTexture(CCTextureCache.sharedTextureCache().addImage(SpritePreferences.INDICATOR_OFF));
      }
    }
  }

  /**
   * Start new level. Replace current scene to be able to open new
   * LevelMenuScene and refresh content.
   *
   * @param levelToStart New level to start.
   */
  private void startNewLevel(final int levelToStart) {
    InGameHelper.turnAllSensorsOff(this);
    CCScene scene = GameLayer.scene(levelToStart);
    scene.setTag(GlobalPreferences.GAME_LAYER_TAG);
    CCScene fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }

  /**
   * Check if level is unlocked.
   *
   * @param levelNumber Level number.
   * @param prefs SharedPreferences object.
   * @return True if level is unlocked.
   */
  private boolean isLevelUnlocked(final int levelNumber, final SharedPreferences prefs) {
    return prefs.getBoolean(SharedPreferencesKeys.LEVEL_UNLOCKED_INFO_KEY + levelNumber, false);
  }

  /**
   * Convert difficulty parameter name to string.
   *
   * @param difficulty Current selected difficulty.
   * @return Difficulty name.
   */
  private String getDifficulty(String difficulty) {
    if (difficulty == null || difficulty.isEmpty() || difficulty.equals(GlobalPreferences.LEVEL_EASY)) {
      return "Easy";
    }
    if (difficulty.equals(GlobalPreferences.LEVEL_MEDIUM)) {
      return "Medium";
    }
    if (difficulty.equals(GlobalPreferences.LEVEL_HARD)) {
      return "Hard";
    }
    return "Easy";
  }
}
