package com.steveplays.stevesmanhuntminigame.util;

import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_DAY;

public class WeatherUtil {
	public static final int MIN_TIME_UNTIL_WEATHER_CHANGE_TICKS = (int) Math.round(0.5 * TICKS_PER_DAY);
	public static final int MAX_TIME_UNTIL_WEATHER_CHANGE_TICKS = (int) Math.round(7.5 * TICKS_PER_DAY);
}
