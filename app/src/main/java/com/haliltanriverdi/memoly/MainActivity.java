package com.haliltanriverdi.memoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.haliltanriverdi.memoly.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    private FirebaseAuth mAuth;
    private long backPressedTime = 0;

    private Toast backToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();


        SharedPreferences prefs = getSharedPreferences("memoly_prefs", MODE_PRIVATE);
        boolean tutorialShown = prefs.getBoolean("main_tutorial_shown", false);

        if (!tutorialShown) {
            showMainTutorial();
            prefs.edit().putBoolean("main_tutorial_shown", true).apply();
        }

        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    if (backToast != null) backToast.cancel();
                    finish();
                } else {
                    backToast = Toast.makeText(MainActivity.this, "Çıkmak için tekrar geri tuşuna basın", Toast.LENGTH_SHORT);
                    backToast.show();
                }

                backPressedTime = System.currentTimeMillis();
            }
        });
        loadWelcomeText();
    }



    private void showMainTutorial() {
        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(mainBinding.wordlemenu, "Kelime Oyunu", "Wordle oyununa gitmek için buraya tıkla.")
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
                        TapTarget.forView(mainBinding.wordlistmenu, "Kelime Listesi", "Ezberlenecek kelimeleri burada görebilirsin.")
                                .outerCircleColor(R.color.soft)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.black)
                                .descriptionTextColor(android.R.color.black)
                                .titleTextSize(22)
                                .descriptionTextSize(16)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .cancelable(false)
                                .transparentTarget(true),
                        TapTarget.forView(mainBinding.quizmenu, "Quiz", "Kısa testlerle bilginizi sınayın.")
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
                        TapTarget.forView(mainBinding.ogrenilenmenu, "Öğrenilenler", "Öğrendiğiniz kelimeleri burada bulabilirsiniz.")
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
                        TapTarget.forView(mainBinding.profilemenu, "Profil", "Profil bilgilerinizi düzenleyebilirsiniz.")
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
                        TapTarget.forView(mainBinding.infomenu, "Uygulama Hakkında", "Uygulama hakkında bilgi almak için buraya tıklayın.")
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
                        // Tutorial tamamlandığında yapılacak işlemler
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        // Her adımda yapılacaklar (opsiyonel)
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Kullanıcı yarıda bırakırsa yapılacaklar
                    }
                });

        sequence.start();
    }

    private void loadWelcomeText() {
        mainBinding.welcomeProgress.setVisibility(View.VISIBLE);
        mainBinding.tvWelcome.setVisibility(View.GONE);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            mainBinding.tvWelcome.setText("Hoş geldin!");
            mainBinding.welcomeProgress.setVisibility(View.GONE);
            mainBinding.tvWelcome.setVisibility(View.VISIBLE);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    if (name != null && !name.isEmpty()) {
                        mainBinding.tvWelcome.setText("Merhaba, " + name + "!");
                    } else {
                        mainBinding.tvWelcome.setText("Merhaba!");
                    }

                    mainBinding.welcomeProgress.setVisibility(View.GONE);
                    mainBinding.tvWelcome.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    mainBinding.tvWelcome.setText("Merhaba!");
                    mainBinding.welcomeProgress.setVisibility(View.GONE);
                    mainBinding.tvWelcome.setVisibility(View.VISIBLE);
                });
    }


    public void mainToWordle(View view){
        //Wordle Ekran
    }

    public void mainToWordList(View view){
        Intent intent = new Intent(MainActivity.this, WordList.class);
        startActivity(intent);
    }

    public void mainToQuiz(View view){
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        startActivity(intent);
    }

    public void mainToLearnedWord(View view){
        Intent intent = new Intent(MainActivity.this, LearnedWordsActivity.class);
        startActivity(intent);
    }

    public void mainToProfile(View view){
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void mainToInfo(View view){
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        startActivity(intent);
    }

}