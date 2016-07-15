package ru.sbrf.zsb.android.rorb;

import android.content.Context;
import android.util.Log;

import java.util.Currency;
import java.util.Date;

import ru.sbrf.zsb.android.exceptions.UserInsertDbException;
import ru.sbrf.zsb.android.exceptions.UserRegistrationException;
import ru.sbrf.zsb.android.netload.NetFetcher;

/**
 * Created by munk1 on 16.06.2016.
 */
public class UserContext {

    private static UserContext mInstance;
    private Context mContextApplication;

    public static UserContext getCurrentUserContext(Context ctx){
        if(mInstance == null){
            mInstance = new UserContext(ctx);
        }
        return mInstance;
    }

    public UserContext(Context ctx) {
        mContextApplication = ctx;
    }

    public void registerUser(UserRegistrationModel user) throws UserRegistrationException {
        NetFetcher httpClient = new NetFetcher(mContextApplication);
        try{
            httpClient.userRegistration(user);
        }
        catch (Exception ex){
            Log.e("ERROR",ex.getMessage());
            throw ex;
        }
    }


}
