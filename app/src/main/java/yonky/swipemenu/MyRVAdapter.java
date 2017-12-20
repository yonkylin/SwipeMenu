package yonky.swipemenu;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2017/12/18.
 */

public class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyViewHolder> {
    String[] strings;
    public MyRVAdapter(String[] strings) {
        this.strings= strings;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv_title.setText(strings[position]);
    }

    @Override
    public int getItemCount() {
        return strings.length;
    }
    static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_title;
        public MyViewHolder(View itemView){
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }
}
