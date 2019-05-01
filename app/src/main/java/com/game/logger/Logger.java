package com.game.logger;

import com.game.preferences.GlobalPreferences;
import org.cocos2d.config.ccMacros;

/**
 * Logger class.
 *
 * @author niewinskip
 */
public final class Logger {

  /**
   * Default tag for logger.
   */
  private static final String LOG_TAG = "LOG_TAG";

  /**
   * Private constructor.
   */
  private Logger() {

  }

  /**
   * Make log with default log tag.
   *
   * @param s Log message.
   */
  public static void log(final String s) {
    if (GlobalPreferences.IS_LOGGER_ON) {
      log(LOG_TAG, s);
    }
  }

  /**
   * Make log with custom log tag.
   *
   * @param tag Log tag.
   * @param s Log message.
   */
  public static void log(final String tag, final String s) {
    if (GlobalPreferences.IS_LOGGER_ON) {
      ccMacros.CCLOG(tag, tag + ": " + s);
    }
  }
}
