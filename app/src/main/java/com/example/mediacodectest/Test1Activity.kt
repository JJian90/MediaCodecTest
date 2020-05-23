package com.example.mediacodectest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mediacodectest.decoder.AudioDecoder
import com.example.mediacodectest.decoder.VideoDecoder
import kotlinx.android.synthetic.main.activity_test1.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Test1Activity : AppCompatActivity() {
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(2)
    private var videoDecoder: VideoDecoder? = null
    private var audioDecoder: AudioDecoder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test1)
        initPermission()

    }

    private fun initPlayer() {
        val path =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "videoTest" + File.separator + "10000003.mp4"


        //创建视频解码器
        videoDecoder = VideoDecoder(path, surfaceView, null)
        threadPool.execute(videoDecoder)

        //创建音频解码器
        audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        //开启播放
        playBtn.setOnClickListener {
            play()
        }
        play()
    }

    var isplay = true

    private fun play() {
        if (isplay) {
            playBtn.text = "暂停"
            isplay = false
            videoDecoder?.pause()
            audioDecoder?.pause()
        } else {
            isplay = true
            playBtn.text = "播放"
            videoDecoder?.goOn()
            audioDecoder?.goOn()
        }


    }

    val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val mPermissionList = ArrayList<String>()
    val mRequestCode = 0x1//权限请求码
    fun initPermission() {
        mPermissionList.clear()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this@Test1Activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mPermissionList.add(permission)
            }
        }
        if (!mPermissionList.isEmpty()) {
            // 后续操作...
            ActivityCompat.requestPermissions(this@Test1Activity, permissions, mRequestCode)
        } else {
            Toast.makeText(this@Test1Activity, "全部授予！", Toast.LENGTH_SHORT).show()
            initPlayer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0x1 -> for (i in 0 until grantResults.size) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) Toast.makeText(
                    this,
                    "您有未授予的权限，可能影响使用",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }
}
