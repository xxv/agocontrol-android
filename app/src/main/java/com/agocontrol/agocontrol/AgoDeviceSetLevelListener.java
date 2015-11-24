package com.agocontrol.agocontrol;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.SeekBar;

import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONException;

public class AgoDeviceSetLevelListener implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = AgoDeviceSetLevelListener.class.getSimpleName();

    private final AgoDevice myDevice;

    public AgoDeviceSetLevelListener(final AgoDevice _myDevice) {
        myDevice = _myDevice;
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress,
                                  final boolean fromUser) {
        // TODO update UI with actual value in seekbar
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
        // TODO make the value visible
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        // TODO make the value invisible
        new sendLevelAsync().execute(myDevice, seekBar.getProgress());


    }

    private class sendLevelAsync extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(final Object... params) {
            Log.i(TAG, "setting level async: " + params[1]);

            final AgoDevice dev = (AgoDevice) params[0];
            final String level = String.valueOf(params[1]);
            final AgoConnection connection = myDevice.getConnection();
            try {
                connection.setDeviceLevel(myDevice.getUuid(), level);
            } catch (final JSONRPCException | JSONException e) {
                Log.e(TAG, "Error making network request", e);
            }
            return null;
        }

    }

}
