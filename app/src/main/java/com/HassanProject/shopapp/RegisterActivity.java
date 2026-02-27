package com.HassanProject.shopapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        
        // Use direct findViewById for EditTexts to be safer
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        
        // If etEmail is still null, try getting it from TIL as fallback (though findViewById should work)
        if (etEmail == null && tilEmail != null) etEmail = tilEmail.getEditText();
        if (etPassword == null && tilPassword != null) etPassword = tilPassword.getEditText();

        btnRegister = findViewById(R.id.btnRegister);

        TextView tvLogin = findViewById(R.id.tvLogin);
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> registerUser());
        } else {
            Toast.makeText(this, "Error: Register button not found", Toast.LENGTH_LONG).show();
        }
    }

    private void registerUser() {
        if (etEmail == null || etPassword == null) {
            Toast.makeText(this, "Error: Input fields not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isAdmin = false;  // Admin can only be set via AdminConfig login

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        User user = new User(firebaseUser.getUid(), email, isAdmin);
                        mDatabase.child("users").child(firebaseUser.getUid()).setValue(user)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Database update failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                } else {
                    String error = "Registration failed";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        error = "Password is too weak";
                    } catch (FirebaseAuthUserCollisionException e) {
                        error = "Email is already in use";
                    } catch (Exception e) {
                        error = "Error: " + e.getMessage();
                        e.printStackTrace(); // Log full error to Logcat
                    }
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
    }
}
