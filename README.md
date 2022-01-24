# lrcview
[![](https://jitpack.io/v/wangchenyan/lrcview.svg)](https://jitpack.io/#wangchenyan/lrcview)

## 系列文章
- [Android开源在线音乐播放器——波尼音乐](https://juejin.im/post/5c373a32e51d4551cc6df6db)
- [Android开源音乐播放器之播放器基本功能](https://juejin.im/post/5c373a32e51d45521315fc50)
- [Android开源音乐播放器之高仿云音乐黑胶唱片](https://juejin.im/post/5c373a336fb9a04a016488e8)
- [Android开源音乐播放器之自动滚动歌词](https://juejin.im/post/5c373a336fb9a049f43b85de)
- [Android开源音乐播放器之在线音乐列表自动加载更多](https://juejin.im/post/5c373a336fb9a049b82aaaaf)

## 前言
上一节我们仿照云音乐实现了黑胶唱片专辑封面，这节我们该实现歌词显示了。当然，歌词不仅仅是显示就完了，作为一个有素质的音乐播放器，我们当然还需要根据歌曲进度自动滚动歌词，并且要支持上下拖动。

- 项目地址：https://github.com/wangchenyan/lrcview
- 有问题请提Issues
- 如果喜欢，欢迎Star！

## 简介
Android歌词控件，支持上下拖动歌词，歌词自动换行，自定义属性，支持双语歌词。

![](https://raw.githubusercontent.com/wangchenyan/lrcview/master/art/screenshot.gif)

## 更新说明
`v 2.2`
- 新增支持点击事件

`v 2.1.0`
- 新增支持双语歌词
- 修复横竖屏切换问题

`v 2.0`
- 新增上下拖动歌词功能

`v 1.4`
- 解析歌词放在工作线程中
- 优化多行歌词时动画不流畅

`v 1.3`
- 支持多个时间标签

`v 1.2`
- 支持RTL（从右向左）语言

`v 1.1`
- 新增歌词自动换行
- 新增自定义歌词Padding
- 优化歌词解析

`v 1.0`
- 支持自动滚动
- 支持自定义属性

## 使用
**Gradle**
```
// root project build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

// module build.gradle
// "latestVersion"改为文首徽章后对应的数值
implementation 'me.wcy:lrcview:latestVersion'
```

## 属性
| 属性 | 描述 |
| ---- | ---- |
| lrcTextSize | 当前歌词文本字体大小 |
| lrcNormalTextSize | 普通歌词文本字体大小 |
| lrcNormalTextColor | 非当前行歌词字体颜色 |
| lrcCurrentTextColor | 当前行歌词字体颜色 |
| lrcTimelineTextColor | 拖动歌词时选中歌词的字体颜色 |
| lrcTextGravity | 歌词对齐方向，center：居中对齐，left：靠左对齐，right：靠右对齐，默认为 center |
| lrcDividerHeight | 歌词间距 |
| lrcAnimationDuration | 歌词滚动动画时长 |
| lrcLabel | 没有歌词时屏幕中央显示的文字，如“暂无歌词” |
| lrcPadding | 歌词文字的左右边距 |
| lrcTimelineColor | 拖动歌词时时间线的颜色 |
| lrcTimelineHeight | 拖动歌词时时间线的高度 |
| lrcPlayDrawable | 拖动歌词时左侧播放按钮图片 |
| lrcTimeTextColor | 拖动歌词时右侧时间字体颜色 |
| lrcTimeTextSize | 拖动歌词时右侧时间字体大小 |

## 方法
| 方法 | 描述 |
| ---- | ---- |
| loadLrc(File lrcFile) | 加载歌词文件 |
| loadLrc(File mainLrcFile, File secondLrcFile) | 加载双语歌词文件，两种语言的歌词时间戳需要一致 |
| loadLrc(String lrcText) | 加载歌词文本 |
| loadLrc(String mainLrcText, String secondLrcText) | 加载双语歌词文本，两种语言的歌词时间戳需要一致 |
| loadLrcByUrl(String lrcUrl) | 加载在线歌词文本，默认使用 utf-8 编码 |
| loadLrcByUrl(String lrcUrl, String charset) | 加载在线歌词文本 |
| hasLrc() | 歌词是否有效 |
| setLabel(String label) | 设置歌词为空时视图中央显示的文字，如“暂无歌词” |
| updateTime(long time) | 刷新歌词 |
| ~~onDrag(long time)~~ | ~~将歌词滚动到指定时间。已弃用，请使用 updateTime(long) 代替~~ |
| ~~setOnPlayClickListener(OnPlayClickListener onPlayClickListener)~~ | ~~设置拖动歌词时，播放按钮点击监听器。如果为非 null ，则激活歌词拖动功能，否则将将禁用歌词拖动功能。已弃用，请使用 setDraggable 代替~~ |
| setDraggable(Boolean draggable, OnPlayClickListener onPlayClickListener) | 设置歌词是否允许拖动。如果允许拖动，则 OnPlayClickListener 不能为 null |
| setOnTapListener(LrcView view, float x, float y) | 设置歌词控件点击监听器 |
| setNormalColor | 设置非当前行歌词字体颜色 |
| setCurrentColor | 设置当前行歌词字体颜色 |
| setTimelineTextColor | 设置拖动歌词时选中歌词的字体颜色 |
| setTimelineColor | 设置拖动歌词时时间线的颜色 |
| setTimeTextColor | 设置拖动歌词时右侧时间字体颜色 |
| setCurrentTextSize | 当前歌词文本字体大小 |
| setNormalTextSize | 普通歌词文本字体大小 |

## 思路分析
正常播放时，当前播放的那一行应该在视图中央，首先计算出每一行位于中央时画布应该滚动的距离。<br>
将所有歌词按顺序画出，然后将画布滚动的相应的距离，将正在播放的歌词置于屏幕中央。<br>
歌词滚动时要有动画，使用属性动画即可，我们可以使用当前行和上一行的滚动距离作为动画的起止值。<br>
多行歌词绘制采用StaticLayout。

上下拖动时，歌词跟随手指滚动，绘制时间线。<br>
手指离开屏幕时，一段时间内，如果没有下一步操作，则隐藏时间线，同时将歌词滚动到实际位置，回到正常播放状态；<br>
如果点击播放按钮，则跳转到指定位置，回到正常播放状态。

## 代码实现
onDraw 中将歌词文本绘出，mOffset 是当前应该滚动的距离
```
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int centerY = getHeight() / 2;

    // 无歌词文件
    if (!hasLrc()) {
        mLrcPaint.setColor(mCurrentTextColor);
        @SuppressLint("DrawAllocation")
        StaticLayout staticLayout = new StaticLayout(mDefaultLabel, mLrcPaint, (int) getLrcWidth(),
                Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
        drawText(canvas, staticLayout, centerY);
        return;
    }

    int centerLine = getCenterLine();

    if (isShowTimeline) {
        mPlayDrawable.draw(canvas);

        mTimePaint.setColor(mTimelineColor);
        canvas.drawLine(mTimeTextWidth, centerY, getWidth() - mTimeTextWidth, centerY, mTimePaint);

        mTimePaint.setColor(mTimeTextColor);
        String timeText = LrcUtils.formatTime(mLrcEntryList.get(centerLine).getTime());
        float timeX = getWidth() - mTimeTextWidth / 2;
        float timeY = centerY - (mTimeFontMetrics.descent + mTimeFontMetrics.ascent) / 2;
        canvas.drawText(timeText, timeX, timeY, mTimePaint);
    }

    canvas.translate(0, mOffset);

    float y = 0;
    for (int i = 0; i < mLrcEntryList.size(); i++) {
        if (i > 0) {
            y += (mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) / 2 + mDividerHeight;
        }
        if (i == mCurrentLine) {
            mLrcPaint.setColor(mCurrentTextColor);
        } else if (isShowTimeline && i == centerLine) {
            mLrcPaint.setColor(mTimelineTextColor);
        } else {
            mLrcPaint.setColor(mNormalTextColor);
        }
        drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), y);
    }
}
```
手势监听器
```
private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
    @Override
    public boolean onDown(MotionEvent e) {
        if (hasLrc() && mOnPlayClickListener != null) {
            mScroller.forceFinished(true);
            removeCallbacks(hideTimelineRunnable);
            isTouching = true;
            isShowTimeline = true;
            invalidate();
            return true;
        }
        return super.onDown(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (hasLrc()) {
            mOffset += -distanceY;
            mOffset = Math.min(mOffset, getOffset(0));
            mOffset = Math.max(mOffset, getOffset(mLrcEntryList.size() - 1));
            invalidate();
            return true;
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (hasLrc()) {
            mScroller.fling(0, (int) mOffset, 0, (int) velocityY, 0, 0, (int) getOffset(mLrcEntryList.size() - 1), (int) getOffset(0));
            isFling = true;
            return true;
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (hasLrc() && isShowTimeline && mPlayDrawable.getBounds().contains((int) e.getX(), (int) e.getY())) {
            int centerLine = getCenterLine();
            long centerLineTime = mLrcEntryList.get(centerLine).getTime();
            // onPlayClick 消费了才更新 UI
            if (mOnPlayClickListener != null && mOnPlayClickListener.onPlayClick(centerLineTime)) {
                isShowTimeline = false;
                removeCallbacks(hideTimelineRunnable);
                mCurrentLine = centerLine;
                invalidate();
                return true;
            }
        }
        return super.onSingleTapConfirmed(e);
    }
};
```
滚动动画
```
private void scrollTo(int line, long duration) {
    float offset = getOffset(line);
    endAnimation();

    mAnimator = ValueAnimator.ofFloat(mOffset, offset);
    mAnimator.setDuration(duration);
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
```

代码比较简单，大家根据源码和注释很容易就能看懂。到这里，我们已经实现了可拖动的歌词控件了。<br>
截图看比较简单，大家可以运行源码或下载[波尼音乐](https://github.com/wangchenyan/ponymusic)查看详细效果。

## 关于作者
掘金：https://juejin.cn/user/2313028193754168<br>
微博：https://weibo.com/wangchenyan1993

## License

    Copyright 2017 wangchenyan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
