package com.tomtom.deliveryroute;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static com.tomtom.deliveryroute.RouteService.ROUTESTOP;

/**
 * RouteLoader loads the latest .csv file on creation and then monitors the directory
 * for new files to be put.
 * <p>
 * TODO: make it support multiple paths
 */

public class RouteLoader {
    private static final String TAG = "RouteLoader";
    private static String PATH_ROOT = Environment.getExternalStorageDirectory().getPath();
    //private static String PATH_ROUTEDIR = PATH_ROOT + File.separator + "routes";
    private Context mContext;
    private Route mRoute;
    private File mLoadedFile;
    private Handler mHandler;
    private FileObserver mObserver;

    public RouteLoader(final Context context, final Route route) {
        mContext = context;
        mRoute = route;
        mHandler = new Handler(Looper.getMainLooper());

        loadLatestFile();

        mObserver = new FileObserver(PATH_ROOT) {
            @Override
            public void onEvent(final int event, String filename) {
                final File file = new File(PATH_ROOT + File.separator + filename);
                Log.d(TAG, "> onEvent (event=" + event + ")");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "in runnable belonging to onEvent");
                        if (event == FileObserver.CLOSE_WRITE && isCsvFile(file)) {
                            Log.d(TAG, "CLOSE_WRITE [" + PATH_ROOT + File.separator + file + "]");
                            load(file);
                        } else if (event == FileObserver.DELETE) {
                            if (mLoadedFile != null) {
                                try {
                                    if (file.getCanonicalPath().equals(mLoadedFile.getCanonicalPath())) {
                                        Log.d(TAG, "current route has been DELETED");
                                        mRoute.clear();
                                        loadLatestFile();
                                    } else {
                                        Log.d(TAG, "file deleted but this was not our loaded file (" + file.getCanonicalPath() + ")");
                                    }
                                } catch (final IOException ex) {
                                    Log.w(TAG, "IOException trying to compare " + file.getPath() + " and " + mLoadedFile.getPath());
                                }
                            }
                        }
                    }
                });
                Log.d(TAG, "< onEvent");
            }
        };
        mObserver.startWatching(); // START OBSERVING
    }

    private void loadLatestFile() {
        Log.d(TAG, "Loading latest file (most recently modified)");
        //File paths[] = new String()[];
        final File file = findLatestFile(PATH_ROOT);
        if (file != null) {
            load(file);
        }
    }

    private boolean isCsvFile(File file) {
        return (file.isFile() && (file.getName().endsWith(".csv") || file.getName().endsWith((".CSV"))));
    }

    private File findLatestFile(String path) {
        File latest = null;
        long latest_lastmodified = -1;

//        for (String path: paths) {
        File directory = new File(path);
        File[] files = directory.listFiles();

        Log.d("Files", "Size: " + files.length);
        for (File file : files) {
            if (isCsvFile(file)) {
                Log.d(TAG, "FileName:" + file.getName() + " lastmod:" + file.lastModified());
                if (file.lastModified() > latest_lastmodified) {
                    latest = file;
                    latest_lastmodified = file.lastModified();
                }
            }
        }
//        }

        if (latest == null) {
            Log.d(TAG, "no route file found");
        } else {
            Log.d(TAG, "Latest file (most recently modified) = [" + latest.getPath() + "]");
        }
        return latest;
    }

    private void load(final File file) {
        mRoute.loadFromPath(file.getPath());
        mLoadedFile = file;

        // Show toast message that we are loading the route from this file
        String toastText = mContext.getString(R.string.loading_route) + " " + file.getName();
        Toast toast = Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT);
        toast.show();

        // Go to first stop
        RouteStop stop = mRoute.nextStop();

        // Plan the route
        Intent intent = new Intent(mContext, RouteService.class);
        intent.putExtra(ROUTESTOP, stop);
        mContext.startService(intent);
    }
}
