package com.example.excuseme;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SAMPLE_FILE = "sample.pdf";

    @ViewById
    PDFView pdfView;

    @NonConfigurationInstance
    Integer pageNumber;


    @AfterViews
    void afterViews() {
        SharedPreferences shared_pref = getSharedPreferences("shared_pref", MODE_PRIVATE);
        if (shared_pref != null)
            pageNumber = shared_pref.getInt("page", 0);

        pdfView.setBackgroundColor(Color.LTGRAY);
        displayFromAsset();
        setTitle(SAMPLE_FILE);
    }

    private void displayFromAsset() {
        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }


    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        SharedPreferences.Editor editor = getSharedPreferences("shared_pref", MODE_PRIVATE).edit();
        editor.putInt("page", page);
        editor.apply();
        setTitle(String.format("%s %s / %s", SAMPLE_FILE, page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.i(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.i(TAG, "Cannot load page " + page);
    }
}
