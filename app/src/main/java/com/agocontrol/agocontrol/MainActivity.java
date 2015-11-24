package com.agocontrol.agocontrol;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.agocontrol.agocontrol.nfc.NdefReaderTask;

import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends ListActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String MIME_TEXT_PLAIN = "text/plain";

    private NfcAdapter mNfcAdapter;

    AgoConnection mConnection;
    ArrayList<AgoDevice> deviceList;
    DeviceAdapter deviceAdapter;
    private ListView lv;

    private ImageView mVideoFrame;

    ProgressDialog progDlg;

    private static final int SHOW_PREFERENCES = 1;
    private String mAgoHostname;
    private String mAgoPort;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateFromPreferences();

        deviceList = new ArrayList<>();
        setContentView(R.layout.devices_list_view);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter != null) {

            if (!mNfcAdapter.isEnabled()) {
                // TODO: adapter is disabled
            } else {

                // we're good to go
            }
        } else {
            // TODO: nfc not available

        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_settings: {
                final Intent i;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    i = new Intent(this, PreferencesActivity.class);
                } else {
                    i = new Intent(this, FragmentPreferences.class);
                }
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
            }
            case R.id.reload: {
                new OpenConnection().execute();
                return true;
            }
        }
        return false;
    }

    private void updateFromPreferences() {
        final Context context = getApplicationContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mAgoHostname = prefs.getString(PreferencesActivity.PREF_AGOCONTROL_HOSTNAME, "");
        mAgoPort = prefs.getString(PreferencesActivity.PREF_AGOCONTROL_PORT, "8008");

    }

    public void onActivtyResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PREFERENCES) {
            updateFromPreferences();
        }

    }

    private class OpenConnection extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {
            if (TextUtils.isEmpty(mAgoHostname)) {
                Log.w(TAG, "Not executing command; empty hostname");

                return null;
            }

            Log.i(TAG, "Trying connection to " + mAgoHostname + ':' + mAgoPort);
            mConnection = new AgoConnection(mAgoHostname, mAgoPort);
            try {
                deviceList = mConnection.getDeviceList();
            } catch (final JSONRPCException | JSONException e) {
                Log.e(TAG, "Error making network request", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            final boolean hideUnknownDevices =
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                            .getBoolean(PreferencesActivity.PREF_HIDE_UNKNOWN_DEVICES, true);

            deviceAdapter = new DeviceAdapter(MainActivity.this, deviceList,
                    hideUnknownDevices ? new DeviceFilterHideUnknown() : null);
            setListAdapter(deviceAdapter);

            if (progDlg != null) {
                progDlg.dismiss();
            }
            progDlg = null;

            Log.i(TAG, deviceList.size() + " devices returned");
        }

        @Override
        protected void onPreExecute() {
            if (progDlg == null) {
                progDlg = ProgressDialog.show(MainActivity.this, null, getString(R.string.opening_connection), true, true);
            } else {
                progDlg.show();
            }
        }
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    public static void stopForegroundDispatch(final Activity activity, final NfcAdapter adapter) {
        if (adapter != null) {
            adapter.disableForegroundDispatch(activity);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            NdefReaderTask.setupForegroundDispatch(this, mNfcAdapter);
        }
        new OpenConnection().execute();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(final Intent intent) {
        final String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            final String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(mConnection).execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            final String[] techList = tag.getTechList();
            final String searchedTech = Ndef.class.getName();
            for (final String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(mConnection).execute(tag);
                    break;
                }
            }
        }
    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {

        final AgoDevice myDevice = deviceAdapter.getItem(position);
        Log.i(TAG, "clicked uuid " + myDevice.uuid);
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle(myDevice.getName());
        dialog.setContentView(R.layout.device_control_dlg);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        //show/hide views based on device type
        final LinearLayout llOnOff = (LinearLayout) dialog.findViewById(R.id.llOnOff);
        final LinearLayout llDrapes = (LinearLayout) dialog.findViewById(R.id.llDrapes);

        final LinearLayout llCamera = (LinearLayout) dialog.findViewById(R.id.llCamera);
        final FrameLayout flDimmer = (FrameLayout) dialog.findViewById(R.id.flDimmer);
        final SeekBar sbSetLevel = (SeekBar) dialog.findViewById(R.id.sbSetLevel);
        final Button btnGetVideoFrame = (Button) dialog.findViewById(R.id.btnGetVideoFrame);
        final Button btnRunScenario = (Button) dialog.findViewById(R.id.btnRunScenario);
        final Button btnOn = (Button) dialog.findViewById(R.id.btnOn);
        final Button btnOff = (Button) dialog.findViewById(R.id.btnOff);
        final Button btnOpen = (Button) dialog.findViewById(R.id.btnOpen);
        final Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
        final Button btnStop = (Button) dialog.findViewById(R.id.btnStop);
        final TextView tvLevel = (TextView) dialog.findViewById(R.id.tvLevel);
        mVideoFrame = (ImageView) dialog.findViewById(R.id.ivVideoFrame);

        if (myDevice.deviceType.equalsIgnoreCase("switch")) {
            llOnOff.setVisibility(View.VISIBLE);
            btnOn.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "on"));
            btnOff.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "off"));
        } else if (myDevice.deviceType.equalsIgnoreCase("drapes")) {
            llDrapes.setVisibility(View.VISIBLE);
            flDimmer.setVisibility(View.VISIBLE);

            btnOpen.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "on"));
            btnClose.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "off"));
            btnStop.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "stop"));

            sbSetLevel.setOnSeekBarChangeListener(new AgoDeviceSetLevelListener(myDevice));

        } else if (myDevice.deviceType.equalsIgnoreCase("dimmer")) {
            llOnOff.setVisibility(View.VISIBLE);
            flDimmer.setVisibility(View.VISIBLE);
            btnOn.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "on"));
            btnOff.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "off"));

            sbSetLevel.setOnSeekBarChangeListener(new AgoDeviceSetLevelListener(myDevice));

        } else if (myDevice.deviceType.equalsIgnoreCase("camera")) {
            llCamera.setVisibility(View.VISIBLE);
            btnGetVideoFrame.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    new getVideoFrame().execute(MainActivity.this, myDevice);
                }
            });
        } else if (myDevice.deviceType.equalsIgnoreCase("scenario")) {
            btnRunScenario.setVisibility(View.VISIBLE);
            btnRunScenario.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "on"));
        }


//         Button button_on = (Button)dialog.findViewById(R.id.button_on);
//         button_on.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "on"));
//         Button button_off = (Button)dialog.findViewById(R.id.button_off);
//         button_off.setOnClickListener(new AgoDeviceOnClickListener(myDevice, "off"));
        dialog.show();
    }

    private class getVideoFrame extends AsyncTask<Object, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(final Object... params) {
            final AgoWebcamFrameRetriever awfr = new AgoWebcamFrameRetriever((Context) params[0], (AgoDevice) params[1]);
            return awfr.getBitmap();
        }

        @Override
        protected void onPostExecute(final Bitmap result) {
            super.onPostExecute(result);
            if (progDlg != null) {
                progDlg.dismiss();
            }
            progDlg = null;
            mVideoFrame.setImageBitmap(result);
        }

        @Override
        protected void onPreExecute() {
            if (progDlg == null) {
                progDlg = ProgressDialog.show(MainActivity.this, null, getString(R.string.retrieving_video_frame), true, true);
            } else {
                progDlg.show();
            }
        }


    }

    private static class DeviceFilterHideUnknown implements DeviceAdapter.DeviceFilter {
        @Override
        public boolean isDeviceShown(final AgoDevice device) {
            return DeviceAdapter.ICON_MAPPING.containsKey(device.deviceType);
        }
    }
}
