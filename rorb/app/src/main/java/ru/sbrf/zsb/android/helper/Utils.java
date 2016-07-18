package ru.sbrf.zsb.android.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.tv.TvView;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Администратор on 27.05.2016.
 */
public class Utils {
    public static final String DB_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS";
    public static final String SQLITE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static LocationManager sLocationManager;
    private static LocationListener sListener;
    private static Location sCurrLocation;
    //private static Location sLastLocation;

    public static boolean regExMatches(String value, String regularExpression) {
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static String fromJsonString(JSONObject json, String tag) {
        String res = json.optString(tag);
        if (res.trim() == "null") {
            return null;
        }
        return res;
    }

    public static Date ConvertToDateWithFormat(String dateString, String format) {
        if (dateString == null || dateString.trim() == "")
            return null;
        Date convertedDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            //convertedDate = new Date();
            try {
                convertedDate = dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            return convertedDate;
        }
        return convertedDate;
    }

    public static Date ConvertToDate(String dateString) {
        return ConvertToDateWithFormat(dateString, DB_DATE_FORMAT);
    }

    public static Date ConvertToDateSQLITE(String dateString) {
        return ConvertToDateWithFormat(dateString, SQLITE_DATE_FORMAT);
    }

    public static String getStringFromDateTime(Date date) {
        if (date == null)
            return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getStringFromDateTime(Date date, String format) {
        if (date == null)
            return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getStringFromDateSQLITE(Date date) {
        return getStringFromDateTime(date, SQLITE_DATE_FORMAT);
    }


    /*public static Date getLocalDate(Date date)
    {
        if (date == null)
            return null;
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();// cal.getTimeZone();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Utils.DB_DATE_FORMAT);
        dateFormat.setTimeZone(tz);

        try {
            Date result = dateFormat.parse(Utils.getStringFromDateTime(date));
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }*/


    public static Date getLocalDate(Date date) {
        Date result = convertTimeZone(date, TimeZone.getTimeZone("UTC"), TimeZone.getDefault());
        return result;
    }


    //Конвертирует дату из одной временной зону в другую
    public static Date convertTimeZone(Date date, TimeZone fromTimeZone, TimeZone toTimeZone) {
        long fromTimeZoneOffset = getTimeZoneUTCAndDSTOffset(date, fromTimeZone);
        long toTimeZoneOffset = getTimeZoneUTCAndDSTOffset(date, toTimeZone);

        return new Date(date.getTime() + (toTimeZoneOffset - fromTimeZoneOffset));
    }

    private static long getTimeZoneUTCAndDSTOffset(Date date, TimeZone timeZone) {
        long timeZoneDSTOffset = 0;
        if (timeZone.inDaylightTime(date)) {
            timeZoneDSTOffset = timeZone.getDSTSavings();
        }

        return timeZone.getRawOffset() + timeZoneDSTOffset;
    }


    public static File getPhotoDirectory() {
        File result = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "brglass");
        if (!result.exists())
            result.mkdirs();
        return result;
    }



    // Check supported file extensions
    private boolean IsSupportedFile(String filePath) {
        String ext = filePath.substring((filePath.lastIndexOf(".") + 1),
                filePath.length());

        if (PhotoGridConstant.FILE_EXTN
                .contains(ext.toLowerCase(Locale.getDefault())))
            return true;
        else
            return false;

    }

    /*
     * getting screen width
     */
    public static int getScreenWidth(Context context) {
        int columnWidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }

    public static String fromArrayToBase64(byte[] data) {
        if (data == null)
            return null;
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static byte[] fromBase64ToArray(String string) {
        if (string == null)
            return null;
        return Base64.decode(string, Base64.DEFAULT);
    }

    public static byte[] resizeImage(String filename, int size) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        boolean isWidthMax = photoW >= photoH;

        int scaleFactor = 1;

        if (size > 0) {

            if (isWidthMax) {
                scaleFactor = photoW / size;
            } else {
                scaleFactor = photoH / size;
            }
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bm = BitmapFactory.decodeFile(filename, bmOptions);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        return stream.toByteArray();
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || s.length() == 0 || isWhitespace(s);

    }

    private static boolean isWhitespace(String s) {
        int length = s.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*public static LocationManager getLocationManager(final Context context) {
        if (sLocationManager == null) {
            sLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            sListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    sLastLocation = location;
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                    Toast.makeText(context, "Провайдер "+ provider + " доступен", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Toast.makeText(context, "Провайдер "+ provider + " недоступен", Toast.LENGTH_SHORT).show();
                }
            };

            if (!refreshLocation(context)) return null;
        }

        return sLocationManager;
    }
    */

    private static boolean refreshLocation(Context context) {
        if (sLocationManager == null)
            return false;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        sLocationManager.removeUpdates(sListener);
        sLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, sListener);
        return true;
    }


    /*public static String getLocationProvider(Context context) {
        LocationManager locationManager = getLocationManager(context);
        if (locationManager != null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            return locationManager.getBestProvider(criteria, true);
        }
        return null;
    }
    */

    public static float calcDistance(Location location, float endLatitude, float endLongitude) {
        if (location == null)
            return 0f;
        float[] dist = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), endLatitude, endLongitude, dist);
        return dist[0];
    }

    public static void setCurrLocation(Location currLocation) {
        sCurrLocation = currLocation;
    }

    public static Location getCurrLocation()
    {
        return sCurrLocation;

        //return sLastLocation;
    }

    /*
     * Resizing image size
     */
    public static Bitmap decodePhoto(byte[] file, int WIDTH, int HEIGHT)
    {
        if (file == null)
            return null;

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(new ByteArrayInputStream(file), null, o);


        final int REQUIRED_WIDTH = WIDTH;
        final int REQUIRED_HIGHT = HEIGHT;
        int scale = 1;
        while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
                && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
            scale *= 2;

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(new ByteArrayInputStream(file), null, o2);
    }

    public static Bitmap decodeFile(String filePath, int WIDTH, int HEIGHT) {
        try {

            File f = new File(filePath);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);


            final int REQUIRED_WIDTH = WIDTH;
            final int REQUIRED_HIGHT = HEIGHT;
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
                    && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
                scale *= 2;

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    //public static Location getCurrLocation(Context context) {
       /* LocationManager locationManager =  (LocationManager)context.getApplicationContext().getSystemService(context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }

        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
        */

    //}


    /*public static Location getCurrLocation(Context context) {
        LocationManager locationManager = getLocationManager(context);
        if (locationManager == null) {
            return null;
        }


        String provider = getLocationProvider(context);
        if (provider == null) {
            return null;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        refreshLocation(context);
        return locationManager.getLastKnownLocation(provider);
    }
    */


}
