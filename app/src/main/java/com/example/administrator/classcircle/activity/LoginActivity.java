package com.example.administrator.classcircle.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.classcircle.R;
import com.example.administrator.classcircle.li.*;
import com.example.administrator.classcircle.utils.ThreadUtils;
import com.example.administrator.classcircle.entity.User;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.security.spec.PSSParameterSpec;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private ImageView mImageView;
    private EditText mUsername;
    private EditText mPassword;
    private Button mButton;
    private String mLoginPassword;
    private TextView mTvHead;
    private RelativeLayout mRelativeLayout;
    private boolean isLoginFailed = false;

    private Message msg = new Message();
    private long mExitTime;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {
//        mTvHead = (TextView) findViewById(R.id.id_header_tv);
//        mTvHead.setText("班圈");
//        mRelativeLayout = (RelativeLayout) findViewById(R.id.recyclerView);
//        mRelativeLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mImageView = (ImageView) findViewById(R.id.id_login_img);
        mUsername = (EditText) findViewById(R.id.id_login_username);
        mPassword = (EditText) findViewById(R.id.id_login_password);
        mButton = (Button) findViewById(R.id.id_login_btnLogin);
        EMClient.getInstance().getCurrentUser();

    }

    @Override
    protected void initListener() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SplashActivity.mLoginUserName = mUsername.getText().toString().trim();
                mLoginPassword = mPassword.getText().toString().trim();

                if (SplashActivity.mLoginUserName.length() != 0  ) {
                    if (mLoginPassword.length() == 0){
                        return;
                    }else {
                        hideKeyBoard();
                        showProgress("正在登录...");
                        checkTimeOut();

                        ThreadUtils.runOnBackgroundThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: --------------------登录用户名密码" + SplashActivity.mLoginUserName + "+++" + mLoginPassword);
                                getDataFromBmob(SplashActivity.mLoginUserName, mLoginPassword);
                            }
                        });
                    }
                    return;
                }
            }
        });
    }


    private void getDataFromBmob(final String mLoginUserName, final String loginPassword) {

        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("userName", mLoginUserName);

        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    Log.d(TAG, "done: ---查询bmob成功");
                    if (list.size() != 0){
                        getUserInfo(list,mLoginUserName,loginPassword);
                    }else {
                        userNameNotExist();
                    }
                } else {
                    Log.d(TAG, "done: ---查询bmob失败");
                    loginFail();
                }
            }
        });
    }

    private void getUserInfo(List<User> list,String userName, String passWord) {
        for (User user : list) {
            String getBmobPassword = user.getPassword();
            SplashActivity.mClassID = user.getClassId();
            SplashActivity.mObjID = user.getObjectId();

            Log.d(TAG, "done: -----+" + SplashActivity.mClassID);
            Log.d(TAG, "done: ----------------查询用户名密码成功" + SplashActivity.mLoginUserName + "+++" + getBmobPassword);
            //字符串比较  .equals
            Log.d(TAG, "getUserInfo: -----"+ passWord+"--"+getBmobPassword);
            if (getBmobPassword.equals(passWord) ) {
                ThreadUtils.runOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        RegisterAndLoginEMClient();
                    }
                });
            } else {
                ThreadUtils.runUiThread(new Runnable() {
                    @Override
                    public void run() {
                        passwordError();
                    }
                });
            }
        }
    }

    private void RegisterAndLoginEMClient() {

        try {
            EMClient.getInstance().createAccount(SplashActivity.mLoginUserName, mLoginPassword);
            loginEMClient();
        } catch (final HyphenateException e) {
            if (e != null) {
                Log.d(TAG, "RegisterAndLoginEMClient: +++++++++++++" + e.getErrorCode() + "  " + e.getDescription());
                loginEMClient();
                return;
            } else {
                Log.d(TAG, "done: ---注册Emclient失败");
                ThreadUtils.runUiThread(new Runnable() {
                    @Override
                    public void run() {
                        registerFail();
                    }
                });
            }
        }
    }

    private void loginEMClient() {
        EMClient.getInstance().login(SplashActivity.mLoginUserName, mLoginPassword, new EMCallBack() {
            @Override
            public void onSuccess() {
                ThreadUtils.runUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: ---------------登录环信成功");
                        loginSuccess();
                    }
                });
            }

            @Override
            public void onError(final int i, final String s) {
                ThreadUtils.runUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i == 200) {
                            loginSuccess();
                        } else {
                            Log.d(TAG, "run: ++++++++++++++" + i + "===" + s);
                            loginFail();
                        }
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    private void userNameNotExist() {
        isLoginFailed =true;
        hideProgress();
        showToast("用户名不存在");
    }

    private void passwordError() {
        isLoginFailed =true;

        hideProgress();
        showToast("密码错误");
    }

    private void loginFail() {
        isLoginFailed =true;
        hideProgress();
        showToast("登录失败");
    }

    private void registerFail() {
        isLoginFailed =true;
        hideProgress();
        showToast("账号或密码错误");
    }

    private void loginSuccess() {
        timer.cancel();
        ThreadUtils.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                EMClient.getInstance().chatManager().loadAllConversations();
                EMClient.getInstance().groupManager().loadAllGroups();
            }
        });

        hideProgress();
        if (SplashActivity.mClassID != null && SplashActivity.mClassID.length() > 0) {

            goTo(MainActivity.class, true);
        } else {
            goTo(CreateClassActivity.class, true);
        }

        this.finish();
    }



    private Timer timer;
    private final int TIMER_EXECUTE = 1;

    private final int ERROR_MESSAGE = 1;
    private final int CHECK_TIME = 10000;
    private EThread eThread;
    private final int LOGIND_FALIE = 2;


    public void checkTimeOut() {


        //timer for check the thread
        timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isLoginFailed){
                        checkThread();
                        Log.d(TAG, "checkTimeOut: -0---sendMess");
                    }else {
                        timer.cancel();
                        isLoginFailed = false;
                        Log.d(TAG, "checkTimeOut: -1---sendMess");
                    }
                }
            }, CHECK_TIME);

            eThread = new EThread();
            eThread.start();


    }


    @Override
    protected Dialog onCreateDialog(int id) {
        return new AlertDialog.Builder(this).setTitle("")
                .setMessage("登录超时").create();
    }




    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            mHandler.obtainMessage();
            switch (msg.what) {
                case LOGIND_FALIE:
                    timer.cancel();// 关闭计时器
                    if (mHandler.hasMessages(TIMER_EXECUTE)){
                        mHandler.removeMessages(TIMER_EXECUTE);
                    }
                    break;
                case TIMER_EXECUTE:
                    if (eThread.getState().toString().equals("TERMINATED") ||
                            eThread.getState().toString().equals("TIMED_WAITING")) {
                        eThread.stopThread(true);
                        hideProgress();
                        showDialog(ERROR_MESSAGE);
                        timer.cancel();// 关闭计时器
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    protected void checkThread() {
        if (msg != null){
            msg.what = TIMER_EXECUTE;
            mHandler.sendMessage(msg);
        }
    }

    class EThread extends Thread {
        private boolean flag = true;

        public void stopThread(boolean flag) {
            this.flag = !flag;
        }

        @Override
        public void run() {
            Looper.prepare();

            try {
                Thread.sleep(11000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timer.cancel();// 关闭计时器
            if (!flag) {
                return;
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            showToast("再按一次退出");
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
}
