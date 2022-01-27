package com.example.treepassport.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.treepassport.R;
import com.google.iot.cbor.CborMap;
import com.google.iot.cbor.CborParseException;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import COSE.CoseException;
import COSE.Encrypt0Message;
import COSE.Message;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import nl.minvws.encoding.Base45;

public class ScanQrcodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    ZXingScannerView zXingScannerView;
    Message message;

    private static final int BUFFER_SIZE = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        zXingScannerView = findViewById(R.id.ZXingScannerView_QRCode);

        //取得相機權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this
                        , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    100);
        }else{
            openQRCamera();
        }
    }

    // 開啟相機
    private void openQRCamera(){
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }

    // 取得權限回傳
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults[0] == 0) {
            openQRCamera();
        } else {
            Toast.makeText(this, "尚未取的權限", Toast.LENGTH_SHORT).show();
        }
    }

    // 關閉相機
    @Override
    protected void onStop() {
        zXingScannerView.stopCamera();
        super.onStop();
    }

    // 取得QRCode掃描到的物件回傳
    @Override
    public void handleResult(Result rawResult) {
        String result = rawResult.getText();
        Log.i("wu", result);
        byte[] bytecompressed = Base45.getDecoder().decode(result.substring(4));

        Inflater inflater = new Inflater();
        inflater.setInput(bytecompressed);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytecompressed.length);
        byte[] buffer = new byte[BUFFER_SIZE];
        while (!inflater.finished()) {
            int count = 0;
            try {
                count = inflater.inflate(buffer);
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
            outputStream.write(buffer, 0, count);
        }

        try {
            message = Encrypt0Message.DecodeFromBytes(outputStream.toByteArray());
        } catch (CoseException e) {
            e.printStackTrace();
        }

        CborMap cborMap = null;
        try {
            cborMap = CborMap.createFromCborByteArray(message.GetContent());
        } catch (CborParseException e) {
            e.printStackTrace();
        }

        String t = cborMap.toString(2);
        String json = cborMap.toJsonString();
        Log.i("wu", json);
        Log.i("wu", t);

        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONObject evidence = jsonObject.getJSONObject("-260").getJSONObject("1").getJSONArray("v").getJSONObject(0);
            Log.i("wu", "Unique certificate identifier 數位證明識別碼 : " + evidence.getString("ci"));
            Log.i("wu", "Country in which the vaccine was administered 接種國家 : " + evidence.getString("co"));
            Log.i("wu", "Total number of doses recommended 應接種劑次 : " + evidence.getString("dn"));
            Log.i("wu", "Date of the latest dose received ( DD / MM / YYYY) 接種日期 : " + evidence.getString("dt"));
            Log.i("wu", "Certificate issuer 數位證明發行機構 : " + evidence.getString("is"));
            Log.i("wu", "Vaccine marketing authorization holder or manufacturer 疫苗經銷商或製造商 : " + evidence.getString("ma"));
            Log.i("wu", "Vaccine medicinal product 疫苗名稱 : " + evidence.getString("mp"));
            Log.i("wu", "Number of doses received 已接種劑次 : " + evidence.getString("sd"));
            Log.i("wu", "Vaccine or prophylaxis 疫苗類型 : " + evidence.getString("tg"));
            Log.i("wu", "Disease or agent targeted 預防疾病名稱 : " + evidence.getString("vp"));

            String  birth = jsonObject.getJSONObject("-260").getJSONObject("1").getString("dob");
            Log.i("wu", "Date of birth ( DD / MM / YYYY) 生日 : " + birth);
            JSONObject name = jsonObject.getJSONObject("-260").getJSONObject("1").getJSONObject("nam");
            Log.i("wu", "Surname(s) and given name(s) 姓名 : " + name.getString("fn") + name.getString("gn") + "\n" + name.getString("fnt") + "," + name.getString("gnt"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //ZXing相機預設掃描到物件後就會停止，以此這邊再次呼叫開啟，使相機可以為連續掃描之狀態
        openQRCamera();
    }

}