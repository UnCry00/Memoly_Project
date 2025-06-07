# 📱 Kelime Öğrenme Mobil Uygulama

Bu proje, İngilizce kelime öğrenimini kolaylaştırmak amacıyla geliştirilmiş bir mobil uygulamadır. Android Studio ortamında Java dili kullanılarak geliştirilmiş olup, kullanıcı verileri ve içerikler Firebase ile yönetilmektedir.

## 🚀 Özellikler

- **Tanıtım Ekranı**: İlk girişte kullanıcıya tanıtım ekranları sunulur. Daha sonra "Giriş Yap" ve "Kayıt Ol" butonları yer alır.
- **Kayıt Ol Sayfası**:
  - Bilgi girişi: Ad, soyad, e-posta, şifre, şifre tekrar, profil fotoğrafı (galeriden).
  - Firebase Authentication ve Fierstore ile kayıt.
- **Giriş Yap Sayfası**:
  - Giriş: Mail ve şifre ile.
  - "Şifremi Unuttum" özelliği: Kullanıcı maili ile şifre sıfırlama maili gönderilir.
- **Ana Sayfa**:
  - Seçenekler: Quiz Başlat, Kelime Listesi, Puzzle, Raporlar.
  - Uygulama içerisindeki ekranlara yönlendirme butonaları.
- **Oturum Yönetimi**:
  - Kullanıcı giriş yaptıysa tekrar mail/şifre istemeden doğrudan ana sayfa açılır.
  - Logout ile oturum kapatılır, uygulama başlangıç ekranına döner.

---

## 📚 Kelime Listesi Modülü

- **Ekran Yapısı**:
  - Kelime ekleme sayfası için buton.
  - Üstte sabit: Arama çubuğu (Türkçe/İngilizce arama destekli, dinamik filtreleme).
  - Liste: Her satırda Türkçe-İngilizce kelime, düzenle ve sil butonları.
- **Kelime Düzenleme**:
  - Var olan kelimenin bilgileriyle kelime ekleme ekranı açılır.
  - "Düzenle" butonuyla veritabanında güncelleme yapılır.
- **Kelime Silme**:
  - Pop-up ile kelimenin resmi, Türkçesi, İngilizcesi gösterilir.
  - Silme işlemi onayla gerçekleştirilir.
- **Kelime Ekleme**:
  - Gerekli bilgiler:
    - Türkçe anlam
    - İngilizce karşılık
    - 5 örnek cümle
    - Temsili görsel (galeriden seçilir)
  - Zaten ekli olan kelimeler için uyarı verilir.
  - Tüm bilgiler Firebase veritabanına kaydedilir.

---

## ❓ Quiz Modülü

- **Quiz Giriş Ekranı**:
  - “Quiz Başlat” butonu
  - “Oyun Ayarları” butonu
- **Quiz İçerik Ekranı**:
  - Üstte kelime görseli
  - İngilizce anlam sorusu
  - 4 şıklı cevap (1 doğru + 3 yanlış)
  - Doğru seçimde yeşil, yanlışta kırmızı tepkiler
  - Doğru cevap bir kere yanıp söner
- **Oyun Ayarları**:
  - Günlük quizte çıkacak kelime sayısı (1–20 arası seçim)

---

## 📊 Raporlama

Kullanıcının doğru bildiği kelimelerin quizdeki soru sayısına oranı verilir.

### Öğrenme Algoritması (SRS - Spaced Repetition System):

- Bir kelimenin **tam öğrenilmiş** sayılması için 6 farklı zaman aralığında doğru yanıtlanması gerekir:
  1 gün, 1 hafta, 1 ay, 3 ay, 6 ay, 1 yıl
- Kullanıcı bir defa hata yaparsa kelime için tekrar 1. adımdan başlanır.
- Bilinme sayısı ve tarihleri Firebase üzerinde tutulur.

---

## ❓ Wordle Modülü

- Kullancının quiz aşamasında doğru bildiği kelimelele tasarlanan wordle oyunu.
- Doğru bilinen kelimeler listesinden çekilen kelimeler görsel ve Türkçe kelime olarak kullanıcıya gösterilir wordle tarzı sistem ile kullanıcının İngilizcesini bilmesi beklenir.
- Kullanıcı her doğru bildiği kelime için wordle seviyesi bir artar.
- Kullanıcının karşısına çıkan kelime aynı gün tekrar çıkmaz 24 saat sonra tekrar sorulmaya başlanır.

## Profil Modülü

- Veritabanı üzerinenden çekilen kullanıcı bilgileri kullancııya gösterilerek düzenleme seçeneği sunulur.
- Kullanıcı mail, isim soyisim, profil fotoğrafı gibi bilgilerini güncelleyebilir.

## 📚 Öğrenilen Kelime Listesi Modülü

- Quiz aşamasında doğru cevaplanan soruların seviyeleri artar.
- Bir seviye üzeri kelimeler bu listede filtrelenerek gösterilir.
- Kelimenin en kısa zamanda bir daha ne zaman karşısına çıkacağı gösterilir.
- Kelime öğrenme mekanizması gereği bir kere hatalı bilindiğinde listeden kaldırılır.

## 🛠️ Kullanılan Teknolojiler

| Teknoloji | Açıklama |
|----------|----------|
| Java | Android uygulama geliştirme dili |
| Android Studio | Geliştirme ortamı |
| Firebase Authentication | Giriş / kayıt işlemleri |
| Firebase Fierstore | Kullanıcı ve kelime verileri |
| Firebase Storage | Profil ve kelime görselleri için medya depolama |

---

## 🔐 Güvenlik ve Kullanıcı Verileri

- Her kullanıcının verileri benzersizdir, başka bir kullanıcıyla karışmaz.
- Firebase kimlik doğrulaması ve güvenlik kuralları ile korunur.

---

## 📂 Proje Yapısı (Özet)
<pre> 
  ``` 
  app/ 
  ├── adapter/ 
  │ ├── ExampleSentenceAdapter.java 
  │ ├── WordAdapter.java 
  ├── fierbase/ 
  │ ├── FirebaseManager.java 
  ├── model/ 
  │ ├── LearnedWord.java 
  │ ├── QuizQuestion.java 
  │ ├── QuizSession.java 
  │ ├── Word.java 
  │ ├── WordLearningStatus.java 
  ├── onBoarding/ 
  │ ├── adapter/ 
  │ │ ├── OnboardingPagerAdapter.java 
  │ ├── OnboardingActivity.java 
  │ ├── OnboardingFragment1.java 
  │ ├── OnboardingFragment2.java 
  │ ├── OnboardingFragment3.java 
  ├── quiz/ 
  │ ├── adapter/ 
  │ │ ├── LearnedWordAdapter.java 
  │ │ ├── QuizWordAdapter.java 
  │ ├── QuizManager.java 
  ├── utils/ 
  │ ├── ImageUtils.java 
  ├── wordle/ 
  │ ├── adapter/ 
  │ │ ├── WordleCellAdapter.java 
  │ │ ├── WordleKeyboardAdapter.java 
  │ ├── model/ 
  │ │ ├── WordleCell.java 
  │ │ ├── WordleKey.java 
  │ ├── WordleGameActivity.java 
  ├── AddWordActivity.java 
  ├── ForgetPasswordActivity.java 
  ├── InfoActivity.java 
  ├── LauncherActivity.java 
  ├── LearnedWordsActivity.java 
  ├── LoginActivity.java 
  ├── MainActivity.java 
  ├── ProfileActivity.java 
  ├── QuizActivity.java 
  ├── QuizSettingsActivity.java 
  ├── RegisterActivity.java 
  ├── WordDetailActivity.java 
  ├── WordList.java 
  ``` 
</pre>


