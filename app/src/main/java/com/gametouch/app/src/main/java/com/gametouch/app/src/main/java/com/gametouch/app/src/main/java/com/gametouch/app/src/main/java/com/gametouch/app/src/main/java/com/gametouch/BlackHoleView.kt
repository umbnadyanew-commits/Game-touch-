package com.gametouch

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class BlackHoleView(context: Context) : View(context) {

    private var W = 0f
    private var H = 0f
    private var cx = 0f
    private var cy = 0f
    private var angle = 0f
    private var running = true

    private val particles = mutableListOf<Particle3D>()
    private val maxParticles = 400

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var coreGradient: RadialGradient
    private lateinit var glowGradient: RadialGradient

    // Faktor proyeksi 3D
    private val fov = 600f
    private val tilt = 0.5f

    init {
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = 3f
        glowPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        W = w.toFloat()
        H = h.toFloat()
        cx = W / 2f
        cy = H / 2f

        coreGradient = RadialGradient(
            cx, cy, 55f,
            intArrayOf(
                Color.BLACK,
                Color.BLACK,
                Color.argb(180, 60, 0, 120)
            ),
            floatArrayOf(0f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )

        glowGradient = RadialGradient(
            cx, cy, 260f,
            intArrayOf(
                Color.argb(200, 255, 140, 30),
                Color.argb(120, 255, 60, 0),
                Color.argb(60, 150, 0, 255),
                Color.argb(0, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.35f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    inner class Particle3D(x0: Float, y0: Float, z0: Float) {
        var x = x0
        var y = y0
        var z = z0

        var vx = (Random.nextFloat() - 0.5f) * 2f
        var vy = (Random.nextFloat() - 0.5f) * 2f
        var vz = (Random.nextFloat() - 0.5f) * 2f

        var life = 1f
        var size = Random.nextFloat() * 2.5f + 1f

        val hue = 20f + Random.nextFloat() * 260f
        val color = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))

        fun update() {
            val dx = -x
            val dy = -y
            val dz = -z
            val dist = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(1f)

            val force = 12000f / (dist * dist + 200f)

            vx += (dx / dist) * force
            vy += (dy / dist) * force
            vz += (dz / dist) * force

            vx += -dz * 0.02f
            vz += dx * 0.02f

            vx *= 0.99f
            vy *= 0.99f
            vz *= 0.99f

            x += vx
            y += vy
            z += vz

            if (dist < 30f) life -= 0.08f
            if (dist > 1200f) life -= 0.05f
        }

        fun project(): Triple<Float, Float, Float> {
            val scale = fov / (fov + z + 400f)
            val px = cx + x * scale
            val py = cy + y * scale * (1f - tilt * 0.3f)
            return Triple(px, py, scale)
        }

        fun draw(canvas: Canvas) {
            val (px, py, scale) = project()
            if (scale < 0.05f) return

            paint.color = color
            paint.alpha = (life.coerceIn(0f, 1f) * 255).toInt()

            val drawSize = size * scale * 1.5f
            canvas.drawCircle(px, py, drawSize, paint)
        }
    }

    private fun drawAccretionDisk(canvas: Canvas) {
        for (ring in 0 until 3) {
            val rx = 90f + ring * 30f
            val ry = rx * tilt

            canvas.save()
            canvas.rotate((angle * (1 + ring * 0.5f)) * 30f, cx, cy)

            ringPaint.color = Color.HSVToColor(
                floatArrayOf(20f + ring * 40f, 1f, 1f)
            )
            ringPaint.alpha = 180 - ring * 30

            val rect = RectF(
                cx - rx, cy - ry,
                cx + rx, cy + ry
            )
            canvas.drawOval(rect, ringPaint)
            canvas.restore()
        }

        for (ring in 0 until 3) {
            val rx = 90f + ring * 30f
            val ry = rx * tilt

            canvas.save()
            canvas.rotate(-(angle * (1 + ring * 0.5f)) * 30f, cx, cy)

            ringPaint.color = Color.HSVToColor(
                floatArrayOf(200f + ring * 30f, 1f, 1f)
            )
            ringPaint.alpha = 120 - ring * 20

            val rect = RectF(
                cx - rx, cy - ry,
                cx + rx, cy + ry
            )
            canvas.drawOval(rect, ringPaint)
            canvas.restore()
        }
    }

    private fun drawBlackHole(canvas: Canvas) {
        glowPaint.shader = glowGradient
        canvas.drawCircle(cx, cy, 260f, glowPaint)
        glowPaint.shader = null

        drawAccretionDisk(canvas)

        corePaint.shader = coreGradient
        canvas.drawCircle(cx, cy, 55f, corePaint)
        corePaint.shader = null

        ringPaint.color = Color.argb(220, 200, 230, 255)
        ringPaint.strokeWidth = 2.5f
        canvas.drawCircle(cx, cy, 58f, ringPaint)
        ringPaint.strokeWidth = 3f

        ringPaint.color = Color.argb(120, 255, 200, 100)
        ringPaint.strokeWidth = 1.5f
        canvas.drawCircle(cx, cy, 62f, ringPaint)
        ringPaint.strokeWidth = 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.argb(50, 0, 0, 0)
        canvas.drawRect(0f, 0f, W, H, paint)

        drawBlackHole(canvas)

        val sorted = particles.sortedBy { it.z }
        val iterator = sorted.iterator()
        val toRemove = mutableListOf<Particle3D>()

        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update()
            p.draw(canvas)
            if (p.life <= 0f) toRemove.add(p)
        }
        particles.removeAll(toRemove)

        if (particles.size < maxParticles && Random.nextInt(10) < 4) {
            spawnRandom()
        }

        angle += 0.01f
        if (running) invalidate()
    }

    private fun spawnRandom() {
        val radius = 600f + Random.nextFloat() * 400f
        val theta = Random.nextFloat() * (2 * PI).toFloat()
        val phi = Random.nextFloat() * (PI).toFloat() - (PI / 2).toFloat()

        val x = radius * cos(theta) * cos(phi)
        val y = radius * sin(phi)
        val z = radius * sin(theta) * cos(phi)

        particles.add(Particle3D(x, y, z))
    }

    private fun spawn(x: Float, y: Float, n: Int) {
        for (i in 0 until n) {
            if (particles.size < maxParticles) {
                val dx = x - cx
                val dy = y - cy
                val z = Random.nextFloat() * 400f - 200f
                particles.add(Particle3D(dx, dy, z))
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    spawn(event.getX(i), event.getY(i), 6)
                }
            }
            MotionEvent.ACTION_UP -> {
                spawn(event.x, event.y, 15)
            }
        }
        return true
    }

    fun resume() { running = true; invalidate() }
    fun pause() { running = false }
}
