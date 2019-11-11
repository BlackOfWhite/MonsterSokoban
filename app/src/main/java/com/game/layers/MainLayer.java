package com.game.layers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.game.MainActivity;
import com.game.MonsterSokoban.R;
import com.game.helpers.InGameHelper;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.game.preferences.SharedPreferencesKeys;
import com.game.preferences.SpritePreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import java.util.Arrays;
import java.util.List;
import org.cocos2d.actions.instant.CCCallFuncND;
import org.cocos2d.actions.interval.CCScaleTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCTexture2D;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.transitions.CCFadeTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;

/**
 * Main layer class.
 *
 * @author niewinskip
 */
public class MainLayer extends CCLayer {

  /**
   * First button tag. Reserve next 100 tags for other buttons.
   */
  private static final int BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag for credits button.
   */
  private static final int CREDITS_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Tag for facebook button.
   */
  private static final int FACEBOOK_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Z order.
   */
  private static final int Z0 = 0, Z1 = 1, Z2 = 2;
  private static final int RC_ACHIEVEMENT_UI = 9003;
  /**
   * App context.
   */
  private final Context appContext = CCDirector.sharedDirector().getActivity();
  /**
   * List of buttons' paths. Sound sprite has to be the last one.
   */
  private final List<String> buttonsList = Arrays.asList(SpritePreferences.B_PLAY, SpritePreferences.B_ACHIEVEMENTS,
      SpritePreferences.B_SHOP, SpritePreferences.B_FX11);
  /**
   * Detect click.
   */
  private CGPoint startLocation;

  /**
   * Constructor.
   */
  public MainLayer() {
    // Load sprite for background
    /**
     * Screen size.
     */
    CGSize screenSize = DevicePreferences.screenSize;
    CCSprite bg = InGameHelper.getBackground(screenSize);
    addChild(bg, Z0);

    // Add panel to hold menu buttons.
    CCSprite panel = CCSprite.sprite(SpritePreferences.WIDE_PANEL);
    panel.setScale((screenSize.width * 0.8f) / panel.getContentSize().width);
    panel.setAnchorPoint(CGPoint.ccp(0.5f, 0f));
    panel.setPosition(screenSize.width / 2.0f, 0);
    addChild(panel, Z1);

    float panelX = screenSize.width * 0.1f;
    float panelW = panel.getContentSize().width * panel.getScale();
    float panelH = panel.getContentSize().height * panel.getScale();
    float margin = panelW * 0.08f;
    float usablePanelW = panelW - 2 * margin;
    /**
     * Device unique scale.
     */
    float generalScaleFactor = DevicePreferences.generalScaleFactor;
    float spacing = 5 * generalScaleFactor;
    /**
     * Width of the button.
     */
    /**
     * Number of buttons in the button list.
     */
    int numberOfButtons = buttonsList.size();
    float buttonW = (usablePanelW - ((numberOfButtons - 1) * spacing)) / numberOfButtons;

    // Check if sound button has correct path. Sound button is the last one
    // in the array.
    buttonsList.set(buttonsList.size() - 1, getSoundButtonPath());

    // Display buttons.
    int x = 0;
    for (String s : buttonsList) {
      CCSprite sprite = CCSprite.sprite(s);
      sprite.setScale(buttonW / sprite.getContentSize().width);
      sprite.setPosition(panelX + margin + (x * (buttonW + spacing)) + buttonW / 2.0f,
          panelH * 0.2f + buttonW / 2.0f);
      addChild(sprite, Z2, BUTTON_TAG + x);
      x++;
    }

    // Add top buttons.
    CCSprite credits = CCSprite.sprite("Buttons/credits.png");
    credits.setScale(buttonW * 0.75f / credits.getContentSize().width);
    credits.setPosition(panelX + credits.getContentSize().width / 2.0f * credits.getScale() + margin,
        screenSize.height - credits.getContentSize().height / 2.0f * credits.getScale());
    addChild(credits, Z2, CREDITS_BUTTON_TAG);

    CCSprite facebook = CCSprite.sprite("Buttons/facebook.png");
    facebook.setScale(buttonW * 0.75f / facebook.getContentSize().width);
    facebook.setPosition(panelX + facebook.getContentSize().width / 2.0f * facebook.getScale() + margin
            + buttonW * 0.75f + spacing,
        screenSize.height - facebook.getContentSize().height / 2.0f * facebook.getScale());
    addChild(facebook, Z2, FACEBOOK_BUTTON_TAG);
    loadPreferences();

    InGameHelper.turnAllSensorsOn(this);

    // Load background music.
    // SoundEngine.sharedEngine().playSound(appContext,
    // R.raw.backgroundmusic, true);
    initSound();
  }

  /**
   * Start new scene.
   *
   * @return CCScene.
   */
  public static CCScene scene() {
    CCScene scene = CCScene.node();
    CCLayer layer = new MainLayer();
    scene.addChild(layer);
    return scene;
  }

  /**
   * Load game preferences. Set default values if game is loaded for the first
   * time.
   */
  public final void loadPreferences() {

    // Load Game Data For First Time Or Reload
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CCDirector.sharedDirector().getActivity());

    // If application is used for the first time.
    if (!sp.getBoolean(SharedPreferencesKeys.FIRST_PLAY_KEY, false)) {
      sp.edit().putBoolean(SharedPreferencesKeys.FIRST_PLAY_KEY, true).apply();

      for (int x = 1; x <= GlobalPreferences.TOTAL_NUMBER_OF_LEVELS; x++) {
        sp.edit().putInt(SharedPreferencesKeys.LEVEL_INFO_KEY + x, 0).apply();
        if (x == 1 || x == (GlobalPreferences.NUMBER_OF_EASY_LEVELS_CREATED + 1)
            || x == (GlobalPreferences.NUMBER_OF_EASY_LEVELS_CREATED
            + GlobalPreferences.NUMBER_OF_MEDIUM_LEVELS_CREATED + 1)) {
          sp.edit().putBoolean(SharedPreferencesKeys.LEVEL_UNLOCKED_INFO_KEY + x, true).apply();
        } else {
          sp.edit().putBoolean(SharedPreferencesKeys.LEVEL_UNLOCKED_INFO_KEY + x, false).apply();
        }
      }

      sp.edit().putInt(SharedPreferencesKeys.TOTAL_DIAMONDS_COLLECTED, 0).apply();

      for (int x = 0; x < GlobalPreferences.heroList_256.size(); x++) {
        sp.edit().putBoolean(SharedPreferencesKeys.HERO_STATUS + x, false).apply();
      }
      sp.edit().putBoolean(SharedPreferencesKeys.HERO_STATUS + "0", true).apply();
      sp.edit().putInt(SharedPreferencesKeys.SELECTED_HERO, 0).apply();
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
   * start appropriate scene.
   */
  @Override
  public final boolean ccTouchesEnded(final MotionEvent event) {
    CGPoint endLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    CCSprite bsPlay = (CCSprite) getChildByTag(BUTTON_TAG);
    CCSprite bsAchievements = (CCSprite) getChildByTag(BUTTON_TAG + 1);
    CCSprite bsShop = (CCSprite) getChildByTag(BUTTON_TAG + 2);
    CCSprite bsSettings = (CCSprite) getChildByTag(BUTTON_TAG + 3);
    CCSprite bsCredits = (CCSprite) getChildByTag(CREDITS_BUTTON_TAG);
    CCSprite bsFacebook = (CCSprite) getChildByTag(FACEBOOK_BUTTON_TAG);
    CGRect boundingBox = bsSettings.getBoundingBox();
    if (InGameHelper.detectClick(startLocation, endLocation)) {
      if (InGameHelper.spriteClicked(bsPlay, startLocation, endLocation)) {
        openDifficultyScene();
      } else if (InGameHelper.spriteClicked(bsAchievements, startLocation, endLocation)) {
        displayAchievements();
      } else if (InGameHelper.spriteClicked(bsShop, startLocation, endLocation)) {
        openShopScene();
      } else if (InGameHelper.spriteClicked(bsCredits, startLocation, endLocation)) {
        openCreditsScene();
      } else if (boundingBox.contains(startLocation.x, startLocation.y)
          || boundingBox.contains(endLocation.x, endLocation.y)) {
        changeSoundMode(bsSettings);
      } else if (InGameHelper.spriteClicked(bsFacebook, startLocation, endLocation)) {
        Intent facebookIntent = getOpenFacebookIntent(CCDirector.sharedDirector().getActivity());
        CCDirector.sharedDirector().getActivity().startActivity(facebookIntent);
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
    Logger.log("Clicked back button in MainLayer");
    InGameHelper.turnAllSensorsOff(this);
    InGameHelper.popAndReplaceSceneWithTag();
    return true;
  }

  /**
   * Intent to open the official Facebook app. If the Facebook app is not
   * installed then the default web browser will be used.
   *
   * @param context Activity context.
   * @return An intent that will open the Facebook page/profile.
   */
  private Intent getOpenFacebookIntent(Context context) {
    Logger.log("Trying to open facebook page.");
    return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/MonsterSokoban"));
  }

  /**
   * Start new scene.
   */
  private void openCreditsScene() {
    InGameHelper.turnAllSensorsOff(this);
    CCScene scene = CreditsLayer.scene();
    scene.setTag(GlobalPreferences.CREDITS_LAYER_TAG);
    CCScene fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }

  /**
   * Start new scene.versionCode
   */
  private void openDifficultyScene() {
    InGameHelper.turnAllSensorsOff(this);
    CCScene scene = DifficultyLayer.scene();
    scene.setTag(GlobalPreferences.DIFFICULTY_LAYER_TAG);
    CCScene fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }

  /**
   * Start new scene.
   */
  private void displayAchievements() {
    final MainActivity context = MainActivity.getINSTANCE();
    Logger.log("displayAchievements");
    CCDirector.sharedDirector().getActivity().runOnUiThread(() -> {
      GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
      if (account != null) {
        Games.getAchievementsClient(context, account).getAchievementsIntent()
            .addOnSuccessListener(
                intent -> context.startActivityForResult(intent, RC_ACHIEVEMENT_UI));
      } else {
        MainActivity.getINSTANCE().signInGoogleApi();
      }
    });
  }

  /**
   * Start new scene.
   */
  private void openShopScene() {
    InGameHelper.turnAllSensorsOff(this);
    CCScene scene = ShopLayer.scene();
    scene.setTag(GlobalPreferences.SHOP_LAYER_TAG);
    CCScene fade = CCFadeTransition.transition(1f, scene);
    CCDirector.sharedDirector().replaceScene(fade);
  }

  /**
   * Animate sprite and change sound mode.
   */
  private void changeSoundMode(CCSprite sprite) {
    if (sprite.numberOfRunningActions() > 0) {
      return;
    }
    // Create sequence.
    float scale = sprite.getScale();
    CCScaleTo shrink1 = CCScaleTo.action(0.1f, scale * 0.6f);
    CCCallFuncND changeSprite = CCCallFuncND.action(this, "changeSpriteTexture", sprite);
    CCScaleTo grow = CCScaleTo.action(0.1f, scale * 1.2f);
    CCScaleTo shrink2 = CCScaleTo.action(0.1f, scale);
    CCSequence se = CCSequence.actions(shrink1, changeSprite, grow, shrink2);
    sprite.runAction(se);
  }

  /**
   * Change sound mode and sprite texture.
   *
   * @param sender Sender callback.
   * @param sprite CCSprite.
   */
  public final void changeSpriteTexture(final Object sender, final Object sprite) {
    DevicePreferences.SOUND_MODE++;
    DevicePreferences.SOUND_MODE %= 4;
    String path = "Buttons/FX" + (DevicePreferences.SOUND_MODE / 2) + (DevicePreferences.SOUND_MODE % 2) + ".png";
    CCTexture2D texture = CCTextureCache.sharedTextureCache().addImage(path);
    ((CCSprite) sprite).setTexture(texture);
    if (DevicePreferences.SOUND_MODE >= 2) {
      SoundEngine.sharedEngine().resumeSound();
    } else {
      SoundEngine.sharedEngine().pauseSound();
    }
    Logger.log("Sound mode set to: " + DevicePreferences.SOUND_MODE);
  }

  public String getSoundButtonPath() {
    return "Buttons/FX" + (DevicePreferences.SOUND_MODE / 2) + (DevicePreferences.SOUND_MODE % 2) + ".png";
  }

  public void initSound() {
    if (DevicePreferences.SOUND_MODE >= 2) {
      SoundEngine.sharedEngine().playSound(appContext, R.raw.backgroundmusic, true);
    }
  }
}
