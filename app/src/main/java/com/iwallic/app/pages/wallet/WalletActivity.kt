package com.iwallic.app.pages.wallet

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.adapters.WalletHistoryAdapter
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.pages.main.MainActivity
import com.iwallic.app.models.WalletAgentModel
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletDBUtils
import com.iwallic.app.utils.WalletUtils

class WalletActivity : BaseActivity() {
    private lateinit var createB: Button
    private lateinit var importB: Button
    private lateinit var historyFAB: FloatingActionButton
    private lateinit var history: ArrayList<WalletAgentModel>
    private lateinit var historyLL: LinearLayout
    private lateinit var historyRV: RecyclerView
    private lateinit var gateLL: LinearLayout
    private lateinit var backFAB: FloatingActionButton
    private lateinit var adapter: WalletHistoryAdapter

    private lateinit var adapterViewManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_wallet)
        initDOM()
        initListener()
        initHistory()
    }

    private fun initDOM() {
        createB = findViewById(R.id.wallet_create_btn)
        importB = findViewById(R.id.wallet_import_btn)
        historyFAB = findViewById(R.id.wallet_history_btn)
        historyRV = findViewById(R.id.wallet_history_list_view)
        historyLL = findViewById(R.id.wallet_history_list)
        gateLL = findViewById(R.id.wallet_gate)
        backFAB = findViewById(R.id.wallet_history_list_back)
    }

    private fun initListener() {
        createB.setOnClickListener {
            startActivity(Intent(this, WalletCreateActivity::class.java))
        }
        importB.setOnClickListener {
            startActivity(Intent(this, WalletImportActivity::class.java))
        }
        historyFAB.setOnClickListener {
            historyLL.visibility = View.VISIBLE
            gateLL.visibility = View.GONE
        }
        backFAB.setOnClickListener {
            historyLL.visibility = View.GONE
            gateLL.visibility = View.VISIBLE
        }
    }

    private fun initHistory() {
        history = WalletDBUtils(this).getAll()
        if (history.isNotEmpty()) {
            historyFAB.visibility = View.VISIBLE
            resolveList()
        }
    }

    private fun resolveList() {
        adapterViewManager = LinearLayoutManager(this)
        adapter = WalletHistoryAdapter(history)
        historyRV.layoutManager = adapterViewManager
        historyRV.adapter = adapter
        adapter.onChoose().subscribe {
            resolveOpen(adapter.getData(it))
        }
        adapter.onDelete().subscribe {
            resolveDel(adapter.getData(it), it)
        }
    }

    private fun resolveOpen(w: WalletAgentModel) {
        Log.i("【Wallet】", "open wallet【${w._ID}】")
        DialogUtils.password(this).subscribe { pwd ->
            if (pwd.isEmpty()) {
                return@subscribe
            }
            DialogUtils.load(this).subscribe {
                WalletUtils.switch(baseContext, w, pwd).subscribe {rs ->
                    it.dismiss()
                    if (rs == 0) {
                        val intent = Intent(baseContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    } else {
                        if (!DialogUtils.error(baseContext, rs)) {
                            Toast.makeText(baseContext, "$rs", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun resolveDel(w: WalletAgentModel, p: Int) {
        Log.i("【Wallet】", "del wallet at【$p】")
        DialogUtils.confirm(
            this,
            R.string.dialog_title_warn,
            R.string.dialog_content_addrdel,
            R.string.dialog_ok,
            R.string.dialog_no
        ).subscribe {
            if (!it) {
                return@subscribe
            }
            WalletUtils.remove(baseContext, w)
            adapter.remove(p)
            if (adapter.itemCount == 0) {
                historyLL.visibility = View.GONE
                gateLL.visibility = View.VISIBLE
                historyFAB.visibility = View.GONE
            }
        }
    }
}
