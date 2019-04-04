package ca.cmpt276theta.sudokuvocabulary.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

import ca.cmpt276theta.sudokuvocabulary.R;
import ca.cmpt276theta.sudokuvocabulary.controller.TTSHandler;
import ca.cmpt276theta.sudokuvocabulary.model.GameData;

import static android.content.Context.VIBRATOR_SERVICE;

public class GameView extends View {

    private final Vibrator mVibrator;
    private final boolean isLandscapeMode;
    private final TTSHandler mTTSHandler;
    private final int gridSize;
    private final int subGridSizeHori;
    private final int subGridSizeVerti;
    private final Handler handler = new Handler();
    private float mGridWidth;
    private float mGridHeight;
    private int mTouchPositionX;
    private int mTouchPositionY;
    private GameData mGameData;
    private boolean isLongPress;
    private final Runnable longPressed = new Runnable() {
        public void run() {
            if (GameData.isListenMode())
                readWord();
            else {
                isLongPress = true;
                GameView.this.invalidate();
            }
        }
    };
    private boolean isVibrated;
    private float tempX = 0;
    private float tempY = 0;

    public GameView(Context context) {
        super(context);
        isLandscapeMode = getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;
        isLongPress = false;
        isVibrated = false;
        mVibrator = (Vibrator) this.getContext().getSystemService(VIBRATOR_SERVICE);
        mTouchPositionX = -1;
        mTouchPositionY = -1;
        mTTSHandler = new TTSHandler(context);
        gridSize = GameData.getGridSize();
        subGridSizeHori = GameData.getSubGridSizeHori();
        subGridSizeVerti = GameData.getSubGridSizeVerti();
    }

    public void setGameData(GameData gameData) {
        this.mGameData = gameData;
    }

    public int getTouchPositionX() {
        return mTouchPositionX;
    }

    public TTSHandler getTTSHandler() {
        return mTTSHandler;
    }

    public void setTouchPosition(int x, int y) {
        mTouchPositionX = x;
        mTouchPositionY = y;
    }



    public int getTouchPositionY() {
        return mTouchPositionY;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isLandscapeMode)
            setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
        else
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mGridWidth = w / (float) gridSize;
        if (!isLandscapeMode)
            mGridHeight = mGridWidth;
        else
            mGridHeight = h / (float) gridSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHighlight(canvas);
        drawConflict(canvas);
        drawGrid(canvas);
        drawWord(canvas);
        if (isLongPress && !GameData.isListenMode())
            drawHint(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mTouchPositionX = (int) (event.getX() / mGridWidth);
        mTouchPositionY = (int) (event.getY() / mGridHeight);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.postDelayed(longPressed, 500);
                tempX = mTouchPositionX;
                tempY = mTouchPositionY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(tempX - mTouchPositionX) > 0 || Math.abs(tempY - mTouchPositionY) > 0) {
                    isLongPress = false;
                    handler.removeCallbacks(longPressed);
                }
                break;
            case MotionEvent.ACTION_UP:
                isLongPress = false;
                handler.removeCallbacks(longPressed);
                isVibrated = false;
        }
        if (mTouchPositionX < 0 || mTouchPositionY < 0 || mTouchPositionX > (gridSize - 1) || mTouchPositionY > (gridSize - 1)) {
            mTouchPositionX = -1;
            mTouchPositionY = -1;
        }
        this.invalidate();
        return true;
    }

    private void drawHighlight(Canvas canvas) {
        if (mTouchPositionX != -1) {
            final Paint highlightPaint = new Paint();
            highlightPaint.setColor(getResources().getColor(R.color.highlightRec));
            highlightPaint.setAlpha(80);
            canvas.drawRect(mTouchPositionX * mGridWidth, 0, (mTouchPositionX + 1) * mGridWidth,
                    gridSize * mGridHeight, highlightPaint);
            canvas.drawRect(0, mTouchPositionY * mGridHeight, gridSize * mGridWidth,
                    (mTouchPositionY + 1) * mGridHeight, highlightPaint);
        }
    }

    private void drawConflict(Canvas canvas) {
        if (mTouchPositionX != -1 && !(mGameData.getPuzzle()[mTouchPositionY][mTouchPositionX] == 0)) {
            final Paint conflictPaint = new Paint();
            conflictPaint.setColor(getResources().getColor(R.color.conflict));
            final int key = mGameData.getPuzzle()[mTouchPositionY][mTouchPositionX];
            for (int i = 0; i < gridSize; i++) {
                if ((key == mGameData.getPuzzle()[i][mTouchPositionX]) && i != mTouchPositionY)
                    canvas.drawRect(mTouchPositionX * mGridWidth, i * mGridHeight,
                            (mTouchPositionX + 1) * mGridWidth, (i + 1) * mGridHeight, conflictPaint);
                if ((key == mGameData.getPuzzle()[mTouchPositionY][i]) && i != mTouchPositionX)
                    canvas.drawRect(i * mGridWidth, mTouchPositionY * mGridHeight,
                            (i + 1) * mGridWidth, (mTouchPositionY + 1) * mGridHeight, conflictPaint);
            }

            final int firstCellOfSubgridX = mTouchPositionX / subGridSizeHori * subGridSizeHori;
            final int firstCellOfSubgridY = mTouchPositionY / subGridSizeVerti * subGridSizeVerti;
            for (int i = 0; i < subGridSizeVerti; i++) {
                if (firstCellOfSubgridY + i != mTouchPositionY) {
                    final float topPosition = (firstCellOfSubgridY + i) * mGridHeight;
                    final float bottomPosition = (firstCellOfSubgridY + i + 1) * mGridHeight;
                    for (int j = 0; j < subGridSizeHori; j++) {
                        final float rightPosition = (firstCellOfSubgridX + j + 1) * mGridWidth;
                        final float leftPosition = (firstCellOfSubgridX + j) * mGridWidth;
                        if ((firstCellOfSubgridX + j != mTouchPositionX) && (key == mGameData.getPuzzle()[firstCellOfSubgridY + i][firstCellOfSubgridX + j]))
                            canvas.drawRect(leftPosition, topPosition, rightPosition, bottomPosition, conflictPaint);
                    }
                }
            }

        }
    }

    private void drawWord(Canvas canvas) {
        final Paint wordPaint = new Paint();
        wordPaint.setAntiAlias(true);
        wordPaint.setTextAlign(Paint.Align.CENTER);
        wordPaint.setTextSize(mGridWidth * 0.25f);
        final float y = mGridHeight / 2 - (wordPaint.getFontMetrics().top + wordPaint.getFontMetrics().bottom) / 2;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                final String word = mGameData.getGridContent()[i][j];
                wordPaint.setColor(getResources().getColor(R.color.border));
                wordPaint.setFakeBoldText(false);
                if (word.length() < 7 && (!word.contains("m")) || word.length() < 5)
                    wordPaint.setTextSize(mGridWidth * 0.29f);
                else
                    wordPaint.setTextSize(mGridWidth * 0.25f);
                if (mGameData.getPuzzlePreFilled()[i][j] != 0) {
                    wordPaint.setColor(getResources().getColor(R.color.colorPrimary));
                    wordPaint.setFakeBoldText(true);
                }
                canvas.drawText(word, (j + 0.5f) * mGridWidth, (i * mGridHeight + y), wordPaint);
            }
        }
    }

    private void drawHint(Canvas canvas) {
        if (mTouchPositionX != -1 && !(mGameData.getPuzzle()[mTouchPositionY][mTouchPositionX] == 0)) {
            if (!isVibrated) {
                mVibrator.vibrate(65);
                isVibrated = true;
            }
            final Paint bubblePaint = new Paint();
            bubblePaint.setColor(getResources().getColor(R.color.hintBubble));
            bubblePaint.setAntiAlias(true);
            bubblePaint.setAlpha(200);
            final Paint hintPaint = new Paint();
            hintPaint.setTextAlign(Paint.Align.CENTER);
            hintPaint.setColor(getResources().getColor(R.color.background));
            hintPaint.setTextSize(mGridWidth * 0.4f);
            hintPaint.setAntiAlias(true);
            hintPaint.setFakeBoldText(true);
            final RectF rec;
            final String hintWord1 = mGameData.getLanguageA()[mGameData.getPuzzle()[mTouchPositionY][mTouchPositionX] - 1];
            final String hintWord2 = mGameData.getLanguageB()[mGameData.getPuzzle()[mTouchPositionY][mTouchPositionX] - 1];


            if ((mTouchPositionY == 0 || mTouchPositionY == 1) && mTouchPositionX < ((gridSize + 1) / 2)) {
                final float x = mGridWidth * (mTouchPositionX + 2.15f);
                rec = new RectF(mGridWidth * (mTouchPositionX + 1.2f), mGridHeight * mTouchPositionY,
                        mGridWidth * (mTouchPositionX + 3.1f), mGridHeight * (mTouchPositionY + 1.2f));
                canvas.drawRoundRect(rec, 25, 25, bubblePaint);
                canvas.drawText(hintWord1, x, mGridHeight * (mTouchPositionY + 0.47f), hintPaint);
                hintPaint.setFakeBoldText(false);
                canvas.drawText(hintWord2, x, mGridHeight * (mTouchPositionY + 0.97f), hintPaint);
            } else if ((mTouchPositionY == 0 || mTouchPositionY == 1)) {
                final float x = mGridWidth * (mTouchPositionX - 1f);
                rec = new RectF(mGridWidth * mTouchPositionX, mGridHeight * mTouchPositionY,
                        mGridWidth * (mTouchPositionX - 2f), mGridHeight * (mTouchPositionY + 1.2f));
                canvas.drawRoundRect(rec, 25, 25, bubblePaint);
                canvas.drawText(hintWord1, x, mGridHeight * (mTouchPositionY + 0.47f), hintPaint);
                hintPaint.setFakeBoldText(false);
                canvas.drawText(hintWord2, x, mGridHeight * (mTouchPositionY + 0.97f), hintPaint);
            } else if (mTouchPositionX == 0) {
                final float x = mGridWidth * (mTouchPositionX + 0.9f);
                rec = new RectF(0, mGridHeight * (mTouchPositionY - 1.4f), mGridWidth * (mTouchPositionX + 1.8f), mGridHeight * (mTouchPositionY - 0.2f));
                canvas.drawRoundRect(rec, 25, 25, bubblePaint);
                canvas.drawText(hintWord1, x, mGridHeight * (mTouchPositionY - 0.93f), hintPaint);
                hintPaint.setFakeBoldText(false);
                canvas.drawText(hintWord2, x, mGridHeight * (mTouchPositionY - 0.43f), hintPaint);
            } else if (mTouchPositionX == (gridSize - 1)) {
                final float x = mGridWidth * (mTouchPositionX + 0.15f);
                rec = new RectF(mGridWidth * (mTouchPositionX - 0.8f), mGridHeight * (mTouchPositionY - 1.4f),
                        mGridWidth * 9, mGridHeight * (mTouchPositionY - 0.2f));
                canvas.drawRoundRect(rec, 25, 25, bubblePaint);
                canvas.drawText(hintWord1, x, mGridHeight * (mTouchPositionY - 0.93f), hintPaint);
                hintPaint.setFakeBoldText(false);
                canvas.drawText(hintWord2, x, mGridHeight * (mTouchPositionY - 0.43f), hintPaint);
            } else {
                final float x = mGridWidth * (mTouchPositionX + 0.5f);
                rec = new RectF(mGridWidth * (mTouchPositionX - 0.4f), mGridHeight * (mTouchPositionY - 1.4f),
                        mGridWidth * (mTouchPositionX + 1.4f), mGridHeight * (mTouchPositionY - 0.2f));
                canvas.drawRoundRect(rec, 25, 25, bubblePaint);
                canvas.drawText(hintWord1, x, mGridHeight * (mTouchPositionY - 0.93f), hintPaint);
                hintPaint.setFakeBoldText(false);
                canvas.drawText(hintWord2, x, mGridHeight * (mTouchPositionY - 0.43f), hintPaint);
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        final float girdEdgeHorizontal = mGridWidth * gridSize;
        final float girdEdgeVertical = mGridHeight * gridSize;
        // draw the border
        final Paint borderPaint = new Paint();
        borderPaint.setColor(getResources().getColor(R.color.border));
        borderPaint.setStrokeWidth(5);
        for (int i = 1; i < subGridSizeVerti; i++) {
            final float vertex = i * mGridWidth * subGridSizeHori;
            canvas.drawLine(vertex, 0, vertex, girdEdgeVertical, borderPaint);
        }
        for (int i = 1; i < subGridSizeHori; i++) {
            final float vertex = i * mGridHeight * subGridSizeVerti;
            canvas.drawLine(0, vertex, girdEdgeHorizontal, vertex, borderPaint);
        }

        // draw the subgrid
        borderPaint.setStrokeWidth(1);
        for (int i = 1; i < gridSize; i++) {
            final float vertex1 = i * mGridWidth;
            final float vertex2 = i * mGridHeight;
            canvas.drawLine(vertex1, 0, vertex1,
                    girdEdgeVertical, borderPaint);
            canvas.drawLine(0, vertex2, girdEdgeHorizontal,
                    vertex2, borderPaint);
        }
    }

    private void readWord() {
        if (mTouchPositionX != -1 && mTouchPositionY != -1 && mGameData.getPuzzlePreFilled()[mTouchPositionY][mTouchPositionX] != 0) {
            Locale locale = Locale.US;
            if (GameData.getLanguageMode() == 1)
                locale = Locale.FRENCH;
            mTTSHandler.speak(mGameData.getLanguageA()[mGameData.getPuzzle()[mTouchPositionY][mTouchPositionX] - 1], locale);
            Toast toast = Toast.makeText(getContext(), "Reading...", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 210);
            toast.show();
        }
    }
}
