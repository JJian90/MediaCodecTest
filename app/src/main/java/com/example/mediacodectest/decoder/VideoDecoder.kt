package com.example.mediacodectest.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.mediacodectest.extractor.IExtractor
import com.example.mediacodectest.extractor.VideoExtractor
import java.nio.ByteBuffer

/**
 * 视频解码器
 * Created by Gerald on 2020/5/16.
 */
class VideoDecoder(path: String, sfv: SurfaceView?, surface: Surface?) : BaseDecoder(path) {
    private val TAG = "VideoDecoder"
    private val mSurfaceView = sfv
    private var mSurface = surface

    override fun check(): Boolean {
        /**
         * 检查参数
         * 视频解码支持两种类型渲染表面，一个是SurfaceView，一个Surface。当其实最后都是传递Surface给MediaCodec
         * SurfaceView应该是大家比较熟悉的View了，最常使用的就是用来做MediaPlayer的显示。当然也可以绘制图片、动画等。
         * Surface应该不是很常用了，这里为了支持后续使用OpenGL来渲染视频，所以预先做了支持。
         */
        if (mSurfaceView == null && mSurface == null) {
            Log.w(TAG, "SurfaceView和Surface都为空，至少需要一个不为空")
            mStateListener?.decoderError(this, "显示器为空")
            return false
        }
        return true
    }

    /**
     * 生成数据提取器
     */
    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
    }

    /**
     * 配置解码器
     * 初始化Surface
     * 就是因为考虑到一个问题，SurfaceView的创建是有一个时间过程的，并非马上可以使用，需要通过CallBack来监听它的状态。
     * 在surface初始化完毕后，再配置MediaCodec。
     * 如果使用OpenGL直接传递surface进来，直接配置MediaCodec即可。
     */
    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0)
            notifyDecode()
        } else if (mSurfaceView?.holder?.surface != null) {
            mSurface = mSurfaceView?.holder?.surface
            configCodec(codec, format)
        } else {
            //初始化Surface
            mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    mSurface = holder.surface
                    configCodec(codec, format)
                }
            })

            return false
        }
        return true
    }

    override fun initRender(): Boolean {
        return true
    }

    /**
     * 视频的渲染并不需要客户端手动去渲染，只需提供绘制表面surface，调用releaseOutputBuffer，
     * 将2个参数设置为true即可。所以，这里也不用在做什么操作了
     */
    override fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
    }

    override fun doneDecode() {
    }
}