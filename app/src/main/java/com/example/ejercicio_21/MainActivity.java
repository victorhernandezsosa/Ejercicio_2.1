package com.example.ejercicio_21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;
import android.Manifest;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    VideoView videoView;

    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;

    private Uri video;
    private VideoDbHelper dbHelper;
    private File archivovideo;
    Button grabar, guardarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = (VideoView) findViewById(R.id.videoView);
        grabar = (Button) findViewById(R.id.grabar);
        guardarButton = (Button) findViewById(R.id.guardarButton);

        grabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarGrab();
            }
        });

        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

    }

    private void save() {
        if (video != null) {
            String videoPath = getRealPathFromURI(video); // Obtener el path real del Uri del video capturado
            videoalmacenamiento(videoPath); // Guardar el video en el almacenamiento interno compartido
            videobd(videoPath); // Guardar la información del video en la base de datos
        } else {
            Toast.makeText(this, "Graba un video primero", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private void videobd(String videoPath) {

        dbHelper = new VideoDbHelper(this);
        dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(VideoDbHelper.COLUMN_VIDEO_PATH, videoPath);

        long newRowId = dbHelper.getWritableDatabase().insert(VideoDbHelper.TABLE_VIDEOS, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Información del video guardada en la base de datos", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar información del video en la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void videoalmacenamiento(String videoPath) {
        try {
            File source = new File(videoPath);
            File destinationDir = new File(getExternalFilesDir(null), "MyVideos");
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }
            String videoName = "my_video.mp4";
            archivovideo = new File(destinationDir, videoName);

            InputStream inStream = new FileInputStream(source);
            OutputStream outStream = new FileOutputStream(archivovideo);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

            Toast.makeText(this, "Video guardado en el almacenamiento", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el video en el almacenamiento", Toast.LENGTH_SHORT).show();
        }
    }



    private void iniciarGrab() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent tomarvideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (tomarvideo.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(tomarvideo, REQUEST_VIDEO_CAPTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            video = data.getData();
            videoView.setVideoURI(video);
            videoView.start();
            
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Se necesita permiso para guardar el video", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarGrab();
            } else {
                Toast.makeText(this, "El permiso de la cámara es necesario para grabar video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (archivovideo != null && archivovideo.exists()) {
            archivovideo.delete();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}