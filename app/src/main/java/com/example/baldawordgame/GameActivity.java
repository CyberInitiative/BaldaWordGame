package com.example.baldawordgame;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.Map;
import java.util.regex.Pattern;

public class GameActivity extends AppCompatActivity {

    private final static String TAG = "GAME_ACTIVITY";
    public final static String CURRENT_GAME_ROOM_KEY = "CURRENT_GAME_ROOM_KEY";

    //region VIEWS
    private EditText inputReceiver;
    private RecyclerView recyclerViewPlayerDictionary;
    private RecyclerView recyclerViewOpponentDictionary;
    private RecyclerView recyclerViewShowPanel;
    private LinearLayout gameBoardLayout;
    private ImageButton buttonConfirmCombination;
    private Button buttonSkipTurn;
    private TextView textViewTimer;
    private TextView textViewPlayerDictionaryPlug;
    private TextView textViewOpponentDictionaryPlug;
    private LetterCellButton[][] letterCellButtons;
    //endregion

    private GameViewModel gameViewModel;
    private DictionaryAdapter playerDictionaryAdapter;
    private DictionaryAdapter opponentDictionaryAdapter;
    private ShowPanelAdapter showPanelAdapter;
    private InputMethodManager imm;
    private final TextWatcher inputReceiverTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (gameViewModel.getGameBoard().getCurrentLetterCell() != null && editable.toString().length() == 1 &&
                    Pattern.matches("[а-яА-Я]+", editable.toString())
            ) {
                gameViewModel.getGameBoard().writeMementoForLetterCell(gameViewModel.getGameBoard().getCurrentLetterCell());

                gameViewModel.getGameBoard().getCurrentLetterCell().setLetter(inputReceiver.getText().toString());
                gameViewModel.getGameBoard().getCurrentLetterCell().setState(LetterCell.LETTER_CELL_INTENDED_STATE);

                gameViewModel.getGameBoard().getCurrentLetterCell().notifySubscriberAboutStateChange();
                gameViewModel.getGameBoard().getCurrentLetterCell().notifySubscriberAboutLetterChange();

                gameViewModel.getGameBoard().setIntendedLetter(gameViewModel.getGameBoard().getCurrentLetterCell());
                gameViewModel.getGameBoard().setCurrentLetterCell(null);

                imm.hideSoftInputFromWindow(inputReceiver.getWindowToken(), 0);
            } else {
                editable.clear();
            }
        }
    };

    //region LiveData Observers
    private final Observer<Boolean> dataLoadedStateObserver = state -> {
        if (state) {
            Log.d(TAG, "INVOKED");
            createGameButtons(gameViewModel.getGameRoom().getGameGridSize());
            setObservers();
            addTextWatcherToInputReceiver();
            recyclersViewSetting();
            gameViewModel.turnOnVocabularyListener(playerDictionaryAdapter, opponentDictionaryAdapter);
            gameViewModel.getPlayersVocabulary().addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
                @Override
                public void onChanged(ObservableList sender) {

                }

                @Override
                public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {

                }

                @Override
                public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                    Log.d(TAG, "positionStart: " + positionStart + "; itemCount: " + itemCount);
                    for(int i = positionStart; i < (positionStart + itemCount); i++){
                        playerDictionaryAdapter.notifyItemInserted(i);
                    }
                }

                @Override
                public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {

                }

                @Override
                public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                    for(int i = positionStart; i < (positionStart + itemCount); i++){
                        playerDictionaryAdapter.notifyItemRemoved(i);
                    }
                }
            });
            gameViewModel.getOpponentsVocabulary().addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
                @Override
                public void onChanged(ObservableList sender) {

                }

                @Override
                public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {

                }

                @Override
                public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                    for(int i = positionStart; i < (positionStart + itemCount); i++){
                        opponentDictionaryAdapter.notifyItemInserted(i);
                    }
                }

                @Override
                public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {

                }

                @Override
                public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                    for(int i = positionStart; i < (positionStart + itemCount); i++){
                        opponentDictionaryAdapter.notifyItemRemoved(i);
                    }
                }
            });
            gameViewModel.getLettersCombination().addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
                @Override
                public void onChanged(ObservableList sender) {

                }

                @Override
                public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {

                }

                @Override
                public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                    for(int i = positionStart; i < (positionStart + itemCount); i++){
                        showPanelAdapter.notifyItemInserted(i);
                    }
                }

                @Override
                public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {

                }

                @Override
                public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                    for(int i = positionStart; i < (positionStart + itemCount); i++){
                        showPanelAdapter.notifyItemRemoved(i);
                    }
                }
            });

        }
    };

//    private final Observer<String> initialWordLiveDataObserver = new Observer<String>() {
//        @Override
//        public void onChanged(String initialWord) {
//            if (initialWord != null && !initialWord.equals(gameViewModel.getGameVocabulary().getInitialWord())) {
//                gameViewModel.getGameVocabulary().writeInitialWord(initialWord);
//            }
//        }
//    };
//    private final Observer<String> currentHostLiveDataObserver = new Observer<String>() {
//        @Override
//        public void onChanged(String currentHostKey) {
//            if (currentHostKey.equals(gameViewModel.getGameProcessData().getHostPlayerKey())) {
//                gameViewModel.getGameProcessData().setHostPlayerKey(currentHostKey);
//            }
//        }
//    };
//    private final Observer<String> keyOfPlayerWhoseTurnLiveDataObserver = new Observer<String>() {
//        @Override
//        public void onChanged(String keyOfPlayerWhoseTurn) {
//            if (keyOfPlayerWhoseTurn != null && keyOfPlayerWhoseTurn.equals(gameViewModel.getGameProcessData().getKeyOfPlayerWhoseTurnIt())) {
//                gameViewModel.getGameProcessData().setKeyOfPlayerWhoseTurnIt(keyOfPlayerWhoseTurn);
//            }
//        }
//    };
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        String gameRoomKey = intent.getStringExtra(GameActivity.CURRENT_GAME_ROOM_KEY);

        viewsSettings();
        gameViewModel = new ViewModelProvider(this, new GameViewModelFactory(gameRoomKey)).get(GameViewModel.class);
        gameViewModel.getDataLoadedStateLiveData().observe(GameActivity.this, dataLoadedStateObserver);
        Log.d(TAG, "GAME ROOM KEY FROM INTENT: " + gameRoomKey);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        visibilitySetting();
        if (gameViewModel != null) {
            if (gameViewModel.getDataLoadedStateLiveData().getValue() != null) {
                addTextWatcherToInputReceiver();
            }
        }
    }

    @Override
    protected void onStop() {
        if (gameViewModel != null) {
            if (gameViewModel.getDataLoadedStateLiveData().getValue() != null) {
                removeTextWatcherFromInputReceiver();
            }
        }
        super.onStop();
    }

    private void visibilitySetting() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void recyclersViewSetting() {
        playerDictionaryAdapter = new DictionaryAdapter(gameViewModel.getPlayersVocabulary());
        opponentDictionaryAdapter = new DictionaryAdapter(gameViewModel.getOpponentsVocabulary());

        recyclerViewPlayerDictionary.setAdapter(playerDictionaryAdapter);
        recyclerViewOpponentDictionary.setAdapter(opponentDictionaryAdapter);
        registerAdapterDataObserver(recyclerViewPlayerDictionary, textViewPlayerDictionaryPlug);
        registerAdapterDataObserver(recyclerViewOpponentDictionary, textViewOpponentDictionaryPlug);

        showPanelAdapter = new ShowPanelAdapter(gameViewModel.getGameBoard().getLettersCombination());
        recyclerViewShowPanel.setAdapter(showPanelAdapter);
    }

    private void registerAdapterDataObserver(@NonNull RecyclerView recyclerView, @NonNull TextView textView) {
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

    private void addTextWatcherToInputReceiver() {
        inputReceiver.addTextChangedListener(inputReceiverTextWatcher);
    }

    private void removeTextWatcherFromInputReceiver() {
        inputReceiver.removeTextChangedListener(inputReceiverTextWatcher);
    }

    private void setObservers() {
        Log.d(TAG, "setObservers() INVOKED");
        for (int i = 0; i < gameViewModel.getArrayListOfLetterCellLiveData().size(); i++) {
            gameViewModel.getArrayListOfLetterCellLiveData().get(i).observe(GameActivity.this, updatedCell -> {

                if (updatedCell != null) {

                    int row = updatedCell.getRowIndex();
                    int column = updatedCell.getColumnIndex();

                    LetterCell letterCellFromViewModel = gameViewModel.getGameBoard().getLetterCellByRowAndColumn(row, column);

                    if (letterCellFromViewModel != null) {
                        if (!gameViewModel.getGameBoard()
                                .checkIfLetterIsPartOfCombination(letterCellFromViewModel.getRowIndex(), letterCellFromViewModel.getColumnIndex())) {
                            gameViewModel.getGameBoard().writeMementoForLetterCell(letterCellFromViewModel);

                            Log.d(TAG, "setObservers(); onChanged(); LetterCell updatedCell: " + updatedCell);
                            letterCellFromViewModel.setStateAndNotifySubscriber(updatedCell.getState());
                            letterCellFromViewModel.setLetterAndNotifySubscriber(updatedCell.getLetter());
                        }

                    }
                }
            });
        }
    }

    private void viewsSettings() {
        inputReceiver = findViewById(R.id.input_receiver);
        gameBoardLayout = findViewById(R.id.game_board_layout);
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
        recyclerViewShowPanel.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        buttonConfirmCombination.setOnClickListener(click -> gameViewModel.confirmCombination());
    }

    private void createGameButtons(int gameGridSize) {

        letterCellButtons = new LetterCellButton[gameGridSize][gameGridSize];
        for (int i = 0; i < gameGridSize; i++) { //rows

            LinearLayout rowLinearLayout = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLinearLayout.setLayoutParams(rowParams);
            gameBoardLayout.addView(rowLinearLayout);

            for (int j = 0; j < gameGridSize; j++) { //columns

                LetterCellButton letterCellButton = new LetterCellButton(this);
                Log.d(TAG, "created: " + letterCellButton);
                letterCellButtons[i][j] = letterCellButton;

                LetterCell letterCell = gameViewModel
                        .getGameBoard().getLetterCellByRowAndColumn(i, j);

                Log.d(TAG, "createGameButtons(); LetterCell object is: " + letterCell);
                letterCell.addSubscriber(letterCellButton);
                letterCell.notifySubscriberAboutLetterChange();
                letterCell.notifySubscriberAboutStateChange();

                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f
                );
                buttonParams.width = 200;
                buttonParams.height = 200;
                buttonParams.setMargins(10, 10, 10, 10);
                letterCellButton.setLayoutParams(buttonParams);

                letterCellButton.setOnLongClickListener(event -> {
                    longClickHandling(letterCell);
                    return true;
                });

                letterCellButton.setOnClickListener(event -> clickHandling(letterCell));

                rowLinearLayout.addView(letterCellButton);
            }
        }
    }

    private void longClickHandling(@NonNull LetterCell letterCell) {
        if (letterCell.getState().equals(LetterCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE) ||
                letterCell.getState().equals(LetterCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE)) {

            gameViewModel.getGameBoard().eraseFromCombination(letterCell);

        } else if (letterCell.getState().equals(LetterCell.LETTER_CELL_INTENDED_STATE)) {
            gameViewModel.getGameBoard().getMementoForLetterCell(letterCell);
            letterCell.notifySubscriberAboutStateChange();
            letterCell.notifySubscriberAboutLetterChange();

            gameViewModel.getGameBoard().eraseAllFromCombination();
            gameViewModel.getGameBoard().setIntendedLetter(null);
        }
    }

    private void clickHandling(@NonNull LetterCell letterCell) {
        ObservableArrayList<LetterCell> combination = gameViewModel.getGameBoard().getLettersCombination();

        if (letterCell.getState().equals(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE)) {
            if (gameViewModel.getGameBoard().getIntendedLetter() == null) {
                inputReceiver.requestFocus();
                inputReceiver.setText(null);
                imm.toggleSoftInputFromWindow(inputReceiver.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                gameViewModel.getGameBoard().setCurrentLetterCell(letterCell);
            }
        } else if (letterCell.getState().equals(LetterCell.LETTER_CELL_WITH_LETTER_STATE)) {
            if (gameViewModel.getGameBoard().getIntendedLetter() != null) {
                if (!combination.isEmpty() &&
                        GameBoard.checkIfOneLetterIsCloseToAnother(combination.get(combination.size()-1), letterCell)) {
                    gameViewModel.getGameBoard().setLetterCellAsPartOfCombination(letterCell);
                    showPanelAdapter.notifyItemInserted(combination.size() - 1);
                } else if (combination.isEmpty()) {
                    gameViewModel.getGameBoard().setLetterCellAsPartOfCombination(letterCell);
                    showPanelAdapter.notifyItemInserted(combination.size() - 1);
                }
            }
        } else if (letterCell.getState().equals(LetterCell.LETTER_CELL_INTENDED_STATE)) {
            if (!combination.isEmpty() &&
                    GameBoard.checkIfOneLetterIsCloseToAnother(combination.get(combination.size()-1), letterCell)) {
                gameViewModel.getGameBoard().setIntendedLetterCellAsPartOfCombination(letterCell);
                showPanelAdapter.notifyItemInserted(combination.size() - 1);
            } else if (combination.isEmpty()) {
                gameViewModel.getGameBoard().setIntendedLetterCellAsPartOfCombination(letterCell);
                showPanelAdapter.notifyItemInserted(combination.size() - 1);
            }
        } else if (letterCell.getState().equals(LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)) {
            //Ignore
        }
    }

    @Override
    protected void onDestroy() {
        for(Map.Entry<DatabaseReference, LetterCell> entry : gameViewModel.getGameBoard().getLetterCellToRef().entrySet()){
            Log.d(TAG, "subscriber is: " + entry.getValue().getSubscriber());
            entry.getValue().removeSubscriber();
        }
        super.onDestroy();
    }
}