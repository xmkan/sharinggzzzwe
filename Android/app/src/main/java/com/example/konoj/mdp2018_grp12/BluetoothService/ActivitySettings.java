package com.example.konoj.mdp2018_grp12.BluetoothService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.konoj.mdp2018_grp12.MainActivity;
import com.example.konoj.mdp2018_grp12.R;

public class ActivitySettings extends AppCompatActivity {

    public static final String MY_PREFERENCE = "MyPref";
    public static final String FUNCTION_1 = "function1String";
    public static final String FUNCTION_2 = "function2String";


    SharedPreferences sharedPreferences;

    EditText txtFunction1;
    EditText txtFunction2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);

        txtFunction1 = findViewById(R.id.txtFunction1);
        txtFunction2 = findViewById(R.id.txtFunction2);

        txtFunction1.setText(sharedPreferences.getString(FUNCTION_1,"Function 1 not set"));
        txtFunction2.setText(sharedPreferences.getString(FUNCTION_2,"Function 2 not set"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settingmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.btnSave:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(FUNCTION_1, txtFunction1.getText().toString());
                editor.putString(FUNCTION_2, txtFunction2.getText().toString());
                editor.commit();
                ActivitySettings.this.finish();
                return true;
            default:
                return false;
        }
    }
}
