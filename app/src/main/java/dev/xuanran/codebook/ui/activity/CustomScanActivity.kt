package dev.xuanran.codebook.ui.activity

import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import android.os.Bundle
import android.widget.ImageButton
import dev.xuanran.codebook.R

class CustomScanActivity : CaptureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 注意：不要在这里调用 setContentView，让父类处理
        super.onCreate(savedInstanceState)
        
        // 添加返回按钮
        findViewById<ImageButton>(R.id.back_button)?.setOnClickListener {
            finish()
        }
    }

    override fun initializeContent(): DecoratedBarcodeView {
        // 使用默认的扫描视图
        return super.initializeContent()
    }
} 