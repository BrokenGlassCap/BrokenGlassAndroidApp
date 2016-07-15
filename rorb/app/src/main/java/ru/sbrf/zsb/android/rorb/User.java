package ru.sbrf.zsb.android.rorb;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.UserNotAuthenticatedException;

import java.util.Date;

import ru.sbrf.zsb.android.helper.Utils;

/**
 * Created by munk1 on 16.06.2016.
 */
public class User {

    private String mEmail;
    private String mFirstName;
    private String mLastName;
    private String mToken;
    private Date mExpireToken;
    private Date mLastLogin;
    private byte[] mAvatarImg;
    private Context mContext;
    private String mPass;   

    private String mTokenType;

    public static final String EMAIL = "Email";
    public static final String REGISTRATION_PARAM_PASSWORD = "Password";
    public static final String REGISTRATION_PARAM_CONFIRM_PASS = "ConfirmPassword";
    public static final String FIRST_NAME = "FirstName";
    public static final String LAST_NAME = "LastName";
    public static final String ACCESS_TOKEN = "access_token";
    private static final String PASSWORD = "Password";
    public static final String EXPIRES_IN = "expires_in";
    public static final String EXPIRE_TOKEN_DATE = "ExpireTokenDate";
    public static final String TOKEN_TYPE = "token_type";
    public static final String LOCAL_FILE = "settings";
    private static User curUser;

    public String getPass() {
        return mPass;
    }

    public void setPass(String pass) {
        mPass = pass;
    }

    public String getTokenType() {
        return mTokenType;
    }

    public void setTokenType(String tokenType) {
        mTokenType = tokenType;
    }


    public User(Context context) {
        mContext = context.getApplicationContext();
    }

    public static User getInstance(Context context)
    {
        if (curUser == null) {
            curUser = new User(context);
            ReadPrefs(curUser);
        }
        return curUser;
    }

    private static void ReadPrefs(User user) {
        if (user == null)
            return;
        SharedPreferences sp = user.mContext.getSharedPreferences(LOCAL_FILE, Context.MODE_PRIVATE);
        user.mEmail = sp.getString(EMAIL, "");
        user.mFirstName = sp.getString(FIRST_NAME, "");
        user.mLastName = sp.getString(LAST_NAME, "");
        user.mToken =sp.getString(ACCESS_TOKEN, "");
        user.mTokenType = sp.getString(TOKEN_TYPE, "");
        user.mPass = sp.getString(PASSWORD, "");
        user.mExpireToken = new Date(sp.getLong(EXPIRE_TOKEN_DATE, 0));
    }


    public User(String mEmail, String mFirstName, String mLastName) {
        this.mEmail = mEmail;
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getFio()
    {
        String result= (mFirstName + " " + mLastName).trim();
        return  result;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public byte[] getAvatarImg() {
        return mAvatarImg;
    }

    public void setAvatarImg(byte[] mAvatarImg) {
        this.mAvatarImg = mAvatarImg;
    }

    public Date getExpireToken() {
        return mExpireToken;
    }

    public void setExpireToken(Date mExpireToken) {
        this.mExpireToken = mExpireToken;
    }

    public Date getLastLogin() {
        return mLastLogin;
    }

    public void setLastLogin(Date mLastLogin) {
        this.mLastLogin = mLastLogin;
    }

    //Пользователь в системе, если имеется почта в настройках и токен не просрочен
    public void authorizedCheck() throws UserNotAuthException {
        if (Utils.isNullOrWhitespace(mEmail))
        {
            throw new UserNotAuthException("Необходимо зарегистрироватся или войти в систему");
        }
        if (isExpired())
        {
            throw new UserNotAuthException("Необходимо повторной войти в систему");
        }

    }


    public boolean isExpired()
    {
        return mExpireToken == null || mExpireToken.compareTo(new Date()) <= 0;
    }

    public String getFullToken()
    {
        return mTokenType + " " + mToken;
    }

    public void ClearLogon() {
        mEmail = "";
        mToken= "";
        mExpireToken = new Date();
        mLastLogin = null;
        mPass = "";
    }

    public void saveLocal() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(EMAIL, mEmail);
        editor.putString(LAST_NAME, mLastName);
        editor.putString(ACCESS_TOKEN, mToken);
        editor.putString(TOKEN_TYPE, mTokenType);
        editor.putString(PASSWORD, mPass);
        editor.putLong(EXPIRE_TOKEN_DATE, mExpireToken.getTime());

        editor.commit();
    }
}
