package gr.georkouk.theguardaians.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import gr.georkouk.theguardaians.R;
import gr.georkouk.theguardaians.models.Message;


@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private Context context;
    private List<Message> messages;
    private OnRowClickListener onRowClickListener;

    public MessageListAdapter(Context context){
        this.context = context;
        this.messages = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_row, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MessageListAdapter.ViewHolder holder, int position) {
        Message message = this.messages.get(position);

        holder.textView1.setText(message.getStatus());
        holder.textView2.setText(
                "Room: " + message.getRoomNumber() + " - Object: " + message.getObjectName()
        );
        holder.textView3.setText(message.getTimestamp());

        if(this.onRowClickListener != null){
            holder.itemView.setOnClickListener(view -> onRowClickListener.onMessageClick(message));
        }
    }

    @Override
    public int getItemCount() {
        return (null != this.messages ? this.messages.size() : 0);
    }

    public void swapData(List<Message> messages){
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    public void addMessage(Message message){
        this.messages.add(message);
        this.notifyDataSetChanged();
    }

    public void setOnRowClickListener(OnRowClickListener onRowClickListener){
        this.onRowClickListener = onRowClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textView1) TextView textView1;
        @BindView(R.id.textView2) TextView textView2;
        @BindView(R.id.textView3) TextView textView3;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnRowClickListener {

        void onMessageClick(Message message);

    }

}
