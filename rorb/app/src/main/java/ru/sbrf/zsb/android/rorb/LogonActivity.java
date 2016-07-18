package ru.sbrf.zsb.android.rorb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.FileNotFoundException;

import ru.sbrf.zsb.android.helper.Utils;
import ru.sbrf.zsb.android.netload.NetFetcher;

public class LogonActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button mBtnOk;
    private TokenTask mTask;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logon);
        emailInput = (EditText) findViewById(R.id.txtEmail);
        passwordInput = (EditText) findViewById(R.id.txtPassword);
        User user = User.getInstance(this);
        emailInput.setText(user.getEmail());
        passwordInput.setText(user.getPass());

        mProgress = (ProgressBar) findViewById(R.id.logon_activity_progressBar);

        mBtnOk = (Button) findViewById(R.id.btnRegistrationSubmit);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = User.getInstance(LogonActivity.this);
                user.ClearLogon();
                user.setEmail(LogonActivity.this.emailInput.getText().toString());
                user.setPass(LogonActivity.this.passwordInput.getText().toString());
                LogonActivity.this.mTask = new TokenTask();
                LogonActivity.this.mTask.execute(user);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTask != null)
        {
            mTask.cancel(true);
            mTask = null;
        }
    }

    private class TokenTask extends AsyncTask<User, Void, String>{
        private User mUser;
        private String error;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LogonActivity.this.enableControls(false);
            LogonActivity.this.mProgress.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            LogonActivity.this.mProgress.setVisibility(View.INVISIBLE);
            if (!Utils.isNullOrWhitespace(s))
            {
                Toast.makeText(LogonActivity.this, s, Toast.LENGTH_LONG).show();
            }
            else
            {
                ClaimeList.get(LogonActivity.this).deleteLocalClames();
                LogonActivity.this.finish();
            }
        }

        @Override
        protected String  doInBackground(User... params) {
            mUser = params[0];
            NetFetcher nf = new NetFetcher(LogonActivity.this);
            try {
                nf.updateToken(mUser);
            }
            catch (FileNotFoundException ex)
            {
                error = "Некорректные данные подключения!";
                return error;
            }
            catch (Exception ex)
            {

                error = "Ошибка при входе на сервер!";
                return error;
            }
            return null;
        }
    }

    private void enableControls(boolean enable) {
        emailInput.setEnabled(enable);
        passwordInput.setEnabled(enable);
    }
}
