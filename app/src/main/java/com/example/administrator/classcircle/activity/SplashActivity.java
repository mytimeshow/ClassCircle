package com.example.administrator.classcircle.activity;

import android.os.Handler;
import android.util.Log;

import com.example.administrator.classcircle.C;
import com.example.administrator.classcircle.R;
import com.example.administrator.classcircle.entity.User;
import com.example.administrator.classcircle.li.CreateClassActivity;
import com.example.administrator.classcircle.utils.SharePreUtil;
import com.hyphenate.chat.EMClient;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    public static String mLoginUserName;
    public static String mClassID;
    public static String mObjID;
    public static String mImgUrl;

    private Handler mHandler = new Handler();


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isFistRun = SharePreUtil.isFirstRun(getApplicationContext());
                if (isFistRun){
                    //checkLoginState();
                    checkLoginState();

                    goTo(GuideActivity.class,true);
                    SharePreUtil.saveFirstRun(getApplicationContext(),true);
                }else {
                    checkLoginState();
//                        goTo(LoginActivity.class,true);
                }

            }
        }, C.SPLASH_DELAY_TIME);


    }




    private void checkLoginState() {

        Log.d(TAG, "checkLoginState: -----"+EMClient.getInstance().isLoggedInBefore());

        if (EMClient.getInstance().isLoggedInBefore()){
            Log.d(TAG, "checkLoginState: ----user"+EMClient.getInstance().getCurrentUser());
            mLoginUserName = EMClient.getInstance().getCurrentUser();
            queryMyInfo(mLoginUserName);
//            goTo(MainActivity.class,true);

        }else {
            goTo(LoginActivity.class,true);

        }
    }

    private void queryMyInfo(final String userName) {
        Log.d(TAG, "queryMyInfo: ====");
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("userName", userName);
        query.setLimit(50);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null){
                    Log.d(TAG, "done: ---查询bmob成功");
                    for (User user : list) {
                        Log.d(TAG, "done: --+"+user.getClassId());
                        mClassID = user.getClassId();
                        mObjID = user.getObjectId();
                        Log.d(TAG, "done: -----+" + SplashActivity.mClassID);
                        Log.d(TAG, "done: ----------------查询用户名" + mLoginUserName);

                        if (user.getPic() != null){
                            Log.d(TAG, "done: -imgUrl---"+user.getPic().getUrl());
                            mImgUrl=user.getPic().getUrl();
                           // Toast.makeText(SplashActivity.this, user.getUserName(), Toast.LENGTH_SHORT).show();
                        }

                        if (mClassID.length() <= 0){
                            goTo(CreateClassActivity.class,true);
                            return;
                        }else {
                            goTo(MainActivity.class,true);
                    }

                    }
                }else {
                    Log.d(TAG, "done: ----查询失败"+e.toString());
                    goTo(LoginActivity.class,true);

                }
            }

        });
    }

    @Override
    protected void initListener() {

    }
}
