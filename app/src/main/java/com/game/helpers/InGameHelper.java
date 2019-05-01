package com.game.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import com.game.MainActivity;
import com.game.layers.DifficultyLayer;
import com.game.layers.LevelMenuLayer;
import com.game.layers.MainLayer;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.game.preferences.SharedPreferencesKeys;
import com.game.preferences.SpritePreferences;
import java.util.HashSet;
import java.util.Set;
import org.cocos2d.actions.interval.CCScaleTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.transitions.CCFadeTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;

/**
 * Helper class containing useful/core/common methods for whole project.
 *
 * @author niewinskip
 */
public final class InGameHelper {

  /**
   * Tag for text added to sprite.
   */
  public static final int TEXT_TAG = 999;
  /**
   * First tag. Carefully.
   */
  private static int tag = 5_000;
  /**
   * Set of tags. Assure uniqueness.
   */
  private static Set<Integer> tagSet;

  /**
   * Private constructor.
   */
  private InGameHelper() {

  }

  /**
   * Check if finger gesture can be classified as click.
   *
   * @param startLocation Touch.
   * @param endLocation Release.
   * @return True if click, false if not.
   */
  public static boolean detectClick(final CGPoint startLocation, final CGPoint endLocation) {
    if (endLocation == null || startLocation == null) {
      return false;
    }

    AchievementHelper.canTouchThis();

    float diffX = Math.abs(endLocation.x - startLocation.x);
    float diffY = Math.abs(endLocation.y - startLocation.y);

    if (diffX <= DevicePreferences.CLICK_THRESHOLD && diffY <= DevicePreferences.CLICK_THRESHOLD) {
      Logger.log("Click detected.");
      return true;
    }

    return false;
  }

  /**
   * Check if finger gesture can be classified as swipe.
   *
   * @param startLocation Touch.
   * @param endLocation Release.
   * @return True if swipe, false if not.
   */
  public static String detectMove(final CGPoint startLocation, final CGPoint endLocation) {
    if (detectClick(startLocation, endLocation)) {
      return GlobalPreferences.NO_MOVE;
    }
    if (startLocation == null || endLocation == null) {
      return GlobalPreferences.NO_MOVE;
    }

    AchievementHelper.canTouchThis();

    float diffX = startLocation.x - endLocation.x;
    float diffY = startLocation.y - endLocation.y;
    float absDiffX = Math.abs(diffX);
    float absDiffY = Math.abs(diffY);

    if (absDiffX > absDiffY && diffX < 0) {
      Logger.log("RIGHT");
      return GlobalPreferences.MOVE_RIGHT;
    } else if (absDiffX > absDiffY && diffX >= 0) {
      Logger.log("LEFT");
      return GlobalPreferences.MOVE_LEFT;
    } else if (absDiffX <= absDiffY && diffY < 0) {
      Logger.log("UP");
      return GlobalPreferences.MOVE_UP;
    } else if (absDiffX <= absDiffY && diffY >= 0) {
      Logger.log("DOWN");
      return GlobalPreferences.MOVE_DOWN;
    }
    return GlobalPreferences.NO_MOVE;
  }

  /**
   * Create and adjust background.
   *
   * @param screenSize CGSize screen size.
   * @return Background CCSprite.
   */
  public static CCSprite getBackground(final CGSize screenSize) {
    CCSprite bg;
    float width = screenSize.width;
    if (width <= 512) {
      bg = CCSprite.sprite(SpritePreferences.BG_512);
    } else if (width <= 1024) {
      bg = CCSprite.sprite(SpritePreferences.BG_1024);
    } else {
      bg = CCSprite.sprite(SpritePreferences.BG_2048);
    }
    float scaleX = screenSize.width / bg.getContentSize().width;
    float scaleY = screenSize.height / bg.getContentSize().height;
    bg.setScale(Math.max(scaleX, scaleY));
    bg.setPosition(screenSize.width / 2.0f, screenSize.height / 2.0f);
    Logger.log("Created background image with scale " + bg.getScale() + ". Screen height is " + width);
    return bg;
  }

  /**
   * Create panel for displaying other items.
   *
   * @param panelSpritePath Panel sprite path.
   * @param screenSize CGSize screen size.
   * @return Panel sprite.
   */
  public static CCSprite getMainPanel(final String panelSpritePath, final CGSize screenSize) {
    CCSprite panel = CCSprite.sprite(panelSpritePath);
    float scaleX = screenSize.width / panel.getContentSize().width;
    float scaleY = screenSize.height / panel.getContentSize().height;
    panel.setScale(Math.min(scaleX, scaleY) * 0.95f);
    panel.setPosition(screenSize.width / 2.0f, screenSize.height / 2.0f);
    return panel;
  }

  /**
   * Create title text for panel or sprite.
   *
   * @param panel CCSprite panel.
   * @param title String title.
   * @return CCBitmapFontAtlas
   */
  public static CCBitmapFontAtlas getPanelTitle(final CCSprite panel, String title) {
    float textWidth = panel.getBoundingBox().size.width * 0.25f;
    float textHeight = panel.getBoundingBox().size.height * 0.09f;
    CCBitmapFontAtlas titleText = CCBitmapFontAtlas.bitmapFontAtlas(title, GlobalPreferences.FONT);
    titleText.setScale(getPreferredScale(textWidth, textHeight, titleText.getContentSize()));
    titleText.setPosition(DevicePreferences.screenSize.width / 2.0f,
        DevicePreferences.screenSize.height / 2.0f + panel.getBoundingBox().size.height * 0.35f);
    return titleText;
  }

  /**
   * Get selected hero sprite.
   *
   * @param context Application context.
   * @return Hero's CCSprite.
   */
  public static CCSprite getHeroSprite(final Context context, float spriteResolution) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    int id = prefs.getInt(SharedPreferencesKeys.SELECTED_HERO, 1) + 1;
    String path = "Heroes/hero" + id + "_" + Math.round(spriteResolution) + ".png";
    Logger.log("Chosen hero's sprite path: " + path);
    return CCSprite.sprite(path);
  }

  /**
   * Generate unique tag.
   *
   * @return Tag.
   */
  public static int generateUniqueTag() {
    if (tagSet == null) {
      tagSet = new HashSet<>();
      tagSet.add(tag);
      return tag;
    }
    while (tagSet.contains(tag)) {
      /**
       * Tag step.
       */
      int tagStep = 1000;
      if ((tag + tagStep) >= Integer.MAX_VALUE) {
        tag = 0;
      }
      tag += tagStep;
    }
    tagSet.add(tag);
    return tag;
  }

  /**
   * Check if sprite contains one of the locations.
   *
   * @param startLocation Clicked.
   * @param endLocation Released.
   * @return True if sprite was clicked, false if not.
   */
  public static boolean spriteClicked(final CCSprite sprite, final CGPoint startLocation, final CGPoint endLocation) {
    if (!InGameHelper.detectClick(startLocation, endLocation) || sprite == null) {
      return false;
    }
    CGRect boundingBox = sprite.getBoundingBox();
    if (boundingBox.contains(startLocation.x, startLocation.y)
        || boundingBox.contains(endLocation.x, endLocation.y)) {
      if (sprite.numberOfRunningActions() == 0) {
        sprite.runAction(getClickEffect(sprite));
        return true;
      }
    }
    Logger.log("Cannot start new action for sprite, there is already running action");
    return false;
  }

  /**
   * Create animation sequence for clicked sprite.
   *
   * @param sprite Sprite to animate.
   * @return CCSequence.
   */
  public static CCSequence getClickEffect(final CCSprite sprite) {
    float scale = sprite.getScale();
    CCScaleTo shrink1 = CCScaleTo.action(0.1f, scale * 0.6f);
    CCScaleTo grow = CCScaleTo.action(0.1f, scale * 1.2f);
    CCScaleTo shrink2 = CCScaleTo.action(0.1f, scale);
    return CCSequence.actions(shrink1, grow, shrink2);
  }

  /**
   * Create animation sequence for for a sprite.
   *
   * @param sprite Sprite to animate.
   * @param destScale Excepted final scale.
   * @return CCSequence.
   */
  public static CCSequence getSpriteIntroEffect(final CCSprite sprite, float destScale) {
    if (sprite.numberOfRunningActions() > 0) {
      return null;
    }
    CCScaleTo grow = CCScaleTo.action(0.2f, destScale * 1.2f);
    CCScaleTo shrink2 = CCScaleTo.action(0.2f, destScale);
    return CCSequence.actions(grow, shrink2);
  }

  /**
   * Pop scene using fade transition.
   */
  public static void popFadeScene() {
    CCScene scene = CCDirector.sharedDirector().getPreviousScene();
    CCFadeTransition fade = CCFadeTransition.transition(1.0f, scene);
    // If there was at least more than one scene of the stack.
    if (scene != null) {
      CCDirector.sharedDirector().popScene(fade);
    } else {
      CCDirector.sharedDirector().popScene();
    }
  }

  /**
   * Replace scene using transition.
   *
   * @param scene CCScene to replace with.
   */
  public static void replaceFadeScene(final CCScene scene) {
    CCFadeTransition fade = CCFadeTransition.transition(1.0f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }

  /**
   * This method should be run when calling for new layer. Disables touch
   * events for specified layer and touch pad.
   *
   * @param layer CCLayer to disable.
   */
  public static void turnAllSensorsOff(final CCLayer layer) {
    if (layer == null) {
      Logger.log("Cant turn sensors off, layer is null.");
      return;
    }
    Logger.log("Turning sensors off");
    layer.setIsTouchEnabled(false);
    layer.setIsKeyEnabled(false);
    MainActivity mainActivity = (MainActivity) CCDirector.sharedDirector().getActivity();
    mainActivity.setTouch(false);
    Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        layer.setIsTouchEnabled(true);
        layer.setIsKeyEnabled(true);
      }
    }, 1000);
  }

  /**
   * This method should be run in constructor of every new layer. It enables
   * touch events for specified layer and also enables touch pad after 2
   * seconds.
   *
   * @param layer CCLayer to enable.
   */
  public static void turnAllSensorsOn(final CCLayer layer) {
    if (layer == null) {
      Logger.log("Cant turn sensors on, layer is null.");
      return;
    }
    Logger.log("Turning sensors on");
    layer.setIsTouchEnabled(true);
    layer.setIsKeyEnabled(true);
    Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        MainActivity mainActivity = (MainActivity) CCDirector.sharedDirector().getActivity();
        mainActivity.setTouch(true);
      }
    }, 2000);
  }

  /**
   * Calculate preferred scale for sprite.
   *
   * @param width Desired width.
   * @param height Desired height.
   * @param contentSize Sprite original size.
   * @return Preferred scale
   */
  public static float getPreferredScale(final float width, final float height, final CGSize contentSize) {
    float scaleX = width / contentSize.width;
    float scaleY = height / contentSize.height;
    return Math.min(scaleX, scaleY);
  }

  /**
   * Create font bitmap and add it as a child of a sprite.
   *
   * @param sprite CCSprite.
   * @param value String value.
   */
  public static void addTextToSprite(CCSprite sprite, final String value) {
    float textW = sprite.getBoundingBox().size.width * 0.8f;
    float textH = sprite.getBoundingBox().size.height * 0.6f;
    CCBitmapFontAtlas text = CCBitmapFontAtlas.bitmapFontAtlas(value, GlobalPreferences.FONT);
    text.setScale(InGameHelper.getPreferredScale(textW, textH, text.getContentSize()) / sprite.getScale());
    text.setPosition(sprite.getBoundingBox().size.width / 2.0f / sprite.getScale(),
        sprite.getBoundingBox().size.height / 2.0f / sprite.getScale());
    sprite.addChild(text, sprite.getZOrder() + 1, TEXT_TAG);
  }

  /**
   * Remove text from sprite.
   *
   * @param sprite CCSprite
   */
  public static void removeTextFromSprite(final CCSprite sprite) {
    sprite.removeChildByTag(TEXT_TAG, true);
  }

  /**
   * Use this method to keep track of layers and all transitions. Here, the
   * scenes are initialized with tags and handled properly.
   */
  public static void popAndReplaceSceneWithTag() {
    int sceneTag = CCDirector.sharedDirector().getRunningScene().getTag();
    CCScene scene = null;
    if (sceneTag == GlobalPreferences.CREDITS_LAYER_TAG) {
      scene = MainLayer.scene();
      scene.setTag(GlobalPreferences.MAIN_LAYER_TAG);
    } else if (sceneTag == GlobalPreferences.SHOP_LAYER_TAG) {
      scene = MainLayer.scene();
      scene.setTag(GlobalPreferences.MAIN_LAYER_TAG);
    } else if (sceneTag == GlobalPreferences.DIFFICULTY_LAYER_TAG) {
      scene = MainLayer.scene();
      scene.setTag(GlobalPreferences.MAIN_LAYER_TAG);
    } else if (sceneTag == GlobalPreferences.LEVEL_SELECTION_LAYER_TAG) {
      scene = DifficultyLayer.scene();
      scene.setTag(GlobalPreferences.DIFFICULTY_LAYER_TAG);
    } else if (sceneTag == GlobalPreferences.GAME_LAYER_TAG) {
      scene = LevelMenuLayer.scene(GlobalPreferences.CURRENT_LEVEL_DIFFICULTY);
      scene.setTag(GlobalPreferences.LEVEL_SELECTION_LAYER_TAG);
    }
    CCScene fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }
}
