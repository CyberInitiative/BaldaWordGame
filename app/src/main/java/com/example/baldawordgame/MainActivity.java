package com.example.baldawordgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.baldawordgame.model.Dictionary;
import com.example.baldawordgame.view_adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private ViewPager2 mainPager;
    private ViewPagerAdapter mainPagerAdapter;
    private TabLayout tabLayout;

    private ArrayList<String> arrayList = new ArrayList<String>() {
        {
            add("Профиль");
            add("Комнаты");
            add("Создать комнату");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreateView() called");
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        visibilityOptions();
        Log.d(TAG, "onStart() called;");
    }

    private void init() {
//        firebaseDatabase.setPersistenceEnabled(true);
        Dictionary.loadDictionaryFromFirebase().addOnCompleteListener(task -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent loginActivityIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginActivityIntent);
            }

            mainPager = findViewById(R.id.mainPager);
            tabLayout = findViewById(R.id.tabLayoutMain);
            mainPagerAdapter = new ViewPagerAdapter(this);
            mainPager.setAdapter(mainPagerAdapter);
            mainPager.setUserInputEnabled(false);
            View child = mainPager.getChildAt(0);
            if (child instanceof RecyclerView) {
                child.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }

            new TabLayoutMediator(tabLayout, mainPager,
                    (tab, position) -> tab.setText(arrayList.get(position))).attach();
        });

    }

    private void visibilityOptions() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

}