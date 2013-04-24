package com.metropolia.skydrive.dialogs;

/**
 * @author Anoja
 * Date: 04.04.13
 * Time: 9:12
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockActivity;
import com.metropolia.skydrive.R;

/**
 * This class opens-up the download dialog and handles the user actions.
 */

public class DownloadDialog extends SherlockActivity
{

    public static final String EXTRA_FILE_POSITION = "file_position";
    public static final int DOWNLOAD_REQUEST = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.download_dialog);
        ((Button) findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        ((Button) findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent data = new Intent();
                data.putExtra(EXTRA_FILE_POSITION, getIntent().getIntExtra(EXTRA_FILE_POSITION, 0));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }
}
