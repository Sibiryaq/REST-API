package ru.aston.testData;

public class TestConstants {
    public static final long FIRST_ID = 1;
    public static final String FIRST_USER_NAME = "Masha";
    public static final String FIRST_USER_EMAIL = "email@mail.ru";

    public static final long SECOND_ID = 2;
    public static final String SECOND_USER_NAME = "Katya";
    public static final String SECOND_USER_EMAIL = "ka@mail.ru";

    public static final long THIRD_ID = 3;
    public static final String THIRD_USER_NAME = "Olya";
    public static final String THIRD_USER_EMAIL = "olya@mail.ru";

    public static final String FIRST_EVENT_TITLE = "Going to the cinema";
    public static final String FIRST_EVENT_DESCRIPTION = "The film name is Terminator";

    public static final String SECOND_EVENT_TITLE = "Going to the park";

    public static final String SECOND_EVENT_DESCRIPTION = "The park is located next to the square";
    public static final String CONTACT_PHONE = "89876543210";
    public static final String CONTACT_ADDRESS = "Russia, Moscow, Rubanenko Street 1";


    public static final String UPDATED_USER_NAME = "Ivan";

    public static final String UPDATED_USER_EMAIL = "ivan@mail.ru";
    public static final String UPDATED_EVENT_TITLE = "Lost";
    public static final String UPDATED_CONTACT_ADDRESS = "Russia, Kazan, Rubanenko Street 15";

    public static final String USER_JSON = "{\n" +
            "    \"name\": \"Alina123\",\n" +
            "    \"email\": \"mail@mail.ru\"\n" +
            "}";

    public static final String INCORRECT_JSON = "{\n" +
            "     field : value,\n" +
            "     field : value\n" +
            "}";

    public static final String EVENT_JSON = "{\n" +
            "    \"title\": \"Event\",\n" +
            "    \"description\": \"description2\"\n" +
            "}";

    public static final String CONTACT_JSON = "{\n" +
            "    \"phone\": \"89003271434\",\n" +
            "    \"address\": \"my new address\"\n" +
            "}";

    public static final String CONTACT_PATH = "event/" + FIRST_ID + "/contact";
    public static final String PARTICIPANT_PATH = "event/" + FIRST_ID + "/participants";
    public static final String INCORRECT_PATH = "incorrect";
}
