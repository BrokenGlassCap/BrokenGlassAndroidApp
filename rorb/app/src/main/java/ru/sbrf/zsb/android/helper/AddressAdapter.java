package ru.sbrf.zsb.android.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ru.sbrf.zsb.android.rorb.Address;
import ru.sbrf.zsb.android.rorb.AddressList;
import ru.sbrf.zsb.android.rorb.R;
import ru.sbrf.zsb.android.rorb.RefObjectList;

/**
 * Created by Администратор on 15.07.2016.
 */
public class AddressAdapter extends ArrayAdapter<Address> {
    private final LayoutInflater inflater;
    private AddressList mAddressList;

    public AddressAdapter(Context context, int resource, AddressList objects) {
        super(context, resource, objects);
        mAddressList = objects;
        this.inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }


    public AddressList getAddressList() {
        return mAddressList;
    }

    @Override
    public int getCount() {
        return mAddressList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.address_item, null);
        }

        Address addr = mAddressList.get(position);
        TextView address = (TextView) view.findViewById(R.id.address_item_address);
        address.setText(addr.toString());
        TextView distance = (TextView) view.findViewById(R.id.address_item_distance);
        distance.setText(addr.getTextDistance());
        return view;
    }
}
