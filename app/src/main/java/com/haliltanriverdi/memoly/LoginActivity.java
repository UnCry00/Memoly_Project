package com.haliltanriverdi.memoly;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.haliltanriverdi.memoly.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding loginBinding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = loginBinding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
    }

    public void register(View view){
        Intent intent = new Intent(this,RegisterActivity.class);
        startActivity(intent);
    }


    public void login(View view){
        String email = loginBinding.email.getText().toString().trim();
        String password = loginBinding.password.getText().toString().trim();

        if(!email.isEmpty() && !password.isEmpty()){
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Giriş Başarılı!", Toast.LENGTH_LONG).show();
                                SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("is_logged_in", true);
                                editor.apply();
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Exception exception = task.getException();

                                // Şifre hatalı ya da kullanıcı bulunamadıysa ortak mesaj ver
                                if (exception instanceof FirebaseAuthInvalidUserException ||
                                        exception instanceof FirebaseAuthInvalidCredentialsException) {

                                    Toast.makeText(LoginActivity.this, "E-posta veya şifre hatalı.", Toast.LENGTH_LONG).show();
                                } else {
                                    // Diğer tüm hatalarda sistem mesajı gösterilsin
                                    String errorMessage = "Hata: " + (exception != null ? exception.getLocalizedMessage() : "Bilinmeyen bir hata oluştu.");
                                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

        }else{
            Toast.makeText(LoginActivity.this,"Tüm Alanları Doldurunuz!",Toast.LENGTH_LONG).show();
        }

    }

    public void loginToForgetPassword(View view){
        Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
        startActivity(intent);
    }
}