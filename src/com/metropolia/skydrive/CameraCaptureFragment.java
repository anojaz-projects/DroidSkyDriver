package com.metropolia.skydrive;

/**
 * @author Anoja
 * Date: 27.04.13
 * Time: 19:59
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This class is a fragment class for the camera capture.
 */

public class CameraCaptureFragment extends Fragment
{		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_fragment, null);
        return v;
    }	
}
