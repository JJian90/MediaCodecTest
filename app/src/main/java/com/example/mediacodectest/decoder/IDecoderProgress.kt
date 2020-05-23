package com.example.mediacodectest.decoder

/**
 * 解码进度
 * Created by Gerald on 2020/5/16.
 */
interface IDecoderProgress {
    /**
     * 视频宽高回调
     */
    fun videoSizeChange(width: Int, height: Int, rotationAngle: Int)

    /**
     * 视频播放进度回调
     */
    fun videoProgressChange(pos: Long)
}