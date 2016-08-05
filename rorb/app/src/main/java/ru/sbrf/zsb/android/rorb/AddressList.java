package ru.sbrf.zsb.android.rorb;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;

import ru.sbrf.zsb.android.helper.Utils;
import ru.sbrf.zsb.android.netload.NetFetcher;

public class AddressList extends RefObjectList<Address> {
    private static AddressList sAddressList;
    private Comparator<Address> mDistComparator;

    public AddressList(Context c) {
        super(c);
        mDistComparator = new Comparator<Address>() {
            @Override
            public int compare(Address lhs, Address rhs) {
                int res;
                if (lhs.getDistance() == rhs.getDistance()) {
                    res = 0;
                } else if (lhs.getDistance() > rhs.getDistance()) {
                    res = 1;
                } else {
                    res = -1;
                }

                if (res == 0) {
                    res = lhs.getCity().compareTo(rhs.getCity());
                }

                if (res == 0) {
                    res = lhs.getAddressName().compareTo(rhs.getAddressName());
                }

                return res;
            }
        };
    }


    public static void refresh()
    {
        if (sAddressList != null)
        {
            DBHelper db = new DBHelper(sAddressList.mContext);
            Log.d(MainActivity.TAG, "Запуск загрузки списка адресов из локальной бд");
            sAddressList = db.getAddressListFromDb();
        }
    }

    public AddressList getListWithLocation(boolean sort)
    {
        AddressList result = new AddressList(mContext);
        for (Address address : sAddressList
             ) {
            address.setDistance(Utils.calcDistance(Utils.getCurrLocation(), address.getLatitude(), address.getLongitude()));
            result.add(address);
        }

        if (sort)
        {
            Collections.sort(result, mDistComparator);
        }
        return result;
    }

    public static AddressList get(Context c){
        if (sAddressList == null){
            DBHelper db = new DBHelper(c);
            Log.d(MainActivity.TAG, "Запуск загрузки списка адресов из локальной бд");
            sAddressList = db.getAddressListFromDb();
        }
        return sAddressList;
    }

    @Override
    public void saveToDb()
    {
        DBHelper db = new DBHelper(mContext);
        db.UpdateAllAddresses(this);
    }

    @Override
    public void loadFromDb() {
        DBHelper db = new DBHelper(mContext);
        clear();
        addAll(db.getAddressListFromDb());
    }


    public Address getAddressByID(int id) {
        for (Address s : this) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public AddressList useFilter(String query) {
        AddressList result = new AddressList(mContext);
        if (Utils.isNullOrWhitespace(query)) {
            result.addAll(sAddressList);
            return result;
        }

        for (Address addr :
                sAddressList) {
            if (addr.toString().toUpperCase().contains(query))
            {
                result.add(addr);
                addr.recalcDistance();
            }
        }

        Collections.sort(result, mDistComparator);
        return result;
    }
}
