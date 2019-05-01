package com.game.layers;

import android.view.KeyEvent;
import android.view.MotionEvent;
import com.game.helpers.InGameHelper;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.game.preferences.SpritePreferences;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

public class CreditsLayer extends CCLayer {

  /**
   * First button tag. Reserve next 100 tags for other buttons.
   */
  private static final int CANCEL_BUTTON_TAG = InGameHelper.generateUniqueTag();
  /**
   * Z order.
   */
  private static final int Z0 = 0, Z1 = 1, Z2 = 2;
  /**
   * Detect click.
   */
  private CGPoint startLocation;

  /**
   * Box2d.
   */
  // private World world;
  // private Body groundBody;
  // private Fixture bottomFixture;
  // private Fixture squareFixture;
  //
  // /**
  // * Number of bodies. Should be equal to number of heroes.
  // */
  // private static final int BODY_COUNT = 6;
  public CreditsLayer() {
    // Load sprite for background
    CGSize screenSize = DevicePreferences.screenSize;
    CCSprite bg = InGameHelper.getBackground(screenSize);
    addChild(bg, Z0);

    // Load sprite for level panel
    CCSprite panel = InGameHelper.getMainPanel(SpritePreferences.CREDITS_PANEL, screenSize);
    addChild(panel, Z1);

    // Create title
    CCBitmapFontAtlas panelText = InGameHelper.getPanelTitle(panel, "Credits");
    addChild(panelText, Z2);

    CCBitmapFontAtlas codeText = CCBitmapFontAtlas.bitmapFontAtlas("CODE:", GlobalPreferences.FONT);
    codeText.setScale(panel.getBoundingBox().size.height * 0.09f / codeText.getContentSize().height);
    codeText.setPosition(screenSize.width / 2.0f, panel.getBoundingBox().size.height * 0.7f);
    addChild(codeText, Z2);

    CCBitmapFontAtlas codeAuthorText = CCBitmapFontAtlas.bitmapFontAtlas("Piotr Niewinski", GlobalPreferences.FONT);
    codeAuthorText.setScale(panel.getBoundingBox().size.height * 0.08f / codeAuthorText.getContentSize().height);
    codeAuthorText.setPosition(screenSize.width / 2.0f, panel.getBoundingBox().size.height * 0.6f);
    addChild(codeAuthorText, Z2);

    CCBitmapFontAtlas musicText = CCBitmapFontAtlas.bitmapFontAtlas("MUSIC:", GlobalPreferences.FONT);
    musicText.setScale(panel.getBoundingBox().size.height * 0.09f / musicText.getContentSize().height);
    musicText.setPosition(screenSize.width / 3.0f, panel.getBoundingBox().size.height * 0.45f);
    addChild(musicText, Z2);

    CCBitmapFontAtlas musicAuthorText = CCBitmapFontAtlas.bitmapFontAtlas("Rick Dickert", GlobalPreferences.FONT);
    musicAuthorText.setScale(panel.getBoundingBox().size.height * 0.08f / musicAuthorText.getContentSize().height);
    musicAuthorText.setPosition(screenSize.width / 3.0f, panel.getBoundingBox().size.height * 0.35f);
    addChild(musicAuthorText, Z2);

    CCBitmapFontAtlas graphicsText = CCBitmapFontAtlas.bitmapFontAtlas("GRAPHICS:", GlobalPreferences.FONT);
    graphicsText.setScale(panel.getBoundingBox().size.height * 0.09f / graphicsText.getContentSize().height);
    graphicsText.setPosition(screenSize.width / 3.0f * 2.0f, panel.getBoundingBox().size.height * 0.45f);
    addChild(graphicsText, Z2);

    CCBitmapFontAtlas graphicsAuthorText = CCBitmapFontAtlas.bitmapFontAtlas("graphicriver",
        GlobalPreferences.FONT);
    graphicsAuthorText
        .setScale(panel.getBoundingBox().size.height * 0.08f / graphicsAuthorText.getContentSize().height);
    graphicsAuthorText.setPosition(screenSize.width / 3.0f * 2.0f, panel.getBoundingBox().size.height * 0.35f);
    addChild(graphicsAuthorText, Z2);

    // Cancel button.
    CCSprite cancel = CCSprite.sprite(SpritePreferences.B_CANCEL);
    cancel.setScale(screenSize.width * 0.13f / cancel.getContentSize().width);
    cancel.setPosition(screenSize.width / 2.0f + panel.getBoundingBox().size.width / 2.0f,
        screenSize.height / 2.0f + panel.getBoundingBox().size.height * 0.37f);
    addChild(cancel, Z2, CANCEL_BUTTON_TAG);

    InGameHelper.turnAllSensorsOn(this);
    // setUpBox2d();
  }

  /**
   * Create scene.
   *
   * @return Scene.
   */
  public static CCScene scene() {
    CCScene scene = CCScene.node();
    CCLayer layer = new CreditsLayer();
    scene.addChild(layer);
    return scene;
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
    CCSprite cancelSprite = (CCSprite) getChildByTag(CANCEL_BUTTON_TAG);
    if (InGameHelper.detectClick(startLocation, endLocation)) {
      if (InGameHelper.spriteClicked(cancelSprite, startLocation, endLocation)) {
        InGameHelper.turnAllSensorsOff(this);
        InGameHelper.popAndReplaceSceneWithTag();
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
    Logger.log("Clicked back button in CreditsLayer");
    InGameHelper.turnAllSensorsOff(this);
    InGameHelper.popAndReplaceSceneWithTag();
    return true;
  }
}
