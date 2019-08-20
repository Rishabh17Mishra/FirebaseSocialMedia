package com.example.firebasesocialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class SocialMediaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ImageView postImageView;
    private Button btnCreatePost;
    private TextView textView;
    private EditText edtDescription;
    private ListView usersListView;
    private Bitmap bitmap;
    private String imageIdentifier;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_social_media );

        mAuth = FirebaseAuth.getInstance();

        postImageView = findViewById(R.id.postImageView);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        edtDescription = findViewById(R.id.edtDescription);
        usersListView = findViewById(R.id.usersListView);
        usernames = new ArrayList<>(  );
        adapter = new ArrayAdapter( this, android.R.layout.simple_list_item_1, usernames );
        usersListView.setAdapter( adapter );
        postImageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        } );
        btnCreatePost.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageToServer();
            }
        } );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.my_menu, menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logoutItem:
               logout();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }

    private void logout(){
        mAuth.signOut();
        finish();
    }

    private void selectImage(){
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            Uri chosenImageData = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageData);
                postImageView.setImageBitmap(bitmap);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    private void uploadImageToServer(){

        if (bitmap != null) {
            // Get the data from an ImageView as bytes
            postImageView.setDrawingCacheEnabled( true );
            postImageView.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress( Bitmap.CompressFormat.PNG, 100, baos );
            byte[] data = baos.toByteArray();

            imageIdentifier = UUID.randomUUID() + ".png"; //Generates Unique Value for our image name here of PNG type

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child( "my_images" ).child( imageIdentifier ).putBytes( data );
            uploadTask.addOnFailureListener( new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toasty.error( SocialMediaActivity.this, exception.toString(), Toasty.LENGTH_SHORT ).show();
                }
            } ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toasty.success( SocialMediaActivity.this, "Upload Successful", Toasty.LENGTH_SHORT ).show();
                    edtDescription.setVisibility( View.VISIBLE );
                    FirebaseDatabase.getInstance().getReference().child( "my_users" ).addChildEventListener( new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            String username = (String) dataSnapshot.child( "username" ).getValue();
                            usernames.add( username );
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );
                }
            } );
        }
    }
}
