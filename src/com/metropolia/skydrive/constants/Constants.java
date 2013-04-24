package com.metropolia.skydrive.constants;

import com.metropolia.skydrive.util.Scopes;

/**
 * @author Anoja
 * Date: 27.02.13
 * Time: 14:03
 */
final public class Constants
{
	public static final String APP_CLIENT_ID = "00000000480D5D82";

    /* App permissions. Be as precise as possible. */
    public static final String[] APP_SCOPES = {
            Scopes.OFFLINE_ACCESS,
            Scopes.CONTACTS_SKYDRIVE,
            Scopes.SKYDRIVE_UPDATE
    };


    public static final String LOGTAG = "ASE";


    /* Actions */
    public static final String ACTION_CANCEL_DOWN = "com.metropolia.skydrive.CANCEL_DOWN";
    public static final String ACTION_CANCEL_UP = "com.metropolia.skydrive.CANCEL_UP";
    public static final String ACTION_SONG_CHANGE = "com.metropolia.skydrive.SONG_CHANGE";

    /* Extras */
    public static final String EXTRA_SONG_TITLE = "songTitle";

    /* Saved state */
    public static final String STATE_CURRENT_FOLDER = "currentFolderState";
    public static final String STATE_CURRENT_HIERARCHY = "currentFolderHierarchyState";
    public static final String STATE_PREVIOUS_FOLDERS = "previousFolderIdsState";
    public static final String STATE_CURRENTLY_SELECTED = "currentlySelectedFiles";
    public static final String STATE_ACTION_MODE_CURRENTLY_ON = "actionModeOn";

    /* Bytes */
    public static final long THUMBS_MAX_SIZE = 10485760; //10MB
    public static final long CACHE_MAX_SIZE = 104857600; //100MB

    /* Settings */
    public static final String CONFIRM_EXIT = "confirm_exit";
    public static final String THUMBNAILS_DISABLED = "thumbnails_disabled";


    private Constants()
    {
        throw new AssertionError("Unable to create Constants object.");
    }
}
