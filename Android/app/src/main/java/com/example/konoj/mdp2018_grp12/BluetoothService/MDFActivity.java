package com.example.konoj.mdp2018_grp12.BluetoothService;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.konoj.mdp2018_grp12.R;

import org.w3c.dom.Text;

public class MDFActivity extends AppCompatActivity {

    private TextView mdf1;
    private TextView mdf2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdf);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       mdf1=(TextView)findViewById(R.id.mdf1Text);
       mdf2=(TextView)findViewById(R.id.mdf2Text);


        Bundle b = new Bundle();
        b = getIntent().getExtras();
        String mdf1Text = b.getString("mdf1");
        String mdf2Text = b.getString("mdf2");

        mdf1.setText(mdf1Text);
        mdf2.setText(mdf2Text);

    }

}
