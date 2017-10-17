package me.wcy.lrcview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 歌词
 * Created by wcy on 2015/11/9.
 */
public class LrcView extends View {
    private List<LrcEntry> mLrcEntryList = new ArrayList<>();
    private TextPaint mPaint = new TextPaint();
    private float mDividerHeight;
    private long mAnimationDuration;
    private int mNormalColor;
    private int mCurrentColor;
    private String mLabel;
    private float mLrcPadding;
    private ValueAnimator mAnimator;
    private float mOffset;
    private int mCurrentLine;
    private Object mFlag;

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LrcView);
        float textSize = ta.getDimension(R.styleable.LrcView_lrcTextSize, getContext().getResources().getDimension(R.dimen.lrc_text_size));
        mDividerHeight = ta.getDimension(R.styleable.LrcView_lrcDividerHeight, getContext().getResources().getDimension(R.dimen.lrc_divider_height));
        int defDuration = getContext().getResources().getInteger(R.integer.lrc_animation_duration);
        mAnimationDuration = ta.getInt(R.styleable.LrcView_lrcAnimationDuration, defDuration);
        mAnimationDuration = (mAnimationDuration < 0) ? defDuration : mAnimationDuration;
        mNormalColor = ta.getColor(R.styleable.LrcView_lrcNormalTextColor, getContext().getResources().getColor(R.color.lrc_normal_text_color));
        mCurrentColor = ta.getColor(R.styleable.LrcView_lrcCurrentTextColor, getContext().getResources().getColor(R.color.lrc_current_text_color));
        mLabel = ta.getString(R.styleable.LrcView_lrcLabel);
        mLabel = TextUtils.isEmpty(mLabel) ? getContext().getString(R.string.lrc_label) : mLabel;
        mLrcPadding = ta.getDimension(R.styleable.LrcView_lrcPadding, 0);
        ta.recycle();

        mPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setNormalColor(int normalColor) {
        mNormalColor = normalColor;
        postInvalidate();
    }

    public void setCurrentColor(int currentColor) {
        mCurrentColor = currentColor;
        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initEntryList();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 无歌词文件
        if (!hasLrc()) {
            mPaint.setColor(mCurrentColor);
            @SuppressLint("DrawAllocation")
            StaticLayout staticLayout = new StaticLayout(mLabel, mPaint, (int) getLrcWidth(),
                    Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
            drawText(canvas, staticLayout, getHeight() / 2);
            return;
        }

        canvas.translate(0, mOffset);

        float y = 0;
        for (int i = 0; i < mLrcEntryList.size(); i++) {
            if (i > 0) {
                y += (mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) / 2 + mDividerHeight;
            }
            mPaint.setColor((i == mCurrentLine) ? mCurrentColor : mNormalColor);
            drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), y);
        }
    }

    /**
     * 画一行歌词
     *
     * @param y 歌词中心 Y 坐标
     */
    private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
        canvas.save();
        canvas.translate(mLrcPadding, y - staticLayout.getHeight() / 2);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * 设置歌词为空时屏幕中央显示的文字，如“暂无歌词”
     */
    public void setLabel(final String label) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                mLabel = label;
                invalidate();
            }
        });
    }

    /**
     * 加载歌词文件
     *
     * @param lrcFile 歌词文件
     */
    public void loadLrc(final File lrcFile) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                reset();

                setFlag(lrcFile);
                new AsyncTask<File, Integer, List<LrcEntry>>() {
                    @Override
                    protected List<LrcEntry> doInBackground(File... params) {
                        return LrcEntry.parseLrc(params[0]);
                    }

                    @Override
                    protected void onPostExecute(List<LrcEntry> lrcEntries) {
                        if (getFlag() == lrcFile) {
                            onLrcLoaded(lrcEntries);
                            setFlag(null);
                        }
                    }
                }.execute(lrcFile);
            }
        });
    }

    /**
     * 加载歌词文件
     *
     * @param lrcText 歌词文本
     */
    public void loadLrc(final String lrcText) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                reset();

                setFlag(lrcText);
                new AsyncTask<String, Integer, List<LrcEntry>>() {
                    @Override
                    protected List<LrcEntry> doInBackground(String... params) {
                        return LrcEntry.parseLrc(params[0]);
                    }

                    @Override
                    protected void onPostExecute(List<LrcEntry> lrcEntries) {
                        if (getFlag() == lrcText) {
                            onLrcLoaded(lrcEntries);
                            setFlag(null);
                        }
                    }
                }.execute(lrcText);
            }
        });
    }

    private void onLrcLoaded(List<LrcEntry> entryList) {
        if (entryList != null && !entryList.isEmpty()) {
            mLrcEntryList.addAll(entryList);
        }

        initEntryList();
        invalidate();
    }

    /**
     * 刷新歌词
     *
     * @param time 当前播放时间
     */
    public void updateTime(final long time) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                if (!hasLrc()) {
                    return;
                }

                int line = findShowLine(time);
                if (line != mCurrentLine) {
                    mCurrentLine = line;
                    newline(line);
                }
            }
        });
    }

    /**
     * 将歌词滚动到指定时间
     *
     * @param time 指定的时间
     * @deprecated 请使用 {@link #updateTime(long)} 代替
     */
    public void onDrag(long time) {
        updateTime(time);
    }

    /**
     * 二分法查找当前应该显示的行数（最后一个 <= time 的行数）
     */
    private int findShowLine(long time) {
        int left = 0;
        int right = mLrcEntryList.size();
        while (left <= right) {
            int middle = (left + right) / 2;
            long middleTime = mLrcEntryList.get(middle).getTime();

            if (time < middleTime) {
                right = middle - 1;
            } else {
                if (middle + 1 >= mLrcEntryList.size() || time < mLrcEntryList.get(middle + 1).getTime()) {
                    return middle;
                }

                left = middle + 1;
            }
        }

        return 0;
    }

    /**
     * 歌词是否有效
     *
     * @return true，如果歌词有效，否则false
     */
    public boolean hasLrc() {
        return !mLrcEntryList.isEmpty();
    }

    private void reset() {
        endAnimation();
        mLrcEntryList.clear();
        mOffset = 0;
        mCurrentLine = 0;
        invalidate();
    }

    private void initEntryList() {
        if (!hasLrc() || getWidth() == 0) {
            return;
        }

        Collections.sort(mLrcEntryList);

        for (LrcEntry lrcEntry : mLrcEntryList) {
            lrcEntry.init(mPaint, (int) getLrcWidth());
        }

        mOffset = getHeight() / 2;
    }

    /**
     * 换行动画<br>
     * 属性动画只能在主线程使用
     */
    private void newline(int line) {
        endAnimation();

        int offset = getOffset(line);

        mAnimator = ValueAnimator.ofFloat(mOffset, offset);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    private void endAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    private int getOffset(int line) {
        if (mLrcEntryList.get(line).getOffset() == Integer.MIN_VALUE) {
            int offset = getHeight() / 2;
            for (int i = 1; i <= line; i++) {
                offset -= (mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) / 2 + mDividerHeight;
            }
            mLrcEntryList.get(line).setOffset(offset);
        }

        return mLrcEntryList.get(line).getOffset();
    }

    private float getLrcWidth() {
        return getWidth() - mLrcPadding * 2;
    }

    private void runOnUi(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            post(r);
        }
    }

    private Object getFlag() {
        return mFlag;
    }

    private void setFlag(Object flag) {
        this.mFlag = flag;
    }
}
