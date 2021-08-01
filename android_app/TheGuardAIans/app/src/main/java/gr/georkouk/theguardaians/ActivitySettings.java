package gr.georkouk.theguardaians;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gr.georkouk.theguardaians.models.Settings;

@SuppressLint("NonConstantResourceId")
public class ActivitySettings extends AppCompatActivity {

    @BindView(R.id.etServerURI) EditText etServerURI;
    @BindView(R.id.etClientID) EditText etClientID;
    @BindView(R.id.etUsername) EditText etUsername;
    @BindView(R.id.etPassword) EditText etPassword;
    @BindView(R.id.etTopic) EditText etTopic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        Settings settings = Utils.readSettings(this);

        this.etServerURI.setText(settings.getServerURI());
        this.etClientID.setText(settings.getClientID());
        this.etUsername.setText(settings.getUsername());
        this.etPassword.setText(settings.getPassword());
        this.etTopic.setText(settings.getTopic());
    }

    @OnClick(R.id.btSave)
    public void btSaveClick(View view){
        SharedPreferences.Editor settings =
                getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();

        settings.putString("serverURI", etServerURI.getText().toString());
        settings.putString("clientID", etClientID.getText().toString());
        settings.putString("username", etUsername.getText().toString());
        settings.putString("password", etPassword.getText().toString());
        settings.putString("topic", etTopic.getText().toString());

        settings.apply();

        this.finish();
    }

}
