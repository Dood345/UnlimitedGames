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
import java.util.List;
import java.util.ArrayList;

public class Game2048Fragment extends Fragment {

    private FragmentGame2048Binding binding;
    private Game2048ViewModel viewModel;
    private GestureDetector gestureDetector;
    private final java.util.Map<String, TextView> activeViews = new java.util.HashMap<>();
    private int cellSize;
    private int gridMargin;

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

        binding.gameBoard.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.gameBoard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        calculateDimensions();
                        drawBackgroundGrid();
                        // Initial update without animation to populating the board
                        if (viewModel.board.getValue() != null) {
                            updateBoard(viewModel.board.getValue(), false);
                        }
                    }
                });

        viewModel.board.observe(getViewLifecycleOwner(), board -> updateBoard(board, true));
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

    private void calculateDimensions() {
        int width = binding.gameBoard.getWidth();
        // 4x4 grid with margins
        // 5 margins total (left, right, 3 internal) if we do even spacing
        // Or simplified: just divide by 4 and add padding inside tiles.
        // Let's stick to the previous margin logic approx:
        gridMargin = (int) (8 * getResources().getDisplayMetrics().density);
        // spread margins: | M | Tile | M | Tile | M | Tile | M | Tile | M |
        // Total width = 4 * cellSize + 5 * gridMargin
        cellSize = (width - 5 * gridMargin) / 4;
    }

    private void drawBackgroundGrid() {
        // Draw static empty cells
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                View backgroundTile = new View(requireContext());
                android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                        cellSize, cellSize);
                params.leftMargin = gridMargin + j * (cellSize + gridMargin);
                params.topMargin = gridMargin + i * (cellSize + gridMargin);
                backgroundTile.setLayoutParams(params);

                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(8f);
                drawable.setColor(Color.parseColor("#CDC1B4"));
                backgroundTile.setBackground(drawable);

                binding.gameBoard.addView(backgroundTile);
            }
        }
    }

    private void updateBoard(Tile[][] board, boolean animate) {
        if (board == null || cellSize == 0)
            return;

        java.util.Set<String> newIds = new java.util.HashSet<>();
        java.util.Set<String> processedOldIds = new java.util.HashSet<>();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Tile tile = board[i][j];
                if (tile != null) {
                    newIds.add(tile.getId());

                    int targetLeft = gridMargin + j * (cellSize + gridMargin);
                    int targetTop = gridMargin + i * (cellSize + gridMargin);

                    if (activeViews.containsKey(tile.getId())) {
                        TextView view = activeViews.get(tile.getId());
                        if (animate) {
                            view.animate()
                                    .translationX(targetLeft)
                                    .translationY(targetTop)
                                    .setDuration(100)
                                    .start();
                        } else {
                            view.setTranslationX(targetLeft);
                            view.setTranslationY(targetTop);
                        }
                        view.setText(String.valueOf(tile.getValue()));
                        view.setBackground(getTileBackground(tile.getValue()));
                    } else {
                        List<String> parents = tile.getMergedFromIds();
                        if (animate && parents != null && !parents.isEmpty()) {
                            for (String parentId : parents) {
                                if (activeViews.containsKey(parentId)) {
                                    TextView parentView = activeViews.get(parentId);
                                    processedOldIds.add(parentId);
                                    parentView.bringToFront();
                                    parentView.animate()
                                            .translationX(targetLeft)
                                            .translationY(targetTop)
                                            .setDuration(100)
                                            .withEndAction(() -> binding.gameBoard.removeView(parentView))
                                            .start();
                                }
                            }
                            createTileView(tile, targetLeft, targetTop, true, 100);
                        } else {
                            createTileView(tile, targetLeft, targetTop, animate, 0);
                        }
                    }
                }
            }
        }

        java.util.Iterator<java.util.Map.Entry<String, TextView>> it = activeViews.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, TextView> entry = it.next();
            String id = entry.getKey();
            if (!newIds.contains(id)) {
                if (processedOldIds.contains(id)) {
                    it.remove();
                } else {
                    TextView view = entry.getValue();
                    if (animate) {
                        view.animate()
                                .alpha(0f)
                                .setDuration(100)
                                .withEndAction(() -> binding.gameBoard.removeView(view))
                                .start();
                    } else {
                        binding.gameBoard.removeView(view);
                    }
                    it.remove();
                }
            }
        }
    }

    private void createTileView(Tile tile, int left, int top, boolean animate, int startDelay) {
        TextView view = new TextView(requireContext());
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(cellSize,
                cellSize);
        view.setLayoutParams(params);
        view.setGravity(android.view.Gravity.CENTER);
        view.setTextSize(24);
        view.setText(String.valueOf(tile.getValue()));
        view.setBackground(getTileBackground(tile.getValue()));
        view.setTranslationX(left);
        view.setTranslationY(top);

        binding.gameBoard.addView(view);
        activeViews.put(tile.getId(), view);

        if (animate) {
            view.setScaleX(0f);
            view.setScaleY(0f);
            view.animate()
                    .setStartDelay(startDelay)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start();
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
