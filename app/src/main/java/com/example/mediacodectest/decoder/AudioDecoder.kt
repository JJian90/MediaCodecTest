package com.example.mediacodectest.decoder

import android.media.*
import com.example.mediacodectest.extractor.AudioExtractor
import com.example.mediacodectest.extractor.IExtractor
import java.nio.ByteBuffer

/**
 * 音频解码器
 * Created by Gerald on 2020/5/16.
 */
class AudioDecoder(path: String) : BaseDecoder(path) {
    /**
     * 采样率
     **/
    private var mSampleRate = -1

    /**
     * 声音通道数量
     ***/
    private var mChannels = 1

    /**
     * PCM采样位数
     **/
    private var mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 音频播放器
     * */
    private var mAudioTrack: AudioTrack? = null

    /**
     * 音频数据缓存
     * */
    private var mAudioOutTempBuf: ShortArray? = null

    override fun check(): Boolean {
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return AudioExtractor(path)
    }

    /**
     * 2. 获取参数不一样
     * 音频播放需要获取采样率，通道数，采样位数等
     */
    override fun initSpecParams(format: MediaFormat) {
        try {
            mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)

            mPCMEncodeBit = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                format.getInteger(MediaFormat.KEY_PCM_ENCODING)
            } else {
                //如果没有这个参数，默认为16位采样
                AudioFormat.ENCODING_PCM_16BIT
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 1. 初始化解码器
     * 音频不需要surface，直接传null
     */
    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        codec.configure(format, null, null, 0)
        return true
    }

    /**
     * 3. 需要初始化一个音频渲染器：AudioTrack
     * 由于解码出来的数据是PCM数据，所以直接使用AudioTrack播放即可。在initRender() 中对其进行初始化。
     * 根据通道数量配置单声道和双声道
     * 根据采样率、通道数、采样位数计算获取最小缓冲区
     */
    override fun initRender(): Boolean {
        val channel = if (mChannels == 1) {
            //单声道
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            //双声道
            AudioFormat.CHANNEL_OUT_STEREO
        }

        //获取最小缓冲区
        val minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMEncodeBit)

        mAudioOutTempBuf = ShortArray(minBufferSize / 2)
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,//播放类型：音乐
            mSampleRate, //采样率
            channel, //通道
            mPCMEncodeBit, //采样位数
            minBufferSize, //缓冲区大小
            AudioTrack.MODE_STREAM
        ) //播放模式：数据流动态写入，另一种是一次性写入

        mAudioTrack!!.play()
        return true
    }

    /**
     * 4. 手动渲染音频数据，实现播放
     * 最后就是将解码出来的数据写入AudioTrack，实现播放。
     * 有一点注意的点是，需要把解码数据由ByteBuffer类型转换为ShortBuffer，这时Short数据类型的长度要减半。
     */
    override fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        if (mAudioOutTempBuf!!.size < bufferInfo.size / 2) {
            mAudioOutTempBuf = ShortArray(bufferInfo.size / 2)
        }
        outputBuffer.position(0)
        outputBuffer.asShortBuffer().get(mAudioOutTempBuf, 0, bufferInfo.size / 2)
        mAudioTrack!!.write(mAudioOutTempBuf!!, 0, bufferInfo.size / 2)
    }

    override fun doneDecode() {
        mAudioTrack?.stop()
        mAudioTrack?.release()
    }
}