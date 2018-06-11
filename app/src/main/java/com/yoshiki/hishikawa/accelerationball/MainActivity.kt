package com.yoshiki.hishikawa.accelerationball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener
        , SurfaceHolder.Callback {

    /* Parameter */
    private lateinit var sensorManager: SensorManager
    private var surfaceWidth: Int = 0  // サーフェスビューの幅
    private var surfaceHeight: Int = 0 // サーフェスビューの高さ

    private val radius = 50.0f    // ボールの半径
    private val coef = 1000.0f    // ボールの移動量調整用

    private var ballX: Float = 0f // ボールの現在のx座標
    private var ballY: Float = 0f // ボールの現在のy座標
    private var vx: Float = 0f    // ボールのx方向への加速度
    private var vy: Float = 0f    // ボールのy方向への加速度
    private var time: Long = 0L   // 前回時間の保持

    /* method */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 画面を縦方向に固定
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        // イベントリスナーの登録
        val holder = surfaceView.holder
        holder.addCallback(this)
    }

    // サーフェスが作成された時の処理
    // サーフェスとは、描画用のSurfaceViewの内部にある高速描画専用のレイヤー
    override fun surfaceCreated(p0: SurfaceHolder?) {

        // 加速度センサークラスの取得
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 加速度センサーの監視開始（第３引数：センサーの更新頻度）
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    // サーフェス変更時の処理
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height

        // ボールの座標をサーフェスビューの画面中央に設定
        ballX = (width / 2).toFloat()
        ballY = (height / 2).toFloat()
    }

    // サーフェスが破棄された時の処理
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // 加速度センサーの監視終了
        sensorManager.unregisterListener(this)
    }

    // センサーの精度が変更された時の処理
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    // センサーの値が更新された時の処理
    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return

        if(time == 0L) time = System.currentTimeMillis()
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = -event.values[0] // 画面の描画方向に合わせるため反転
            val y = event.values[1]

            // 前回からの経過時間
            var t = (System.currentTimeMillis() - time).toFloat()
            t /= 1000.0f

            // 現在時刻の保持
            time = System.currentTimeMillis()

            /* 初速度v0からt秒後に達した時の加速度と移動距離の関係式
              d = v0 * t + 1/2 * a * t^2
            * */

            // x,y軸方向の移動距離
            val dx = vx * t + x * t * t / 2.0f
            val dy = vx * t + y * t * t / 2.0f

            // 移動後のボールの座標
            ballX += dx * coef
            ballY += dy * coef

            // 次に加速度センサーから値を受け取った時に再計算するため、加速度を保持
            vx += x * t
            vy += y * t

            // 座標が0より小さい場合、加速度を反転して、画面に収まるようにする（ボールの半径を意識しつつ）
            if(ballX - radius < 0 && vx < 0) {
                vx = -vx / 1.5f
                ballX = radius
            // 座標がサーフェスの幅を超えてしまった場合も反転して画面に収まるようにする。
            } else if(ballX + radius > surfaceWidth && vx > 0) {
                vx = -vx / 1.5f
                ballX = surfaceWidth - radius
            }

            // y座標も同様
            if(ballY - radius < 0 && vy < 0) {
                vy = -vy / 1.5f
                ballY = radius
            } else if(ballY + radius > surfaceWidth && vy > 0) {
                vy = -vy / 1.5f
                ballY = surfaceWidth - radius
            }

            // ボール位置をサーフェスビューに描画
            drawCanvas()
        }
    }

    // ボール位置をサーフェスビューに描画
    private fun drawCanvas() {
        val canvas = surfaceView.holder.lockCanvas()
        canvas.drawColor(Color.YELLOW)
        canvas.drawCircle(ballX, ballY, radius, Paint().apply {
            color = Color.MAGENTA
        })
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }
}
