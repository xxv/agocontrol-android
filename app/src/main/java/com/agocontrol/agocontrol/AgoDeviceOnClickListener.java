package com.agocontrol.agocontrol;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONException;


public class AgoDeviceOnClickListener implements View.OnClickListener {

    private static final String TAG = AgoDeviceOnClickListener.class.getSimpleName();

    private AgoDevice myDevice;
    private String command;

    public AgoDeviceOnClickListener(AgoDevice _myDevice, String _command) {
        this.myDevice = _myDevice;
        this.command = _command;

    }

    public void onClick(View v) {
        Log.i(TAG, "UUID: " + myDevice.getName() + "clicked");
        new sendCommandAsync().execute();
    }

    public AgoDevice getAgoDevice() {
        return myDevice;
    }

    private class sendCommandAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            AgoConnection connection = myDevice.getConnection();
            try {
                connection.sendCommand(myDevice.getUuid(), command);
            } catch (final JSONRPCException | JSONException e) {
                Log.e(TAG, "Error making network request", e);
            }
            return null;
        }

    }
}
