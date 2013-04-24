package com.metropolia.skydrive;

/**
 * @author Anoja
 * Date: 07.04.13
 * Time: 19:59
 */

import java.io.File;

import com.actionbarsherlock.view.MenuItem;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * This capture images using the inbuilt camera and then will store
 * to a location and also will be displayed in the same window.
 */

public class CameraCaptureActivity extends Activity
{
	
	private static final int TAKE_PICTURE_CODE = 100;
	
	private Bitmap cameraBitmap = null;
	ImageView imageiewImageCaptured;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_capture_main);
				
		((Button)findViewById(R.id.take_picture)).setOnClickListener(btnClick);
	}
	
	private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.take_picture: openCamera();   break;
                //case R.id.detect_face:  detectFaces();  break;  
            }
        }
	};
	
	private void openCamera(){
	    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	    Uri uriSavedImage=Uri.fromFile(new File("/sdcard/cameraCapturedImage1.jpg"));
	    intent.putExtra("camera_output", uriSavedImage); 
	    startActivityForResult(intent, TAKE_PICTURE_CODE);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        if(TAKE_PICTURE_CODE == requestCode){
                processCameraImage(data);
        }
    }
	
	private void processCameraImage(Intent intent){
		imageiewImageCaptured = (ImageView)findViewById(R.id.imagecaptured);     
	    
	    cameraBitmap = (Bitmap)intent.getExtras().get("data");
	     
	    imageiewImageCaptured.setImageBitmap(cameraBitmap);
	    
	    String filepath = Environment.getExternalStorageDirectory() + "/dsdcameracapture" + System.currentTimeMillis() + ".jpg";
	     
        try {
            FileOutputStream fos = new FileOutputStream(filepath);
             
            cameraBitmap.compress(CompressFormat.JPEG, 90, fos);
             
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}*/
	
	public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

}
