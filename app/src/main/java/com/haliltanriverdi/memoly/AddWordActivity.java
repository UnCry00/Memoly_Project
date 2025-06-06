package com.haliltanriverdi.memoly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.haliltanriverdi.memoly.databinding.ActivityAddWordBinding;
import com.haliltanriverdi.memoly.firebase.FirebaseManager;
import com.haliltanriverdi.memoly.model.Word;
import com.haliltanriverdi.memoly.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddWordActivity extends AppCompatActivity {

    public static final String EXTRA_WORD_ID = "word_id";
    public static final String EXTRA_EDIT_MODE = "edit_mode";

    private ActivityAddWordBinding binding;
    private FirebaseManager firebaseManager;
    private boolean editMode = false;
    private Word currentWord;
    private Uri selectedImageUri;

    // ActivityResultLauncher for gallery
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    selectedImageUri = ImageUtils.compressImage(this,selectedImageUri,700,700,80);
                    Glide.with(this)
                            .load(selectedImageUri)
                            .fitCenter()
                            .into(binding.wordImage);
                }
            }
    );

    // ActivityResultLauncher for permission
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    Toast.makeText(this, "Galeri erişimi için izin gerekli", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddWordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Login kontrol
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Oturumunuz kapandı, lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class); // Replace with your login activity
            startActivity(intent);
            finish();
            return;
        }

        firebaseManager = FirebaseManager.getInstance();

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        editMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);
        String wordId = getIntent().getStringExtra(EXTRA_WORD_ID);

        getSupportActionBar().setTitle(editMode ? "Kelime Düzenle" : "Yeni Kelime Ekle");

        binding.buttonSave.setText(editMode ? "Düzenle" : "Ekle");

        if (editMode && wordId != null) {
            loadWordData(wordId);
        } else {
            currentWord = new Word();
        }
    }

    private void loadWordData(String wordId) {
        firebaseManager.getWord(wordId, new FirebaseManager.OnWordOperationListener() {
            @Override
            public void onSuccess(Word word) {
                currentWord = word;

                binding.editTurkishWord.setText(word.getTurkishWord());
                binding.editEnglishWord.setText(word.getEnglishWord());

                List<String> sentences = word.getExampleSentences();
                if (sentences.size() >= 5) {
                    binding.editExample1.setText(sentences.get(0));
                    binding.editExample2.setText(sentences.get(1));
                    binding.editExample3.setText(sentences.get(2));
                    binding.editExample4.setText(sentences.get(3));
                    binding.editExample5.setText(sentences.get(4));
                }

                // Glide ile resmi yükle
                if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
                    Glide.with(AddWordActivity.this)
                            .load(word.getImagePath())
                            .placeholder(R.drawable.uploadimage)
                            .error(R.drawable.uploadimage)
                            .fitCenter()
                            .into(binding.wordImage);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddWordActivity.this, "Kelime yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void onWordImageClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Galeriye erişim için izin gerekli", Snackbar.LENGTH_INDEFINITE)
                            .setAction("İzin ver", v -> permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES))
                            .show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Galeriye erişim için izin gerekli", Snackbar.LENGTH_INDEFINITE)
                            .setAction("İzin ver", v -> permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE))
                            .show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    public void onSaveWordClick(View view) {
        String turkishWord = binding.editTurkishWord.getText().toString().trim();
        String englishWord = binding.editEnglishWord.getText().toString().trim();
        String example1 = binding.editExample1.getText().toString().trim();
        String example2 = binding.editExample2.getText().toString().trim();
        String example3 = binding.editExample3.getText().toString().trim();
        String example4 = binding.editExample4.getText().toString().trim();
        String example5 = binding.editExample5.getText().toString().trim();

        if (TextUtils.isEmpty(turkishWord)) {
            binding.editTurkishWord.setError("Türkçe kelime gerekli");
            return;
        }

        if (TextUtils.isEmpty(englishWord)) {
            binding.editEnglishWord.setError("İngilizce kelime gerekli");
            return;
        }

        if (TextUtils.isEmpty(example1)) {
            binding.editExample1.setError("Birinci örnek cümle gerekli");
            return;
        }

        if (TextUtils.isEmpty(example2)) {
            binding.editExample2.setError("İkinci örnek cümle gerekli");
            return;
        }

        if (TextUtils.isEmpty(example3)) {
            binding.editExample3.setError("Üçüncü örnek cümle gerekli");
            return;
        }

        if (TextUtils.isEmpty(example4)) {
            binding.editExample4.setError("Dördüncü örnek cümle gerekli");
            return;
        }

        if (TextUtils.isEmpty(example5)) {
            binding.editExample5.setError("Beşinci örnek cümle gerekli");
            return;
        }

        if (selectedImageUri == null && (!editMode || currentWord.getImagePath() == null)) {
            Toast.makeText(this, "Lütfen bir resim seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> exampleSentences = new ArrayList<>();
        exampleSentences.add(example1);
        exampleSentences.add(example2);
        exampleSentences.add(example3);
        exampleSentences.add(example4);
        exampleSentences.add(example5);

        setFormEnabled(false);

        if (selectedImageUri != null) {
            uploadImage(turkishWord, englishWord, exampleSentences);
        } else {
            saveOrUpdateWord(turkishWord, englishWord, exampleSentences, currentWord.getImagePath());
        }
    }

    private void uploadImage(String turkishWord, String englishWord, List<String> exampleSentences) {
        firebaseManager.uploadWordImage(selectedImageUri, new FirebaseManager.OnImageUploadListener() {
            @Override
            public void onSuccess(String storagePath, String downloadUrl) {
                saveOrUpdateWord(turkishWord, englishWord, exampleSentences, downloadUrl);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddWordActivity.this, "Resim yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
                setFormEnabled(true);
            }
        });
    }

    private void saveOrUpdateWord(String turkishWord, String englishWord, List<String> exampleSentences, String imagePath) {
        if (editMode && currentWord != null) {
            currentWord.setTurkishWord(turkishWord);
            currentWord.setEnglishWord(englishWord);
            currentWord.setExampleSentences(exampleSentences);
            if (imagePath != null) {
                currentWord.setImagePath(imagePath);
            }

            firebaseManager.updateWord(currentWord, new FirebaseManager.OnWordOperationListener() {
                @Override
                public void onSuccess(Word word) {
                    Toast.makeText(AddWordActivity.this, "Kelime güncellendi", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(AddWordActivity.this, error, Toast.LENGTH_SHORT).show();
                    setFormEnabled(true);
                }
            });
        } else {
            Word newWord = new Word();
            newWord.setTurkishWord(turkishWord);
            newWord.setEnglishWord(englishWord);
            newWord.setExampleSentences(exampleSentences);
            newWord.setImagePath(imagePath);

            firebaseManager.addWord(newWord, new FirebaseManager.OnWordOperationListener() {
                @Override
                public void onSuccess(Word word) {
                    Toast.makeText(AddWordActivity.this, "Kelime eklendi", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(AddWordActivity.this, error, Toast.LENGTH_SHORT).show();
                    setFormEnabled(true);
                }
            });
        }
    }

    private void setFormEnabled(boolean enabled) {
        binding.editTurkishWord.setEnabled(enabled);
        binding.editEnglishWord.setEnabled(enabled);
        binding.editExample1.setEnabled(enabled);
        binding.editExample2.setEnabled(enabled);
        binding.editExample3.setEnabled(enabled);
        binding.editExample4.setEnabled(enabled);
        binding.editExample5.setEnabled(enabled);
        binding.wordImage.setEnabled(enabled);
        binding.buttonSave.setEnabled(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}