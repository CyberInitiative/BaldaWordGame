package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameBoardAccessor {
    private static final DatabaseReference GAME_BOARDS = FirebaseDatabase.getInstance().getReference().child("gameBoards");
    private static final String TAG = "GameBoardAccessor";

    //to prevent instantiation
    private GameBoardAccessor() {
        throw new AssertionError();
    }

    public static Task<HashMap<DatabaseReference, LetterCell>> writeGameBoard(@NonNull String gameRoomKey, int gameBoardSize) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);

        HashMap<DatabaseReference, LetterCell> refToLetterCell = new HashMap<>();
        ArrayList<Task<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < gameBoardSize; i++) { //rows
            for (int j = 0; j < gameBoardSize; j++) { //columns
                LetterCell letterCell = new LetterCell(j, i);
                DatabaseReference letterCellKey = ref.push();
                refToLetterCell.put(letterCellKey, letterCell);

                Task<Void> setValueTask = letterCellKey.setValue(letterCell);
                tasks.add(setValueTask);
            }
        }

        return Tasks.whenAll(tasks).continueWith(task -> {
            return refToLetterCell;
        });
    }

    public static Task<HashMap<LetterCell, DatabaseReference>> fetchGameBoard(@NonNull String gameRoomKey) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);

        Task<DataSnapshot> fetchTask = ref.get();
        return fetchTask.continueWith(task -> {
            HashMap<LetterCell, DatabaseReference> letterCellToRef = new HashMap<>();

            DataSnapshot snapshot = task.getResult();
            for (DataSnapshot child : snapshot.getChildren()) {
                LetterCell letterCell = child.getValue(LetterCell.class);
                DatabaseReference letterCellRef = child.getRef();

                letterCellToRef.put(letterCell, letterCellRef);
            }

            return letterCellToRef;
        });
    }

    public static Task<Void> updateLetterCell(@NonNull LetterCell cell, @NonNull String gameRoomKey, @NonNull DatabaseReference cellRef) {
        DatabaseReference path = FirebaseDatabase.getInstance().getReference().child("gameBoards").child(gameRoomKey);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(cellRef.getKey() + "/letter", cell.getLetter());
        childUpdates.put(cellRef.getKey() + "/state", cell.getState());

        return path.updateChildren(childUpdates);
    }

}

    /*

    public static Task<ArrayList<FirebaseQueryLiveData>> fetchArrayListOfFirebaseQueryLiveDataLetterCell(@NonNull String gameRoomKey) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);

        Task<DataSnapshot> fetchTask = ref.get();
        return fetchTask.continueWith(task -> {
            DataSnapshot dataSnapshot = task.getResult();
            ArrayList<FirebaseQueryLiveData> arr = new ArrayList<>();

            for (DataSnapshot child : dataSnapshot.getChildren()) {
                FirebaseQueryLiveData firebaseQueryLiveData = new FirebaseQueryLiveData(child.getRef());
                arr.add(firebaseQueryLiveData);
            }

            return arr;
        });
    }



     public static Task<ArrayList<LetterCell>> writeGameBoard(@NonNull String gameRoomKey, int gameBoardSize) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);

        HashMap<DatabaseReference, LetterCell> refToLetterCell = new HashMap<>();
        ArrayList<LetterCell> letterCells = new ArrayList<>();
        for (int i = 0; i < gameBoardSize; i++) { //rows
            for (int j = 0; j < gameBoardSize; j++) { //columns
                LetterCell letterCell = new LetterCell(j, i);
                letterCells.add(letterCell);
            }
        }

        Task<Void> setValueTask = ref.setValue(letterCells);
        return setValueTask.continueWith(task -> letterCells);
    }


    @NonNull
    public static ValueEventListener addGameBoardChangeListener(@NonNull String gameRoomKey, @NonNull ArrayList<LetterCell> gameBoardSnapshot) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<LetterCell> arrayList = new ArrayList<>();
                for (DataSnapshot snapshotChild : snapshot.getChildren()) {
                    LetterCell letterCell = snapshotChild.getValue(LetterCell.class);
                    arrayList.add(letterCell);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        return valueEventListener;
    }

    public static Task<ArrayList<LetterCell>> fetchLetterCells(@NonNull String gameRoomKey) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);
        return ref.get().continueWith(task -> {
            DataSnapshot dataSnapshot = task.getResult();
            ArrayList<LetterCell> arr = new ArrayList<>();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                LetterCell letterCell = child.getValue(LetterCell.class);
                arr.add(letterCell);
            }
            return arr;
        });
    }

    public static Task<ArrayList<LetterCell>> fetchLetterCells(@NonNull DatabaseReference ref) {
        return ref.get().continueWith(task -> {
            DataSnapshot dataSnapshot = task.getResult();
            ArrayList<LetterCell> arr = new ArrayList<>();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                LetterCell letterCell = child.getValue(LetterCell.class);
                arr.add(letterCell);
            }
            return arr;
        });
    }

        public static Task<ArrayList<FirebaseQueryLiveData>>
    fetchArrayListOfFirebaseQueryLiveDataLetterCell(@NonNull String gameRoomKey) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);
        return ref.get().continueWith(task -> {
            DataSnapshot dataSnapshot = task.getResult();
            ArrayList<FirebaseQueryLiveData> arr = new ArrayList<>();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                FirebaseQueryLiveData firebaseQueryLiveData = new FirebaseQueryLiveData(child.getRef());
                arr.add(firebaseQueryLiveData);
            }
            return arr;
        });
    }

    public static Task<ArrayList<FirebaseQueryLiveData>> fetchArrayListOfFirebaseQueryLiveDataLetterCell(@NonNull DatabaseReference ref) {
        return ref.get().continueWith(task -> {
            DataSnapshot dataSnapshot = task.getResult();
            ArrayList<FirebaseQueryLiveData> arr = new ArrayList<>();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                FirebaseQueryLiveData firebaseQueryLiveData = new FirebaseQueryLiveData(child.getRef());
                arr.add(firebaseQueryLiveData);
            }
            return arr;
        });
    }
 */