package com.agocontrol.agocontrol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DeviceAdapter extends BaseAdapter {

    private static final String TAG = DeviceAdapter.class.getSimpleName();

    protected final Context mContext;
    protected final ArrayList<AgoDevice> mDevices;
    protected final LayoutInflater mInflater;

    static final Map<String, Integer> ICON_MAPPING = new HashMap<>();

    static {
        ICON_MAPPING.put("switch", R.drawable.ic_on_off);
        ICON_MAPPING.put("dimmer", R.drawable.ic_dimmer);
        ICON_MAPPING.put("camera", R.drawable.ic_camera);
        ICON_MAPPING.put("zwavecontroller", R.drawable.ic_zwave_controller);
        ICON_MAPPING.put("controller", R.drawable.ic_zwave_controller);
        ICON_MAPPING.put("scenario", R.drawable.ic_launcher);
    }

    Comparator<AgoDevice> deviceComparator = new Comparator<AgoDevice>() {
        public int compare(final AgoDevice obj1, final AgoDevice obj2) {
            return obj1.getName().compareToIgnoreCase(obj2.getName());
        }
    };

    public DeviceAdapter(@NonNull final Context context,
                         @NonNull final ArrayList<AgoDevice> devices,
                         @Nullable final DeviceFilter filter) {
        mContext = context;
        final ArrayList<AgoDevice> filteredList = new ArrayList<>(devices.size());
        if (filter != null) {
           for (final AgoDevice device : devices) {
               if (filter.isDeviceShown(device)) {
                   filteredList.add(device);
               }
           }
            mDevices = filteredList;
        } else {
            mDevices = devices;
        }

        Collections.sort(mDevices, deviceComparator);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public AgoDevice getItem(final int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View v = convertView;
        if (convertView == null) {
            v = mInflater.inflate(R.layout.device_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.deviceName = (TextView) v.findViewById(R.id.tvDeviceName);
            holder.deviceType = (ImageView) v.findViewById(R.id.ivDeviceType);
            v.setTag(holder);
        }
        final AgoDevice device = (AgoDevice) getItem(position);
        final ViewHolder holder = (ViewHolder) v.getTag();
        holder.deviceName.setText(device.name);

        final String key = device.deviceType.toLowerCase();

        if (ICON_MAPPING.containsKey(key)) {
            holder.deviceType.setImageResource(ICON_MAPPING.get(key));
        } else {
            holder.deviceType.setImageResource(R.drawable.ic_unknown);
        }

        return v;
    }

    protected class ViewHolder {
        public TextView deviceName;
        public ImageView deviceType;
    }

    public interface DeviceFilter {
        boolean isDeviceShown(AgoDevice device);
    }
}
