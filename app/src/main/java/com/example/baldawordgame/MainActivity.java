package com.example.baldawordgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MAIN_ACTIVITY";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
//    private DatabaseReference databaseRef;
//    private DatabaseReference dictionaryRef;

    private ViewPager2 mainPager;
    private ViewPagerAdapter mainPagerAdapter;
    private TabLayout tabLayoutMain;

//    private ArrayList<String> dictionary = new ArrayList<>();

    private ArrayList<String> arrayList = new ArrayList() {
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

//        ArrayList<String> strings = new ArrayList<>();
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                AssetManager am = getAssets();
//                InputStream in = null;
//                try {
//                    in = am.open("txts/russian_nouns.txt");
//                } catch (FileNotFoundException ex) {
//                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Reader file = null;
//                try {
//                    file = new InputStreamReader(in, "UTF-8");
//                } catch (UnsupportedEncodingException ex) {
//                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                int c;
//                ArrayList<Character> arr = new ArrayList<>();
//                try {
//                    if (file != null) {
//                        while ((c = file.read()) != -1) {
//                            if ((char) c != '\n') {
//                                if ((char) c != '\uFEFF') {
//                                    arr.add((char) c);
//                                }
//                            } else {
//                                StringBuilder builder = new StringBuilder(arr.size());
//                                for (Character ch : arr) {
//                                    builder.append(ch);
//                                }
//                                strings.add(builder.toString().trim().toLowerCase());
//                                arr.clear();
//                            }
//                        }
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                for(String str : strings){
//                    if(!strings.contains("-")){
//                        databaseRef.child("dictionary").push().setValue(str);
//                    }
//                }
////                strings.removeIf(i -> i.contains("-"));
////                System.out.println(strings.size());
//            }
//        });
//        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called;");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called;");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called;");
    }

    private void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
//        firebaseDatabase.setPersistenceEnabled(true);
//        dictionaryRef = FirebaseDatabase.getInstance().getReference("dictionary");
//        databaseRef = firebaseDatabase.getReferenceFromUrl("https://baldawordgame-default-rtdb.europe-west1.firebasedatabase.app/");
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            Intent logg = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(logg);
        }

        mainPager = findViewById(R.id.mainPager);
        tabLayoutMain = findViewById(R.id.tabLayoutMain);
        mainPagerAdapter = new ViewPagerAdapter(this);
        mainPager.setAdapter(mainPagerAdapter);
        View child = mainPager.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

//        dictionaryRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Iterator<DataSnapshot> dataSnapshotIterator = snapshot.getChildren().iterator();
//                while (dataSnapshotIterator.hasNext()) {
//                    dictionary.add(dataSnapshotIterator.next().getValue(String.class));
//                }
//                Log.d(TAG, "dictionary is set;");
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        new TabLayoutMediator(tabLayoutMain, mainPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(arrayList.get(position));
                    }
                }).attach();
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