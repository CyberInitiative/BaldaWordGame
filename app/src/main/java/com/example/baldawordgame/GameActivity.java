package com.example.baldawordgame;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baldawordgame.databinding.ActivityGameBinding;
import com.example.baldawordgame.fragment.DefaultGameEndDialogFragment;
import com.example.baldawordgame.fragment.EnemySurrendersAndLeavesDialogFragment;
import com.example.baldawordgame.fragment.SkipTurnDialogFragment;
import com.example.baldawordgame.fragment.SurrenderAndLeaveDialogFragment;
import com.example.baldawordgame.fragment.SurrenderDialogFragment;
import com.example.baldawordgame.model.FoundWord;
import com.example.baldawordgame.model.GameBoard;
import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.GameRoom;
import com.example.baldawordgame.model.LetterCell;
import com.example.baldawordgame.model.User;
import com.example.baldawordgame.view.LetterCellButton;
import com.example.baldawordgame.view_adapter.DictionaryAdapter;
import com.example.baldawordgame.view_adapter.ShowPanelAdapter;
import com.example.baldawordgame.viewmodel.GameViewModel;
import com.example.baldawordgame.viewmodel_factory.GameViewModelFactory;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class GameActivity extends AppCompatActivity
        implements SurrenderAndLeaveDialogFragment.SurrenderAndLeaveDialogAnswerListener,
        SurrenderDialogFragment.SurrenderDialogAnswerListener,
        EnemySurrendersAndLeavesDialogFragment.EnemySurrendersAndLeavesAnswerListener,
        SkipTurnDialogFragment.SkipTurnDialogAnswerListener,
        DefaultGameEndDialogFragment.RematchAnswerListener {

    private final static String TAG = "GAME_ACTIVITY";
    public final static String CURRENT_GAME_ROOM_KEY = "CURRENT_GAME_ROOM_KEY";
    private long timerServerTimeOffset = 0;

    private ActivityGameBinding binding;
    private LetterCellButton[][] letterCellButtons;

    private GameViewModel gameViewModel;
    private DictionaryAdapter playerDictionaryAdapter, opponentDictionaryAdapter;
    private ArrayList<FoundWord> playerFoundWords = new ArrayList<>();
    private ArrayList<FoundWord> opponentFoundWords = new ArrayList<>();
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

                gameViewModel.getGameBoard().getCurrentLetterCell()
                        .setLetter(binding.inputReceiver.getText().toString().toLowerCase());
                gameViewModel.getGameBoard().getCurrentLetterCell().setState(LetterCell.LETTER_CELL_INTENDED_STATE);

                gameViewModel.getGameBoard().getCurrentLetterCell().notifySubscriberAboutStateChange();
                gameViewModel.getGameBoard().getCurrentLetterCell().notifySubscriberAboutLetterChange();

                gameViewModel.getGameBoard().setIntendedLetter(gameViewModel.getGameBoard().getCurrentLetterCell());
                gameViewModel.getGameBoard().setCurrentLetterCell(null);

                imm.hideSoftInputFromWindow(binding.inputReceiver.getWindowToken(), 0);
            } else {
                editable.clear();
            }
        }
    };

    private void initViewsSetting() {
        binding.buttonDictionaryCheck.setEnabled(false);
        binding.buttonConfirmCombination.setEnabled(false);
        binding.skipTurnButton.setEnabled(false);
        binding.buttonSurrender.setEnabled(false);

        binding.recyclerViewPlayerDictionary.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewOpponentDictionary.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.transparent_divider));

        binding.recyclerViewPlayerDictionary.addItemDecoration(dividerItemDecoration);
        binding.recyclerViewOpponentDictionary.addItemDecoration(dividerItemDecoration);

        binding.recyclerViewShowPanel.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        binding.buttonConfirmCombination.setOnClickListener(click -> {
            if (!gameViewModel.endTurn(TurnTerminationCode.COMBINATION_SUBMITTED)) {
                Log.d(TAG, "endTurn: error");
            }
//            else if(){
//
//            }
        });
        binding.buttonSurrender.setOnClickListener(click -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            SurrenderDialogFragment surrenderDialogFragment = new SurrenderDialogFragment();
            surrenderDialogFragment.setCancelable(true);
            surrenderDialogFragment.show(fragmentManager, SurrenderDialogFragment.TAG);
        });
        binding.skipTurnButton.setOnClickListener(click -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            SkipTurnDialogFragment skipTurnDialogFragment = new SkipTurnDialogFragment();
            skipTurnDialogFragment.setCancelable(true);
            skipTurnDialogFragment.show(fragmentManager, SkipTurnDialogFragment.TAG);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Log.d(TAG, "onCreate() called");

        Intent intent = getIntent();
        String gameRoomKey = intent.getStringExtra(GameActivity.CURRENT_GAME_ROOM_KEY);

        initViewsSetting();
        setVisibilityForLoadingProgressViews(View.VISIBLE);
        setVisibilityForGameViews(View.GONE);

        gameViewModel = new ViewModelProvider(this, new GameViewModelFactory(gameRoomKey)).get(GameViewModel.class);

        gameViewModel.getGameRoomFetchedStatus().observe(GameActivity.this, isGameRoomFetched -> {
            if (isGameRoomFetched) {
                setLiveDataObserversOnGameRoomFetchedTrueStatus();
            }
        });
        gameViewModel.getDataConsumedStatus().observe(GameActivity.this, isDataConsumed -> {
            if (isDataConsumed) {
                createGameButtons(gameViewModel.getGameRoom().getGameBoardSize());
                setLetterCellLiveDataObservers();
                addTextWatcherToInputReceiver();
                recyclersViewSetting();
                setLiveDataObserversOnDataConsumedTrueStatus();

                binding.buttonDictionaryCheck.setEnabled(true);
                binding.buttonSurrender.setEnabled(true);

                setVisibilityForLoadingProgressViews(View.GONE);
                setVisibilityForGameViews(View.VISIBLE);
            }
        });

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void setVisibilityForLoadingProgressViews(int visibilityCode) {
        binding.textViewLoading.setVisibility(visibilityCode);
        binding.progressBarLoading.setVisibility(visibilityCode);
    }

    private void setVisibilityForGameViews(int visibilityCode) {
        binding.inputReceiver.setVisibility(visibilityCode);
        binding.topLinearLayout.setVisibility(visibilityCode);
        binding.gameBoardLayout.setVisibility(visibilityCode);
        binding.recyclerViewShowPanel.setVisibility(visibilityCode);
        binding.playersGameStatistic.setVisibility(visibilityCode);
        binding.bottomLinearLayout.setVisibility(visibilityCode);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
        setUIOptions();
        if (gameViewModel != null) {
            if (gameViewModel.getDataConsumedStatus().getValue() != null) {
                addTextWatcherToInputReceiver();
            }
            if (gameViewModel.getGameVocabulary() != null) {
                gameViewModel.getGameVocabulary().setGameVocabularyListener();
            }
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() called");
        if (gameViewModel != null) {
            if (gameViewModel.getDataConsumedStatus().getValue() != null) {
                removeTextWatcherFromInputReceiver();
            }
            if (gameViewModel.getGameVocabulary() != null) {
                gameViewModel.getGameVocabulary().removeGameVocabularyListener();
            }
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onDestroy() {
        for (Map.Entry<DatabaseReference, LetterCell> entry : gameViewModel.getGameBoard().getRefToLetterCell().entrySet()) {
//            Log.d(TAG, "subscriber is: " + entry.getValue().getSubscriber());
            entry.getValue().removeSubscriber();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            new SurrenderAndLeaveDialogFragment().show(fragmentManager, SurrenderAndLeaveDialogFragment.TAG);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setLiveDataObserversOnGameRoomFetchedTrueStatus() {
        gameViewModel.getHostPlayerUIDNewValueSnapshotLiveData().observe(GameActivity.this, hostUID -> {
            if (!gameViewModel.getGameRoom().getHostPlayerUID().equals(hostUID)) {
                gameViewModel.getGameRoom().setHostPlayerUID(hostUID);
                if (gameViewModel.getGameRoom().getDataState().equals(GameRoom.DataStatus.DATA_NOT_PREPARED)) {
                    gameViewModel.reactToGameStageUpdated(GameRoom.DataStatus.DATA_NOT_PREPARED);
                }
            }
        });
        gameViewModel.getDataStateNewValueSnapshotLiveData().observe(GameActivity.this, dataState -> {
            if (!gameViewModel.getGameRoom().getDataState().equals(dataState)) {
                gameViewModel.getGameRoom().setDataState(dataState);
            }
            gameViewModel.reactToGameStageUpdated(dataState);
        });
        gameViewModel.getTurnTimer().observe(GameActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(Long timeLeft) {
                long minutes = (timeLeft / 1000) / 60;
                long seconds = (timeLeft / 1000) % 60;
                String time = String.format("%s : %s", minutes, seconds);
                binding.textViewTimer.setText(time);
                if (timeLeft < 0) {
                    //новый ход записывает тот игрок, чья очередь сейчас ходить; если этот игрок свернул игру, за него
                    //новый ход запишет хост;
                    //не реализовано.
                    if(gameViewModel.getActivePlayerStatusCode() == GameProcessData.PlayerStatusCode.IN_BACKGROUND.getValue()){

                    }

                    if(gameViewModel.getGameProcessData().getTurn().getActivePlayerKey().equals(User.fetchPlayerUID())) {
                        gameViewModel.endTurn(TurnTerminationCode.TIME_IS_UP);
                    }
                }
            }
        });
    }

    private void setLiveDataObserversOnDataConsumedTrueStatus() {
        gameViewModel.getTurnNewValueSnapshotLiveData().observe(GameActivity.this, turn -> {
            if (turn != null && gameViewModel.getGameProcessData().getTurn() != turn) {

                if (gameViewModel.getGameBoard().checkIfThereIsAvailableLetterCell()) {
                    gameViewModel.getGameProcessData().setTurn(turn);
                    gameViewModel.getTurnTimer().startTimer(turn.getTurnStartedAt(), timerServerTimeOffset);

                    if (!turn.getActivePlayerKey().equals(User.fetchPlayerUID())) {
                        binding.activePlayerLabel.setText(R.string.opponent_active_turn);
                        setLetterCellButtonsEnabled(false);
                        binding.skipTurnButton.setEnabled(false);
                        binding.buttonConfirmCombination.setEnabled(false);
                    } else {
                        binding.activePlayerLabel.setText(R.string.player_active_turn);
                        setLetterCellButtonsEnabled(true);
                        binding.skipTurnButton.setEnabled(true);
                        binding.buttonConfirmCombination.setEnabled(true);
                    }
                } else {
                    //count points and send endcode;
                }

            }
        });
        gameViewModel.getFirstPlayerScoreNewValueSnapshotLiveData().observe(GameActivity.this, value -> {
            if (value != null) {
                gameViewModel.getGameProcessData().setFirstPlayerScore(value);
                if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.fetchPlayerUID())) {
                    binding.playerScore.setText(String.valueOf(gameViewModel.getGameProcessData().getFirstPlayerScore()));
                } else {
                    binding.opponentScore.setText(String.valueOf(gameViewModel.getGameProcessData().getFirstPlayerScore()));
                }
            }
        });
        gameViewModel.getSecondPlayerScoreNewValueSnapshotLiveData().observe(GameActivity.this, value -> {
            if (value != null) {
                gameViewModel.getGameProcessData().setSecondPlayerScore(value);
                if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.fetchPlayerUID())) {
                    binding.playerScore.setText(String.valueOf(gameViewModel.getGameProcessData().getSecondPlayerScore()));
                } else {
                    binding.opponentScore.setText(String.valueOf(gameViewModel.getGameProcessData().getSecondPlayerScore()));
                }
            }
        });
        gameViewModel.getFirstPlayerSkippedTurnsNewValueSnapshotLiveData().observe(GameActivity.this, value -> {
            if (value != null) {
                gameViewModel.getGameProcessData().setFirstPlayerSkippedTurns(value);
                switch (value) {
                    case 0:
                        break;
                    case 1:
                        if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.fetchPlayerUID())) {
                            binding.playerFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        } else {
                            binding.opponentFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        }
                        break;
                    case 2:
                        if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.fetchPlayerUID())) {
                            binding.playerFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                            binding.playerSecondSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        } else {
                            binding.opponentFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                            binding.opponentSecondSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        }
                        break;
                    case 3:
                        if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.fetchPlayerUID())) {
                            binding.playerFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                            binding.playerSecondSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                            binding.playerThirdSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        } else {
                            binding.opponentFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                            binding.opponentSecondSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                            binding.opponentThirdSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        }
                        break;
                    default:
                        gameViewModel.getGameProcessData().writeGameOverCode(GameProcessData.GameOverCode.PLAYER_TWO_WIN);
                        //send end code;
                        break;
                }
            }
        });
        gameViewModel.getSecondPlayerSkippedTurnsNewValueSnapshotLiveData().observe(GameActivity.this, value -> {
            if (value != null) {
                gameViewModel.getGameProcessData().setSecondPlayerSkippedTurns(value);
                switch (value) {
                    case 0:
                        break;
                    case 1:
                        if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.fetchPlayerUID())) {
                            binding.playerFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        } else {
                            binding.opponentFirstSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        }
                        break;
                    case 2:
                        if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.fetchPlayerUID())) {
                            binding.playerSecondSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        } else {
                            binding.opponentSecondSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        }
                        break;
                    case 3:
                        if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.fetchPlayerUID())) {
                            binding.playerThirdSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        } else {
                            binding.opponentThirdSkippedTurn.setImageResource(R.drawable.skip_turn_icon);
                        }
                        break;
                    default:
                        gameViewModel.getGameProcessData().writeGameOverCode(GameProcessData.GameOverCode.PLAYER_ONE_WIN);
                        //send end code;
                        break;
                }
            }
        });
        gameViewModel.getGameOverCodeNewValueSnapshotLiveData().observe(GameActivity.this, value -> {
            if (value != null) {
                gameViewModel.getTurnTimer().stopTimer();
                if (value == GameProcessData.GameOverCode.PLAYER_TWO_SURRENDER_AND_LEAVE.getValue()) {
                    if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.fetchPlayerUID())) {
//                                gameViewModel.getTurnTimer().stopTimer();
                        EnemySurrendersAndLeavesDialogFragment enemySurrendersAndLeavesDialogFragment
                                = new EnemySurrendersAndLeavesDialogFragment();
                        enemySurrendersAndLeavesDialogFragment.setCancelable(false);
                        enemySurrendersAndLeavesDialogFragment.show(getSupportFragmentManager(), SurrenderDialogFragment.TAG);
                    }
                } else if (value == GameProcessData.GameOverCode.PLAYER_ONE_SURRENDER_AND_LEAVE.getValue()) {
                    if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.fetchPlayerUID())) {
//                                gameViewModel.getTurnTimer().stopTimer();
                        EnemySurrendersAndLeavesDialogFragment enemySurrendersAndLeavesDialogFragment
                                = new EnemySurrendersAndLeavesDialogFragment();
                        enemySurrendersAndLeavesDialogFragment.setCancelable(false);
                        enemySurrendersAndLeavesDialogFragment.show(getSupportFragmentManager(), SurrenderDialogFragment.TAG);
                    }
                } else if (value == GameProcessData.GameOverCode.PLAYER_ONE_WIN.getValue()) {
                    Bundle bundle = new Bundle();
                    DefaultGameEndDialogFragment defaultGameEndDialogFragment = new DefaultGameEndDialogFragment();
                    if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.fetchPlayerUID())) {
                        bundle.putInt(DefaultGameEndDialogFragment.GAME_RESULT_KEY, DefaultGameEndDialogFragment.PLAYER_WIN_CODE);
                    } else if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.fetchPlayerUID())) {
                        bundle.putInt(DefaultGameEndDialogFragment.GAME_RESULT_KEY, DefaultGameEndDialogFragment.PLAYER_LOSE_CODE);
                    }
                    defaultGameEndDialogFragment.setArguments(bundle);
                    defaultGameEndDialogFragment.setCancelable(false);
                    defaultGameEndDialogFragment
                            .show(getSupportFragmentManager(), DefaultGameEndDialogFragment.TAG);
                } else if (value == GameProcessData.GameOverCode.PLAYER_TWO_WIN.getValue()) {
                    Bundle bundle = new Bundle();
                    DefaultGameEndDialogFragment defaultGameEndDialogFragment = new DefaultGameEndDialogFragment();
                    if (gameViewModel.getGameRoom().getFirstPlayerUID().equals(User.fetchPlayerUID())) {
                        bundle.putInt(DefaultGameEndDialogFragment.GAME_RESULT_KEY, DefaultGameEndDialogFragment.PLAYER_LOSE_CODE);
                    } else if (gameViewModel.getGameRoom().getSecondPlayerUID().equals(User.fetchPlayerUID())) {
                        bundle.putInt(DefaultGameEndDialogFragment.GAME_RESULT_KEY, DefaultGameEndDialogFragment.PLAYER_WIN_CODE);
                    }
                    defaultGameEndDialogFragment.setArguments(bundle);
                    defaultGameEndDialogFragment.setCancelable(false);
                    defaultGameEndDialogFragment
                            .show(getSupportFragmentManager(), DefaultGameEndDialogFragment.TAG);
                }
            }
        });
        gameViewModel.getServerOffsetNewValueSnapshotLiveData().observe(GameActivity.this, timeOffset -> {
            timerServerTimeOffset = timeOffset;
        });
    }

    private void setLetterCellLiveDataObservers() {
//        Log.d(TAG, "setObservers() INVOKED");
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

//                            Log.d(TAG, "setObservers(); onChanged(); LetterCell updatedCell: " + updatedCell);
                            letterCellFromViewModel.setStateAndNotifySubscriber(updatedCell.getState());
                            letterCellFromViewModel.setLetterAndNotifySubscriber(updatedCell.getLetter());
                        }

                    }
                }
            });
        }
    }

    private void setUIOptions() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void recyclersViewSetting() {
        playerDictionaryAdapter = new DictionaryAdapter(playerFoundWords);
        opponentDictionaryAdapter = new DictionaryAdapter(opponentFoundWords);

        gameViewModel.getGameVocabulary().setGameVocabularyListener();
        gameViewModel.getGameVocabulary().getFoundWords().addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
            @Override
            public void onChanged(ObservableList sender) {

            }

            @Override
            public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {

            }

            @Override
            public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
//                Log.d(TAG, "onItemRangeInserted");
//                Log.d(TAG, "POSITION START: " + positionStart + "; ITEM COUNT: " + itemCount);
                for (int i = positionStart; i < (positionStart + itemCount); i++) {
                    FoundWord word = (FoundWord) sender.get(positionStart);
//                    Log.d(TAG, "onItemRangeInserted(); word: " + word);

                    if (word.getPlayerKey().equals(User.fetchPlayerUID())) {
                        playerFoundWords.add(word);
//                        Log.d(TAG, "playerFoundWords.add(word); word: " + word);
                        playerDictionaryAdapter.notifyItemInserted(playerFoundWords.size() - 1);
                    } else if (word.getPlayerKey().equals(gameViewModel.getGameRoom().getMyOpponentKey())) {
                        opponentFoundWords.add(word);
//                        Log.d(TAG, "opponentFoundWords.add(word); word: " + word);
                        opponentDictionaryAdapter.notifyItemInserted(opponentFoundWords.size() - 1);
                    }
                }

            }

            @Override
            public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {

            }

            @Override
            public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                for (int i = positionStart; i < itemCount; i++) {
                    FoundWord word = (FoundWord) sender.get(positionStart);
                    if (word.getPlayerKey().equals(User.fetchPlayerUID())) {
                        playerFoundWords.remove(word);
                        playerDictionaryAdapter.notifyItemRemoved(playerFoundWords.size() - 1);
                    }
                }
            }
        });

        binding.recyclerViewPlayerDictionary.setAdapter(playerDictionaryAdapter);
        binding.recyclerViewOpponentDictionary.setAdapter(opponentDictionaryAdapter);
        registerAdapterDataObserver(binding.recyclerViewPlayerDictionary, binding.textViewPlayerDictionaryPlug);
        registerAdapterDataObserver(binding.recyclerViewOpponentDictionary, binding.textViewOpponentDictionaryPlug);

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
        binding.recyclerViewShowPanel.setAdapter(showPanelAdapter);
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
        binding.inputReceiver.addTextChangedListener(inputReceiverTextWatcher);
    }

    private void removeTextWatcherFromInputReceiver() {
        binding.inputReceiver.removeTextChangedListener(inputReceiverTextWatcher);
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
            binding.gameBoardLayout.addView(rowLinearLayout);

            for (int j = 0; j < gameGridSize; j++) { //columns

                LetterCellButton letterCellButton = new LetterCellButton(this);
//                Log.d(TAG, "created: " + letterCellButton);
                letterCellButtons[i][j] = letterCellButton;

                LetterCell letterCell = gameViewModel
                        .getGameBoard().getLetterCellByRowAndColumn(i, j);

//                Log.d(TAG, "createGameButtons(); LetterCell object is: " + letterCell);
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

        switch (letterCell.getState()) {
            case LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE:
                if (gameViewModel.getGameBoard().getIntendedLetter() == null) {
                    binding.inputReceiver.requestFocus();
                    binding.inputReceiver.setText(null);
                    imm.showSoftInput(binding.inputReceiver, InputMethodManager.SHOW_IMPLICIT);
                    gameViewModel.getGameBoard().setCurrentLetterCell(letterCell);
                }
                break;
            case LetterCell.LETTER_CELL_WITH_LETTER_STATE:
                if (gameViewModel.getGameBoard().getIntendedLetter() != null) {
                    if (!combination.isEmpty() &&
                            GameBoard.checkIfOneLetterIsCloseToAnother(combination.get(combination.size() - 1), letterCell)) {
                        gameViewModel.getGameBoard().setLetterCellAsPartOfCombination(letterCell);
                    } else if (combination.isEmpty()) {
                        gameViewModel.getGameBoard().setLetterCellAsPartOfCombination(letterCell);
                    }
                }
                break;
            case LetterCell.LETTER_CELL_INTENDED_STATE:
                if (!combination.isEmpty() &&
                        GameBoard.checkIfOneLetterIsCloseToAnother(combination.get(combination.size() - 1), letterCell)) {
                    gameViewModel.getGameBoard().setIntendedLetterCellAsPartOfCombination(letterCell);
                } else if (combination.isEmpty()) {
                    gameViewModel.getGameBoard().setIntendedLetterCellAsPartOfCombination(letterCell);
                }
                break;
            case LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE:
                //ignore
                break;
        }
    }

    private void setLetterCellButtonsEnabled(boolean enabled) {
        for (int j = 0; j < letterCellButtons[0].length; j++) {
            for (int i = 0; i < letterCellButtons.length; i++) {
                letterCellButtons[i][j].setEnabled(enabled);
            }
        }
    }

    @Override
    public void onSurrenderAndLeaveYesAnswer(DialogFragment dialog) {
        Log.d(TAG, "onSurrenderAndLeaveYesAnswer");
        gameViewModel.surrenderAndLeave();
        User.eraseJoinedGameRoomKey();
        finish();
    }

    @Override
    public void onSurrenderAndLeaveNoAnswer(DialogFragment dialog) {
        Log.d(TAG, "onSurrenderAndLeaveNoAnswer");
    }

    @Override
    public void onSurrenderYesAnswer(DialogFragment dialog) {
        Log.d(TAG, "dialog YES answer received");
    }

    @Override
    public void onSurrenderNoAnswer(DialogFragment dialog) {
        Log.d(TAG, "dialog NO answer received");
    }

    @Override
    public void onEnemySurrendersAndLeavesYesAnswer(DialogFragment dialog) {
        gameViewModel.eraseGameRoom();
        finish();
    }

    @Override
    public void onSkipTurnYesAnswer(DialogFragment dialog) {
        gameViewModel.endTurn(TurnTerminationCode.TURN_SKIPPED);
    }

    @Override
    public void onSkipTurnNoAnswer(DialogFragment dialog) {
        Log.d(TAG, "skipTurnDialog NO answer received");
    }

    @Override
    public void onRematchYesAnswer(DialogFragment dialog) {
        //WRITE REMATCH ANSWER TO FIREBASE
    }

    @Override
    public void onRematchNoAnswer(DialogFragment dialog) {
        //WRITE REMATCH ANSWER TO FIREBASE
        User.eraseJoinedGameRoomKey();
        finish();
    }
}