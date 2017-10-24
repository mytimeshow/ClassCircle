package com.example.administrator.classcircle.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.administrator.classcircle.Bean.ClassAlbum;
import com.example.administrator.classcircle.R;
import com.example.administrator.classcircle.li.ShowImageActivity;
import com.example.administrator.classcircle.lisenner.OnclickLisenner;

import java.util.List;


/**
 * Created by Administrator on 2017/9/18 0018.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>{
    private OnclickLisenner lisenner;
    private String classNum;
    private List<ClassAlbum>  albumList;
    private Context mcontext;





    public  AlbumAdapter(List<ClassAlbum>  albumList,Context mcontext,String classNum){

        this.albumList=albumList;
this.classNum=classNum;
        this.mcontext=mcontext;
    }


   static class ViewHolder extends  RecyclerView.ViewHolder{
        private View Myview;
        private ImageView album1;





        public ViewHolder(View itemView) {
            super(itemView);
            Myview=itemView;
            album1= (ImageView) itemView.findViewById(R.id.album1);






        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.albun_item,parent,false);
       final ViewHolder holder=new ViewHolder(view);





        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //final ClassAlbum classAlbum=albumList.get(position);


        holder.album1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mcontext,ShowImageActivity.class);

                intent.putExtra("classNum",classNum);

                intent.putExtra("image",albumList.get(position).getAlbum1());
               //传点击是哪张照片的positon过去，并在viewpager当中显示
                intent.putExtra("position",String.valueOf(position));

                mcontext.startActivity(intent);
            }
        });

        Glide.with(mcontext).
                load(albumList.get(position).getAlbum1()).into(holder.album1);




    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }




}
