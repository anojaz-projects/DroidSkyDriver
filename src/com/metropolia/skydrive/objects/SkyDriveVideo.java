package com.metropolia.skydrive.objects;

/**
 * @author Anoja
 * Date: 07.04.13
 * Time: 18:15
 */

import org.json.JSONObject;

/**
 * This class represents a sky-drive object i.e. Sky-drive video.
 */

public class SkyDriveVideo extends SkyDriveObject
{

    public static final String TYPE = "video";

    public SkyDriveVideo(JSONObject object)
    {
        super(object);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public long getSize()
    {
        return mObject.optLong("size");
    }

    public int getCommentsCount()
    {
        return mObject.optInt("comments_count");
    }

    public boolean getCommentsEnabled()
    {
        return mObject.optBoolean("comments_enabled");
    }

    public String getSource()
    {
        return mObject.optString("source");
    }

    public int getTagsCount()
    {
        return mObject.optInt("tags_count");
    }

    public boolean getTagsEnabled()
    {
        return mObject.optBoolean("tags_enabled");
    }

    public String getPicture()
    {
        return mObject.optString("picture");
    }

    public int getHeight()
    {
        return mObject.optInt("height");
    }

    public int getWidth()
    {
        return mObject.optInt("width");
    }

    public int getDuration()
    {
        return mObject.optInt("duration");
    }

    public int getBitrate()
    {
        return mObject.optInt("bitrate");
    }
}
