package com.metropolia.skydrive.dialogs;

/**
 * @author Anoja
 * Date: 07.04.13
 * Time: 19:12
 */

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.metropolia.skydrive.BrowserForSkyDriveApplication;
import com.metropolia.skydrive.R;
import com.metropolia.skydrive.constants.Constants;
import com.metropolia.skydrive.util.JsonKeys;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class opens-up 'Create a new folder' dialog. 
 * Always creates in the current directory.
 */

public class NewFolderDialog extends SherlockActivity
{
    private LiveConnectClient mClient;
    private String mCurrentFolderId;
    private String LOGTAC;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_folder_dialog);
        setTitle(getString(R.string.newFolderTitle));

        mCurrentFolderId = getIntent().getStringExtra("metropolia.skydrive.CURRENT_FOLDER");

        BrowserForSkyDriveApplication app = (BrowserForSkyDriveApplication) getApplication();
        mClient = app.getConnectClient();
        LOGTAC = Constants.LOGTAG;


        final EditText name = (EditText) findViewById(R.id.nameEditText);
        final EditText description = (EditText) findViewById(R.id.descriptionEditText);

        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mClient == null)
                {
                    return;
                }

                Map<String, String> folder = new HashMap<String, String>();
                folder.put(JsonKeys.NAME, name.getText().toString());
                folder.put(JsonKeys.DESCRIPTION, description.getText().toString());

                mClient.postAsync(mCurrentFolderId,
                        new JSONObject(folder),
                        new LiveOperationListener()
                        {
                            @Override
                            public void onError(LiveOperationException exception, LiveOperation operation)
                            {

                                Toast.makeText(getApplicationContext(), R.string.errorFolderCouldNotBeCreated, Toast.LENGTH_SHORT).show();
                                Log.e(LOGTAC, exception.getMessage());
                            }

                            @Override
                            public void onComplete(LiveOperation operation)
                            {
                                JSONObject result = operation.getResult();
                                if (result.has(JsonKeys.ERROR))
                                {
                                    Toast.makeText(getApplicationContext(), R.string.errorFolderCouldNotBeCreated, Toast.LENGTH_SHORT).show();
                                } else
                                {
                                    ((BrowserForSkyDriveApplication) getApplication()).getCurrentBrowser().reloadFolder();
                                    finish();
                                }
                            }
                        });
            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
}
