package ru.sbrf.zsb.android.rorb;

/**
 * Created by Администратор on 15.07.2016.
 */
public class UserNotAuthException extends Exception {


    public UserNotAuthException() {
        super("User not authorized");
    }

    public UserNotAuthException(String s) {
        super(s);
    }

    public UserNotAuthException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UserNotAuthException(Throwable throwable) {
        super(throwable);
    }
}
