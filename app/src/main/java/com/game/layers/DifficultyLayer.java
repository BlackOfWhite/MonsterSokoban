package com.game.layers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.game.helpers.AchievementHelper;
import com.game.helpers.InGameHelper;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.game.preferences.SharedPreferencesKeys;
import com.game.preferences.SpritePreferences;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.transitions.CCFadeTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

/**
 * Class for displaying and choosing levels difficulty.
 *
 * @author niewinskip
 */
public class DifficultyLayer extends CCLayer {

  /**
   * Z order.
   */
  private static final int Z0 = 0, Z1 = 1, Z2 = 2, Z3 = 3, Z4 = 4;
  /**
   * Tag for cancel button.
   */
  private static final int CANCEL_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag for easy difficulty panel.
   */
  private static final int EASY_BUTTON_TAG = InGameHelper.generateUniqueTag();
  private static final int EASY_PANEL_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag for medium difficulty panel.
   */
  private static final int MEDIUM_BUTTON_TAG = InGameHelper.generateUniqueTag();
  private static final int MEDIUM_PANEL_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag for hard difficulty panel.
   */
  private static final int HARD_BUTTON_TAG = InGameHelper.generateUniqueTag();
  private static final int HARD_PANEL_TAG = InGameHelper.generateUniqueTag();
  /**
   * Font path.
   */
  private static final String FONT = GlobalPreferences.FONT;
  /**
   * Application context.
   */
  private static Context appContext;
  /**
   * Global device scaling factor.
   */
  private final float generalScaleFactor = DevicePreferences.generalScaleFactor;
  /**
   * Screen size.
   */
  private CGSize screenSize = DevicePreferences.screenSize;
  /**
   * Center of the screen's width.
   */
  private final float halfW = screenSize.width / 2.0f;

  /**
   * Center of the screen's height.
   */
  private final float halfH = screenSize.height / 2.0f;
  /**
   * Finger first and last location.
   */
  private CGPoint startLocation;

  /**
   * Constructor.
   */
  public DifficultyLayer() {
    appContext = CCDirector.sharedDirector().getActivity();
    makeGUI();
    InGameHelper.turnAllSensorsOn(this);
  }

  /**
   * Start Level Menu Layer.
   *
   * @return scene object.
   */
  public static CCScene scene() {
    CCScene scene = CCScene.node();
    CCLayer layer = new DifficultyLayer();
    scene.addChild(layer);
    return scene;
  }

  /**
   * Prepare GUI.
   */
  private void makeGUI() {
    // Load sprite for background.
    CCSprite bg = InGameHelper.getBackground(screenSize);
    addChild(bg, Z0);

    // Load sprite for panel.
    CCSprite panel = InGameHelper.getMainPanel(SpritePreferences.CREDITS_PANEL, screenSize);
    addChild(panel, Z1);

    // Create title
    CCBitmapFontAtlas panelText = InGameHelper.getPanelTitle(panel, "Choose Difficulty");
    addChild(panelText, Z2);

    // Cancel button.
    CCSprite cancel = CCSprite.sprite(SpritePreferences.B_CANCEL);
    cancel.setScale(screenSize.width * 0.12f / cancel.getContentSize().width);
    cancel.setPosition(halfW + panel.getBoundingBox().size.width / 2.0f,
        halfH + panel.getBoundingBox().size.height * 0.37f);
    addChild(cancel, Z2, CANCEL_BUTTON_TAG);

    // Create panels and buttons.
    float margin = 10 * generalScaleFactor;
    float availableWidth = panel.getBoundingBox().size.width * 0.8f;
    float availableHeight = panel.getBoundingBox().size.height * 0.6f;
    availableWidth -= 4 * margin;
    availableHeight -= 2 * margin;
    float difficultyPanelW = availableWidth / 3;
    float difficultyPanelH = availableHeight / 2;
    float startPosX = halfW - margin - difficultyPanelW;

    // Level difficulty panels
    for (int x = 0; x < 3; x++) {
      CCSprite difficultyPanel;
      if (Math.min(difficultyPanelH, difficultyPanelW) <= 128) {
        if (x == 0) {
          difficultyPanel = CCSprite.sprite(SpritePreferences.LEVEL_EASY_PANEL_128);
        } else if (x == 1) {
          difficultyPanel = CCSprite.sprite(SpritePreferences.LEVEL_MEDIUM_PANEL_128);
        } else {
          difficultyPanel = CCSprite.sprite(SpritePreferences.LEVEL_HARD_PANEL_128);
        }
      } else {
        if (x == 0) {
          difficultyPanel = CCSprite.sprite(SpritePreferences.LEVEL_EASY_PANEL_256);
        } else if (x == 1) {
          difficultyPanel = CCSprite.sprite(SpritePreferences.LEVEL_MEDIUM_PANEL_256);
        } else {
          difficultyPanel = CCSprite.sprite(SpritePreferences.LEVEL_HARD_PANEL_256);
        }
      }
      difficultyPanel.setScale(InGameHelper.getPreferredScale(difficultyPanelW, difficultyPanelH,
          difficultyPanel.getContentSize()));
      difficultyPanel.setPosition(startPosX + x * (margin + difficultyPanelW), halfH * 1.1f);
      int tag = (x == 0 ? EASY_PANEL_TAG : (x == 1 ? MEDIUM_PANEL_TAG : HARD_PANEL_TAG));
      addChild(difficultyPanel, Z3, tag);

      // Level difficulty buttons
      CCSprite button = CCSprite.sprite(SpritePreferences.B_WIDE_DARK);
      button.setScale(
          InGameHelper.getPreferredScale(difficultyPanelW, availableHeight * 0.23f, button.getContentSize()));
      button.setPosition(difficultyPanel.getPosition().x,
          difficultyPanel.getPosition().y - difficultyPanel.getBoundingBox().size.height / 2.0f
              - button.getBoundingBox().size.height / 2.0f - margin / 2.0f);
      tag = (x == 0 ? EASY_BUTTON_TAG : (x == 1 ? MEDIUM_BUTTON_TAG : HARD_BUTTON_TAG));
      addChild(button, Z4, tag);

      String value = (x == 0 ? "Easy" : (x == 1 ? "Medium" : "Hard"));
      InGameHelper.addTextToSprite(button, value);
    }

    // Load number of unlocked levels.
    int skullsCollected = 0;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
    for (int x = 1; x <= GlobalPreferences.TOTAL_NUMBER_OF_LEVELS; x++) {
      skullsCollected += prefs.getInt(SharedPreferencesKeys.LEVEL_INFO_KEY + x, 0);
    }
    // Display dark overlay.
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
    // Create skull sprite.
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
    // Display number of skulls.
    CCBitmapFontAtlas numberOfSkulls = CCBitmapFontAtlas.bitmapFontAtlas(skullsCollected + "", FONT);
    numberOfSkulls.setAnchorPoint(1f, 0.5f);
    numberOfSkulls.setScale(0.78f * height / numberOfSkulls.getContentSize().height);
    numberOfSkulls.setPosition(halfW - margin / 2.0f, posY);
    addChild(numberOfSkulls, Z3);

    // BUG # 81
    if (skullsCollected > 0) {
      AchievementHelper.toBeOrNotToBe(skullsCollected);
    }
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

    // Click.
    if (InGameHelper.detectClick(startLocation, endLocation)) {
      // Check if cancel button was clicked.
      CCSprite cancel = (CCSprite) getChildByTag(CANCEL_BUTTON_TAG);
      CCSprite easyButton = (CCSprite) getChildByTag(EASY_BUTTON_TAG);
      CCSprite easyPanel = (CCSprite) getChildByTag(EASY_PANEL_TAG);
      CCSprite mediumButton = (CCSprite) getChildByTag(MEDIUM_BUTTON_TAG);
      CCSprite mediumPanel = (CCSprite) getChildByTag(MEDIUM_PANEL_TAG);
      CCSprite hardButton = (CCSprite) getChildByTag(HARD_BUTTON_TAG);
      CCSprite hardPanel = (CCSprite) getChildByTag(HARD_PANEL_TAG);
      if (InGameHelper.spriteClicked(cancel, startLocation, endLocation)) {
        InGameHelper.turnAllSensorsOff(this);
        InGameHelper.popAndReplaceSceneWithTag();
      } else if (InGameHelper.spriteClicked(easyButton, startLocation, endLocation) || InGameHelper
          .spriteClicked(easyPanel, startLocation, endLocation)) {
        openLevelScene(GlobalPreferences.LEVEL_EASY);
      } else if (InGameHelper.spriteClicked(mediumButton, startLocation, endLocation) || InGameHelper
          .spriteClicked(mediumPanel, startLocation, endLocation)) {
        openLevelScene(GlobalPreferences.LEVEL_MEDIUM);
      } else if (InGameHelper.spriteClicked(hardButton, startLocation, endLocation) || InGameHelper
          .spriteClicked(hardPanel, startLocation, endLocation)) {
        openLevelScene(GlobalPreferences.LEVEL_HARD);
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
    Logger.log("Clicked back button in DifficultyLayer");
    InGameHelper.turnAllSensorsOff(this);
    InGameHelper.popAndReplaceSceneWithTag();
    return true;
  }

  /**
   * Start new scene.
   *
   * @param difficulty Chosen difficulty
   */
  private void openLevelScene(final String difficulty) {
    InGameHelper.turnAllSensorsOff(this);
    AchievementHelper.dontStopMeNow();
    CCScene scene = LevelMenuLayer.scene(difficulty);
    scene.setTag(GlobalPreferences.LEVEL_SELECTION_LAYER_TAG);
    CCScene fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }
}
