package com.game.layers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.game.helpers.AchievementHelper;
import com.game.helpers.InGameHelper;
import com.game.logger.Logger;
import com.game.model.extensions.HeroPanel;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.game.preferences.SharedPreferencesKeys;
import com.game.preferences.SpritePreferences;
import java.util.List;
import org.cocos2d.actions.instant.CCCallFunc;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.extensions.scroll.CCScrollView;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

/**
 * Display CCLayer containing scrollView of every item available in shop.
 *
 * @author niewinskip
 */
public class ShopLayer extends CCLayer {

  /**
   * Z order.
   */
  private static final int Z0 = 0, Z1 = 1, Z2 = 2, Z3 = 3, Z4 = 4, Z5 = 5;
  /**
   * Select button tag.
   */
  private static final int PURCHASE_OR_SELECT_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Cancel button tag.
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
   * Time after which indicators will be refreshed/updated.
   */
  private static final float BUTTON_REFRESH_DELAY = 0.25f;
  /**
   * Location of finger touch and release.
   */
  private CGPoint startLocation;
  /**
   * Number of diamonds collected.
   */
  private int diamondsCollected;
  /**
   * Application context.
   */
  private final Context appContext;

  /**
   * Global device scaling factor.
   */
  private final float generalScaleFactor = DevicePreferences.generalScaleFactor;

  /**
   * Screen size.
   */
  private final CGSize screenSize = DevicePreferences.screenSize;

  /**
   * Shared preferences.
   */
  private SharedPreferences prefs;

  /**
   * Scroll view.
   */
  private CCScrollView scrollView;

  /**
   * List of heroes available to buy or select in shop.
   */
  private List<HeroPanel> heroList = null;

  /**
   * Default constructor.
   */
  public ShopLayer() {
    this(0);
  }

  /**
   * Constructor. Allows layer to start with last viewed page.
   *
   * @param currentPage Current page in scroll view.
   */
  public ShopLayer(final int currentPage) {
    appContext = CCDirector.sharedDirector().getActivity();
    prefs = PreferenceManager.getDefaultSharedPreferences(this.appContext);
    int page = currentPage;
    if (currentPage == 0) {
      page = prefs.getInt(SharedPreferencesKeys.SELECTED_HERO, 0);
    }
    makeGUI(page);
    InGameHelper.turnAllSensorsOn(this);
  }

  /**
   * Start scene.
   *
   * @return Scene.
   */
  public static CCScene scene() {
    CCScene scene = CCScene.node();
    CCLayer layer = new ShopLayer();
    scene.addChild(layer);
    return scene;
  }

  /**
   * Start scene with scroll view with offset.
   *
   * @param offset Scroll view's x offset.
   * @return Scene.
   */
  public static CCScene scene(final int offset) {
    CCScene scene = CCScene.node();
    CCLayer layer = new ShopLayer(offset);
    scene.addChild(layer);
    return scene;
  }

  /**
   * Load graphics.
   *
   * @param currentPage Scroll view offset.
   */
  private void makeGUI(final int currentPage) {
    float halfW = screenSize.width / 2.0f;
    float halfH = screenSize.height / 2.0f;

    // Background.
    CCSprite bg = InGameHelper.getBackground(screenSize);
    addChild(bg, Z0);

    // Shop panel.
    CCSprite panel = InGameHelper.getMainPanel(SpritePreferences.LEVEL_PANEL, screenSize);
    addChild(panel, Z1);

    // Cancel button.
    CCSprite cancel = CCSprite.sprite(SpritePreferences.B_CANCEL);
    cancel.setScale(screenSize.width * 0.13f / cancel.getContentSize().width);
    cancel.setPosition(halfW + panel.getBoundingBox().size.width / 2.0f,
        halfH + panel.getBoundingBox().size.height * 0.37f);
    addChild(cancel, Z5, CANCEL_BUTTON_TAG);

    // Next button.
    CCSprite next = CCSprite.sprite(SpritePreferences.B_NEXT);
    next.setScale(screenSize.width * 0.13f / next.getContentSize().width);
    next.setPosition(halfW + panel.getBoundingBox().size.width / 2.0f, halfH);
    addChild(next, Z5, NEXT_BUTTON_TAG);

    // Previous button.
    CCSprite previous = CCSprite.sprite(SpritePreferences.B_PREVIOUS);
    previous.setScale(screenSize.width * 0.13f / previous.getContentSize().width);
    previous.setPosition(halfW - panel.getBoundingBox().size.width / 2.0f, halfH);
    addChild(previous, Z5, PREVIOUS_BUTTON_TAG);

    // Add scrollView
    scrollView = CCScrollView.view(CGSize.zero());
    scrollView.bounces = true;
    scrollView.setClipToBounds(true);
    scrollView.direction = 1; // for horizontal scrolling.
    scrollView.setPagingEnabled(true);
    scrollView.setCurrentPageNum(currentPage); // Bug - Issue #34
    scrollView.setViewSize(CGSize.make(screenSize.width, screenSize.height));
    scrollView.setContentSize(
        CGSize.make(((GlobalPreferences.NUMBER_OF_HEROES_IN_SHOP) * screenSize.width), screenSize.height));
    scrollView.setContentOffset(CGPoint.ccp(-currentPage * screenSize.width, 0));
    addChild(scrollView, Z2);

    // Find id of selected hero.
    int selectedHeroId = prefs.getInt(SharedPreferencesKeys.SELECTED_HERO, 1);

    // Number of monsters collected.
    int monstersCollected = 0;

    // Make panel of each item in shop.
    for (int x = 0; x < GlobalPreferences.NUMBER_OF_HEROES_IN_SHOP; x++) {
      CCSprite itemPanel = CCSprite.sprite(SpritePreferences.ITEM_PANEL);
      if (halfH <= itemPanel.getContentSize().height) {
        itemPanel.setScale(halfH / itemPanel.getContentSize().height);
      }
      itemPanel.setPosition(halfW + x * screenSize.width - itemPanel.getBoundingBox().size.width / 2.0f,
          halfH - itemPanel.getBoundingBox().size.height / 2.0f);
      scrollView.addChild(itemPanel, Z3);

      // Choose appropriate set of heroes' resolution. Do it only once.s
      if (heroList == null) {
        if (itemPanel.getBoundingBox().size.width * 0.6f > 128) {
          heroList = GlobalPreferences.heroList_256;
        } else {
          heroList = GlobalPreferences.heroList_128;
        }
      }

      // Add item sprite.
      CCSprite hero = heroList.get(x).getSprite();
      hero.setScale(itemPanel.getBoundingBox().size.width * 0.6f / hero.getContentSize().width);
      hero.setPosition(halfW + x * screenSize.width - hero.getBoundingBox().size.width / 2.0f,
          halfH - hero.getBoundingBox().size.height / 2.0f + itemPanel.getBoundingBox().size.height * 0.14f);
      scrollView.addChild(hero, Z4);

      // Set text's preferred height.
      float textHeight = itemPanel.getBoundingBox().size.height * 0.17f;
      float posY = screenSize.height * 0.4f;
      String price = heroList.get(x).getPrice() + "";
      String name = heroList.get(x).getName() + "";

      // Add item name.
      CCBitmapFontAtlas nameBM = CCBitmapFontAtlas.bitmapFontAtlas(name, GlobalPreferences.FONT);
      nameBM.setScale(textHeight / nameBM.getContentSize().height);
      nameBM.setPosition(halfW + x * screenSize.width - nameBM.getBoundingBox().size.width / 2.0f,
          screenSize.height * 0.87f - nameBM.getBoundingBox().size.height);
      scrollView.addChild(nameBM, Z4);

      // Add item price.
      boolean itemStatus = prefs.getBoolean(SharedPreferencesKeys.HERO_STATUS + x, false);

      // If was not bought.
      if (!itemStatus) {
        CCBitmapFontAtlas priceBM = CCBitmapFontAtlas.bitmapFontAtlas(price, GlobalPreferences.FONT);
        priceBM.setScale(textHeight / priceBM.getContentSize().height);
        priceBM.setPosition(halfW + x * screenSize.width - priceBM.getBoundingBox().size.width / 2.0f,
            posY - priceBM.getBoundingBox().size.height);
        scrollView.addChild(priceBM, Z4);

        // Add diamond currency image.
        CCSprite diamond = CCSprite.sprite(SpritePreferences.I_DIAMOND);
        diamond.setScale(textHeight / diamond.getContentSize().height);
        diamond.setPosition(halfW + x * screenSize.width - itemPanel.getBoundingBox().size.width * 0.33f,
            posY - diamond.getBoundingBox().size.height);
        scrollView.addChild(diamond, Z4);
      } else {
        monstersCollected++;
        // If item was bought.
        String msg = "Purchased";
        // If item was selected.
        if (x == selectedHeroId) {
          msg = "Selected";
        }
        CCBitmapFontAtlas purchased = CCBitmapFontAtlas.bitmapFontAtlas(msg, GlobalPreferences.FONT);
        purchased.setScale(textHeight / purchased.getContentSize().height);
        purchased.setPosition(halfW + x * screenSize.width - purchased.getBoundingBox().size.width / 2.0f,
            posY - purchased.getBoundingBox().size.height);
        scrollView.addChild(purchased, Z4);
      }
    }

    // Load number of total diamonds collected from preferences.
    diamondsCollected = prefs.getInt(SharedPreferencesKeys.TOTAL_DIAMONDS_COLLECTED, 0);
    float margin = 10 * generalScaleFactor;
    float bottomBarCenterY = screenSize.height * 0.095f;
    float textHeight = screenSize.height * 0.09f;
    // Display text overlay
    CCSprite overlay;
    // 64, because h = 64 and w = 128
    if (textHeight <= 64) {
      overlay = CCSprite.sprite(SpritePreferences.TEXT_PANEL_128);
    } else {
      overlay = CCSprite.sprite(SpritePreferences.TEXT_PANEL_256);
    }
    overlay.setScale(1.3f * textHeight / overlay.getContentSize().height);
    overlay.setPosition(screenSize.width / 3.0f, bottomBarCenterY);
    addChild(overlay, Z3);

    // Display total number of collected diamonds.
    CCBitmapFontAtlas numberOfDiamonds = CCBitmapFontAtlas.bitmapFontAtlas(diamondsCollected + "",
        GlobalPreferences.FONT);
    numberOfDiamonds.setAnchorPoint(1f, 0.5f);
    numberOfDiamonds.setScale(0.9f * textHeight / numberOfDiamonds.getContentSize().height);
    numberOfDiamonds.setPosition(screenSize.width / 3.0f - margin, bottomBarCenterY);
    addChild(numberOfDiamonds, Z4);

    // Display diamond item sprite.
    CCSprite diamond = CCSprite.sprite(SpritePreferences.I_DIAMOND);
    diamond.setAnchorPoint(0f, 0.5f);
    diamond.setScale(0.9f * textHeight / diamond.getContentSize().height);
    diamond.setPosition(screenSize.width / 3.0f + margin, bottomBarCenterY);
    addChild(diamond, Z4);

    // Create purchase / select button.
    boolean purchased = prefs.getBoolean(SharedPreferencesKeys.HERO_STATUS + currentPage, false);
    CCSprite purchaseSelectButton = CCSprite.sprite(SpritePreferences.B_WIDE);
    purchaseSelectButton.setScale(textHeight * 1.5f / purchaseSelectButton.getContentSize().height);
    purchaseSelectButton.setPosition(screenSize.width / 3 * 2, bottomBarCenterY);
    addChild(purchaseSelectButton, Z4, PURCHASE_OR_SELECT_BUTTON_TAG);
    if (purchased) {
      InGameHelper.addTextToSprite(purchaseSelectButton, "Select");
    } else {
      InGameHelper.addTextToSprite(purchaseSelectButton, "Purchase");
    }

    // Achievement.
    if (monstersCollected > 1) {
      AchievementHelper.collector(monstersCollected);
    }
  }

  /**
   * Get position of first finger touch.
   */
  @Override
  public boolean ccTouchesBegan(final MotionEvent event) {
    startLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    return true;
  }

  /**
   * Get position of last finger touch. Detect which button was clicked and
   * start appropriate action.
   */
  @Override
  public final boolean ccTouchesEnded(final MotionEvent event) {
    CGPoint endLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    CCSprite purchaseSelectSprite = (CCSprite) getChildByTag(PURCHASE_OR_SELECT_BUTTON_TAG);
    CCSprite cancelSprite = (CCSprite) getChildByTag(CANCEL_BUTTON_TAG);
    CCSprite nextSprite = (CCSprite) getChildByTag(NEXT_BUTTON_TAG);
    CCSprite previousSprite = (CCSprite) getChildByTag(PREVIOUS_BUTTON_TAG);
    // Click.
    if (InGameHelper.detectClick(startLocation, endLocation)) {
      int cPage = scrollView.getCurrentPageNum();
      if (InGameHelper.spriteClicked(cancelSprite, startLocation, endLocation)) {
        InGameHelper.turnAllSensorsOff(this);
        InGameHelper.popAndReplaceSceneWithTag();
      } else if (InGameHelper.spriteClicked(purchaseSelectSprite, startLocation, endLocation)) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this.appContext);
        boolean purchased = prefs.getBoolean(SharedPreferencesKeys.HERO_STATUS + cPage, false);
        if (purchased) {
          selectHero(cPage);
        } else {
          purchaseHero(cPage);
        }
      } else if (InGameHelper.spriteClicked(nextSprite, startLocation, endLocation)) {
        scrollView.scrollToNextPage(screenSize.width, GlobalPreferences.TOTAL_ITEMS_IN_SHOP);
        scheduleButtonUpdate();
      } else if (InGameHelper.spriteClicked(previousSprite, startLocation, endLocation)) {
        scrollView.scrollToPreviousPage(screenSize.width);
        scheduleButtonUpdate();
      }
    } // Possible finger slide.
    else {
      scheduleButtonUpdate();
    }
    return true;
  }

  /**
   * Override and handle the event called in MainActivity. It ensures that
   * event is always called in OpenGl thread.
   */
  @Override
  public final boolean ccKeyDown(final int id, final KeyEvent ke) {
    Logger.log("Clicked back button in ShopLayer");
    InGameHelper.turnAllSensorsOff(this);
    InGameHelper.popAndReplaceSceneWithTag();
    return true;
  }

  /**
   * Schedule button update.
   */
  public void scheduleButtonUpdate() {
    Logger.log("Scheduling button update");
    CCCallFunc lunch = CCCallFunc.action(this, "updateButton");
    CCSequence se = CCSequence.actions(CCDelayTime.action(BUTTON_REFRESH_DELAY), lunch);
    this.runAction(se);
  }

  /**
   * Update button text.
   */
  public void updateButton() {
    CCSprite purchaseSelectSprite = (CCSprite) getChildByTag(PURCHASE_OR_SELECT_BUTTON_TAG);
    InGameHelper.removeTextFromSprite(purchaseSelectSprite);
    int cPage = scrollView.getCurrentPageNum();
    boolean purchased = prefs.getBoolean(SharedPreferencesKeys.HERO_STATUS + cPage, false);
    if (purchased) {
      InGameHelper.addTextToSprite(purchaseSelectSprite, "Select");
    } else {
      InGameHelper.addTextToSprite(purchaseSelectSprite, "Purchase");
    }
  }

  /**
   * Select hero, based on currently selected page.
   *
   * @param page Current page.
   */
  private void selectHero(int page) {
    if (prefs.getInt(SharedPreferencesKeys.SELECTED_HERO, 0) != page) {
      prefs.edit().putInt(SharedPreferencesKeys.SELECTED_HERO, page).apply();
      reloadScene(page);
    }
  }

  /**
   * Purchase hero, based on currently selected page.
   *
   * @param page Current page.
   */
  private void purchaseHero(int page) {
    int price = heroList.get(page).getPrice();
    int newTotal = diamondsCollected - price;
    if (newTotal >= 0) {
      prefs.edit().putInt(SharedPreferencesKeys.TOTAL_DIAMONDS_COLLECTED, newTotal).apply();
      prefs.edit().putBoolean(SharedPreferencesKeys.HERO_STATUS + page, true).apply();
      reloadScene(page);
      // If page contains "The Zombie"
      if (page == 5) {
        AchievementHelper.walkingDead();
      }
    }
  }

  /**
   * Start new scene.
   *
   * @param offset Scroll view's x offset.
   */
  public final void reloadScene(final int offset) {
    CCScene scene = ShopLayer.scene(offset);
    scene.setTag(GlobalPreferences.SHOP_LAYER_TAG);
    CCDirector.sharedDirector().replaceScene(scene);
  }

}
