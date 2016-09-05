# LrcView

## 系列文章
* [Android开源在线音乐播放器——波尼音乐](http://www.jianshu.com/p/1c0f5c4f64fa)
* [Android开源音乐播放器之播放器基本功能](http://www.jianshu.com/p/bc2f779a5400)
* [Android开源音乐播放器之高仿云音乐黑胶唱片](http://www.jianshu.com/p/f1d8eb8bb3e5)
* [Android开源音乐播放器之自动滚动歌词](http://www.jianshu.com/p/0feb6171b0c5)
* [Android开源音乐播放器之在线音乐列表自动加载更多](http://www.jianshu.com/p/576564627c96)

## 前言
上一节我们仿照云音乐实现了黑胶唱片专辑封面，这节我们该实现歌词显示了。当然，歌词不仅仅是显示就完了，作为一个有素质的音乐播放器，我们当然还需要根据歌曲进度自动滚动歌词，并且要有滚动动画。

* **开源不易，希望能给个Star鼓励**
* 项目地址：https://github.com/ChanWong21/LrcView
* 有问题请提Issues

## 简介
Android歌词控件，支持自动滚动，自定义界面。<br>
![](https://raw.githubusercontent.com/ChanWong21/PonyMusic/master/art/screenshot_04.jpg)

## 使用
**Gradle**
```
compile 'me.wcy.lrcview:lrcview:1.0.0'
```

## 思路分析
首先，当前播放的那一行应该在视图中央，且高亮显示，然后是当前行以前的在上面，当前行以后的在下面，所以我们的绘制流程是先绘制当前行，然后依次绘制上面的和下面的。<br>
歌词滚动时要有动画，这个动画是匀速的，因此使用属性动画即可，我们可以在切换当前行时让视图中心Y坐标向下偏移一行，然后用属性动画将它移回中心位置，这样就达到了动画的目的。<br>
有一点需要注意，由于视图的高度不能确定，所以我们在绘制当前行以上（或以下）的歌词时可能会被截断，因此我们在绘制当前行以上（或以下）歌词时应该先判断下是否超出视图可视范围。

## 代码实现
**绘制过程**
```
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    // 中心Y坐标
    float centerY = getHeight() / 2 + mTextSize / 2 + mAnimOffset;

    // 无歌词文件
    if (!hasLrc()) {
        float centerX = (getWidth() - mCurrentPaint.measureText(label)) / 2;
        canvas.drawText(label, centerX, centerY, mCurrentPaint);
        return;
    }

    // 画当前行
    String currStr = mLrcTexts.get(mCurrentLine);
    float currX = (getWidth() - mCurrentPaint.measureText(currStr)) / 2;
    canvas.drawText(currStr, currX, centerY, mCurrentPaint);

    // 画当前行上面的
    for (int i = mCurrentLine - 1; i >= 0; i--) {
        String upStr = mLrcTexts.get(i);
        float upX = (getWidth() - mNormalPaint.measureText(upStr)) / 2;
        float upY = centerY - (mTextSize + mDividerHeight) * (mCurrentLine - i);
        // 超出屏幕停止绘制
        if (upY - mTextSize < 0) {
            break;
        }
        canvas.drawText(upStr, upX, upY, mNormalPaint);
    }

    // 画当前行下面的
    for (int i = mCurrentLine + 1; i < mLrcTimes.size(); i++) {
        String downStr = mLrcTexts.get(i);
        float downX = (getWidth() - mNormalPaint.measureText(downStr)) / 2;
        float downY = centerY + (mTextSize + mDividerHeight) * (i - mCurrentLine);
        // 超出屏幕停止绘制
        if (downY > getHeight()) {
            break;
        }
        canvas.drawText(downStr, downX, downY, mNormalPaint);
    }
}
```
mAnimOffset是为了实现动画的中心Y坐标偏移值。

**换行动画**
```
/**
 * 换行动画
 * Note:属性动画只能在主线程使用
 */
private void newLineAnim() {
    ValueAnimator animator = ValueAnimator.ofFloat(mTextSize + mDividerHeight, 0.0f);
    animator.setDuration(mAnimationDuration);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mAnimOffset = (float) animation.getAnimatedValue();
            invalidate();
        }
    });
    animator.start();
}
```
代码比较简单，大家根据注释肯定能看懂。到这里，我们已经实现了滚动显示的歌词控件了。<br>
大家可以运行[源码](https://github.com/ChanWong21/PonyMusic/blob/master/app/src/main/java/me/wcy/music/widget/LrcView.java)或下载[波尼音乐](http://fir.im/ponymusic)查看详细效果。