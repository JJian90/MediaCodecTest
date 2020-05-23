package com.example.mediacodectest.base

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * 一帧数据
 * Created by Gerald on 2020/5/16.
 */
class Frame {
    var buffer: ByteBuffer? = null

    var bufferInfo = MediaCodec.BufferInfo()
        private set

    fun setBufferInfo(info: MediaCodec.BufferInfo) {
        bufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags)
    }
}