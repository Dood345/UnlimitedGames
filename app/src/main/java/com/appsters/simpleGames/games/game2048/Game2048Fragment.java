package com.appsters.simpleGames.games.game2048;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.appsters.simpleGames.databinding.FragmentGame2048Binding;

public class Game2048Fragment extends Fragment {

    private FragmentGame2048Binding binding;
    private Game2048ViewModel viewModel;
    private TextView[][] tiles;
    private GestureDetector gestureDetector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(Game2048ViewModel.class);
        gestureDetector = new GestureDetector(requireContext(), new SwipeListener(),
                new android.os.Handler(android.os.Looper.getMainLooper()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentGame2048Binding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        com.appsters.simpleGames.app.util.SoundManager.init(requireContext());

        binding.gameBoard.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.gameBoard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        initializeBoard();
                    }
                });

        viewModel.board.observe(getViewLifecycleOwner(), this::updateBoard);
        viewModel.gameOver.observe(getViewLifecycleOwner(), isGameOver -> {
            binding.gameOverTextView.setVisibility(isGameOver ? View.VISIBLE : View.GONE);
        });

        binding.newGameButton.setOnClickListener(v -> viewModel.newGame());
        binding.gameBoard.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.saveGameState();
    }

    private void initializeBoard() {
        tiles = new TextView[4][4];
        binding.gameBoard.removeAllViews();
        binding.gameBoard.setColumnCount(4);
        binding.gameBoard.setRowCount(4);

        final int margin = (int) (8 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView tile = new TextView(requireContext());

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(i, 1f),
                        GridLayout.spec(j, 1f));
                params.width = 0;
                params.height = 0;
                params.setMargins(margin, margin, margin, margin);

                tile.setLayoutParams(params);
                tile.setGravity(android.view.Gravity.CENTER);
                tile.setTextSize(24);
                binding.gameBoard.addView(tile);
                tiles[i][j] = tile;
            }
        }
        updateBoard(viewModel.board.getValue());
    }

    private void updateBoard(int[][] board) {
        if (board == null || tiles == null)
            return;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tiles[i][j] != null) {
                    TextView tile = tiles[i][j];
                    int value = board[i][j];
                    tile.setText(value == 0 ? "" : String.valueOf(value));
                    tile.setBackground(getTileBackground(value));
                }
            }
        }
    }

    private GradientDrawable getTileBackground(int value) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(8f);
        drawable.setColor(getTileColor(value));
        return drawable;
    }

    private int getTileColor(int value) {
        switch (value) {
            case 2:
                return Color.parseColor("#EEE4DA");
            case 4:
                return Color.parseColor("#EDE0C8");
            case 8:
                return Color.parseColor("#F2B179");
            case 16:
                return Color.parseColor("#F59563");
            case 32:
                return Color.parseColor("#F67C5F");
            case 64:
                return Color.parseColor("#F65E3B");
            case 128:
                return Color.parseColor("#EDCF72");
            case 256:
                return Color.parseColor("#EDCC61");
            case 512:
                return Color.parseColor("#EDC850");
            case 1024:
                return Color.parseColor("#EDC53F");
            case 2048:
                return Color.parseColor("#EDC22E");
            default:
                return Color.parseColor("#CDC1B4");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        viewModel.move(2); // Right
                    } else {
                        viewModel.move(0); // Left
                    }
                    com.appsters.simpleGames.app.util.SoundManager.playSound(com.appsters.simpleGames.R.raw.slide);
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        // A downward swipe has a positive diffY.
                        viewModel.move(3); // Down
                    } else {
                        // An upward swipe has a negative diffY.
                        viewModel.move(1); // Up
                    }
                    com.appsters.simpleGames.app.util.SoundManager.playSound(com.appsters.simpleGames.R.raw.slide);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
