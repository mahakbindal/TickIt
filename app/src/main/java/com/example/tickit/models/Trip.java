package com.example.tickit.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Trip")
public class Trip extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_TITLE = "title";
    public static final String KEY_SAVE_COUNT = "saveCount";
    public static final String KEY_PRIVATE = "isPrivate";
    public static final String KEY_START_DATE = "startDate";
    public static final String KEY_END_DATE = "endDate";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public int getSaveCount() { return (int) getNumber(KEY_SAVE_COUNT); }

    public void setSaveCount(int saveCount) { put(KEY_SAVE_COUNT, saveCount); }

    public boolean getIsPrivate() { return getBoolean(KEY_PRIVATE); }

    public void setIsPrivate(boolean isPrivate) { put(KEY_PRIVATE, isPrivate); }

    public Date getStartDate() { return getDate(KEY_START_DATE); }

    public void setStartDate(Date startDate) { put(KEY_START_DATE, startDate); }

    public Date getEndDate() { return getDate(KEY_END_DATE); }

    public void setEndDate(Date endDate) { put(KEY_END_DATE, endDate); }
}
