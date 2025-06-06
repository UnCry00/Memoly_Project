package com.haliltanriverdi.memoly.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static Uri compressImage(Context context, Uri imageUri, int maxWidth, int maxHeight, int quality) {
        try {
            // Uri'den Bitmap oluştur
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

            // Resmi ölçeklendir
            Bitmap scaledBitmap = scaleBitmap(bitmap, maxWidth, maxHeight);

            // Sıkıştırılmış resmi geçici bir dosyaya kaydet
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("compressed_image", ".jpg", outputDir);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            return Uri.fromFile(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Resim sıkıştırma hatası!", Toast.LENGTH_SHORT).show();
            return imageUri; // Hata olursa orijinal Uri'yi döndür
        }
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}