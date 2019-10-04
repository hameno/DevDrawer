package de.psdev.devdrawer.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import de.psdev.devdrawer.DevDrawerApplication
import de.psdev.devdrawer.R
import de.psdev.devdrawer.adapters.PartialMatchAdapter
import de.psdev.devdrawer.utils.Constants
import de.psdev.devdrawer.utils.getExistingPackages
import kotlinx.android.synthetic.main.edit_dialog.*
import mu.KLogging

class EditDialog: Activity(), TextWatcher {

    companion object: KLogging() {
        @JvmStatic
        fun createStartIntent(context: Context, id: Int, filter: String) = Intent(context, EditDialog::class.java).apply {
            putExtra("id", id)
            putExtra("text", filter)
        }
    }

    private val id: Int by lazy { intent?.extras?.getInt("id")!! }
    private val originalText: String by lazy { intent?.extras?.getString("text").orEmpty() }

    private val appPackages: List<String> by lazy { packageManager.getExistingPackages() }
    private val devDrawerDatabase by lazy { (application as DevDrawerApplication).devDrawerDatabase }
    private val packageNameCompletionAdapter: PartialMatchAdapter by lazy { PartialMatchAdapter(this, appPackages, devDrawerDatabase, true) }

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_dialog)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        editDialogEditText.setText(originalText)
        editDialogEditText.setAdapter(packageNameCompletionAdapter)
        editDialogEditText.addTextChangedListener(this)

        // Change button sends a result back to MainActivity
        changeButton.setOnClickListener {
            val intent = Intent().apply {
                putExtra("id", id)
                putExtra("newText", editDialogEditText.text.toString())
            }
            setResult(Constants.EDIT_DIALOG_CHANGE, intent)
            finish()
        }
    }

    // ==========================================================================================================================
    // TextWatcher
    // ==========================================================================================================================

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) = Unit

    override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) = Unit

    override fun afterTextChanged(editable: Editable) {
        packageNameCompletionAdapter.filter.filter(editable.toString())
    }

}
