package com.example.firebasesocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtUsername, edtPassword;
    private Button btnSignUp, btnSignIn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        FirebaseApp.initializeApp( this );
        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        } );
        btnSignUp.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp();
            }
        } );

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            transitionToSocialMediaActivity();
        }
    }

    private void SignUp(){
        mAuth.createUserWithEmailAndPassword( edtEmail.getText().toString(), edtPassword.getText().toString())
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toasty.success( MainActivity.this, "Signing Up Successful", Toasty.LENGTH_SHORT ).show();
                            FirebaseDatabase.getInstance().getReference().child( "my_users" ).child( task.getResult()
                            .getUser().getUid()).child( "username" ).setValue( edtUsername.getText().toString() );
                            transitionToSocialMediaActivity();
                        }else {
                            Toasty.error( MainActivity.this, "Signing Up Failed", Toasty.LENGTH_SHORT ).show();
                        }
                    }
                } );
    }
    private void SignIn(){
        mAuth.signInWithEmailAndPassword( edtEmail.getText().toString(), edtPassword.getText().toString() )
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toasty.success( MainActivity.this, "Log In Successful", Toasty.LENGTH_SHORT ).show();
                            transitionToSocialMediaActivity();
                        }else {
                            Toasty.error( MainActivity.this, "Log In Failed", Toasty.LENGTH_SHORT ).show();
                        }
                    }
                } );
    }
    private void transitionToSocialMediaActivity() {

        Intent intent = new Intent(this, SocialMediaActivity.class);
//        intent.putExtra("usernameValue", edtUsername.getText().toString());
        startActivity(intent);

    }
}
