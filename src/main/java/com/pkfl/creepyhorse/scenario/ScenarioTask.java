package com.pkfl.creepyhorse.scenario;

import java.util.Locale;

public enum ScenarioTask {
    NONE,
    INTRO_WAIT,
    INTRO_STALK,
    COOLDOWN,
    SADDLE_30,
    SADDLE_10,
    SADDLE_5,
    SADDLED_PAUSE,
    NECK_HOLD,
    STALK_TWO,
    FLIES,
    FOLLOW,
    ROAR_PAUSE,
    FEED,
    RUN_AWAY,
    ENTER,
    PENALTY_CHEAT,
    PENALTY_NOTICE,
    PENALTY_FINAL,
    PENALTY_SHORT,
    HIDE,
    SURVIVE,
    ENTER_CHASE,
    CHASE;


    public static ScenarioTask fromCommand(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "intro" -> INTRO_STALK;
            case "saddle30", "30", "saddle_30" -> SADDLE_30;
            case "saddle10", "10", "saddle_10" -> SADDLE_10;
            case "saddle5", "5", "saddle_5" -> SADDLE_5;
            case "neck" -> NECK_HOLD;
            case "stalk" -> STALK_TWO;
            case "flies", "swat" -> FLIES;
            case "follow" -> FOLLOW;
            case "feed" -> FEED;
            case "enter" -> ENTER;
            case "hide" -> HIDE;
            case "survive" -> SURVIVE;
            case "enterchase", "enter_chase" -> ENTER_CHASE;
            case "chase" -> CHASE;
            default -> NONE;
        };
    }
}

