package com.islavdroid.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static android.content.Intent.ACTION_PICK;

public class MainActivity extends AppCompatActivity {
private Button button2;
    private Button uploadButton;
    private ImageView imageView;
    DatabaseReference database;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private static final int GALLERY_INTENT =2;
    private static final int CAMERA = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //открываем доступ к корню firebase
         database = FirebaseDatabase.getInstance().getReference();
         storageReference= FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
       button2=(Button)findViewById(R.id.button2);
        imageView =(ImageView)findViewById(R.id.image) ;
        uploadButton=(Button)findViewById(R.id.button3);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA);

            }
        }});

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_INTENT);
            }
        });

    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(11);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_INTENT&&resultCode==RESULT_OK){
            progressDialog.setMessage("Загрузка...");
            progressDialog.show();
            Uri uri =data.getData();
            StorageReference filepath = storageReference.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this,"Загрузка успешна",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });

        }
       else  if(requestCode==CAMERA&&resultCode==RESULT_OK) {
             progressDialog.setMessage("Загрузка...");
             progressDialog.show();
//get the camera image
             Bundle extras = data.getExtras();
             Bitmap bitmap = (Bitmap) data.getExtras().get("data");
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
             byte[] dataBAOS = baos.toByteArray();
             //set the image into imageview
             imageView.setImageBitmap(bitmap);
             /*************** UPLOADS THE PIC TO FIREBASE***************/
             //Firebase storage folder where you want to put the images


//name of the image file (add time to have different files to avoid rewrite on the same file)

             StorageReference imagesRef = storageReference.child("Photos").child("filename" + new Date().getTime());
//upload image

             UploadTask uploadTask = imagesRef.putBytes(dataBAOS);
             uploadTask.addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception exception) {
                     Toast.makeText(getApplicationContext(), "Sending failed", Toast.LENGTH_SHORT).show();
                 }
             }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                     // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

//handle success


                     progressDialog.dismiss();
                 }
             });
         };
        }}


