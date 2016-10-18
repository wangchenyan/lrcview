package me.wcy.lrcview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词
 * Created by wcy on 2015/11/9.
 */
public class LrcView extends View {
    private List<Entry> mEntryList = new ArrayList<>();
    private TextPaint mPaint = new TextPaint();
    private float mTextSize;
    private float mDividerHeight;
    private long mAnimationDuration;
    private int mNormalColor;
    private int mCurrentColor;
    private String mLabel;
    private float mLrcPadding;
    private ValueAnimator mAnimator;
    private float mAnimateOffset;
    private long mNextTime = 0L;
    private int mCurrentLine = 0;

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
        mTextSize = ta.getDimension(R.styleable.LrcView_lrcTextSize, sp2px(16));
        mDividerHeight = ta.getDimension(R.styleable.LrcView_lrcDividerHeight, dp2px(24));
        mAnimationDuration = ta.getInt(R.styleable.LrcView_lrcAnimationDuration, 1000);
        mAnimationDuration = mAnimationDuration < 0 ? 1000 : mAnimationDuration;
        mNormalColor = ta.getColor(R.styleable.LrcView_lrcNormalTextColor, 0xFFFFFFFF);
        mCurrentColor = ta.getColor(R.styleable.LrcView_lrcCurrentTextColor, 0xFFFF4081);
        mLabel = ta.getString(R.styleable.LrcView_lrcLabel);
        mLabel = TextUtils.isEmpty(mLabel) ? "暂无歌词" : mLabel;
        mLrcPadding = ta.getDimension(R.styleable.LrcView_lrcPadding, dp2px(16));
        ta.recycle();

        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (Entry entry : mEntryList) {
            entry.init(mPaint, (int) getLrcWidth());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(0, mAnimateOffset);

        // 中心Y坐标
        float centerY = getHeight() / 2;

        mPaint.setColor(mCurrentColor);

        // 无歌词文件
        if (!hasLrc()) {
            @SuppressLint("DrawAllocation")
            StaticLayout staticLayout = new StaticLayout(mLabel, mPaint, (int) getLrcWidth(), Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            drawText(canvas, staticLayout, centerY - staticLayout.getLineCount() * mTextSize / 2);
            return;
        }

        // 画当前行
        float currY = centerY - mEntryList.get(mCurrentLine).getTextHeight() / 2;
        drawText(canvas, mEntryList.get(mCurrentLine).getStaticLayout(), currY);

        // 画当前行上面的
        mPaint.setColor(mNormalColor);
        float upY = currY;
        for (int i = mCurrentLine - 1; i >= 0; i--) {
            upY -= mDividerHeight + mEntryList.get(i).getTextHeight();

            if (mAnimator == null || !mAnimator.isStarted()) {
                // 动画已经结束，超出屏幕停止绘制
                if (upY < 0) {
                    break;
                }
            }

            drawText(canvas, mEntryList.get(i).getStaticLayout(), upY);

            // 动画未结束，超出屏幕多绘制一行
            if (upY < 0) {
                break;
            }
        }

        // 画当前行下面的
        float downY = currY + mEntryList.get(mCurrentLine).getTextHeight() + mDividerHeight;
        for (int i = mCurrentLine + 1; i < mEntryList.size(); i++) {
            if (mAnimator == null || !mAnimator.isStarted()) {
                // 动画已经结束，超出屏幕停止绘制
                if (downY + mEntryList.get(i).getTextHeight() > getHeight()) {
                    break;
                }
            }

            drawText(canvas, mEntryList.get(i).getStaticLayout(), downY);

            // 动画未结束，超出屏幕多绘制一行
            if (downY + mEntryList.get(i).getTextHeight() > getHeight()) {
                break;
            }

            downY += mEntryList.get(i).getTextHeight() + mDividerHeight;
        }
    }

    private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
        canvas.save();
        canvas.translate(getWidth() / 2, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private float getLrcWidth() {
        return getWidth() - mLrcPadding * 2;
    }

    /**
     * 设置歌词为空时屏幕中央显示的文字，如“暂无歌词”
     */
    public void setLabel(String label) {
        reset();

        mLabel = label;
        postInvalidate();
    }

    /**
     * 加载歌词文件
     *
     * @param lrcFile 歌词文件
     */
    public void loadLrc(File lrcFile) {
        reset();

        if (lrcFile == null || !lrcFile.exists()) {
            postInvalidate();
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile), "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initNextTime();

        postInvalidate();
    }

    /**
     * 加载歌词文件
     *
     * @param lrcText 歌词文本
     */
    public void loadLrc(String lrcText) {
        reset();

        if (TextUtils.isEmpty(lrcText)) {
            postInvalidate();
            return;
        }

        String[] array = lrcText.split("\\n");
        for (String line : array) {
            parseLine(line);
        }

        initNextTime();

        postInvalidate();
    }

    /**
     * 刷新歌词
     *
     * @param time 当前播放时间
     */
    public void updateTime(long time) {
        // 避免重复绘制
        if (time < mNextTime) {
            return;
        }
        for (int i = mCurrentLine; i < mEntryList.size(); i++) {
            if (mEntryList.get(i).getTime() > time) {
                mNextTime = mEntryList.get(i).getTime();
                mCurrentLine = i < 1 ? 0 : i - 1;
                newlineAnimate(i);
                break;
            } else if (i == mEntryList.size() - 1) {
                // 最后一行
                mCurrentLine = mEntryList.size() - 1;
                mNextTime = Long.MAX_VALUE;
                newlineAnimate(i);
                break;
            }
        }
    }

    /**
     * 将歌词滚动到指定时间
     *
     * @param time 指定的时间
     */
    public void onDrag(long time) {
        for (int i = 0; i < mEntryList.size(); i++) {
            if (mEntryList.get(i).getTime() > time) {
                mNextTime = mEntryList.get(i).getTime();
                mCurrentLine = i < 1 ? 0 : i - 1;
                newlineAnimate(i);
                break;
            }
        }
    }

    /**
     * 歌词是否有效
     *
     * @return true，如果歌词有效，否则false
     */
    public boolean hasLrc() {
        return !mEntryList.isEmpty();
    }

    private void reset() {
        mEntryList.clear();
        mCurrentLine = 0;
        mNextTime = 0L;
    }

    private void initNextTime() {
        if (mEntryList.size() > 1) {
            mNextTime = mEntryList.get(1).getTime();
        } else {
            mNextTime = Long.MAX_VALUE;
        }
    }

    /**
     * 解析一行
     *
     * @param line [00:10.61]走过了人来人往
     */
    private void parseLine(String line) {
        line = line.trim();
        Matcher matcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)\\](.+)").matcher(line);
        if (!matcher.matches()) {
            return;
        }

        long min = Long.parseLong(matcher.group(1));
        long sec = Long.parseLong(matcher.group(2));
        long mil = Long.parseLong(matcher.group(3));
        String text = matcher.group(4);

        long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;

        Entry entry = new Entry(time, text);
        mEntryList.add(entry);
    }

    /**
     * 换行动画<br>
     * 属性动画只能在主线程使用
     */
    private void newlineAnimate(int index) {
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.end();
        }

        mAnimator = ValueAnimator.ofFloat(mEntryList.get(index).getTextHeight() + mDividerHeight, 0.0f);
        mAnimator.setDuration(mAnimationDuration * mEntryList.get(index).getStaticLayout().getLineCount());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimateOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    private int dp2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private static class Entry {
        private long time;
        private String text;
        private StaticLayout staticLayout;
        private TextPaint paint;

        Entry(long time, String text) {
            this.time = time;
            this.text = text;
        }

        void init(TextPaint paint, int width) {
            this.paint = paint;
            staticLayout = new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        }

        long getTime() {
            return time;
        }

        String getText() {
            return text;
        }

        StaticLayout getStaticLayout() {
            return staticLayout;
        }

        float getTextHeight() {
            return staticLayout.getLineCount() * paint.getTextSize();
        }
    }
}
