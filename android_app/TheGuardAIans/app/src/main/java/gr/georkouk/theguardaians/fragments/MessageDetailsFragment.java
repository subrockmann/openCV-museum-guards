package gr.georkouk.theguardaians.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gr.georkouk.theguardaians.NotificationActivity;
import gr.georkouk.theguardaians.R;
import gr.georkouk.theguardaians.dao.DaoMessage;
import gr.georkouk.theguardaians.models.Message;
import gr.georkouk.theguardaians.utils.OnSwipeTouchListener;


public class MessageDetailsFragment extends Fragment {

    @BindView(R.id.tvRoomNo) TextView tvRoomNo;
    @BindView(R.id.tvCameraId) TextView tvCameraId;
    @BindView(R.id.tvObjectId) TextView tvObjectId;
    @BindView(R.id.tvObjectName) TextView tvObjectName;
    @BindView(R.id.tvStatus) TextView tvStatus;
    @BindView(R.id.tvTime) TextView tvTime;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.messageDetailsRootLayout) ConstraintLayout messageDetailsRootLayout;

    private Message message;


    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.message_view, container, false);

        ButterKnife.bind(this, view);

        OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener(getActivity()){
            public void onSwipeTop() {

            }
            public void onSwipeRight() {
                togglePreviousOrNextMessage(DaoMessage.PREVIOUS_MESSAGE);
            }
            public void onSwipeLeft() {
                togglePreviousOrNextMessage(DaoMessage.NEXT_MESSAGE);
            }
            public void onSwipeBottom() {

            }
            public void singleTapUp(){
                imageView.performClick();
            }
        };

        messageDetailsRootLayout.setOnTouchListener(onSwipeTouchListener);
        imageView.setOnTouchListener(onSwipeTouchListener);

        if(NotificationActivity.id > 0){
            fillScreen(
                    DaoMessage.getMqttMessage(NotificationActivity.id)
            );
        }

        return view;
    }

    public void fillScreen(Message message){
        this.message = message;

        String imagesRootFolder = Environment.getExternalStorageDirectory() + "/theGuardAIans";
        String imageFile = imagesRootFolder + "/" + message.getFilename();

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .fitCenter()
                .dontTransform()
                .placeholder(R.drawable.cell_shape)
                .error(R.drawable.ic_outline_image_64px);

        Glide.with(getContext())
                .load(imageFile)
                .apply(options)
                .into(imageView);

        tvRoomNo.setText(message.getRoomNumber());
        tvCameraId.setText(message.getCameraId());
        tvObjectId.setText(message.getObjectId());
        tvObjectName.setText(message.getObjectName());
        tvStatus.setText(message.getStatus());
        tvTime.setText(message.getTimestamp());
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    public void clearScreen(){
        Glide.with(getContext()).load(new BitmapFactory()).fitCenter().into(imageView);

        tvRoomNo.setText("");
        tvCameraId.setText("");
        tvObjectId.setText("");
        tvObjectName.setText("");
        tvStatus.setText("");
        tvTime.setText("");
    }

    @OnClick(R.id.btBack)
    public void back(){
        clearScreen();
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @OnClick(R.id.btSeen)
    public void setMessageSeen(){
        DaoMessage.setMessageSeen(this.message.getId());

        clearScreen();
        Message nextMessage = DaoMessage.getPreviousOrNextMqttMessage(message.getId(), DaoMessage.NEXT_MESSAGE);
        if(nextMessage.getId() > 0){
            fillScreen(nextMessage);
            return;
        }

        Message prevMessage = DaoMessage.getPreviousOrNextMqttMessage(message.getId(), DaoMessage.PREVIOUS_MESSAGE);
        if(prevMessage.getId() > 0){
            fillScreen(prevMessage);
            return;
        }

        back();
    }

    @OnClick(R.id.imageView)
    public void imageClick(){
        showItemImage(message);
    }

    public void showItemImage(Message message){
        AlertDialog imageAlert = new AlertDialog.Builder(getContext()).create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(imageAlert.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        View layout = View.inflate(getContext(), R.layout.item_images, null);
        imageAlert.setView(layout);
        imageAlert.show();
        imageAlert.getWindow().setAttributes(lp);

        ImageView image = imageAlert.findViewById(R.id.ivItemImage);

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .fitCenter()
                .dontTransform()
                .placeholder(R.drawable.cell_shape)
                .error(R.drawable.ic_outline_image_64px);

        String imagesRootFolder = Environment.getExternalStorageDirectory() + "/theGuardAIans";
        String imageFile = imagesRootFolder + "/" + message.getFilename();

        assert image != null;
        Glide.with(getContext())
                .load(imageFile)
                .apply(options)
                .into(image);
    }

    private void togglePreviousOrNextMessage(int scrollType){
        Message newMessage = DaoMessage.getPreviousOrNextMqttMessage(message.getId(), scrollType);
        if(newMessage.getId() > 0){
            clearScreen();
            fillScreen(newMessage);
        }
        else{
            Toast.makeText(
                    getContext(),
                    scrollType == DaoMessage.PREVIOUS_MESSAGE ?
                            "You have reached the beginning of the message list." :
                            "You have reached the end of the message list.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

}
