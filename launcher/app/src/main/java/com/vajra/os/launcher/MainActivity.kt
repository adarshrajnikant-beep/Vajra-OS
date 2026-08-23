package com.vajra.os.launcher

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var grid: GridLayout
    private lateinit var pm: android.content.pm.PackageManager
    private var allApps: List<android.content.pm.ResolveInfo> = listOf()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pm = packageManager
        val root = FrameLayout(this)

        val flag = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        listOf("#FF9933", "#FFFFFF", "#138808").forEach { hex ->
            val strip = View(this).apply {
                setBackgroundColor(Color.parseColor(hex))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            }
            flag.addView(strip)
        }
        root.addView(flag, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val title = TextView(this).apply {
            text = "VAJRA OS"
            textSize = 26f
            setTextColor(Color.parseColor("#000080"))
            gravity = Gravity.CENTER
            setPadding(0, 70, 0, 4)
            setTypeface(typeface, Typeface.BOLD)
        }
        content.addView(title)

        val clockText = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.parseColor("#000080"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        content.addView(clockText)

        fun updateClock() {
            val now = Date()
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
            val date = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(now)
            clockText.text = "$time  •  $date"
            handler.postDelayed({ updateClock() }, 1000)
        }
        updateClock()

        val searchBox = EditText(this).apply {
            hint = "Apps khoje..."
            setPadding(30, 20, 30, 20)
            setBackgroundColor(Color.parseColor("#FFFFFF"))
            setTextColor(Color.BLACK)
        }
        val searchWrapper = LinearLayout(this).apply {
            setPadding(24, 0, 24, 16)
            addView(searchBox, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        content.addView(searchWrapper)

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#DDFFFFFF"))
            setPadding(16, 16, 16, 16)
        }

        val scroll = ScrollView(this)
        grid = GridLayout(this).apply { columnCount = 4 }

        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        allApps = pm.queryIntentActivities(launcherIntent, 0)
            .sortedBy { it.loadLabel(pm).toString() }

        fun renderApps(list: List<android.content.pm.ResolveInfo>) {
            grid.removeAllViews()
            for (app in list) {
                val cell = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(20, 20, 20, 20)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 220
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                    }
                }
                val icon = ImageView(this).apply {
                    setImageDrawable(app.loadIcon(pm))
                    layoutParams = LinearLayout.LayoutParams(130, 130)
                }
                val label = TextView(this).apply {
                    text = app.loadLabel(pm)
                    textSize = 11f
                    gravity = Gravity.CENTER
                    setTextColor(Color.BLACK)
                    maxLines = 1
                }
                cell.addView(icon)
                cell.addView(label)
                cell.setOnClickListener {
                    val launch = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        component = ComponentName(
                            app.activityInfo.packageName, app.activityInfo.name
                        )
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(launch)
                }
                grid.addView(cell)
            }
        }
        renderApps(allApps)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                val query = s.toString().lowercase()
                val filtered = allApps.filter {
                    it.loadLabel(pm).toString().lowercase().contains(query)
                }
                renderApps(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        scroll.addView(grid)
        panel.addView(scroll)
        content.addView(panel, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(content, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))

        setContentView(root)
    }

    override fun onBackPressed() {
        // Launcher pe back se exit nahi hona chahiye
    }
}
