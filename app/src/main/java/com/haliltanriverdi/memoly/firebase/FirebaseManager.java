package com.haliltanriverdi.memoly.firebase;

import android.net.Uri;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.haliltanriverdi.memoly.model.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FirebaseManager {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_WORDS = "words";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    private static FirebaseManager instance;

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    private FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private CollectionReference getUserWordsCollection() {
        return db.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .collection(COLLECTION_WORDS);
    }

    public void addWord(Word word, OnWordOperationListener listener) {
        if (currentUser == null) {
            if (listener != null) {
                listener.onFailure("Kullanıcı oturumu bulunamadı");
            }
            return;
        }

        word.setUserId(currentUser.getUid());

        getUserWordsCollection()
                .whereEqualTo("turkishWord", word.getTurkishWord())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            DocumentReference newWordRef = getUserWordsCollection().document();
                            word.setId(newWordRef.getId());

                            newWordRef.set(wordToMap(word))
                                    .addOnSuccessListener(aVoid -> {
                                        if (listener != null) {
                                            listener.onSuccess(word);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (listener != null) {
                                            listener.onFailure(e.getMessage());
                                        }
                                    });
                        } else {
                            if (listener != null) {
                                listener.onFailure("Kelime zaten kayıtlı");
                            }
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure("Kelime kontrolü başarısız: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void updateWord(Word word, OnWordOperationListener listener) {
        if (currentUser == null) {
            if (listener != null) {
                listener.onFailure("Kullanıcı oturumu bulunamadı");
            }
            return;
        }

        word.setUserId(currentUser.getUid());

        getUserWordsCollection()
                .whereEqualTo("turkishWord", word.getTurkishWord())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean canUpdate = true;
                        for (DocumentSnapshot document : task.getResult()) {
                            if (!document.getId().equals(word.getId())) {
                                canUpdate = false;
                                break;
                            }
                        }

                        if (canUpdate) {
                            getUserWordsCollection().document(word.getId())
                                    .set(wordToMap(word))
                                    .addOnSuccessListener(aVoid -> {
                                        if (listener != null) {
                                            listener.onSuccess(word);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (listener != null) {
                                            listener.onFailure(e.getMessage());
                                        }
                                    });
                        } else {
                            if (listener != null) {
                                listener.onFailure("Aynı isimde başka bir kelime zaten kayıtlı");
                            }
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure("Kelime kontrolü başarısız: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void deleteWord(Word word, OnWordOperationListener listener) {
        if (word.getId() == null) {
            if (listener != null) {
                listener.onFailure("Geçersiz kelime ID'si");
            }
            return;
        }

        getUserWordsCollection().document(word.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
                        StorageReference imageRef = storage.getReferenceFromUrl(word.getImagePath());
                        imageRef.delete().addOnSuccessListener(aVoid1 -> {
                            if (listener != null) {
                                listener.onSuccess(word);
                            }
                        }).addOnFailureListener(e -> {
                            if (listener != null) {
                                listener.onSuccess(word);
                            }
                        });
                    } else {
                        if (listener != null) {
                            listener.onSuccess(word);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public void getWord(String wordId, OnWordOperationListener listener) {
        getUserWordsCollection().document(wordId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Word word = mapToWord(documentSnapshot);
                        if (listener != null) {
                            listener.onSuccess(word);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure("Kelime bulunamadı");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public void getAllWords(OnWordsLoadedListener listener) {
        getUserWordsCollection()
                .orderBy("turkishWord", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Word> wordList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Word word = mapToWord(document);
                            wordList.add(word);
                        }
                        if (listener != null) {
                            listener.onWordsLoaded(wordList);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void searchWords(String query, OnWordsLoadedListener listener) {
        String lowerCaseQuery = query.toLowerCase();

        getUserWordsCollection()
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Word> wordList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Word word = mapToWord(document);
                            if (word.getTurkishWord().toLowerCase().contains(lowerCaseQuery) ||
                                    word.getEnglishWord().toLowerCase().contains(lowerCaseQuery)) {
                                wordList.add(word);
                            }
                        }
                        if (listener != null) {
                            listener.onWordsLoaded(wordList);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void uploadWordImage(Uri imageUri, OnImageUploadListener listener) {
        if (currentUser == null) {
            if (listener != null) {
                listener.onFailure("Kullanıcı oturumu bulunamadı");
            }
            return;
        }

        String imageName = "word_images/" + currentUser.getUid() + "/" + UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child(imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (listener != null) {
                            listener.onSuccess(imageRef.toString(), uri.toString());
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    private Map<String, Object> wordToMap(Word word) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", word.getId());
        map.put("turkishWord", word.getTurkishWord());
        map.put("englishWord", word.getEnglishWord());
        map.put("exampleSentences", word.getExampleSentences());
        map.put("imagePath", word.getImagePath());
        map.put("userId", word.getUserId());
        return map;
    }

    private Word mapToWord(DocumentSnapshot document) {
        Word word = new Word();
        word.setId(document.getId());
        word.setTurkishWord(document.getString("turkishWord"));
        word.setEnglishWord(document.getString("englishWord"));

        List<String> sentences = new ArrayList<>();
        List<String> firestoreSentences = (List<String>) document.get("exampleSentences");
        if (firestoreSentences != null) {
            sentences.addAll(firestoreSentences);
        }
        while (sentences.size() < 5) {
            sentences.add("");
        }
        word.setExampleSentences(sentences);

        word.setImagePath(document.getString("imagePath"));
        word.setUserId(document.getString("userId"));
        return word;
    }

    public interface OnWordOperationListener {
        void onSuccess(Word word);
        void onFailure(String error);
    }

    public interface OnWordsLoadedListener {
        void onWordsLoaded(List<Word> words);
        void onFailure(String error);
    }

    public interface OnImageUploadListener {
        void onSuccess(String storagePath, String downloadUrl);
        void onFailure(String error);
    }
}