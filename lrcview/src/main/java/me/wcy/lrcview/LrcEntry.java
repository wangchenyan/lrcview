package me.wcy.lrcview;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * Created by hzwangchenyan on 2016/10/19.
 */
class LrcEntry {
    private long time;
    private String text;
    private StaticLayout staticLayout;
    private TextPaint paint;

    LrcEntry(long time, String text) {
        this.time = time;
        this.text = text;
    }

    void init(TextPaint paint, int width) {
        this.paint = paint;
        staticLayout = new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
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
        if (paint == null || staticLayout == null) {
            return 0;
        }
        return staticLayout.getLineCount() * paint.getTextSize();
    }
}
