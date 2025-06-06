package com.haliltanriverdi.memoly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.haliltanriverdi.memoly.databinding.ActivityRegisterBinding;
import com.haliltanriverdi.memoly.utils.ImageUtils;


import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding registerBinding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Uri imageData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        registerBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = registerBinding.getRoot();
        setContentView(view);
        auth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        registerLauncher();
    }

    public void profileImageUpload(View view) {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Snackbar.make(view, "Galeriye erişim için izin gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver", v -> permissionLauncher.launch(permission))
                        .show();
            } else {
                permissionLauncher.launch(permission);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    public void register(View view){
        String email = registerBinding.signupEmail.getText().toString().trim();
        String password_1 = registerBinding.signupPassword.getText().toString().trim();
        String password_2 = registerBinding.signupConfirm.getText().toString().trim();
        String name = registerBinding.signupName.getText().toString().trim();
        String lastname = registerBinding.signupLastname.getText().toString().trim();
        if(!email.trim().isEmpty() && !password_1.trim().isEmpty() && !password_2.trim().isEmpty() && !name.trim().isEmpty() && !lastname.trim().isEmpty() && imageData != null){
            if(password_1.equals(password_2)){
                auth.createUserWithEmailAndPassword(email,password_1).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                                // Profil fotoğrafı yükle
                                String imageName = "images/" + userID + ".jpg";
                                storageReference.child(imageName).putFile(imageData).addOnSuccessListener(taskSnapshot -> {
                                    storageReference.child(imageName).getDownloadUrl().addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();

                                        // Firestore’a kayıt
                                        HashMap<String, Object> userMap = new HashMap<>();
                                        userMap.put("email", email);
                                        userMap.put("name", name);
                                        userMap.put("lastname", lastname);
                                        userMap.put("imageUrl", imageUrl);

                                        firestore.collection("Users").document(userID).set(userMap);

                                        Toast.makeText(RegisterActivity.this,"Kayıt Başarılı",Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Fotoğraf yüklenemedi", Toast.LENGTH_LONG).show();
                                });

                        }else {
                            Toast.makeText(RegisterActivity.this,"Kayıt İşlemi Başarısız",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else {
               Toast.makeText(RegisterActivity.this,"Lütfen Şifreleri Aynı Giriniz!",Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(RegisterActivity.this,"Tüm Alanları Doldurunuz!",Toast.LENGTH_LONG).show();
        }
    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    imageData = data.getData();
                    imageData = ImageUtils.compressImage(this,imageData,700,700,80);
                    registerBinding.registerImageView.setImageURI(imageData);
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            } else {
                Toast.makeText(RegisterActivity.this, "İzin gerekli!", Toast.LENGTH_LONG).show();
            }
        });
    }

}