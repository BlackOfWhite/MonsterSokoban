package com.game.preferences;

/**
 * Class containing references to static constants in Preferences.
 * 
 * @author Piotr Niewinski.
 */
public final class SharedPreferencesKeys {

	/**
	 * Constructor.
	 */
	private SharedPreferencesKeys() {

	}

	/**
	 * Key format: FirstPlay. Value format: true/false.
	 */
	public static final String FIRST_PLAY_KEY = "FIRST_PLAY_KEY";

	/**
	 * Key format: LevelInfo_21. Value format: integer. Example: 3.
	 */
	public static final String LEVEL_INFO_KEY = "LEVEL_INFO_KEY_";

	/*
	 * Key format: LevelUnlockedInfo_21. Value format: boolean. Example: false.
	 */
	public static final String LEVEL_UNLOCKED_INFO_KEY = "LEVEL_UNLOCKED_INFO_KEY_";

	/**
	 * Key format: "HeroStatus_(integer)". Value format: true/false. Example:
	 * "HeroStatus_1". Value true. Hero with index 1 was already bought. False -
	 * not bought. True - bought.
	 */
	public static final String HERO_STATUS = "HERO_STATUS_";

	/**
	 * Key format: "SelectedHero". Value format: integer. Selected hero will be
	 * fetched from hero list in Settings class.
	 */
	public static final String SELECTED_HERO = "SELECTED_HERO";

	/**
	 * Key format: Diamonds. Value format: integer.
	 */
	public static final String TOTAL_DIAMONDS_COLLECTED = "TOTAL_DIAMONDS_COLLECTED";

}
