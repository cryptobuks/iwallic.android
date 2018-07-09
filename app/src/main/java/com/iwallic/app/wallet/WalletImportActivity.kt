package com.iwallic.app.wallet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.base.MainActivity
import com.iwallic.app.utils.WalletUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class WalletImportActivity : BaseActivity() {

    var backLL: LinearLayout? = null
    var wifET: EditText? = null
    var fileB: Button? = null
    var importB: Button? = null
    var pwdET: EditText? = null
    var confirmET: EditText? = null
    var errorTV: TextView? = null
    var importPB: ProgressBar? = null
    var wif: String = ""
    var pwd: String = ""
    var confirm: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_import)

        initDOM()
        initInput()
        initClick()
    }
    private fun initDOM() {
        backLL = findViewById(R.id.wallet_import_back)
        wifET = findViewById(R.id.wallet_import_wif)
        fileB = findViewById(R.id.wallet_import_btn_file)
        importB = findViewById(R.id.wallet_import_btn_wif)
        pwdET = findViewById(R.id.wallet_import_pwd)
        confirmET = findViewById(R.id.wallet_import_confirm)
        errorTV = findViewById(R.id.wallet_import_error)
        importPB = findViewById(R.id.wallet_import_load_wif)
    }
    private fun initInput() {
        wifET!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                wif = p0.toString()
                resolveError()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        pwdET!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                pwd = p0.toString()
                resolveError()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        confirmET!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                confirm = p0.toString()
                resolveError()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }
    private fun initClick() {
        backLL!!.setOnClickListener {
            finish()
        }
        importB!!.setOnClickListener {
            if (wif.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, R.string.error_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (WalletUtils.check(wif, "wif") && pwd.length > 5 && pwd == confirm) {
                resolveImport()
            }
        }
        fileB!!.setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }
    }
    private fun resolveError() {
        if (!WalletUtils.check(wif, "wif")) {
            errorTV!!.setText(R.string.wallet_import_error_wif)
            errorTV!!.visibility = View.VISIBLE
        } else if (pwd.isNotEmpty() && pwd.length < 6) {
            errorTV!!.setText(R.string.wallet_create_error_short)
            errorTV!!.visibility = View.VISIBLE
        } else if (confirm.isNotEmpty() && confirm != pwd) {
            errorTV!!.setText(R.string.wallet_create_error_mismatch)
            errorTV!!.visibility = View.VISIBLE
        } else {
            errorTV!!.visibility = View.INVISIBLE
        }
    }
    private fun resolveImport() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        importB!!.visibility = View.INVISIBLE
        importPB!!.visibility = View.VISIBLE
        launch {
            var done = true
            val w = WalletUtils.import(wif, pwd)
            if (w == null || !WalletUtils.save(baseContext, w)) {
                done = false
            }
            withContext(UI) {
                importB!!.visibility = View.VISIBLE
                importPB!!.visibility = View.INVISIBLE
                if (done) {
                    val intent = Intent(baseContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, R.string.error_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
