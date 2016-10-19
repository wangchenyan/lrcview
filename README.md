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

* **开源不易，希望能给个Star鼓励**
* 项目地址：https://github.com/ChanWong21/LrcView
* 有问题请提Issues

## 简介
Android歌词控件，支持自动滚动，超长歌词自动换行，自定义属性。

## 更新说明
`v 1.1`
* 新增超长歌词自动换行
* 新增自定义歌词Padding
* 优化歌词解析

`v 1.0`
* 支持自动滚动
* 支持自定义属性

![](https://raw.githubusercontent.com/ChanWong21/LrcView/master/art/screenshot.gif)

## 使用
**Gradle**
```
compile 'me.wcy:lrcview:latestVersion'
```

## 属性
| 属性 | 描述 |
| ---- | ---- |
| lrcTextSize | 歌词文本字体大小 |
| lrcDividerHeight | 歌词间距 |
| lrcNormalTextColor | 非当前行歌词字体颜色 |
| lrcCurrentTextColor | 当前行歌词字体颜色 |
| lrcAnimationDuration | 歌词滚动动画时长 |
| lrcLabel | 没有歌词时屏幕中央显示的文字，如“暂无歌词” |
| lrcPadding | 歌词文字的左右边距 |

## 方法
| 方法 | 描述 |
| ---- | ---- |
| hasLrc() | 歌词是否有效 |
| loadLrc(File) | 加载歌词文件 |
| loadLrc(String) | 加载歌词文本 |
| onDrag(long) | 将歌词滚动到指定时间 |
| setLabel(String) | 设置歌词为空时屏幕中央显示的文字，如“暂无歌词” |
| updateTime(long) | 刷新歌词 |

## 思路分析
首先，当前播放的那一行应该在视图中央，且高亮显示，然后是当前行以前的在上面，当前行以后的在下面，所以我们的绘制流程是先绘制当前行，然后依次绘制上面的和下面的。

歌词滚动时要有动画，使用属性动画即可，我们可以在切换当前行时让视图中心Y坐标向下偏移一行，然后用属性动画将它移回中心位置，这样就达到了动画的目的。

有一点需要注意，由于视图的高度不能确定，所以我们在绘制当前行以上（或以下）的歌词时可能会被截断，因此我们在绘制当前行以上（或以下）歌词时应该先判断下是否超出视图可视范围。

多行歌词绘制采用StaticLayout。

## 代码实现
**绘制过程**
```
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
        StaticLayout staticLayout = new StaticLayout(mLabel, mPaint, (int) getLrcWidth(),
                Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        drawText(canvas, staticLayout, centerY - staticLayout.getLineCount() * mTextSize / 2);
        return;
    }

    // 画当前行
    float currY = centerY - mLrcEntryList.get(mCurrentLine).getTextHeight() / 2;
    drawText(canvas, mLrcEntryList.get(mCurrentLine).getStaticLayout(), currY);

    // 画当前行上面的
    mPaint.setColor(mNormalColor);
    float upY = currY;
    for (int i = mCurrentLine - 1; i >= 0; i--) {
        upY -= mDividerHeight + mLrcEntryList.get(i).getTextHeight();

        if (mAnimator == null || !mAnimator.isRunning()) {
            // 动画已经结束，超出屏幕停止绘制
            if (upY < 0) {
                break;
            }
        }

        drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), upY);

        // 动画未结束，超出屏幕多绘制一行
        if (upY < 0) {
            break;
        }
    }

    // 画当前行下面的
    float downY = currY + mLrcEntryList.get(mCurrentLine).getTextHeight() + mDividerHeight;
    for (int i = mCurrentLine + 1; i < mLrcEntryList.size(); i++) {
        if (mAnimator == null || !mAnimator.isRunning()) {
            // 动画已经结束，超出屏幕停止绘制
            if (downY + mLrcEntryList.get(i).getTextHeight() > getHeight()) {
                break;
            }
        }

        drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), downY);

        // 动画未结束，超出屏幕多绘制一行
        if (downY + mLrcEntryList.get(i).getTextHeight() > getHeight()) {
            break;
        }

        downY += mLrcEntryList.get(i).getTextHeight() + mDividerHeight;
    }
}
```

代码比较简单，大家根据注释肯定能看懂。到这里，我们已经实现了滚动显示的歌词控件了。<br>
截图看不出动画效果，大家可以运行源码或下载[波尼音乐](http://fir.im/ponymusic)查看详细效果。

## 关于作者
简书：http://www.jianshu.com/users/3231579893ac<br>
微博：http://weibo.com/wangchenyan1993

## License

    Copyright 2016 Chay Wong

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
