package com.example.baldawordgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class GameActivity extends AppCompatActivity {

    private final static String TAG = "GAME_ACTIVITY";
    public final static String CURRENT_GAME_ROOM_KEY = "CURRENT_GAME_ROOM_KEY";

    private DatabaseReference openRoomsRef = FirebaseDatabase.getInstance().getReference("openRooms");

    private boolean letterIsSet = false;
    private String currentGameRoomKey;
    private StringBuilder wordBuilder = new StringBuilder();
    private GameCell currentGameCell = null;

    private LinkedList<GameCell> lettersCombination = new LinkedList<>();
    private ArrayList<GameCell> deletedFromCombination = new ArrayList<>();

    private DictionaryAdapter playerDictionaryAdapter;
    private DictionaryAdapter opponentDictionaryAdapter;
    private ShowPanelAdapter showPanelAdapter;

    private RecyclerView recyclerViewPlayerDictionary;
    private RecyclerView recyclerViewOpponentDictionary;
    private RecyclerView recyclerViewShowPanel;

    private LinearLayout gameBoardLayout;
    private EditText inputReceiver;
    private InputMethodManager imm;

    private ImageButton buttonConfirmCombination;
    private Button buttonSkipTurn;

    private TextView textViewTimer;
    private TextView textViewPlayerDictionaryPlug;
    private TextView textViewOpponentDictionaryPlug;
    private GameViewModel gameViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        currentGameRoomKey = intent.getStringExtra(GameActivity.CURRENT_GAME_ROOM_KEY);
        Log.d(TAG, "INFO FROM INTENT: " + currentGameRoomKey);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        visibilityOptions();
    }

    private void init() {
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameBoardLayout = findViewById(R.id.game_board_layout);
        inputReceiver = findViewById(R.id.input_receiver);
        buttonConfirmCombination = findViewById(R.id.buttonConfirmCombination);
        buttonSkipTurn = findViewById(R.id.buttonSkipTurn);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewPlayerDictionaryPlug = findViewById(R.id.textViewPlayerDictionaryPlug);
        textViewOpponentDictionaryPlug = findViewById(R.id.textViewOpponentDictionaryPlug);
        recyclerViewPlayerDictionary = findViewById(R.id.recyclerViewPlayerDictionary);
        recyclerViewOpponentDictionary = findViewById(R.id.recyclerViewOpponentDictionary);
        recyclerViewPlayerDictionary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOpponentDictionary.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.transparent_divider));
        recyclerViewPlayerDictionary.addItemDecoration(dividerItemDecoration);
        recyclerViewOpponentDictionary.addItemDecoration(dividerItemDecoration);

        recyclerViewShowPanel = findViewById(R.id.recyclerViewShowPanel);
        showPanelAdapter = new ShowPanelAdapter(lettersCombination);
        recyclerViewShowPanel.setAdapter(showPanelAdapter);
        recyclerViewShowPanel.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        setButtonSkipTurnOnCLickListener();
        setButtonConfirmCombinationOnClickListener();
        addInputReceiverTextChangedListener();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        viewModelSettings();

    }

    private void dataSettings() {
        createGameGridButtons(gameViewModel.getGameRoom().getGameGridSize());
        playerDictionaryAdapter = new DictionaryAdapter(gameViewModel.getDictionaryManager().getPlayerFoundedWords());
        opponentDictionaryAdapter = new DictionaryAdapter(gameViewModel.getDictionaryManager().getOpponentFoundedWords());
        recyclerViewPlayerDictionary.setAdapter(playerDictionaryAdapter);
        recyclerViewOpponentDictionary.setAdapter(opponentDictionaryAdapter);
        registerAdapterDataObserver(recyclerViewPlayerDictionary, textViewPlayerDictionaryPlug);
        registerAdapterDataObserver(recyclerViewOpponentDictionary, textViewOpponentDictionaryPlug);
    }

    private void viewModelSettings() {
        if (gameViewModel.getGameRoom() == null && DictionaryManager.getDictionary().isEmpty()
                && gameViewModel.getGameCellsManager() == null) {
            Task<DataSnapshot> dictionaryTask = DictionaryManager.loadDictionaryFromFirebase();
            Task<GameRoom> gameRoomTask = GameRoom.getGameRoomFromFirebase(currentGameRoomKey);
            Task<List<Task<?>>> tasks = Tasks.whenAllComplete(dictionaryTask, gameRoomTask);
            tasks.addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                @Override
                public void onComplete(@NonNull Task<List<Task<?>>> task) {
                    if (task.isSuccessful()) {
                        GameRoom gameRoom = (GameRoom) task.getResult().get(1).getResult();
                        gameViewModel.setGameRoom(gameRoom);
                        GameCellsManager gameCellsManager = new GameCellsManager(gameRoom.getGameGridSize(), gameRoom.getGameRoomKey());
                        gameViewModel.setGameCellsManager(gameCellsManager);
                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String snapshotData = snapshot.getValue(String.class);
                                if (snapshotData != null) {
                                    gameCellsManager.applyInitialWord(snapshotData);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };
                        gameRoom.addInitialWordListener(valueEventListener);
                        gameRoom.startInitialWordListening();
                        if (gameRoom.getCurrentHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            String initialWord = gameViewModel.getDictionaryManager().getRandomWord(gameRoom.getGameGridSize());
//                            gameViewModel.getDictionaryManager().getPlayerFoundedWords().add(new FoundedWord(initialWord, null));
                            gameRoom.getInitialWordRef().setValue(initialWord);
                        }
                        dataSettings();
                    }
                }
            });
        } else {
            dataSettings();
        }
    }

    private void setButtonSkipTurnOnCLickListener() {
        buttonSkipTurn.setOnClickListener(event -> {
        });
    }

    private void setButtonConfirmCombinationOnClickListener() {
        buttonConfirmCombination.setOnClickListener(event -> {
            if (!lettersCombination.isEmpty()) {
                wordBuilder.setLength(0);
                boolean intendedLetterMet = false;
                for (GameCell gameCell : lettersCombination) {
                    if (gameCell.getCellState()
                            .equals(GameCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE)) {
                        intendedLetterMet = true;
                    }
                    Log.d(TAG, "setButtonConfirmCombinationOnClickListener(); " + gameCell.getLetterInCell());
                    wordBuilder.append(gameCell.getLetterInCell());
                }
                if (!intendedLetterMet) {
                    return;
                }
                String word = wordBuilder.toString();
                if (gameViewModel.getDictionaryManager().confirmWord(word).
                        equals(DictionaryManager.WordCheckResult.NEW_FOUND_WORD)) {
                } else if (gameViewModel.getDictionaryManager().confirmWord(word).
                        equals(DictionaryManager.WordCheckResult.NEW_FOUND_WORD)) {
                }

//                if (currentGameRoom.confirmWord(wordBuilder.toString()) != GameRoom.WordCheckResult.NEW_FOUND_WORD) {
//                    playerDictionaryArrayList.add(0, wordBuilder.toString());
//                    recyclerViewPlayerDictionary.smoothScrollToPosition(0);
//                    playerDictionaryAdapter.notifyItemInserted(0);
//                    turnIsOverWithConfirmedWord();
//                }
            }
        });
    }

    private void registerAdapterDataObserver(RecyclerView recyclerView, TextView textView) {
        recyclerView.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (recyclerView.getAdapter().getItemCount() > 0) {
                    textView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if (recyclerView.getAdapter().getItemCount() == 0) {
                    textView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addInputReceiverTextChangedListener() {
        inputReceiver.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputReceiver.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (currentGameCell != null && inputReceiver.getText().toString().length() == 1 &&
                        !inputReceiver.getText().toString().equals(" ")
                        && Pattern.matches("[а-яА-Я]+", inputReceiver.getText().toString())) {

                    currentGameCell.setLetterInCell(inputReceiver.getText().toString());
                    imm.hideSoftInputFromWindow(inputReceiver.getWindowToken(), 0);
                    currentGameCell.setCellState(GameCell.LETTER_CELL_INTENDED_STATE);
                    currentGameCell.notifySubscriberAboutStateChange();
                    currentGameCell.notifySubscriberAboutLetterChange();

                    letterIsSet = true;
                    currentGameCell = null;

                } else {
                    inputReceiver.getText().clear();
                }
            }
        });
    }

    private void createGameGridButtons(int gameGridSize) {
        for (int i = 0; i < gameGridSize; i++) {
            LinearLayout createdLayout = new LinearLayout(this);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            createdLayout.setOrientation(LinearLayout.HORIZONTAL);
            createdLayout.setLayoutParams(param);
            gameBoardLayout.addView(createdLayout);
            for (int j = 0; j < gameGridSize; j++) {
                LetterCell letterCell = new LetterCell(this);
                GameCell gameCell = gameViewModel.getGameCellsManager().getLetterCellByColumnAndRowIndex(i, j);
                gameCell.addSubscriber(letterCell);
                Log.d(TAG, "THE GAME CELL " + gameCell);
                gameCell.notifySubscriberAboutLetterChange();
                gameCell.notifySubscriberAboutStateChange();


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                params.setMargins(10, 10, 10, 10);
                letterCell.setLayoutParams(params);
                letterCell.setOnLongClickListener(event -> {
                    longClickHandling(gameCell);
                    return true;
                });
                letterCell.setOnClickListener(event -> {
                    clickHandling(gameCell);
                });
                createdLayout.addView(letterCell);
                Log.d(TAG, "Created");
            }
        }
    }

    private void longClickHandling(GameCell gameCell) {
        if (gameCell.getCellState().equals(GameCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE) ||
                gameCell.getCellState().equals(GameCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE)) {
            for (int i = lettersCombination.size() - 1; i >= 0; i--) {
                if (lettersCombination.get(i) != gameCell) {
                    lettersCombination.get(i).getBackToPreviousState();
                    lettersCombination.get(i).notifySubscriberAboutStateChange();
                    deletedFromCombination.add(lettersCombination.get(i));
                } else if (lettersCombination.get(i) == gameCell) {
                    gameCell.getBackToPreviousState();
                    gameCell.notifySubscriberAboutStateChange();
                    deletedFromCombination.add(gameCell);
                    break;
                }
            }
            if (!deletedFromCombination.isEmpty()) {
                for (int i = 0; i < deletedFromCombination.size(); i++) {
                    for (int j = 0; j < lettersCombination.size(); j++) {
                        if (deletedFromCombination.get(i).equals(lettersCombination.get(j))) {
                            lettersCombination.remove(lettersCombination.get(j));
                            showPanelAdapter.notifyItemRemoved(j);
                            break;
                        }
                    }
                }
                deletedFromCombination.clear();
            }
        } else if (gameCell.getCellState().equals(GameCell.LETTER_CELL_INTENDED_STATE)) {
            gameCell.setLetterInCell(null);
            gameCell.setCellState(GameCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
            gameCell.notifySubscriberAboutStateChange();
            gameCell.notifySubscriberAboutLetterChange();
            if (!lettersCombination.isEmpty()) {
                for (int i = lettersCombination.size(); i-- > 0; ) {
                    lettersCombination.get(i).getBackToPreviousState();
                    lettersCombination.get(i).notifySubscriberAboutStateChange();
                    lettersCombination.remove(i);
                    showPanelAdapter.notifyItemRemoved(i);
                }
            }
            letterIsSet = false;
        }
    }

    private void clickHandling(GameCell gameCell) {
        if (gameCell.getCellState().equals(GameCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)) {
            //Ignore
        } else if (gameCell.getCellState().equals(GameCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE)) {
            if (!letterIsSet) {
                inputReceiver.requestFocus();
                inputReceiver.setText(null);
                imm.toggleSoftInputFromWindow(inputReceiver.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                currentGameCell = gameCell;
            }
        } else if (gameCell.getCellState().equals(GameCell.LETTER_CELL_WITH_LETTER_STATE)) {
            if (letterIsSet) {
                if (!lettersCombination.isEmpty() && gameViewModel.getGameCellsManager().checkIfOneLetterIsCloseToAnother(lettersCombination.getLast(), gameCell)) {
                    setLetterCellAsPartOfCombination(gameCell);
                } else if (lettersCombination.isEmpty()) {
                    setLetterCellAsPartOfCombination(gameCell);
                }
            }
        } else if (gameCell.getCellState().equals(GameCell.LETTER_CELL_INTENDED_STATE)) {
            if (!lettersCombination.isEmpty() && gameViewModel.getGameCellsManager().checkIfOneLetterIsCloseToAnother(lettersCombination.getLast(), gameCell)) {
                setIntendedLetterCellAsPartOfCombination(gameCell);
            } else if (lettersCombination.isEmpty()) {
                setIntendedLetterCellAsPartOfCombination(gameCell);
            }
        }
    }

    private void setIntendedLetterCellAsPartOfCombination(GameCell gameCell) {
        gameCell.setCellState(GameCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE);
        gameCell.notifySubscriberAboutStateChange();
        lettersCombination.addLast(gameCell);
        showPanelAdapter.notifyItemInserted(lettersCombination.size() - 1);
    }

    private void setLetterCellAsPartOfCombination(GameCell gameCell) {
        gameCell.setCellState(GameCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE);
        gameCell.notifySubscriberAboutStateChange();
        lettersCombination.addLast(gameCell);
        showPanelAdapter.notifyItemInserted(lettersCombination.size() - 1);
    }

    private void turnIsOverWithConfirmedWord() {
//        for (GameCell gameCell : gameCells) {
//            if (gameCell.getCellState().equals(GameCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE)
//                    || gameCell.getCellState().equals(GameCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE)) {
//                gameCell.setCellState(GameCell.LETTER_CELL_WITH_LETTER_STATE);
//            }
//        }
        for (int i = lettersCombination.size(); i-- > 0; ) {
            lettersCombination.remove(i);
            showPanelAdapter.notifyItemRemoved(i);
        }
        letterIsSet = false;
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
