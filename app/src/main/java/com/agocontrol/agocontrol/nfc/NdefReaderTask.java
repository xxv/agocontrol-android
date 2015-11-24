package com.agocontrol.agocontrol.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.agocontrol.agocontrol.AgoConnection;
import com.agocontrol.agocontrol.MainActivity;

import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Reads an NDEF tag and sends a proximity event to AgoControl.
 */
public class NdefReaderTask extends AsyncTask<Tag, Void, String> {
    private static final String TAG = NdefReaderTask.class.getSimpleName();
    private final AgoConnection mConnection;

    public NdefReaderTask(@NonNull final AgoConnection connection) {
        mConnection = connection;
    }

    public static void setupForegroundDispatch(@NonNull final Activity activity,
                                               @NonNull final NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent =
                PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
        final IntentFilter[] filters = new IntentFilter[1];
        final String[][] techList = new String[][]{};
        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MainActivity.MIME_TEXT_PLAIN);
        } catch (final IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.", e);
        }
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    @Override
    protected String doInBackground(final Tag... params) {
        final Tag tag = params[0];
        final Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            // NDEF is not supported by this Tag.
            return null;
        }
        final NdefMessage ndefMessage = ndef.getCachedNdefMessage();
        final NdefRecord[] records = ndefMessage.getRecords();
        for (final NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
                    && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {

                    final String text = readText(ndefRecord);
                    mConnection.sendEvent("event.proximity.ndef", text);
                    return text;
                } catch (final UnsupportedEncodingException e) {
                    Log.e(TAG, "Unsupported Encoding", e);
                } catch (final JSONRPCException | JSONException e) {
                    Log.e(TAG, "Error making network request", e);
                }
            }
        }
        return null;
    }

    private String readText(final NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */
        final byte[] payload = record.getPayload();
        // Get the Text Encoding
        final String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        // Get the Language Code
        final int languageCodeLength = payload[0] & 0063;
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"
        // Get the Text
        return new String(payload,
                languageCodeLength + 1,
                payload.length - languageCodeLength - 1,
                textEncoding);
    }

    @Override
    protected void onPostExecute(final String result) {
        // Do nothing.
    }
}
