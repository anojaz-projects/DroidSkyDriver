package com.metropolia.skydrive;

/**
 * @author Anoja
 * Date: 27.03.13
 * Time: 10:59
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.metropolia.skydrive.constants.Constants;
import com.metropolia.skydrive.util.IOUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * This class provides support for traversing through the sky-drive files.
 */
public class FileBrowserActivity extends SherlockListActivity
{
    private ArrayList<String> currentlySelectedFiles;

    public static final String EXTRA_FILES_LIST = "filePaths";

    private File currentFolder;
    private Stack<File> previousFolders;
    private FileBrowserListAdapter fileBrowserListAdapter;
    private ActionMode actionMode;
    private LruCache thumbCache;


    public void addBitmapToThumbCache(String key, Bitmap bitmap)
    {
        if (getBitmapFromThumbCache(key) == null)
        {
            thumbCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromThumbCache(String key)
    {
        return (Bitmap) thumbCache.get(key);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setTitle(getString(R.string.savedFilesTitle));
        setContentView(R.layout.saved_files_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
        final int cacheSize = 1024 * 1024 * memClass / 10;
        thumbCache = new LruCache(cacheSize);

        currentlySelectedFiles = new ArrayList<String>();
        previousFolders = new Stack<File>();
        fileBrowserListAdapter = new FileBrowserListAdapter(getApplicationContext());
        setListAdapter(fileBrowserListAdapter);

        File skyDriveFolder = new File(Environment.getExternalStorageDirectory() + "/SkyDrive/");
        if (!skyDriveFolder.exists())
        {
            skyDriveFolder.mkdirs();
        }

        currentFolder = skyDriveFolder;

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                File file = (File) parent.getItemAtPosition(position);
                if (actionMode == null)
                {
                    if (file.isDirectory())
                    {
                        previousFolders.push(currentFolder);
                        loadFolder(file);
                    } else
                    {
                        Intent data = new Intent();
                        ArrayList<String> filePath = new ArrayList<String>();
                        filePath.add(file.getPath());
                        data.putExtra(EXTRA_FILES_LIST, filePath);
                        setResult(Activity.RESULT_OK, data);
                        finish();
                    }
                } else
                {
                    boolean isChecked = fileBrowserListAdapter.isChecked(position);
                    if (isChecked)
                    {
                        fileBrowserListAdapter.setChecked(position, false);
                        currentlySelectedFiles.remove(
                                ((FileBrowserListAdapter) getListAdapter()).getItem(position).getPath());
                    } else
                    {
                        fileBrowserListAdapter.setChecked(position, true);
                        currentlySelectedFiles.add(
                                ((FileBrowserListAdapter) getListAdapter()).getItem(position).getPath());
                    }
                    updateActionModeTitleWithSelectedCount();
                }
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                if (actionMode == null)
                {
                    actionMode = startActionMode(new FileBrowserActionMode());
                    fileBrowserListAdapter.setChecked(position, true);
                    currentlySelectedFiles.add(
                            ((FileBrowserListAdapter) getListAdapter()).getItem(position).getPath());
                    updateActionModeTitleWithSelectedCount();
                }
                return true;
            }
        });

        if (savedInstanceState != null)
        {
            restoreInstanceState(savedInstanceState);
        }
    }


    private void restoreInstanceState(Bundle savedInstanceState)
    {
        assert savedInstanceState != null;

        if (savedInstanceState.containsKey(Constants.STATE_CURRENT_FOLDER))
        {
            currentFolder = new File(savedInstanceState.getString(Constants.STATE_CURRENT_FOLDER));
        }

        if (savedInstanceState.containsKey(Constants.STATE_PREVIOUS_FOLDERS))
        {
            previousFolders = new Stack<File>();
            String[] folderIds = savedInstanceState.getStringArray(Constants.STATE_PREVIOUS_FOLDERS);
            for (int i = 0; i < folderIds.length; i++)
            {
                previousFolders.push(new File(folderIds[i]));
            }
        }

        if (savedInstanceState.containsKey(Constants.STATE_ACTION_MODE_CURRENTLY_ON))
        {
            if (savedInstanceState.getBoolean(Constants.STATE_ACTION_MODE_CURRENTLY_ON))
            {
                actionMode = startActionMode(new FileBrowserActionMode());
            }
        }

        ((FileBrowserListAdapter) getListAdapter()).setCheckedPositions(((BrowserForSkyDriveApplication) getApplication())
                .getCurrentlyCheckedPositions());

        if (actionMode != null)
        {
            updateActionModeTitleWithSelectedCount();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(Constants.STATE_CURRENT_FOLDER, currentFolder.getPath());


        String[] previous = new String[previousFolders.size()];
        for (int i = 0; i < previous.length; i++)
        {
            previous[i] = previousFolders.get(i).getPath();
        }

        savedInstanceState.putStringArray(Constants.STATE_PREVIOUS_FOLDERS, previous);

        if (actionMode != null)
        {
            savedInstanceState.putBoolean(Constants.STATE_ACTION_MODE_CURRENTLY_ON, true);
        }

        ((BrowserForSkyDriveApplication) getApplication())
                .setCurrentlyCheckedPositions(
                        ((FileBrowserListAdapter) getListAdapter())
                                .getCheckedPositions());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && !previousFolders.isEmpty())
        {
            File folder = previousFolders.pop();
            loadFolder(folder);
            return true;
        } else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        loadFolder(currentFolder);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                navigateBack();
                return true;
            default:
                return false;
        }
    }

    private void navigateBack()
    {
        if (previousFolders.isEmpty())
        {
            finish();
            return;
        }
        loadFolder(previousFolders.pop());
    }

    private void loadFolder(File folder)
    {
        assert folder.isDirectory();
        currentFolder = folder;
        setSupportProgressBarIndeterminateVisibility(true);
        ArrayList<File> adapterFiles = fileBrowserListAdapter.getFiles();
        adapterFiles.clear();
        adapterFiles.addAll(Arrays.asList(folder.listFiles()));
        if (actionMode == null)
        {
            fileBrowserListAdapter.clearChecked();
        }
        fileBrowserListAdapter.notifyDataSetChanged();

        SparseBooleanArray checkedPositions = fileBrowserListAdapter.getCheckedPositions();
        for (int i = 0; i < checkedPositions.size(); i++)
        {
            int adapterPosition = checkedPositions.keyAt(i);
            try
            {
                File fileSelected = fileBrowserListAdapter.getItem(adapterPosition);
                currentlySelectedFiles.add(fileSelected.getPath());
            } catch (IndexOutOfBoundsException e)
            {
                break;
            }
        }
        setSupportProgressBarIndeterminateVisibility(false);
    }

    private int determineFileDrawable(File file)
    {
        if (file.isDirectory())
        {
            return R.drawable.folder;
        }
        String fileType = IOUtil.getFileExtension(file);
        return IOUtil.determineFileTypeDrawable(fileType);
    }

    private class FileBrowserListAdapter extends BaseAdapter
    {
        private final LayoutInflater mInflater;
        private final ArrayList<File> mFiles;
        private View mView;
        private SparseBooleanArray mCheckedPositions;
        private int mChecked;

        public FileBrowserListAdapter(Context context)
        {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mFiles = new ArrayList<File>();
            mCheckedPositions = new SparseBooleanArray();
            mChecked = 0;
        }


        public boolean isChecked(int pos)
        {
            return mCheckedPositions.get(pos, false);
        }

        public int getCheckedCount()
        {
            return this.mChecked;
        }

        public void setCheckedPositions(SparseBooleanArray checkedPositions)
        {
            mChecked = checkedPositions.size();
            this.mCheckedPositions = checkedPositions;
            notifyDataSetChanged();
        }

        public SparseBooleanArray getCheckedPositions()
        {
            return this.mCheckedPositions;
        }

        public void setChecked(int pos, boolean checked)
        {
            if (checked && !isChecked(pos))
            {
                mChecked++;
            } else if (isChecked(pos) && !checked)
            {
                mChecked--;
            }

            mCheckedPositions.put(pos, checked);
            notifyDataSetChanged();
        }

        public void clearChecked()
        {
            mChecked = 0;
            mCheckedPositions = new SparseBooleanArray();
            currentlySelectedFiles.clear();
            notifyDataSetChanged();
        }

        public void checkAll()
        {
            for (int i = 0; i < mFiles.size(); i++)
            {
                if (!isChecked(i))
                {
                    mChecked++;
                }

                mCheckedPositions.put(i, true);
                currentlySelectedFiles.add(mFiles.get(i).getPath());
            }
            notifyDataSetChanged();
        }

        public ArrayList<File> getFiles()
        {
            return mFiles;
        }

        @Override
        public int getCount()
        {
            return mFiles.size();
        }

        @Override
        public File getItem(int position)
        {
            return mFiles.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            mView = convertView != null ? convertView :
                    mInflater.inflate(R.layout.skydrive_list_item,
                            parent, false);
            TextView name = (TextView) mView.findViewById(R.id.nameTextView);
            ImageView type = (ImageView) mView.findViewById(R.id.skyDriveItemIcon);

            final WeakReference viewReference = new WeakReference(convertView);

            File file = getItem(position);
            name.setText(file.getName());

            int fileDrawable = determineFileDrawable(file);
            type.setImageResource(fileDrawable);

            if (fileDrawable == R.drawable.image_x_generic)
            {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (!preferences.getBoolean(Constants.THUMBNAILS_DISABLED, false))
                {

                    final Bitmap bitmap = getBitmapFromThumbCache(file.getName());
                    AsyncTask getThumb = new AsyncTask<File, Void, Bitmap>()
                    {
                        @Override
                        protected Bitmap doInBackground(File... files)
                        {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(files[0].getPath(), options);

                            int sampleSize = (options.outHeight > options.outWidth ? options.outHeight / 60 : options.outWidth / 60);

                            options = new BitmapFactory.Options();
                            options.inSampleSize = sampleSize;
                            options.inScaled = false;

                            Bitmap thumb = BitmapFactory.decodeFile(files[0].getPath(), options);

                            cacheThumb(files[0], thumb);
                            return thumb;
                        }

                        protected void onPostExecute(Bitmap thumb)
                        {
                            if (viewReference != null && viewReference.get() != null)
                            {
                                ((ImageView)
                                        ((View) viewReference.get())
                                                .findViewById(R.id.skyDriveItemIcon))
                                        .setImageBitmap(thumb);
                            }
                        }
                    };

                    File thumbCache = cacheOfThumbExists(file);

                    if (bitmap != null)
                    {
                        if (viewReference != null  && viewReference.get() != null)
                        {
                            ((ImageView)
                                    ((View) viewReference.get())
                                            .findViewById(R.id.skyDriveItemIcon))
                                    .setImageBitmap(bitmap);
                        }
                    }
                    else if(thumbCache != null)
                    {
                        if(viewReference != null
                                && viewReference.get() != null)
                        {
                            getThumb.execute(new File[]{thumbCache});
                        }
                    }
                    else
                    {
                        if(viewReference != null
                                && viewReference.get() != null)
                        {
                            getThumb.execute(new File[]{file});
                        }
                    }
                }
            }
            setChecked(isChecked(position));
            return mView;
        }

        private File cacheOfThumbExists(File file)
        {
            File cacheFolder = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/com.metropolia.skydrive/thumbs/");
            if (!cacheFolder.exists())
            {
                cacheFolder.mkdirs();
                return null;
            }

            File thumbFile = new File(cacheFolder, file.getName());
            if (thumbFile.exists())
            {
                return thumbFile;
            } else
            {
                return null;
            }
        }

        private void cacheThumb(File thumbCacheFile, Bitmap thumb)
        {
            File cacheFolder = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/com.metropolia.skydrive/thumbs/");
            if (!cacheFolder.exists())
            {
                cacheFolder.mkdirs();
            }

            OutputStream out;
            try
            {
                out = new BufferedOutputStream(new FileOutputStream(new File(cacheFolder, thumbCacheFile.getName())));
                thumb.compress(Bitmap.CompressFormat.PNG, 85, out);
                out.flush();
                out.close();
                Log.i(Constants.LOGTAG, "Thumb cached for image " + thumbCacheFile.getName());
            } catch (Exception e)
            {
                Log.e(Constants.LOGTAG, "Could not cache thumbnail for " + thumbCacheFile.getName()
                        + ". " + e.toString());
            }
        }


        private void setChecked(boolean checked)
        {
            if (checked)
            {
                mView.setBackgroundResource(R.color.HightlightBlue);
            } else
            {
                mView.setBackgroundResource(android.R.color.white);
            }
        }
    }


    private class FileBrowserActionMode implements com.actionbarsherlock.view.ActionMode.Callback
    {

        @Override
        public boolean onCreateActionMode(com.actionbarsherlock.view.ActionMode mode, Menu menu)
        {
            menu.add(getString(R.string.delete))
                    .setIcon(R.drawable.ic_menu_delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                            MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            menu.add(getString(R.string.selectAll))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(com.actionbarsherlock.view.ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final com.actionbarsherlock.view.ActionMode mode, MenuItem item)
        {
            String title = item.getTitle().toString();
            if (title.equalsIgnoreCase(getString(R.string.selectAll)))
            {
                fileBrowserListAdapter.checkAll();
                item.setTitle(getString(R.string.selectNone));
                updateActionModeTitleWithSelectedCount();
                return true;
            } else if (title.equalsIgnoreCase(getString(R.string.selectNone)))
            {
                fileBrowserListAdapter.clearChecked();
                item.setTitle(getString(R.string.selectAll));
                updateActionModeTitleWithSelectedCount();
                return true;
            } else if (title.equalsIgnoreCase(getString(R.string.delete)))
            {
                final AlertDialog dialog = new AlertDialog.Builder(getSupportActionBar().getThemedContext()).create();
                dialog.setTitle(getString(R.string.deleteConfirmationTitle));
                dialog.setIcon(R.drawable.warning_triangle);
                StringBuilder deleteMessage = new StringBuilder();
                deleteMessage.append(getString(R.string.deleteConfirmationBody));
                for (int i = 0; i < currentlySelectedFiles.size(); i++)
                {
                    int index = currentlySelectedFiles.get(i).lastIndexOf("/");
                    if (index != -1)
                    {
                        deleteMessage.append(currentlySelectedFiles.get(i)
                                .substring(index + 1));
                        deleteMessage.append("\n");
                    }
                }
                deleteMessage.append(getString(R.string.deleteConfirmationQuestion));

                dialog.setMessage(deleteMessage.toString());
                dialog.setButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        ArrayList<String> files = currentlySelectedFiles;
                        for (int j = 0; j < files.size(); j++)
                        {
                            File file = new File(files.get(j));
                            if (file.exists())
                            {
                                if (file.isDirectory())
                                {
                                    File[] directoryFiles = file.listFiles();
                                    for (int k = 0; k < directoryFiles.length; k++)
                                    {
                                        files.add(directoryFiles[k].getPath());
                                    }
                                } else
                                {
                                    file.delete();
                                }
                            }
                        }

                        /* Second round to remove empty folders */
                        for (int j = files.size() - 1; j >= 0; j--)
                        {
                            File file = new File(files.get(j));
                            file.delete();
                        }

                        ((FileBrowserListAdapter) getListAdapter()).clearChecked();
                        loadFolder(currentFolder);
                        mode.finish();
                    }
                });
                dialog.setButton2(getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            } else
            {
                return false;
            }

        }


        @Override
        public void onDestroyActionMode(com.actionbarsherlock.view.ActionMode mode)
        {
            actionMode = null;
            ((FileBrowserListAdapter) getListAdapter()).clearChecked();
            ((FileBrowserListAdapter) getListAdapter()).notifyDataSetChanged();
            supportInvalidateOptionsMenu();
        }
    }

    private void updateActionModeTitleWithSelectedCount()
    {
        final int checkedCount = ((FileBrowserListAdapter) getListAdapter()).getCheckedCount();
        switch (checkedCount)
        {
            case 0:
                actionMode.setTitle(null);
                break;
            case 1:
                actionMode.setTitle(getString(R.string.selectedOne));
                break;
            default:
                actionMode.setTitle("" + checkedCount + " " + getString(R.string.selectedSeveral));
                break;
        }
    }
}
