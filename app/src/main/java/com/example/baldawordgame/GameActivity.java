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

import com.example.baldawordgame.model.GameBoard;
import com.example.baldawordgame.model.LetterCell;
import com.example.baldawordgame.model.User;
import com.example.baldawordgame.view_adapter.DictionaryAdapter;
import com.example.baldawordgame.view_adapter.ShowPanelAdapter;
import com.example.baldawordgame.viewmodel.GameViewModel;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;
import java.util.regex.Pattern;

public class GameActivity extends AppCompatActivity {

    private final static String TAG = "GAME_ACTIVITY";
    public final static String CURRENT_GAME_ROOM_KEY = "CURRENT_GAME_ROOM_KEY";

    //region VIEWS
    private EditText inputReceiver;
    private RecyclerView recyclerViewPlayerDictionary, recyclerViewOpponentDictionary, recyclerViewShowPanel;
    private LinearLayout gameBoardLayout;
    private ImageButton buttonConfirmCombination;
    private Button buttonSkipTurn;
    private TextView textViewTimer;
    private TextView playerScore, opponentScore;
    private TextView textViewPlayerDictionaryPlug, textViewOpponentDictionaryPlug;
    private LetterCellButton[][] letterCellButtons;
    private long timerServerTimeOffset = 0;
    //endregion

    private GameViewModel gameViewModel;
    private DictionaryAdapter playerDictionaryAdapter, opponentDictionaryAdapter;
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
//            Log.d(TAG, "input: " + editable.toString());
//            if (editable.toString().length() > 1) {
//                String theStr = String.valueOf(editable.toString().charAt(editable.toString().length() - 1));
//                editable.clear();
//                editable.append(theStr);
//                Log.d(TAG, "input: " + editable);
//                gameViewModel.getGameBoard().getCurrentLetterCell().setLetter(editable.toString());
//                gameViewModel.getGameBoard().getCurrentLetterCell().notifySubscriberAboutLetterChange();
//            }
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
//    private final Observer<Boolean> dataLoadedStateObserver = state -> {
//        if (state) {
//            createGameButtons(gameViewModel.getGameRoom().getGameBoardSize());
//            setObservers();
//            addTextWatcherToInputReceiver();
//            recyclersViewSetting();
//        }
//    };

    private final Observer<String> keyOfPlayerWhoseTurnLiveDataObserver = new Observer<String>() {
        @Override
        public void onChanged(String keyOfPlayerWhoseTurn) {
            Log.d(TAG, "changed: " + keyOfPlayerWhoseTurn);
            Log.d(TAG, "Mykey: " + User.getPlayerUid());
            if (keyOfPlayerWhoseTurn != null && !keyOfPlayerWhoseTurn.equals(User.getPlayerUid())) {
                Log.d(TAG, "OPPONENT TURN;");
                for (int i = 0; i < letterCellButtons.length; i++) {
                    for (int j = 0; j < letterCellButtons[i].length; j++) {
                        letterCellButtons[i][j].setEnabled(false);
                    }
                }
            } else if (keyOfPlayerWhoseTurn != null && keyOfPlayerWhoseTurn.equals(User.getPlayerUid())) {
                Log.d(TAG, "MY TURN;");
                for (int i = 0; i < letterCellButtons.length; i++) {
                    for (int j = 0; j < letterCellButtons[i].length; j++) {
                        letterCellButtons[i][j].setEnabled(true);
                    }
                }
            }
        }
    };
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
        gameViewModel.getGameRoomFetchedStatus().observe(GameActivity.this, isGameRoomFetched -> {
            if (isGameRoomFetched) {
                Log.d(TAG, "gameRoomIsFetched");
                setStateObservers();
            }
        });

        Log.d(TAG, "GAME ROOM KEY FROM INTENT: " + gameRoomKey);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void setStateObservers() {
        Log.d(TAG, "setStateObservers();");

        gameViewModel.getTurnTimer().observe(GameActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(Long timeLeft) {
                long minutes = (timeLeft / 1000) / 60;
                long seconds = (timeLeft / 1000) % 60;
                String time = String.format("%s : %s", minutes, seconds);
                textViewTimer.setText(time);
                if (timeLeft < 0) {
                    gameViewModel.endTurn(TurnTerminationCode.TIME_IS_UP);
                }
            }
        });
        gameViewModel.getGameStageUniqueSnapshotLiveData().observe(GameActivity.this, gameStage -> {
            gameViewModel.reactToGameStageUpdated(gameStage);
        });

        gameViewModel.getDataConsumedStatus().observe(GameActivity.this, isDataConsumed -> {
            if (isDataConsumed) {
                Log.d(TAG, "isDataConsumed = " + isDataConsumed);
                createGameButtons(gameViewModel.getGameRoom().getGameBoardSize());
                setObservers();
                addTextWatcherToInputReceiver();
                recyclersViewSetting();

                gameViewModel.getTurnNewValueSnapshotLiveData().observe(GameActivity.this, turn -> {
                    if (turn != null) {
                        gameViewModel.getTurnTimer().startTimer(turn.getTurnStartedAt(), timerServerTimeOffset);
                        gameViewModel.setTurnInGameRoom(turn);
                    }
                });
            }
        });
        gameViewModel.getFirstPlayerScoreSnapshotLiveData().observe(GameActivity.this, value -> {
            if (value != null) {
                if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.getPlayerUid())) {
                    Log.d(TAG, "GAME PROCESS DATA: " + gameViewModel.getGameProcessData());
                    gameViewModel.getGameProcessData().setFirstPlayerScore(value);
                    playerScore.setText(value.toString());
                } else {
                    gameViewModel.getGameProcessData().setSecondPlayerScore(value);
                    opponentScore.setText(value.toString());
                }
            }
        });
        gameViewModel.getSecondPlayerScoreSnapshotLiveData().observe(GameActivity.this, value -> {
            if (value != null) {
                if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.getPlayerUid())) {
                    gameViewModel.getGameProcessData().setSecondPlayerScore(value);
                    playerScore.setText(value.toString());
                } else {
                    gameViewModel.getGameProcessData().setFirstPlayerScore(value);
                    opponentScore.setText(value.toString());
                }
            }
        });

        gameViewModel.getServerOffsetNewValueSnapshotLiveData().observe(GameActivity.this, timeOffset -> {
            timerServerTimeOffset = timeOffset;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        visibilitySetting();
        if (gameViewModel != null) {
            if (gameViewModel.getDataConsumedStatus().getValue() != null) {
                addTextWatcherToInputReceiver();
            }
        }
    }

    @Override
    protected void onStop() {
        if (gameViewModel != null) {
            if (gameViewModel.getDataConsumedStatus().getValue() != null) {
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
        gameViewModel.getGameBoard().getLettersCombination().addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
            @Override
            public void onChanged(ObservableList sender) {

            }

            @Override
            public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
                showPanelAdapter.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                showPanelAdapter.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {

            }

            @Override
            public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                showPanelAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
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

//        gameViewModel.getActivePlayerKeyLiveData().observe(GameActivity.this, keyOfPlayerWhoseTurnLiveDataObserver);

//        gameViewModel.getIntervalLiveData().observe(GameActivity.this, interval ->{
//            if(!(interval <= 0)) {
//                long minutes = (interval / 1000) / 60;
//                long seconds = (interval / 1000) % 60;
//                Log.d(TAG, "INTERVAL: " + interval);
////                String time = String.valueOf(minutes) + " : " + String.valueOf(seconds);
//                String time = String.format("%s : %s", minutes, seconds);
//                textViewTimer.setText(time);
//            }else {
//                gameViewModel.endTurn(TurnTerminationCode.TIME_IS_UP);
//                //INITIATE TURN END;
//            }
//        });
    }

    private void viewsSettings() {
        inputReceiver = findViewById(R.id.input_receiver);
        gameBoardLayout = findViewById(R.id.game_board_layout);
        buttonConfirmCombination = findViewById(R.id.buttonConfirmCombination);
        buttonSkipTurn = findViewById(R.id.buttonSkipTurn);
        textViewTimer = findViewById(R.id.textViewTimer);
        playerScore = findViewById(R.id.playerScore);
        opponentScore = findViewById(R.id.opponentScore);
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

        buttonConfirmCombination.setOnClickListener(click -> {
            if (!gameViewModel.endTurn(TurnTerminationCode.COMBINATION_SUBMITTED)) {
                Log.d(TAG, "endTurn: error");
            }
        });
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
                buttonParams.setMargins(5, 5, 5, 5);
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

            gameViewModel.getGameBoard().eraseEverything();
        }
    }

    private void clickHandling(@NonNull LetterCell letterCell) {
        ObservableArrayList<LetterCell> combination = gameViewModel.getGameBoard().getLettersCombination();

        if (letterCell.getState().equals(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE)) {
            if (gameViewModel.getGameBoard().getIntendedLetter() == null) {
                inputReceiver.requestFocus();
                inputReceiver.setText(null);
                imm.showSoftInput(inputReceiver, InputMethodManager.SHOW_IMPLICIT);
                gameViewModel.getGameBoard().setCurrentLetterCell(letterCell);
            }
        } else if (letterCell.getState().equals(LetterCell.LETTER_CELL_WITH_LETTER_STATE)) {
            if (gameViewModel.getGameBoard().getIntendedLetter() != null) {
                if (!combination.isEmpty() &&
                        GameBoard.checkIfOneLetterIsCloseToAnother(combination.get(combination.size() - 1), letterCell)) {
                    gameViewModel.getGameBoard().setLetterCellAsPartOfCombination(letterCell);
                } else if (combination.isEmpty()) {
                    gameViewModel.getGameBoard().setLetterCellAsPartOfCombination(letterCell);
                }
            }
        } else if (letterCell.getState().equals(LetterCell.LETTER_CELL_INTENDED_STATE)) {
            if (!combination.isEmpty() &&
                    GameBoard.checkIfOneLetterIsCloseToAnother(combination.get(combination.size() - 1), letterCell)) {
                gameViewModel.getGameBoard().setIntendedLetterCellAsPartOfCombination(letterCell);
            } else if (combination.isEmpty()) {
                gameViewModel.getGameBoard().setIntendedLetterCellAsPartOfCombination(letterCell);
            }
        } else if (letterCell.getState().equals(LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)) {
            //ignore
        }
    }

    @Override
    protected void onDestroy() {
        for (Map.Entry<DatabaseReference, LetterCell> entry : gameViewModel.getGameBoard().getRefToLetterCell().entrySet()) {
            Log.d(TAG, "subscriber is: " + entry.getValue().getSubscriber());
            entry.getValue().removeSubscriber();
        }
        super.onDestroy();
    }
}

/*
            gameViewModel.turnOnVocabularyListener();
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

 */