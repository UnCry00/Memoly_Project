package com.haliltanriverdi.memoly;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.haliltanriverdi.memoly.databinding.ActivityForgetPasswordBinding;

public class ForgetPasswordActivity extends AppCompatActivity {

    private ActivityForgetPasswordBinding forgetPasswordBinding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        forgetPasswordBinding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        View view = forgetPasswordBinding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
    }

    public void sendResetMail(View view){
        String mail = forgetPasswordBinding.email.getText().toString().trim();

        if(!mail.trim().isEmpty()){
            auth.sendPasswordResetEmail(mail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            forgetPasswordBinding.forgetpasswordmessage.setText("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi");
                        } else {
                            forgetPasswordBinding.forgetpasswordmessage.setText("Bir Hata Oluştu !\nLütfen internet bağlantınızı ve mail adresinizi kontrol ediniz");
                        }
                    });
        }else{
            Toast.makeText(ForgetPasswordActivity.this,"Lütfen e-posta adresinizi giriniz",Toast.LENGTH_LONG).show();
        }


    }
}