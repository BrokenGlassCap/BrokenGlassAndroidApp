package ru.sbrf.zsb.android.rorb;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.logging.Logger;

import ru.sbrf.zsb.android.exceptions.UserInsertDbException;

/**
 * Created by munk1 on 16.06.2016.
 */
public class UserList extends RefObjectList<User> {

    private DBHelper mDb;

    public UserList(Context c) {
        super(c);
        mDb = new DBHelper(c);
    }

    public static UserList get(Context c){
        UserList list = new UserList(c);
        try{
            list.loadFromDb();
        }
        catch(Exception ex){
            Log.e("ERROR",ex.getMessage());
        }
        return list;
    }

    @Override
    public void saveToDb() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFromDb() {
      //  addAll(mDb.getUserListFromDb());
    }


    /*public User insertUserDb(User user) throws UserInsertDbException {
        return mDb.insertUserIntoDb(user);
    }


    public void deleteAllUsers(){
        mDb.deleteAllUsersFromDB();
    }
    */
}
