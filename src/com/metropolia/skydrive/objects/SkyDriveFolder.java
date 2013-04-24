package com.metropolia.skydrive.objects;

/**
 * @author Anoja
 * Date: 29.03.13
 * Time: 07:35
 */

import org.json.JSONObject;

/**
 * This class represents a sky-drive object i.e. Sky-drive folder.
 */

public class SkyDriveFolder extends SkyDriveObject
{
    public static final String TYPE = "folder";

    public SkyDriveFolder(JSONObject object)
    {
        super(object);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public int getCount()
    {
        return mObject.optInt("count");
    }
}
