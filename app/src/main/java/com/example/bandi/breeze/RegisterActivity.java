package com.example.bandi.breeze;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bandi.breeze.objects.breezeUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etUsername;
    private EditText etPassword;
    private TextView txtStatusTextView;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    FirebaseDatabase database; //ada
    DatabaseReference refer; //ada
    breezeUser usernew; //ada
    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        txtStatusTextView = (TextView) findViewById(R.id.txtStatusTextView) ;

        refer = database.getReference().child("User"); //ada

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this,
                        R.style.Theme_AppCompat_DayNight_Dialog);

                final String username = etUsername.getText().toString();
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();

                if(validateForm()) {
                   // signIn(username, password);

                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Authenticating...");
                    progressDialog.show();


                    signIn(email, password, username);
                    progressDialog.dismiss();

                }
                 else{
                    txtStatusTextView.setText("Missing Info");
                }

            }
        });

    }
    private void getValues(){
        usernew.setEmail(etEmail.getText().toString());
        String key = refer.push().getKey();
        refer.child(key).setValue(usernew);
        key = "";
        //refer.child(usernew.getEmail()).setValue(usernew);
    }

    public void signIn(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            txtStatusTextView.setText("Account creation successful");

                            usernew = new breezeUser(email,username);

                            DatabaseReference database;
// ...
                            database = FirebaseDatabase.getInstance().getReference();
                            database.child("users").child(usernew.getID()).setValue(usernew); //Adds a new user to the database

                            //TODO: Setup user ID system
                            sendEmailVerification(); //Sends verification email from noreply@breeze

                            refer.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   // getValues();
                                    //refer.child("User03").setValue(usernew);
                                    Toast.makeText(RegisterActivity.this, "Data Sent...", Toast.LENGTH_LONG);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            //back to login, username and password filed in
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Account creation failed.",
                                    Toast.LENGTH_SHORT).show();
                            txtStatusTextView.setText(task.getException().toString());
                        }
                    }
                });
    }

    private void sendEmailVerification(){
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]\
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                                    txtStatusTextView.setText("Verification email sent to " + user.getEmail());
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
    }

    private boolean validateForm(){
        boolean valid = true;
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required.");
            valid = false;
        } else {
            etEmail.setError(null);
        }

        if(TextUtils.isEmpty(username)) {
            etUsername.setError("Required.");
            valid = false;
        }else{
            etUsername.setError(null);
        }


        password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Required.");
            valid = false;
        } else {
            etPassword.setError(null);
        }

        return valid;
    }
}
