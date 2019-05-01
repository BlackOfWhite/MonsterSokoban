package com.game.helpers;

import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
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
      Games.Achievements.reveal(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQGQ");
      Games.Achievements.increment(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQGQ", 1);
    }
  }

  /**
   * Unlock after completing 5 levels.
   */
  public static void gettingStarted() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQEQ");
    }
  }

  /**
   * Touch screen 100 times.
   */
  public static void canTouchThis() {
    if (isConnected()) {
      Games.Achievements.reveal(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQDg");
      Games.Achievements.increment(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQDg", 1);
    }
  }

  /**
   * Complete level 27. Must contain a lot of ice.
   */
  public static void icelyDone() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQAA");
    }
  }

  /**
   * Hit reset button 25 times.
   */
  public static void graveMistake() {
    if (isConnected()) {
      Games.Achievements.reveal(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQBQ");
      Games.Achievements.increment(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQBQ", 1);
    }
  }

  /**
   * Hit undo button 100 times.
   */
  public static void backInTime() {
    if (isConnected()) {
      Games.Achievements.reveal(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQCw");
      Games.Achievements.increment(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQCw", 1);
    }
  }

  /**
   * Complete level 42.
   */
  public static void marathon() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQCg");
    }
  }

  /**
   * Collect 90 skulls.
   */
  public static void toBeOrNotToBe(int skullCount) {
    if (isConnected()) {
      Games.Achievements.setSteps(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQEg", skullCount);
    }
  }

  /**
   * Collect 100 diamonds.
   */
  public static void mrScrooge(int diamondsCount) {
    if (isConnected()) {
      Games.Achievements.reveal(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQEw");
      Games.Achievements.setSteps(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQEw", diamondsCount);
    }
  }

  /**
   * Portal yourself 100 times.
   */
  public static void looper() {
    if (isConnected()) {
      Games.Achievements.increment(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQFA", 1);
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
          Games.Achievements.reveal(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQFQ");
          Games.Achievements.increment(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQFQ", minutes);
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
      Games.Achievements.reveal(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQFg");
      if (numOfMonsters == GlobalPreferences.TOTAL_ITEMS_IN_SHOP) {
        Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQFg");
      }
    }
  }

  /**
   * Unlock zombie.
   */
  public static void walkingDead() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQFw");
    }
  }

  /**
   * Walk over 100 ice tiles in a row.
   */
  public static void chillOut() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQGA");
    }
  }

  /**
   * Check if google play API is connected.
   */
  private static boolean isConnected() {
    return DevicePreferences.getGoogleApiClient() != null && DevicePreferences.getGoogleApiClient().isConnected();
  }

  /**
   * Complete the maze. Level 60.
   */
  public static void theMaze() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQHA");
    }
  }

  /**
   * Complete level 30.
   */
  public static void beginner() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQHQ");
    }
  }

  /**
   * Complete level 45.
   */
  public static void advanced() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQHg");
    }
  }

  /**
   * Complete level 60.
   */
  public static void expert() {
    if (isConnected()) {
      Games.Achievements.unlock(DevicePreferences.getGoogleApiClient(), "CgkIrtaC9IUREAIQHw");
    }
  }
}
