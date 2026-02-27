package com.HassanProject.shopapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.config.AdminConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = tilEmail.getEditText();
        etPassword = tilPassword.getEditText();
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        tvForgotPassword.setOnClickListener(v -> {
            // Add forgot password logic here
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email cannot be empty");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password cannot be empty");
            return;
        }

        // Special admin login
        if (email.equals(AdminConfig.ADMIN_USERNAME) && password.equals(AdminConfig.ADMIN_PASSWORD)) {
            email = AdminConfig.ADMIN_EMAIL; // Use admin email from config
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        tilEmail.setError("Email not found");
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        tilPassword.setError("Incorrect password");
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}
