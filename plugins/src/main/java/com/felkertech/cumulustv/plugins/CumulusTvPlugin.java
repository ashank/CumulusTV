package com.felkertech.cumulustv.plugins;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by guest1 on 8/7/2015.
 */
public abstract class CumulusTvPlugin extends AppCompatActivity {
    private static final String TAG = CumulusTvPlugin.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String INTENT_EDIT = "Edit";
    public static final String INTENT_ADD = "Add";
    public static final String INTENT_EXTRA_ACTION = "action";
    public static final String INTENT_EXTRA_READ_ALL = "Readall";
    public static final String INTENT_EXTRA_ALL_CHANNELS = "Allchannels";

    public static final String INTENT_EXTRA_JSON = "JSON";
    public static final String INTENT_EXTRA_ORIGINAL_JSON = "OGJSON";
    public static final String INTENT_EXTRA_ACTION_WRITE = "Write";
    public static final String INTENT_EXTRA_ACTION_DELETE = "Delete";
    public static final String INTENT_EXTRA_ACTION_DATABASE_WRITE = "SaveDatabase";
    public static final String INTENT_EXTRA_SOURCE = "Source";

    public static final String ACTION_ADD_CHANNEL = "com.felkertech.cumulustv.ADD_CHANNEL";
    public static final String ACTION_RECEIVER = "com.felkertech.cumulustv.RECEIVER";

    private static final String CLASS_NAME = "com.felkertech.n.cumulustv";
    private static final String DATA_RECEIVER = "com.felkertech.n.plugins.DataReceiver";

    private boolean isEdit = false;
    private boolean proprietary = true;
    private Intent telegram;
    private String label;

    /**
     * Starts the activity. You can override this to inflate a layout and setup anything else
     * you need
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        telegram = getIntent();
        if (telegram.hasExtra(INTENT_EXTRA_ACTION)) {
            isEdit = telegram.getStringExtra(INTENT_EXTRA_ACTION).equals(INTENT_EDIT);
        }
        if (DEBUG) {
            Log.d(TAG, "Initialized");
        }
    }

    /**
     * This label will be used to identify your plugin
     * @param l A simple user-facing String that tells the user the name of your plugin
     */
    public void setLabel(String l) {
        label = l;
    }

    /**
     * Determines whether you're adding or editing a channel
     * @return
     */
    public boolean areEditing() {
        return isEdit;
    }

    /**
     * Determines whether you are reading all user channels or have just specified a single one
     * @return
     */
    public boolean areReadingAll() {
        return telegram.getStringExtra(INTENT_EXTRA_ACTION).equals(INTENT_EXTRA_READ_ALL);
    }

    /**
     * Writes the data to the local file and then forces a data sync. Then the app closes.
     * @param jsonChannel The jsonchannel you wanted to create or update
     */
    public void saveChannel(CumulusChannel jsonChannel) {
        String jsonString = jsonChannel.toString();
        Intent i = new Intent();
        i.setClassName(CLASS_NAME, DATA_RECEIVER);
        i.setAction(ACTION_RECEIVER);
        i.putExtra(INTENT_EXTRA_JSON, jsonString);
        if (proprietary) {
            i.putExtra(INTENT_EXTRA_SOURCE, getApplicationInfo().packageName + "," +
                    getApplicationInfo().name);
        } else {
            i.putExtra(INTENT_EXTRA_SOURCE, "");
        }
        i.putExtra(INTENT_EXTRA_ACTION, INTENT_EXTRA_ACTION_WRITE);
        if (DEBUG) {
            Log.d(TAG, "   :");
            Log.d(TAG, jsonString);
            Log.d(TAG, "Saving changes");
        }
        sendBroadcast(i);
        finish();
    }

    /**
     * Replaces the original channel with a new channel.
     *
     * @param newChannel The new channel metadat
     * @param original The original channel metadata.
     */
    public void updateChannel(CumulusChannel newChannel, CumulusChannel original) {
        String jsonString = newChannel.toString();
        Intent i = new Intent();
        String ogString = original.toString();
        i.setClassName(CLASS_NAME, DATA_RECEIVER);
        i.setAction(ACTION_RECEIVER);
        i.putExtra(INTENT_EXTRA_JSON, jsonString);
        if (getChannel() != null) {
            i.putExtra(INTENT_EXTRA_ORIGINAL_JSON, ogString);
        }
        if (proprietary) {
            i.putExtra(INTENT_EXTRA_SOURCE, getApplicationInfo().packageName + "," +
                    getApplicationInfo().name);
        } else {
            i.putExtra(INTENT_EXTRA_SOURCE, "");
        }
        i.putExtra(INTENT_EXTRA_ACTION, INTENT_EXTRA_ACTION_WRITE);
        if (DEBUG) {
            Log.d(TAG, "Saving changes");
        }
        sendBroadcast(i);
        finish();
    }

    /**
     * Deletes the provided channel and resyncs. Then the app closes.
     * @param jsonChannel The jsonchannel to delete
     */
    public void deleteChannel(CumulusChannel jsonChannel) {
        String jsonString = jsonChannel.toString();
        Intent i = new Intent(ACTION_RECEIVER);
        i.putExtra(INTENT_EXTRA_JSON, jsonString);
        i.putExtra(INTENT_EXTRA_ACTION, INTENT_EXTRA_ACTION_DELETE);
        sendBroadcast(i);
        finish();
    }

    /**
     * You made special modifications to the database and you're requesting a sync
     */
    public void saveDatabase() {
        Intent i = new Intent(ACTION_RECEIVER);
        i.putExtra(INTENT_EXTRA_ACTION,
                INTENT_EXTRA_ACTION_DATABASE_WRITE);
        sendBroadcast(i);
        finish();
    }

    public CumulusChannel getChannel() {
        if(!telegram.hasExtra(INTENT_EXTRA_ACTION)) {
            return null;
        }
        if(telegram.getStringExtra(INTENT_EXTRA_ACTION).equals(INTENT_ADD)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(telegram.getStringExtra(INTENT_EXTRA_JSON));
            CumulusChannel channel = new CumulusChannel.Builder(jsonObject)
                    .build();
            if (DEBUG) {
                Log.d(TAG, channel.toString());
            }
            return channel;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * For certain plugins, you may want to lock editing from other apps so you don't disrupt
     * something important. For others, you don't care. This boolean enables or disables that
     * feature
     * @param enabled
     */
    public void setProprietaryEditing(boolean enabled) {
        proprietary = enabled;
    }

    public String getAllChannels() {
        if(telegram.hasExtra(INTENT_EXTRA_ALL_CHANNELS)) {
            return telegram.getStringExtra(INTENT_EXTRA_ALL_CHANNELS);
        } else {
            return "";
        }
    }
}
