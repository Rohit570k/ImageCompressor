 package com.example.imagecompressor;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import id.zelory.compressor.Compressor;


 public class ScrollingActivity extends AppCompatActivity {

     public  static  final int RESULT_IMAGE=1;
    ImageView imgOriginal, imgCompressed;
    TextView txtOriginal,txtCompressed,txtQuality;
    EditText txtHeight,txtWidth;
    SeekBar seekBar;
    Button btnCompress;
     FloatingActionButton btnPick;
    File originalImage,compressedImage;
    private static String filepath;

//    File path = Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_PICTURES);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher_foreground);

        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        askPermission();

        imgOriginal=findViewById(R.id.imgOriginal);
        imgCompressed=findViewById(R.id.imgCompressed);
        txtOriginal=findViewById(R.id.txtOriginal);
        txtCompressed=findViewById(R.id.txtCompressed);
        txtQuality=findViewById(R.id.txtQuality);
        txtHeight=findViewById(R.id.txtHeight);
        txtWidth=findViewById(R.id.txtWidth);
        seekBar=findViewById(R.id.seekQuality);
        LinearLayout compressedLayout = (LinearLayout) findViewById(R.id.compressedLayout);
        btnCompress=findViewById(R.id.btnCompress);

        btnPick = (FloatingActionButton) findViewById(R.id.btnPick);
        File path =new File(Environment.getExternalStorageDirectory(),"/MyCompressor");
        filepath=path.getAbsolutePath();
        Log.i("filepath value", "onCreate: "+filepath);
        //if file not exist that will create new folder if it doesnt exist
        // inside the internal storage
        if(!path.exists()){
            path.mkdirs();
        }
//        try {
//            // Make sure the Pictures directory exists.
//            path.mkdirs();
//
//            f.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtQuality.setText("Quality: "+progress);
                seekBar.setMax(100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                openGallery();
            }
        });

        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quality =seekBar.getProgress();
                int width =Integer.parseInt(txtWidth.getText().toString());
                int height= Integer.parseInt(txtHeight.getText().toString());

                try {
                    compressedImage =new Compressor(ScrollingActivity.this)
                            .setMaxWidth(width)
                            .setMaxHeight(height)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(filepath)
                            .compressToFile(originalImage);
                    File finalFile =new File(filepath,originalImage.getName());
                    Bitmap finalBitmap =BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                    imgCompressed.setImageBitmap(finalBitmap);
                    txtCompressed.setText("Size: "+Formatter.formatShortFileSize(ScrollingActivity.this,finalFile.length()));
                    Toast.makeText(ScrollingActivity.this,"Image Compressed&Saved!",Toast.LENGTH_SHORT).show();
                    compressedLayout.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ScrollingActivity.this,"Error While Compressing!",Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

     /**
      * Open the gallery pick the image
      * and return back to activity
      */
     private void openGallery() {
         Intent gallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
         startActivityForResult(gallery,RESULT_IMAGE);

     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(requestCode ==  RESULT_IMAGE&& resultCode==RESULT_OK){
             btnCompress.setVisibility(View.VISIBLE);
             final Uri imageUri=data.getData();
             try {
                 final InputStream imageStream =getContentResolver().openInputStream(imageUri);
                 final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                 imgOriginal.setImageBitmap(selectedImage);
                 originalImage=new File(imageUri.getPath().replace("/raw",""));
                 txtOriginal.setText("Size: "+ Formatter.formatShortFileSize(this,originalImage.length()));
                 Log.i("Originl image size", "onActivityResult: "+Formatter.formatShortFileSize(this,selectedImage.getByteCount()));
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
                 Toast.makeText(ScrollingActivity.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
             }
         }
         else{
             Toast.makeText(this,"No image selected",Toast.LENGTH_SHORT).show();
         }
     }

     private void askPermission() {
         Dexter.withContext(this)
                 .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                 Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 .withListener(new MultiplePermissionsListener(){

                     @Override
                     public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                         if(multiplePermissionsReport.areAllPermissionsGranted()) {
                             Toast.makeText(ScrollingActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                         }
                     }

                     @Override
                     public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                         permissionToken.continuePermissionRequest();
                     }
                 }).check();

     }

}