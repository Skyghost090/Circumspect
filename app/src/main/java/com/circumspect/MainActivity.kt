package com.circumspect

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.ktsh.Shell


class MainActivity : AppCompatActivity() {
    private var exitcode: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edittexttext = findViewById<EditText>(R.id.editTextText)
        val textview = findViewById<TextView>(R.id.textView2)
        val getprocessbutton = findViewById<ImageButton>(R.id.get_process_info)
        val starttracebutton = findViewById<ImageButton>(R.id.start_trace_button)
        val tablayout = findViewById<TabLayout>(R.id.tabLayout)

        getprocessbutton.setBackgroundResource(R.drawable.buttonprocess)
        starttracebutton.setBackgroundResource(R.drawable.buttonprocess)
        if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
            edittexttext.setBackgroundResource(R.drawable.textview)
            textview.setBackgroundResource(R.drawable.textview)
        } else {
            edittexttext.setBackgroundResource(R.drawable.textviewlight)
            textview.setBackgroundResource(R.drawable.textviewlight)
        }

        getprocessbutton.setOnClickListener {
            val internalcommand = Shell.SU.run("cat /proc/${edittexttext.text}/status")
            exitcode = internalcommand.exitCode
            textview.setText(internalcommand.output.toString())
            textview.isVisible = true
        }

        starttracebutton.setOnClickListener {
            if(exitcode != 0){
                Toast.makeText(this, "Pid not found", Toast.LENGTH_SHORT).show()
            } else {
                Thread{
                    Shell.SU.run("mkdir -p /storage/emulated/0/Circumspect/${edittexttext.text}/")
                    Shell.SU.run("touch /storage/emulated/0/Circumspect/${edittexttext.text}/trace.txt")
                    Shell.SU.run("timeout 5m strace -p ${edittexttext.text} -o /storage/emulated/0/Circumspect/${edittexttext.text}/trace.txt")
                }.start()
            }
        }

        fun detectTab() {
            when(tablayout.selectedTabPosition) {
                1 -> {
                    val fileintent = Intent(Intent.ACTION_GET_CONTENT)
                    fileintent.setType("text/plain")
                    val uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Circumspect/")
                    fileintent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                    startActivity(fileintent)
                    tablayout.selectTab(tablayout.getTabAt(0))
                }
                2 -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("About")
                        .setMessage(R.string.motivation_text)
                        .setNeutralButton("Github"){ dialog, id ->
                            val builder = CustomTabsIntent.Builder()
                            builder.setInstantAppsEnabled(true)
                            builder.setDownloadButtonEnabled(false)
                            val customBuilder = builder.build()
                            customBuilder.intent.setPackage("com.android.chrome")
                            customBuilder.launchUrl(this, Uri.parse("https://github.com/Skyghost090"))
                        }
                        .setOnCancelListener {
                            tablayout.selectTab(tablayout.getTabAt(0))
                        }
                    // Create the AlertDialog object and return it.
                    builder.show()
                }
            }
        }

        detectTab()

        tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {detectTab()}
            override fun onTabUnselected(tab: TabLayout.Tab) {detectTab()}
            override fun onTabReselected(tab: TabLayout.Tab) {detectTab()}
        })
    }
}
