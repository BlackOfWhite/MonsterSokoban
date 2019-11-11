package com.game.helpers;

import com.game.MainActivity;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

public final class AchievementHelper {

  /**
   * Private constructor.
   */
  private AchievementHelper() {

  }

  /**
   * Enter poisonous field 50 times.
   */
  public static void intoxicated() {
    if (isConnected()) {
      getClient().reveal("CgkIrtaC9IUREAIQGQ");
      getClient().increment("CgkIrtaC9IUREAIQGQ", 1);
    }
  }

  /**
   * Unlock after completing 5 levels.
   */
  public static void gettingStarted() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQEQ");
    }
  }

  private static AchievementsClient getClient() {
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.getINSTANCE());
    if (account != null) {
      return Games.getAchievementsClient(MainActivity.getINSTANCE(), account);
    }
    return null;
  }

  /**
   * Touch screen 100 times.
   */
  public static void canTouchThis() {
    if (isConnected()) {
      getClient().reveal("CgkIrtaC9IUREAIQDg");
      getClient().increment("CgkIrtaC9IUREAIQDg", 1);
    }
  }

  /**
   * Complete level 27. Must contain a lot of ice.
   */
  public static void icelyDone() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQAA");
    }
  }

  /**
   * Hit reset button 25 times.
   */
  public static void graveMistake() {
    if (isConnected()) {
      getClient().reveal("CgkIrtaC9IUREAIQBQ");
      getClient().increment("CgkIrtaC9IUREAIQBQ", 1);
    }
  }

  /**
   * Hit undo button 100 times.
   */
  public static void backInTime() {
    if (isConnected()) {
      getClient().reveal("CgkIrtaC9IUREAIQCw");
      getClient().increment("CgkIrtaC9IUREAIQCw", 1);
    }
  }

  /**
   * Complete level 42.
   */
  public static void marathon() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQCg");
    }
  }

  /**
   * Collect 90 skulls.
   */
  public static void toBeOrNotToBe(int skullCount) {
    if (isConnected()) {
      getClient().setSteps("CgkIrtaC9IUREAIQEg", skullCount);
    }
  }

  /**
   * Collect 100 diamonds.
   */
  public static void mrScrooge(int diamondsCount) {
    if (isConnected()) {
      getClient().reveal("CgkIrtaC9IUREAIQEw");
      getClient().setSteps("CgkIrtaC9IUREAIQEw", diamondsCount);
    }
  }

  /**
   * Portal yourself 100 times.
   */
  public static void looper() {
    if (isConnected()) {
      getClient().increment("CgkIrtaC9IUREAIQFA", 1);
    }
  }

  /**
   * Play for 2 hours.
   */
  public static void dontStopMeNow() {
    if (DevicePreferences.appStartTime == -1) {
      DevicePreferences.appStartTime = System.currentTimeMillis();
    } else {
      int minutes = (int) ((System.currentTimeMillis() - DevicePreferences.appStartTime) / 60000);
      if (minutes > 0) {
        DevicePreferences.appStartTime = System.currentTimeMillis();
        if (isConnected()) {
          getClient().reveal("CgkIrtaC9IUREAIQFQ");
          getClient().increment("CgkIrtaC9IUREAIQFQ", minutes);
        }
        Logger.log("Play time: " + minutes);
      }
    }
  }

  /**
   * Collect all monsters.
   *
   * @param numOfMonsters Number of monters purchased.
   */
  public static void collector(int numOfMonsters) {
    if (isConnected()) {
      getClient().reveal("CgkIrtaC9IUREAIQFg");
      if (numOfMonsters == GlobalPreferences.TOTAL_ITEMS_IN_SHOP) {
        getClient().unlock("CgkIrtaC9IUREAIQFg");
      }
    }
  }

  /**
   * Unlock zombie.
   */
  public static void walkingDead() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQFw");
    }
  }

  /**
   * Walk over 100 ice tiles in a row.
   */
  public static void chillOut() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQGA");
    }
  }

  /**
   * Check if google play API is connected.
   */
  private static boolean isConnected() {
    return getClient() != null;
  }

  /**
   * Complete the maze. Level 60.
   */
  public static void theMaze() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQHA");
    }
  }

  /**
   * Complete level 30.
   */
  public static void beginner() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQHQ");
    }
  }

  /**
   * Complete level 45.
   */
  public static void advanced() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQHg");
    }
  }

  /**
   * Complete level 60.
   */
  public static void expert() {
    if (isConnected()) {
      getClient().unlock("CgkIrtaC9IUREAIQHw");
    }
  }
}
