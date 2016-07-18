package ru.sbrf.zsb.android.netload;


import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ru.sbrf.zsb.android.exceptions.UserRegistrationException;
import ru.sbrf.zsb.android.helper.Utils;
import ru.sbrf.zsb.android.rorb.Address;
import ru.sbrf.zsb.android.rorb.AddressList;
import ru.sbrf.zsb.android.rorb.Claime;
import ru.sbrf.zsb.android.rorb.ClaimeStatus;
import ru.sbrf.zsb.android.rorb.ClaimeStatusList;
import ru.sbrf.zsb.android.rorb.DBHelper;
import ru.sbrf.zsb.android.rorb.MainActivity;
import ru.sbrf.zsb.android.rorb.MainActivity3;
import ru.sbrf.zsb.android.rorb.R;
import ru.sbrf.zsb.android.rorb.Service;
import ru.sbrf.zsb.android.rorb.ServiceList;
import ru.sbrf.zsb.android.rorb.User;
import ru.sbrf.zsb.android.rorb.UserRegistrationModel;

/**
 * Created by Администратор on 27.05.2016.
 */
public class NetFetcher {
    public static final String ENDPOINT_SERVER = "http://192.168.1.245:8050/";
    //public static final String ENDPOINT_SERVER = "http://217.116.48.210:8085/";
    public static final String ENDPOINT_API = ENDPOINT_SERVER + "api/";

    //public static final String ENDPOINT_API = "http://muzychenkoaa-001-site2.ftempurl.com/api/";
    public static final String SERVICES = "Services";
    public static final String SERVICE_STATUS = "ClaimStates";
    public static final String ADDRESSES = "Adresses";
    //public static final String TAG = "NetFetcher";
    public static final String CLAIMS = "Claims";
    public static final String USER_TOKEN_PARAM = "userEmail";
    private static final String LAST_UPDATE_PARAM = "lastUpdateDate";
    private static final String REGISTRATION = "Registration";
    private static final String OAUTH = "oauth";
    private static final String TOKEN = "token";
    private static final int TIMEOUT = 30000;

    private Context mContext;
    private String mLastError;
    public String getLastError() {
        return mLastError;
    }


    public NetFetcher(Context context) {
        mContext = context;
    }

    //Возвращает массив байт, принятых по сети
    byte[] getUrlBytes(String urlSpec) throws IOException {
        String token = User.getInstance(mContext).getFullToken();
        mLastError = null;
        URL url = new URL(urlSpec);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", token);
            connection.setConnectTimeout(TIMEOUT);
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = new BufferedInputStream(connection.getInputStream());
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(MainActivity3.TAG, "Ошибка соединения: код " + connection.getResponseCode());
                    return null;
                }
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            } finally {
                connection.disconnect();
            }
        }
     catch (SocketTimeoutException ex) {
        throw new SocketTimeoutException("Сервер не доступен, попробуте позже");
    }
        catch (Exception e) {
            Log.d(MainActivity3.TAG, e.getMessage());
            return null;
        }
    }

    //Возвращает запрос в виде строки
    public String getUrl(String urlSpec) throws IOException {
        byte[] arr = getUrlBytes(urlSpec);
        String res = "";
        if (arr != null) {
            res = new String(arr);
        }
        return res;
    }


    //Читаем заявки из сети
    public ArrayList<Claime> fetchClaims() throws JSONException {
        User user = User.getInstance(mContext);
        Log.d(MainActivity.TAG, "Считывание заявок по сети");
        ArrayList<Claime> result = new ArrayList<>();
        try {
            String url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                    .appendPath(NetFetcher.CLAIMS)
                    .appendQueryParameter(NetFetcher.USER_TOKEN_PARAM, user.getEmail())
                    .build().toString();
            String xmlString = getUrl(url);
            if (!Utils.isNullOrWhitespace(xmlString)) {
                JSONArray arr = (JSONArray) new JSONTokener(xmlString).nextValue();
                for (int i = 0; i < arr.length(); i++) {
                    result.add(new Claime(arr.getJSONObject(i), mContext));
                }
            }
            Log.d(MainActivity.TAG, "Считывание заявок по сети завершено");

        } catch (IOException ioe) {

            Log.e(MainActivity.TAG, "Ошибка при чтении заявок: " + ioe.getMessage(), ioe);
        }
        return result;
    }

    //Читаем заявки из сети
    public Claime fetchPhotos(Claime claime) throws JSONException {
        Log.d(MainActivity3.TAG, "Считывание фоток по сети");
        try {
            try {
                String url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                        .appendPath(NetFetcher.CLAIMS)
                        .appendPath(claime.getId())
                        .build().toString();
                String xmlString = getUrl(url);
                if (!Utils.isNullOrWhitespace(xmlString)) {
                    JSONObject obj = new JSONObject(xmlString);
                    Claime cl = new Claime(obj, mContext);
                    claime.getPhotoList().clear();
                    claime.getPhotoList().addAll(cl.getPhotoList());
                    Log.d(MainActivity3.TAG, "Считывание фоток по сети завершено, считано: " + claime.getPhotoList().size());
                    claime.updatePhotosInLocal();
                }

            } catch (IOException ioe) {

                Log.e(MainActivity3.TAG, "Ошибка при чтении фоток: " + ioe.getMessage(), ioe);
            }
        } catch (Exception e) {
            Log.e(MainActivity3.TAG, "Ошибка при чтении фоток: " + e.getMessage(), e);
        }
        return claime;
    }

    //Читаем адреса с сервера
    public AddressList fetchAddresses() throws JSONException, IOException {
        Log.d(MainActivity3.TAG, "Считывание адресов по сети");
        AddressList result = new AddressList(mContext);
        Date lastUpdate = getLastDate(DBHelper.ADDRESS_TBL);

        try {
            String url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                    .appendPath(NetFetcher.ADDRESSES)
                    .build().toString();

            if (lastUpdate != null) {
                url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                        .appendPath(NetFetcher.ADDRESSES)
                        .appendQueryParameter(NetFetcher.LAST_UPDATE_PARAM, Utils.getStringFromDateTime(lastUpdate))
                        .build().toString();
            }

            String xmlString = getUrl(url);
            if (!Utils.isNullOrWhitespace(xmlString)) {
                JSONArray arr = (JSONArray) new JSONTokener(xmlString).nextValue();
                for (int i = 0; i < arr.length(); i++) {
                    Address item = new Address(arr.getJSONObject(i));
                    result.add(item);
                }
            }
            Log.d(MainActivity3.TAG, "Считывание адресов по сети завершено");
        } catch (IOException ioe) {
            Log.e(MainActivity3.TAG, "Ошибка при чтении адресов: " + ioe.getMessage(), ioe);
            throw ioe;
        }
        return result;
    }

    ;

    public ClaimeStatusList fetchStatuses() throws JSONException {
        Log.d(MainActivity3.TAG, "Считывание статусов по сети");
        ClaimeStatusList result = new ClaimeStatusList(mContext);
        Date lastUpdate = getLastDate(DBHelper.STATUS_TBL);

        try {
            String url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                    .appendPath(NetFetcher.SERVICE_STATUS)
                    .build().toString();

            if (lastUpdate != null) {
                url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                        .appendPath(NetFetcher.SERVICE_STATUS)
                        .appendQueryParameter(NetFetcher.LAST_UPDATE_PARAM, Utils.getStringFromDateTime(lastUpdate))
                        .build().toString();
            }

            String xmlString = getUrl(url);
            if (!Utils.isNullOrWhitespace(xmlString)) {
                JSONArray arr = (JSONArray) new JSONTokener(xmlString).nextValue();
                for (int i = 0; i < arr.length(); i++) {
                    result.add(new ClaimeStatus(arr.getJSONObject(i)));

                }
            }
            Log.d(MainActivity3.TAG, "Считывание статусов по сети завершено!");

        } catch (IOException ioe) {
            Log.e(MainActivity3.TAG, "Ошибка при чтении статусов: " + ioe.getMessage(), ioe);
        }
        return result;
    }

    private Date getLastDate(String tbl) {
        DBHelper db = new DBHelper(mContext);
        Date lastUpdate = null;
        try {
            lastUpdate = db.getLastUpdateAt(tbl);
        } finally {
            db.close();
        }
        return lastUpdate;
    }

    public ServiceList fetchServices() throws JSONException {
        Log.d(MainActivity3.TAG, "Считывание сервисов по сети");
        ServiceList result = new ServiceList(mContext);
        Date lastUpdate = getLastDate(DBHelper.SERVICE_TBL);
        try {
            String url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                    .appendPath(NetFetcher.SERVICES)
                    .build().toString();

            if (lastUpdate != null) {
                url = Uri.parse(NetFetcher.ENDPOINT_API).buildUpon()
                        .appendPath(NetFetcher.SERVICES)
                        .appendQueryParameter(NetFetcher.LAST_UPDATE_PARAM, Utils.getStringFromDateTime(lastUpdate))
                        .build().toString();
            }

            String xmlString = getUrl(url);
            if (!Utils.isNullOrWhitespace(xmlString)) {
                JSONArray arr = (JSONArray) new JSONTokener(xmlString).nextValue();
                for (int i = 0; i < arr.length(); i++) {
                    result.add(new Service(arr.getJSONObject(i)));
                }
            }
            Log.d(MainActivity3.TAG, "Считывание сервисов по сети завершено");
        } catch (IOException ioe) {
            Log.e(MainActivity3.TAG, "Ошибка при чтении статусов: " + ioe.getMessage(), ioe);
        }
        return result;
    }

    public void userRegistration(UserRegistrationModel user) throws UserRegistrationException {
        HttpURLConnection connection = null;
        try {
            String url = Uri.parse(NetFetcher.ENDPOINT_SERVER).buildUpon()
                    .appendPath(NetFetcher.REGISTRATION)
                    .build().toString();
            connection = (HttpURLConnection) (new URL(url).openConnection());
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            JSONObject outObj = new JSONObject();
            outObj.put(User.EMAIL, user.getEmail());
            outObj.put(User.REGISTRATION_PARAM_PASSWORD, user.getPassword());
            outObj.put(User.REGISTRATION_PARAM_CONFIRM_PASS, user.getConfirmPassword());
            String testJson = outObj.toString();

            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            try {
                writer.write(outObj.toString());
            } finally {
                writer.close();
                outputStream.close();
            }

            //connection.getInputStream();
            int response = connection.getResponseCode();
            switch (response) {
                case HttpURLConnection.HTTP_CREATED:
                    ;
                    break;
                case HttpURLConnection.HTTP_CONFLICT:
                    throw new Exception("Учетная запись " + user.getEmail() + " уже зарегистрирована!");
                default:
                    throw new Exception("Регистрация не выполнена. На сервере произошила ошибка (код " + response + ")");
            }

        } catch (SocketTimeoutException ex) {
            throw new UserRegistrationException("Сервер не доступен, попробуте позже");
        }

        catch (Exception ex){
            Log.e("ERROR",ex.getMessage());
            throw new UserRegistrationException(ex.getMessage());
        }
        finally {
            if (connection != null){
                connection.disconnect();
            }
        }

    }

    //Получение нового токена
    public void updateToken(User user) throws Exception {
        if (Utils.isNullOrWhitespace(user.getEmail()) || Utils.isNullOrWhitespace(user.getPass()))
        {
            throw new IllegalArgumentException(mContext.getString(R.string.no_login_data));
        }
        String decodeUserPass = Utils.fromArrayToBase64((user.getEmail()+ ":" + user.getPass()).getBytes());
        HttpURLConnection connection = null;
        try{
            String url = Uri.parse(NetFetcher.ENDPOINT_SERVER).buildUpon()
                    .appendPath(NetFetcher.OAUTH)
                    .appendPath(NetFetcher.TOKEN)
                    .build().toString();
            connection = (HttpURLConnection) (new URL(url).openConnection());
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","text/html");
            connection.setRequestProperty("Accept", "application/json");

            connection.setRequestProperty("Authorization", "Basic " + decodeUserPass);
            connection.connect();


            OutputStream outputStream = connection.getOutputStream();

            String content = "grant_type=password&username=" + user.getEmail()+ "&password=" + user.getPass();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            try{
                writer.write(content);
            }
            finally {
                writer.close();
                outputStream.close();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            JSONObject tokenInfo = new JSONObject(sb.toString());
            user.setToken(Utils.fromJsonString(tokenInfo, User.ACCESS_TOKEN));
            user.setTokenType(Utils.fromJsonString(tokenInfo, User.TOKEN_TYPE));
            int expiredIn = tokenInfo.optInt(User.EXPIRES_IN);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, expiredIn);
            user.setExpireToken(calendar.getTime());
            user.saveLocal();

        }
        catch (SocketTimeoutException ex)
        {
            throw new SocketTimeoutException("Сервер не доступен, попробуте позже");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null){
                connection.disconnect();
            }
        }
    }

}
