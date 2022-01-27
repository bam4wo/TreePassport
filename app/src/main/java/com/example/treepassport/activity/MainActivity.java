package com.example.treepassport.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.treepassport.R;

public class MainActivity extends AppCompatActivity {
    private Button btnScan, btnPhoto, btnPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanQrcodeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void initialize(){
        btnScan = findViewById(R.id.btn_scan);
        btnPhoto = findViewById(R.id.btn_get_photo);
        btnPdf = findViewById(R.id.btn_get_pdf);
    }
}