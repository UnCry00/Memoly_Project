package com.haliltanriverdi.memoly;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.haliltanriverdi.memoly.databinding.ActivityProfileBinding;
import com.haliltanriverdi.memoly.utils.ImageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri imageData;
    private String currentImageUrl;
    private boolean isEditMode = false;
    SharedPreferences sharedPreferences;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        registerLaunchers();

        loadUserData();

        binding.editSaveButton.setOnClickListener(v -> {
            if (isEditMode) {
                saveUserData();
            } else {
                enableEditMode();
            }
        });

        binding.editImageButton.setOnClickListener(this::checkStoragePermission);

        binding.profileImageView.setOnClickListener(v -> {
            if (isEditMode) {
                checkStoragePermission(v);
            }
        });
        sharedPreferences = getSharedPreferences("Memoly_profile", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstProfileTutorial", true);

        if (isFirstRun) {
            showProfileTutorial();
        }
    }

    public void logOutButton(View view){
        auth.signOut();
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProfileTutorial() {
        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(binding.editSaveButton, "Düzenle Kaydet", "Profil Bilgilerinizi Buradan Düzenleyebilirsiniz.")
                                .outerCircleColor(R.color.soft)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.black)
                                .descriptionTextColor(android.R.color.black)
                                .titleTextSize(22)
                                .descriptionTextSize(16)
                                .targetRadius(75)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(false)
                                .cancelable(false)
                                .transparentTarget(true),
                        TapTarget.forView(binding.logOutButton, "Oturumu Kapat", "Mevcut oturumunuzu kapatabilrisiniz.")
                                .outerCircleColor(R.color.soft)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.black)
                                .descriptionTextColor(android.R.color.black)
                                .titleTextSize(22)
                                .descriptionTextSize(16)
                                .targetRadius(75)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(false)
                                .cancelable(false)
                                .transparentTarget(true)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Toast.makeText(ProfileActivity.this, "Eğitim tamamlandı!", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isFirstProfileTutorial", false);
                        editor.apply();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        // Bu metod şu anda kullanılmıyor, ileride işlevsellik eklenebilir.
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Bu metod şu anda kullanılmıyor, ileride işlevsellik eklenebilir.
                    }
                });

        sequence.start();
    }


    private void loadUserData() {
        showLoading(true);
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            firestore.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String lastname = documentSnapshot.getString("lastname");
                            String email = documentSnapshot.getString("email");
                            currentImageUrl = documentSnapshot.getString("imageUrl");

                            binding.nameEditText.setText(name);
                            binding.lastnameEditText.setText(lastname);
                            binding.emailEditText.setText(email);

                            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(currentImageUrl)
                                        .placeholder(R.drawable.firstphoto)
                                        .error(R.drawable.errorimage)
                                        .fitCenter()
                                        .into(binding.profileImageView);
                            }
                        }
                        showLoading(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Kullanıcı bilgileri yüklenemedi", e);
                        Snackbar.make(binding.getRoot(), "Bilgiler yüklenemedi, lütfen tekrar deneyin.", Snackbar.LENGTH_LONG).show();
                        showLoading(false);
                    });
        } else {
            showLoading(false);
        }
    }

    private void enableEditMode() {
        isEditMode = true;
        binding.nameInputLayout.setEnabled(true);
        binding.lastnameInputLayout.setEnabled(true);
        binding.emailInputLayout.setEnabled(true);
        binding.editImageButton.setVisibility(View.VISIBLE);
        binding.editSaveButton.setText(getString(R.string.save));
        binding.editSaveButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_save));
    }

    private void disableEditMode() {
        isEditMode = false;
        binding.nameInputLayout.setEnabled(false);
        binding.lastnameInputLayout.setEnabled(false);
        binding.emailInputLayout.setEnabled(false);
        binding.editImageButton.setVisibility(View.GONE);
        binding.editSaveButton.setText(getString(R.string.edit));
        binding.editSaveButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_edit));
    }

    private void saveUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String newName = binding.nameEditText.getText().toString().trim();
            String newLastname = binding.lastnameEditText.getText().toString().trim();
            String newEmail = binding.emailEditText.getText().toString().trim();
            String oldEmail = user.getEmail();

            try {
                validateInputs(newName, newLastname, newEmail);
            } catch (IllegalArgumentException e) {
                Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("lastname", newLastname);
            updates.put("email", newEmail);

            showLoading(true);
            if (imageData != null) {
                confirmImageUpload(userId, updates);
            } else {
                assert oldEmail != null;
                updateUserData(userId, updates, oldEmail, newEmail);
            }
        }
    }

    private void validateInputs(String name, String lastname, String email) {
        if (name.isEmpty() || lastname.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException(getString(R.string.fill_all_fields));
        }
    }

    private void confirmImageUpload(String userId, Map<String, Object> updates) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_image_title))
                .setMessage(getString(R.string.confirm_image_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> uploadNewProfileImage(userId, updates))
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> imageData = null)
                .show();
    }

    private void uploadNewProfileImage(String userId, Map<String, Object> updates) {
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            StorageReference oldImageRef = storage.getReferenceFromUrl(currentImageUrl);
            oldImageRef.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    proceedWithImageUpload(userId, updates);
                } else {
                    Log.e("ProfileActivity", "Eski fotoğraf silinemedi", task.getException());
                    Snackbar.make(binding.getRoot(), getString(R.string.delete_old_image_failed), Snackbar.LENGTH_LONG).show();
                    showLoading(false);
                }
            });
        } else {
            proceedWithImageUpload(userId, updates);
        }
    }

    private void proceedWithImageUpload(String userId, Map<String, Object> updates) {
        String imageName = "images/" + userId + ".jpg";
        StorageReference imageRef = storageReference.child(imageName);

        imageRef.putFile(imageData)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updates.put("imageUrl", imageUrl);
                    currentImageUrl = imageUrl;

                    String oldEmail = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
                    String newEmail = binding.emailEditText.getText().toString().trim();
                    assert oldEmail != null;
                    updateUserData(userId, updates, oldEmail, newEmail);
                }))
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Fotoğraf yüklenemedi", e);
                    Snackbar.make(binding.getRoot(), getString(R.string.image_upload_failed), Snackbar.LENGTH_LONG).show();
                    showLoading(false);
                    disableEditMode();
                });
    }

    private void updateUserData(String userId, Map<String, Object> updates, String oldEmail, String newEmail) {
        DocumentReference userRef = firestore.collection("Users").document(userId);
        boolean emailChanged = !oldEmail.equals(newEmail);

        if (emailChanged) {
            updates.remove("email");
            updateUserEmail(userId, newEmail);
        }

        if (!updates.isEmpty()) {
            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (!emailChanged) {
                            Snackbar.make(binding.getRoot(), getString(R.string.update_success), Snackbar.LENGTH_SHORT).show();
                            disableEditMode();
                            showLoading(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Bilgiler güncellenemedi", e);
                        Snackbar.make(binding.getRoot(), getString(R.string.update_failed), Snackbar.LENGTH_LONG).show();
                        showLoading(false);
                        disableEditMode();
                    });
        } else if (!emailChanged) {
            showLoading(false);
        }
    }

    private void updateUserEmail(String userId, String newEmail) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        showEmailVerificationDialog();
                        startEmailVerificationCheck(userId, newEmail);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "E-posta güncelleme hatası", e);
                        Snackbar.make(binding.getRoot(), getString(R.string.email_update_failed), Snackbar.LENGTH_LONG).show();
                        showLoading(false);
                        disableEditMode();
                    });
        }
    }

    private void showEmailVerificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.email_verification_title))
                .setMessage(getString(R.string.email_verification_message))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    private void startEmailVerificationCheck(String userId, String newEmail) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.reload().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && user.isEmailVerified()) {
                            updateFirestoreEmail(userId, newEmail);
                            handler.removeCallbacks(this);
                        } else {
                            handler.postDelayed(this, 5000); // 5 saniyede bir kontrol
                        }
                    });
                }
            }
        }, 5000);
    }

    private void updateFirestoreEmail(String userId, String newEmail) {
        DocumentReference userRef = firestore.collection("Users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(binding.getRoot(), getString(R.string.email_update_success), Snackbar.LENGTH_SHORT).show();
                    showLoading(false);
                    disableEditMode();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Firestore e-posta güncelleme hatası", e);
                    Snackbar.make(binding.getRoot(), getString(R.string.email_update_failed), Snackbar.LENGTH_LONG).show();
                    showLoading(false);
                });
    }

    private void checkStoragePermission(View view) {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Snackbar.make(view, getString(R.string.permission_required), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.grant_permission), v -> permissionLauncher.launch(permission))
                        .show();
            } else {
                permissionLauncher.launch(permission);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private void registerLaunchers() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    imageData = data.getData();

                    imageData = ImageUtils.compressImage(this, imageData, 800, 800, 80); // 800x800, %80 kalite

                    Glide.with(this)
                            .load(imageData)
                            .fitCenter()
                            .into(binding.profileImageView);
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            } else {
                Snackbar.make(binding.getRoot(), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}