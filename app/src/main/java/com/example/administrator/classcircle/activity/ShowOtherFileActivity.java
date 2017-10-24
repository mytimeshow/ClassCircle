package com.example.administrator.classcircle.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.administrator.classcircle.C;
import com.example.administrator.classcircle.R;
import com.example.administrator.classcircle.entity.Data;
import com.example.administrator.classcircle.utils.DownloadUtil;
import com.example.administrator.classcircle.utils.FileUtils;
import com.example.administrator.classcircle.utils.OpenFileUtils;
import com.example.administrator.classcircle.utils.ThreadUtils;

import java.util.Random;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class ShowOtherFileActivity extends BaseActivity {
    private static final String TAG = "ShowOtherFileActivity";

    private TextView mTvTime;
    private TextView mTvName;
    private ImageView mIvFileTypeImg;
    private TextView mTvFileName;
    private String fileUrl;
    private TextView mTvHeadTitle;
    private FrameLayout mFrameLayout;
    private Button mButton;
    private String mFilePath = null;


    private int progress = 0;
    private Handler progressDialogHandler;
    private int PROGRESS_DIALOG_FLAG = 1;
    private int uploadValue = 0;


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_show_other_file;
    }

    @Override
    protected void init() {
        initLayout();
        loadDataFromBmob();

    }

    private void loadDataFromBmob() {
        Intent intent = getIntent();
        String objId = intent.getStringExtra("FILE_URL");
        BmobQuery<Data> bmobQuery = new BmobQuery<>();
        bmobQuery.getObject(objId, new QueryListener<Data>() {
            @Override
            public void done(Data data, BmobException e) {
                if (e == null){
                    //查询成功
                    Log.d(TAG, "done: ---+ "+data.getName());
                    Log.d(TAG, "done: ---+ "+data.getCreatedAt());
                    Log.d(TAG, "done: ---+ "+data.getUploadFile().getFileUrl());
                    Log.d(TAG, "done: ---+ "+data.getUploadFile().getFilename());

                    String name = data.getName();
                    String time = data.getCreatedAt();
                    fileUrl = data.getUploadFile().getFileUrl();
                    String fileName = data.getUploadFile().getFilename();
                    mTvName.setText(name);
                    mTvTime.setText(String.format("%s 上传",time));
                    mTvFileName.setText(fileName);
                     if (FileUtils.getExtension(fileName).equals("ppt")) {
                        mIvFileTypeImg.setImageResource(R.drawable.file_ppt);
                    } else if (FileUtils.getExtension(fileName).equals("txt")) {
                         mIvFileTypeImg.setImageResource(R.drawable.file_txt);
                    } else if (FileUtils.getExtension(fileName).equals("docx")) {
                         mIvFileTypeImg.setImageResource(R.drawable.file_doc);
                    } else if (FileUtils.getExtension(fileName).equals("pdf")) {
                         mIvFileTypeImg.setImageResource(R.drawable.file_pdf);
                    } else if (FileUtils.getExtension(fileName).equals("zip")) {
                         mIvFileTypeImg.setImageResource(R.drawable.file_zip);
                    }else if (FileUtils.getExtension(fileName).equals("xlsx")) {
                         mIvFileTypeImg.setImageResource(R.drawable.file_xls);
                    } else {
                         mIvFileTypeImg.setImageResource(R.drawable.file_else);
                    }

                    mFrameLayout.setVisibility(View.GONE);
                }else {
                    //查询失败
                }
            }
        });
    }

    @Override
    protected void initListener() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFilePath != null){
                    Intent intent = OpenFileUtils.openFile(mFilePath);
                    startActivity(intent);
                }else {
                    showProgressBarDialog(ProgressDialog.STYLE_HORIZONTAL);
                    ThreadUtils.runOnBackgroundThread(new Runnable() {
                        @Override
                        public void run() {
                            downloadFile();
                        }
                    });

                }
            }
        });
    }
    private void downloadFile(){
        DownloadUtil.get().download(fileUrl, "classCircle", new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                ThreadUtils.runUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFilePath = DownloadUtil.mFile.getPath();
                        showToast("下载成功");
                        mButton.setText("打开文件");

                    }
                });
            }

            @Override
            public void onDownloadProgress(int progress) {
                uploadValue = progress;
            }

            @Override
            public void onDownloadFailed() {
                ThreadUtils.runUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("下载失败");
                        progress = 101;
                        mButton.setText("重新下载");
                    }
                });
            }
        });
    }

    private void showProgressBarDialog(int style){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.file_download);

        progressDialog.setTitle("数据处理中");
        progressDialog.setMessage("请稍后");
        progressDialog.setProgressStyle(style);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.setMax(C.ALERT_DIALOG_MAX_PROGRESS);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialogHandler.removeMessages(PROGRESS_DIALOG_FLAG);
                progress = 0;
                progressDialog.setProgress(progress);
            }
        });
        progressDialog.show();

        progressDialogHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (progress >= C.ALERT_DIALOG_MAX_PROGRESS){
                    //消失 并重置初始值
                    progressDialog.dismiss();
                    progress = 0;
                }else {
                    //  progress++;
                    // progressDialog.incrementProgressBy(1);
                    // 随机设置下一次递增进度 (50 +毫秒)
                    progress = uploadValue;
                    progressDialog.setProgress(progress);
                    progressDialogHandler.sendEmptyMessageDelayed(1, 50 + new Random().nextInt(500));
                }
            }

        };
        // 设置进度初始值
        progress = (progress > 0) ? progress : 0;
        progressDialog.setProgress(progress);
        // 发送消息
        progressDialogHandler.sendEmptyMessage(PROGRESS_DIALOG_FLAG);

    }

    private void initLayout() {

        mTvTime = (TextView) findViewById(R.id.show_other_file_time);
        mTvName = (TextView) findViewById(R.id.show_other_file_headName);
        mIvFileTypeImg = (ImageView) findViewById(R.id.show_other_file_fileImg);
        mTvFileName = (TextView) findViewById(R.id.show_other_file_fileName);
        mTvHeadTitle = (TextView) findViewById(R.id.id_header_tv);
        mTvHeadTitle.setText("下载文件");
        mFrameLayout = (FrameLayout) findViewById(R.id.show_other_file_loading);

        mButton = (Button) findViewById(R.id.show_file_button);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}