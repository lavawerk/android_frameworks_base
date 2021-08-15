package android.os;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.os.FileObserver;
import android.util.Log;

public class FileObserverRecursiv extends FileObserver {
    /** Only modification events */
    @NotifyEventType
    public static final int CHANGES_ONLY = CREATE | DELETE /*| CLOSE_WRITE | MOVE_SELF | MOVED_FROM | MOVED_TO*/;

    List<SingleFileObserver> mObservers;
    String mPath;
    int mMask;

    public FileObserverRecursiv(@NonNull String path) {
        super(path, CHANGES_ONLY/*ALL_EVENTS*/);
        mPath = path;
        mMask = CHANGES_ONLY/*ALL_EVENTS*/;
    }

    public FileObserverRecursiv(@NonNull String path, @NotifyEventType int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    @Override
    public void startWatching() {
        if (mObservers != null) return;

        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.isEmpty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (null == files) continue;

            for (File f : files)
            {
                if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                    stack.push(f.getPath());
                }
            }
        }

        for (SingleFileObserver sfo : mObservers) {
            sfo.startWatching();
        }
    }

    @Override
    public void stopWatching() {
        if (mObservers == null) return;

        for (SingleFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }
        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, @NonNull final String path) {
        switch (event)
        {
            case FileObserver.ACCESS:
                Log.i("FileObserverRecursiv", "ACCESS: " + path);
                break;
            case FileObserver.ATTRIB:
                Log.i("FileObserverRecursiv", "ATTRIB: " + path);
                break;
            case FileObserver.CLOSE_NOWRITE:
                Log.i("FileObserverRecursiv", "CLOSE_NOWRITE: " + path);
                break;
            case FileObserver.CLOSE_WRITE:
                Log.i("FileObserverRecursiv", "CLOSE_WRITE: " + path);
                break;
            case FileObserver.CREATE:
                Log.i("FileObserverRecursiv", "CREATE: " + path);
                break;
            case FileObserver.DELETE:
                Log.i("FileObserverRecursiv", "DELETE: " + path);
                break;
            case FileObserver.DELETE_SELF:
                Log.i("FileObserverRecursiv", "DELETE_SELF: " + path);
                break;
            case FileObserver.MODIFY:
                Log.i("FileObserverRecursiv", "MODIFY: " + path);
                break;
            case FileObserver.MOVE_SELF:
                Log.i("FileObserverRecursiv", "MOVE_SELF: " + path);
                break;
            case FileObserver.MOVED_FROM:
                Log.i("FileObserverRecursiv", "MOVED_FROM: " + path);
                break;
            case FileObserver.MOVED_TO:
                Log.i("FileObserverRecursiv", "MOVED_TO: " + path);
                break;
            case FileObserver.OPEN:
                Log.i("FileObserverRecursiv", "OPEN: " + path);
                break;
            default:
                Log.i("FileObserverRecursiv", "DEFAULT(" + event + "): " + path);
                break;
        }
    }

    /**
     * Monitor single directory and dispatch all events to its parent, with full path.
     * @author    uestc.Mobius <mobius@toraleap.com>
     * @version  2011.0121
     */
    class SingleFileObserver extends FileObserver {
        String mPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            FileObserverRecursiv.this.onEvent(event, newPath);
        }
    }
}
