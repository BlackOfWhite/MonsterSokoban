package com.game.helpers;

import android.content.res.AssetManager;
import com.game.logger.Logger;
import com.game.model.Portal;
import com.game.model.Skull;
import com.game.preferences.GlobalPreferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.cocos2d.nodes.CCDirector;

/**
 * Class for parsing level data files. Creates instances of objects used later
 * in game.
 *
 * @author niewinskip
 */
public class LevelParser {

  /**
   * Hero starting position.
   */
  private int firstPosition;

  /**
   * Number of columns per board.
   */
  private int numberOfColumns;

  /**
   * Number of rows per board.
   */
  private int numberOfRows;

  /**
   * Minimum number of pushes to achieve an award.
   */
  private int minimumPushes;

  /**
   * Table containing level tiles.
   */
  private int[] gameBoard;

  /**
   * Array representing presence of the skulls on the board.
   */
  private boolean[] skullsTable;

  /**
   * List of skulls positions.
   */
  private final List<Skull> skullsList;

  /**
   * List of portals positions.
   */
  private List<Portal> portalsList;

  /**
   * Constructor.
   *
   * @param levelId Selected level id.
   */
  public LevelParser(final int levelId) {
    portalsList = new ArrayList<>();
    skullsList = new ArrayList<>();
    parseLevel(levelId);
  }

  /**
   * Parse selected level for core parameters.
   *
   * @param levelId Selected level id.
   */
  private void parseLevel(final int levelId) {
    BufferedReader br = null;
    try {
      AssetManager assetManager = CCDirector.sharedDirector().getActivity().getAssets();
      InputStream is = assetManager.open("Levels" + File.separator + "level" + levelId);
      br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      String line = br.readLine();
      while (line != null) {
        if (line.startsWith("columns")) {
          numberOfColumns = getValue(line);
        } else if (line.startsWith("rows")) {
          numberOfRows = getValue(line);
        } else if (line.startsWith("pushes")) {
          minimumPushes = getValue(line);
        } else if (line.startsWith("position")) {
          firstPosition = getValue(line);
        } else if (line.startsWith("teleports")) {
          portalsList = parsePortals(line);
        } else if (line.startsWith("skulls")) {
          skullsTable = new boolean[numberOfColumns * numberOfRows + 1];
          parseSkulls(line);
        } else if (line.startsWith("map")) {
          gameBoard = new int[numberOfRows * numberOfColumns + 1];
          line = br.readLine();
          int i = 0;
          while (line != null && i < numberOfRows) {
            Logger.log("Map line parsed " + (i + 1) + ": " + line);
            for (int x = 0; x < numberOfColumns; x++) {
              gameBoard[i * numberOfColumns + x] = mapCharToInt(line.charAt(x));
            }
            i++;
            line = br.readLine();
          }
        }
        line = br.readLine();
      }
    } catch (IOException e) {
      Logger.log("IOException. Parsing error. Level " + levelId + " datafile.");
      e.printStackTrace();
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        Logger.log("IOException. Cannot close file.");
        e.printStackTrace();
      }
    }
    Logger.log(numberOfColumns + " " + numberOfRows + " " + firstPosition + " " + minimumPushes);
  }

  /**
   * Parse line of text in search for specified value.
   *
   * @param line Line to parse.
   * @return Integer value.
   */
  private int getValue(final String line) {
    int start = line.lastIndexOf(':') + 1;
    String value = line.substring(start).replaceAll("\\s", "");
    Logger.log("Value parsed: " + value);
    return Integer.parseInt(value);
  }

  /**
   * Parse line of text in search for portals. Add each portal to list.
   *
   * @param line Line to parse.
   * @return List of portals.
   */
  private List<Portal> parsePortals(final String line) {
    List<Portal> portals = new ArrayList<>();
    int start = line.lastIndexOf(':') + 1;
    String value = line.substring(start).replaceAll("\\s", "");
    Logger.log("Portals parsed: " + value);
    String[] chunks = value.split(",");
    for (String v : chunks) {
      String[] inside = v.split("-");
      int src = Integer.parseInt(inside[0]);
      int dst = Integer.parseInt(inside[1]);
      portals.add(new Portal(src, dst));
    }
    return portals;
  }

  /**
   * Parse line of text in search for skulls. Add each skull to list and to
   * booleans table.
   *
   * @param line Line to parse.
   */
  private void parseSkulls(final String line) {
    int start = line.lastIndexOf(':') + 1;
    String value = line.substring(start).replaceAll("\\s", "");
    Logger.log("Skulls parsed: " + value);
    String[] chunks = value.split(",");
    for (String v : chunks) {
      int i = Integer.parseInt(v);
      skullsList.add(new Skull(i));
      skullsTable[i] = true;
    }
  }

  /**
   * Transform block's graphical representation to constant number.
   *
   * @param c Character used in map.
   * @return Integer representation of character.
   */
  private int mapCharToInt(final char c) {
    if (c == 'X') {
      return GlobalPreferences.BLOCK;
    } else if (c == '*') {
      return GlobalPreferences.FLOOR;
    } else if (c == 'T') {
      return GlobalPreferences.TELEP;
    } else if (c == 'D') {
      return GlobalPreferences.DESTY;
    } else if (c == 'S') {
      return GlobalPreferences.SLIDE;
    } else if (c == 'E') {
      return GlobalPreferences.EMPTY;
    }
    return GlobalPreferences.FLOOR;
  }

  /**
   * Get number of columns in game board.
   *
   * @return Number of columns.
   */
  public final int getNumberOfColumns() {
    return numberOfColumns;
  }

  /**
   * Get number of rows in game board.
   *
   * @return Number of rows.
   */
  public final int getNumberOfRows() {
    return numberOfRows;
  }

  /**
   * Get number of skulls required for level completion.
   *
   * @return Number of skulls to collect.
   */
  public final int getNumberOfSkullsToCollect() {
    return skullsList.size();
  }

  /**
   * Get minimum number of pushes.
   *
   * @return Minimum number of pushes.
   */
  public final int getMinimumPushes() {
    return minimumPushes;
  }

  /**
   * Get hero starting position.
   *
   * @return Hero's first position.
   */
  public final int getHeroFirstPosition() {
    return firstPosition;
  }

  /**
   * Get array representing game board.
   *
   * @return Game board.
   */
  public final int[] getGameBoard() {
    return gameBoard;
  }

  /**
   * Get array representing presence of skulls in game board.
   *
   * @return Skulls' board.
   */
  public final boolean[] getSkullsTable() {
    return skullsTable;
  }

  /**
   * Get list of skulls objects.
   *
   * @return List of skulls.
   */
  public final List<Skull> getSkullsList() {
    return skullsList;
  }

  /**
   * Get list of portals objects.
   *
   * @return List of portals.
   */
  public final List<Portal> getPortalsList() {
    return portalsList;
  }
}
