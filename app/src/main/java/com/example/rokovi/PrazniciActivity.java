package com.example.rokovi;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class PrazniciActivity extends AppCompatActivity {
    private static final String TAG = "PrazniciActivity";

    private TextView Praznici;
    private Button Spremi;
    private Button Odustani;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.praznici_main);
        File file = new File(getFilesDir(), "praznici.txt");
        if (!file.exists()) {
            copyFile(this, file);
        }
        Praznici = (TextView) findViewById(R.id.prazniciFromFile);
        Spremi = (Button) findViewById(R.id.Spremi);
        Odustani = (Button) findViewById(R.id.Odustani);

        Praznici.setText(read_file(this, "praznici.txt"));

        Spremi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toFile = Praznici.getText().toString().replaceAll("\n", ",");
                writeToFile(toFile, PrazniciActivity.this);
                Intent intent = new Intent(PrazniciActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Odustani.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrazniciActivity.this, CalendarActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("praznici.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String read_file(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString().replaceAll(",","\n");
        } catch (FileNotFoundException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    private void copyFile(Context context, File outFile) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open("praznici.txt");
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
