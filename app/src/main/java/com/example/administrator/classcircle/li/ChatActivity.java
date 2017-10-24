package com.example.administrator.classcircle.li;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.administrator.classcircle.Bean.Msg;
import com.example.administrator.classcircle.R;
import com.example.administrator.classcircle.activity.SplashActivity;
import com.example.administrator.classcircle.adapter.ChatAdapter;
import com.example.administrator.classcircle.fragment.Fragment02;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.ValueEventListener;

public class ChatActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private ChatAdapter chatAdapter;
    private Button btn_send;
    private EditText edt_content;
    private List<Msg> msgList=new ArrayList<>();
    private BmobRealTimeData brt=new BmobRealTimeData();
    @Override
    protected int getLayoutRes() {
        return R.layout.activity_chat;
    }

    @Override
    protected void init() {
        addListennerToMsg();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn_send= (Button)findViewById(R.id.btn_sendMsg);
        edt_content= (EditText)findViewById(R.id.edt_content);
        mRecyclerView= (RecyclerView) findViewById(R.id.recyeclerview_content);

        edt_content.setFocusable(true);
        edt_content.setFocusableInTouchMode(true);
        edt_content.requestFocus();


        InputMethodManager imm = (InputMethodManager)ChatActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        initMsgs();
        LinearLayoutManager massage=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(massage);



        BmobQuery<Msg> bmobquery=new BmobQuery<>();
        bmobquery.findObjects(new FindListener<Msg>() {
            @Override
            public void done(List<Msg> list, BmobException e) {
                if(e==null){
                    msgList.addAll(list);
                    chatAdapter=new ChatAdapter(msgList,ChatActivity.this);
                    mRecyclerView.setAdapter(chatAdapter);
                    mRecyclerView.scrollToPosition(msgList.size()-1);
                }

            }
        });



        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content=edt_content.getText().toString();
                if(!"".equals(content)){
                    Msg m=new Msg();
                    m.setStudentId(SplashActivity.mLoginUserName);
                    m.setContent(content);
                    if (Fragment02.imgUrl == null){

                    }else {
                        m.setImgUrl(Fragment02.imgUrl);
                    }

                    m.setName(SplashActivity.mLoginUserName);
                    msgList.add(m);
                    chatAdapter.notifyItemInserted(msgList.size()-1);
                    mRecyclerView.scrollToPosition(msgList.size()-1);
                    edt_content.setText("");
                    m.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if(e==null){

                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void initListener() {

    }
    private void initMsgs() {



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

        }



        return super.onOptionsItemSelected(item);
    }

    private void addListennerToMsg() {
        brt.start(new ValueEventListener() {
            @Override
            public void onConnectCompleted(Exception e) {
                if(e==null){
                    brt.subTableUpdate("Msg");
                }
            }

            @Override
            public void onDataChange(JSONObject jsonObject) {

                BmobQuery<Msg> bmobquery=new BmobQuery<>();
                bmobquery.findObjects(new FindListener<Msg>() {
                    @Override
                    public void done(List<Msg> list, BmobException e) {
                        if(e==null){
                            msgList.clear();
                            msgList.addAll(list);
                            chatAdapter.notifyDataSetChanged();
                            mRecyclerView.scrollToPosition(msgList.size()-1);
                            edt_content.setText("");
                           // Toast.makeText(ChatActivity.this, "haha", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });


    }
}
