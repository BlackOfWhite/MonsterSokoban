package com.game.preferences;

import com.game.logger.Logger;
import com.google.android.gms.common.api.GoogleApiClient;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.types.CGSize;

/**
 * Device useful parameters.
 *
 * @author niewinskip
 */
public class DevicePreferences {

  /**
   * Screen size. Contains width and height.
   */
  public static final CGSize screenSize = CCDirector.sharedDirector().winSize();

  /**
   * In-scale factor. Relevant to screen size.
   */
  public static final float generalScaleFactor = CCDirector.sharedDirector().winSize().height / 500;

  /**
   * Click square area around location where finger was released. Treat
   * buttons within that area as they were clicked.
   */
  public static final float CLICK_THRESHOLD = generalScaleFactor * 30;
  /**
   * Application start time.
   */
  public static long appStartTime = -1;
  /**
   * Sound mode. 0 - mute, 1 - only fx, 2 - only music, 3 - together.
   */
  public static int SOUND_MODE = 3;
  /**
   * Google API Client for managing and using Google Services.
   */
  private static GoogleApiClient googleApiClient = null;

  public static GoogleApiClient getGoogleApiClient() {
    return googleApiClient;
  }

  public static void setGoogleApiClient(final GoogleApiClient gac) {
    if (googleApiClient != null) {
      Logger.log("Google Api Client was already set, overriding!");
    }
    googleApiClient = gac;
  }
}
