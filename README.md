# LrcView
[![Download](https://api.bintray.com/packages/chanwong21/maven/lrcview/images/download.svg)](https://bintray.com/chanwong21/maven/lrcview/_latestVersion)

## 系列文章
* [Android开源在线音乐播放器——波尼音乐](http://www.jianshu.com/p/1c0f5c4f64fa)
* [Android开源音乐播放器之播放器基本功能](http://www.jianshu.com/p/bc2f779a5400)
* [Android开源音乐播放器之高仿云音乐黑胶唱片](http://www.jianshu.com/p/f1d8eb8bb3e5)
* [Android开源音乐播放器之自动滚动歌词](http://www.jianshu.com/p/0feb6171b0c5)
* [Android开源音乐播放器之在线音乐列表自动加载更多](http://www.jianshu.com/p/576564627c96)

## 前言
上一节我们仿照云音乐实现了黑胶唱片专辑封面，这节我们该实现歌词显示了。当然，歌词不仅仅是显示就完了，作为一个有素质的音乐播放器，我们当然还需要根据歌曲进度自动滚动歌词，并且要有滚动动画。

- 项目地址：https://github.com/wangchenyan/LrcView
- 有问题请提Issues
- 如果喜欢，欢迎Star！

## 简介
Android歌词控件，支持自动滚动，超长歌词自动换行，自定义属性。<br>
![](https://raw.githubusercontent.com/wangchenyan/LrcView/master/art/screenshot.gif)

## 更新说明
`v 2.0`
- 新增滚动歌词功能

`v 1.4`
- 解析歌词放在工作线程中
- 优化多行歌词时动画不流畅

`v 1.3`
- 支持多个时间标签

`v 1.2`
- 支持RTL（从右向左）语言

`v 1.1`
- 新增超长歌词自动换行
- 新增自定义歌词Padding
- 优化歌词解析

`v 1.0`
- 支持自动滚动
- 支持自定义属性

## 使用
**Gradle**
```
// `latestVersion`改为文首徽章后对应的数值
compile 'me.wcy:lrcview:latestVersion'
```

## 属性
| 属性 | 描述 |
| ---- | ---- |
| lrcTextSize | 歌词文本字体大小 |
| lrcNormalTextColor | 非当前行歌词字体颜色 |
| lrcCurrentTextColor | 当前行歌词字体颜色 |
| lrcDividerHeight | 歌词间距 |
| lrcAnimationDuration | 歌词滚动动画时长 |
| lrcLabel | 没有歌词时屏幕中央显示的文字，如“暂无歌词” |
| lrcPadding | 歌词文字的左右边距 |

## 方法
| 方法 | 描述 |
| ---- | ---- |
| hasLrc() | 歌词是否有效 |
| loadLrc(File) | 加载歌词文件 |
| loadLrc(String) | 加载歌词文本 |
| setLabel(String) | 设置歌词为空时屏幕中央显示的文字，如“暂无歌词” |
| updateTime(long) | 刷新歌词 |
| ~~onDrag(long)~~ | ~~将歌词滚动到指定时间，已弃用，请使用 updateTime(long) 代替~~ |
| setNormalColor(int) | 设置非当前行歌词字体颜色 |
| setCurrentColor(int) | 设置当前行歌词字体颜色 |

## 思路分析
当前播放的那一行应该在视图中央，且高亮显示，计算出每一行位于中央时画布应该滚动的距离。

将所有歌词按顺序画出，然后将画布滚动的相应的距离，将正在播放的歌词置于屏幕中央。

歌词滚动时要有动画，使用属性动画即可，我们可以根据当前行和上一行的滚动距离来做动画。

多行歌词绘制采用StaticLayout。

## 代码实现
**绘制过程**
onDraw 中将歌词文本绘出，mOffset 是当前应该滚动的距离
```
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.translate(0, mOffset);

    // 无歌词文件
    if (!hasLrc()) {
        mPaint.setColor(mCurrentColor);
        @SuppressLint("DrawAllocation")
        StaticLayout staticLayout = new StaticLayout(mLabel, mPaint, (int) getLrcWidth(),
                Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
        drawText(canvas, staticLayout, getHeight() / 2);
        return;
    }

    float y = 0;
    for (int i = 0; i < mLrcEntryList.size(); i++) {
        if (i > 0) {
            y += (mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) / 2 + mDividerHeight;
        }
        mPaint.setColor((i == mCurrentLine) ? mCurrentColor : mNormalColor);
        drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), y);
    }
}
```
换行时根据该行应该滚动的距离做动画
```
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
```

代码比较简单，大家根据注释肯定能看懂。到这里，我们已经实现了滚动显示的歌词控件了。<br>
截图看不出动画效果，大家可以运行源码或下载[波尼音乐](http://fir.im/ponymusic)查看详细效果。

## 关于作者
简书：http://www.jianshu.com/users/3231579893ac<br>
微博：http://weibo.com/wangchenyan1993

## License

    Copyright 2016 wangchenyan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
