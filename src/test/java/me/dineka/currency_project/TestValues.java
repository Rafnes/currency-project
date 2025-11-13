package me.dineka.currency_project;

import java.util.UUID;

public class TestValues {
    public static final UUID VALID_ID_1 = UUID.fromString("cff434db-87f5-4044-b47f-cd56d02e3b78");
    public static final UUID VALID_ID_2 = UUID.fromString("b4faff68-089a-4307-9cc6-70331889dc5d");

    public static final String VALID_NAME_1 = "Тестовая валюта";
    public static final String VALID_NAME_2 = "Turkish Lira";

    public static final String VALID_CODE_1 = "TST";
    public static final String VALID_CODE_2 = "TRY";

    public static final int VALID_NOMINAL_1 = 1;
    public static final int VALID_NOMINAL_2 = 15;

    public static final double VALID_RATE_1 = 0.144;
    public static final double VALID_RATE_2 = 82.12;

    public static final String INVALID_NAME_1 = "111";
    public static final String INVALID_NAME_2 = "Валюта 200";
    public static final String INVALID_NAME_3 = " ";
    public static final String INVALID_NAME_4 = "$$$!";

    public static final String INVALID_CODE_1 = "TRKLR";
    public static final String INVALID_CODE_2 = "F F";
    public static final String INVALID_CODE_3 = "F-F";
    public static final String INVALID_CODE_4 = "SS";
    public static final String INVALID_CODE_5 = "VAL20";
    public static final String INVALID_CODE_6 = "va@";

    public static final int INVALID_NOMINAL_TOO_LOW = -24;
    public static final double INVALID_RATE_TOO_LOW = -77.244;
}
