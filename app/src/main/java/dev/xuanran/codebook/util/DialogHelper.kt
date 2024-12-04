package dev.xuanran.codebook.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dev.xuanran.codebook.BuildConfig
import dev.xuanran.codebook.R
import dev.xuanran.codebook.bean.Constants
import dev.xuanran.codebook.bean.account.AccountEntity
import dev.xuanran.codebook.bean.account.model.AccountViewModel
import dev.xuanran.codebook.callback.DialogEditTextCallback
import java.lang.Exception
import java.lang.StringBuilder
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.Random

class DialogHelper(context: Context) {
    private val context: Context
    private val inflater: LayoutInflater

    init {
        this.context = context
        this.inflater = LayoutInflater.from(context)
    }

    fun showEncryptionTypeDialog(
        onFingerprintSelected: DialogInterface.OnClickListener?,
        onPasswordSelected: DialogInterface.OnClickListener?
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(R.string.choose_encryption_method)
            .setMessage(R.string.choose_encryption_method_tips)
            .setCancelable(false)
            .setPositiveButton(R.string.fingerprint, onFingerprintSelected)
            .setNegativeButton(R.string.password, onPasswordSelected)
            .show()
    }

    fun showCancelFingerprintDialog(
        reAuthClick: DialogInterface.OnClickListener?,
        exitClick: DialogInterface.OnClickListener?
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(R.string.fingerprint_title)
            .setMessage(R.string.fingerprint_auth_cancel)
            .setCancelable(false)
            .setPositiveButton(R.string.re_auth, reAuthClick)
            .setNegativeButton(R.string.exit, exitClick)
            .show()
    }

    fun showImportDialog(uri: Uri?, callback: DialogEditTextCallback) {
        val builder = MaterialAlertDialogBuilder(context)
        val dialogView = inflater.inflate(R.layout.dialog_password, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.password_input)
        val saltInput = dialogView.findViewById<EditText>(R.id.salt_input)
        builder.setTitle(R.string.import_data)
        builder.setView(dialogView)
        builder.setPositiveButton(
            R.string.importStr,
            DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int ->
                callback.onEditTextEntered(
                    (saltInput.getText().toString() +
                            Constants.EXPORT_IMPORT_PASS_SPILT
                            + passwordInput.getText().toString())
                )
            })
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    fun showVerifyDialog() {
        val dialogView = inflater.inflate(R.layout.dialog_verfiy, null)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(false)
            .setView(dialogView)
            .show()
    }


    fun showDonateDialog() {
        val dialogView = inflater.inflate(R.layout.dialog_donate, null)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(dialogView)
            .setNegativeButton(
                "取消",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> dialog!!.dismiss() })
            .setPositiveButton(
                "打开支付宝",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> openAlipay() })
            .show()

        val imageView = dialogView.findViewById<ImageView>(R.id.iv_donate_image)
        try {
            val inputStream = context.getAssets().open("donate_image.png")
            val drawable = Drawable.createFromStream(inputStream, null)
            imageView.setImageDrawable(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startPasswordFlow(
        callback: DialogEditTextCallback,
        exit: DialogInterface.OnClickListener?
    ) {
        // 弹出密码验证对话框，让用户输入密码
        val builder = MaterialAlertDialogBuilder(context)
        val dialogView = inflater.inflate(R.layout.dialog_password_enter, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.password_input)
        val passwordInputTips = dialogView.findViewById<TextView>(R.id.password_input_tips)
        passwordInputTips.setText(R.string.set_password_tips)
        builder.setTitle(R.string.enter_password)
        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setPositiveButton(
            R.string.ok,
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                val password = passwordInput.getText().toString()
                callback.onEditTextEntered(password)
            })
        builder.setNegativeButton(R.string.exit, exit)
        builder.show()
    }

    private fun openAlipay() {
        val uri =
            "alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/fkx19506bgcutjrzzq9ud44"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    fun showExportDialog(callback: DialogEditTextCallback) {
        val builder = MaterialAlertDialogBuilder(context)
        val dialogView = inflater.inflate(R.layout.dialog_password, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.password_input)
        val saltInput = dialogView.findViewById<EditText>(R.id.salt_input)
        val saltByte = PasswordUtils.generateSalt()
        val salt = Base64.getEncoder().encodeToString(saltByte)
        saltInput.setText(salt)
        saltInput.setEnabled(false)
        builder.setTitle(R.string.export_data)
        builder.setView(dialogView)
        builder.setPositiveButton(
            R.string.export,
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                val exportDataPassword = (saltInput.getText().toString()
                        + Constants.EXPORT_IMPORT_PASS_SPILT
                        + passwordInput.getText().toString())
                callback.onEditTextEntered(exportDataPassword)
            })
        builder.setNegativeButton(R.string.cancel, null)
        builder.setNeutralButton(
            R.string.copy_salt,
            DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int ->
                ClipboardUtils.copyToClipboard(context, "text", salt)
            })
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener(View.OnClickListener { view: View? ->
                ClipboardUtils.copyToClipboard(context, "text", salt)
                Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show()
            })
    }


    fun showAddAccountDialog(accountViewModel: AccountViewModel) {
        val accountEntity = AccountEntity()
        val dialogView = inflater.inflate(R.layout.add_content_view_dialog, null)

        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(dialogView)

        val appNameInputLayout =
            dialogView.findViewById<TextInputLayout>(R.id.add_content_view_dialog_appName)
        val accountIDInputLayout =
            dialogView.findViewById<TextInputLayout>(R.id.add_content_view_dialog_accountID)
        val passwordInputLayout =
            dialogView.findViewById<TextInputLayout>(R.id.add_content_view_dialog_password)
        val more = dialogView.findViewById<ImageView>(R.id.add_content_view_dialog_more)
        val cancel = dialogView.findViewById<Button>(R.id.add_content_view_dialog_cancel)
        val save = dialogView.findViewById<Button>(R.id.add_content_view_dialog_save)
        val dialog = builder.create()

        cancel.setOnClickListener(View.OnClickListener { view: View? -> dialog.cancel() })
        save.setOnClickListener(View.OnClickListener { view: View? ->
            val appName = appNameInputLayout.getEditText()!!.getText().toString()
            val accountID = accountIDInputLayout.getEditText()!!.getText().toString()
            val password = passwordInputLayout.getEditText()!!.getText().toString()
            accountEntity.setAppName(appName)
            accountEntity.setUsername(accountID)
            accountEntity.setPassword(password)
            accountEntity.setCreateTime(Date())
            accountViewModel.insert(accountEntity)
            dialog.cancel()
        })

        more.setOnClickListener(View.OnClickListener { view: View? ->
            val popupMenu = PopupMenu(view!!.getContext(), view)
            popupMenu.getMenuInflater().inflate(R.menu.add_popup_menu, popupMenu.getMenu())
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                if (item!!.getItemId() == R.id.dialog_popup_menu_remark) {
                    showRemarkDialog(view, accountEntity)
                }
                if (item.getItemId() == R.id.dialog_popup_menu_password_generator) {
                    showPasswordGeneratorDialog(DialogEditTextCallback { text: String? ->
                        passwordInputLayout.getEditText()!!
                            .setText(text)
                    })
                }
                false
            })
            popupMenu.show()
        })

        dialog.show()
    }

    private fun showRemarkDialog(view: View, account: AccountEntity) {
        val dialogView = inflater.inflate(R.layout.dialog_create_remark, null)
        val edittext = dialogView.findViewById<TextInputLayout>(R.id.dialog_create_remark_edittext)

        MaterialAlertDialogBuilder(view.getContext())
            .setView(dialogView)
            .setPositiveButton(
                R.string.ok,
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    account.setRemark(edittext.getEditText()!!.getText().toString())
                })
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    fun showAboutDialog() {
        val dialogView = inflater.inflate(R.layout.dialog_about, null)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(dialogView)
            .setPositiveButton(
                android.R.string.ok,
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> dialog!!.dismiss() })
            .show()

        val githubLink = dialogView.findViewById<TextView>(R.id.github_link)
        githubLink.setOnClickListener(View.OnClickListener { v: View? ->
            val url = context.getString(R.string.github_url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(url))
            context.startActivity(intent)
        })

        val buildInfo = dialogView.findViewById<TextView>(R.id.build_info)

        buildInfo.setText(
            context.getString(R.string.build_time) + BuildConfig.BUILD_TIME + "\n" +
                    context.getString(R.string.git_hash) + BuildConfig.GIT_HASH
        )
    }

    /**
     * 显示隐私协议
     *
     * @param okClick     同意回调
     * @param agreeStatus 是否已同意，已同意状态下CheckBox选中不可更改，弹窗禁用
     */
    fun showUserAgreementDialog(okClick: View.OnClickListener, agreeStatus: Boolean) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_user_agreement, null)

        val checkBox = dialogView.findViewById<CheckBox>(R.id.dialog_user_agreement_cb)

        val builder = MaterialAlertDialogBuilder(context)
        val alertDialog = builder.setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .setCancelable(agreeStatus)
            .create()
        alertDialog.show()

        // 获取当前语言
        val language = Locale.getDefault().getLanguage()
        var assetPath = "en/user_rule.txt" // 默认英文版本

        // 根据语言设置选择相应的路径
        if ("zh" == language) {
            assetPath = "zh/user_rule.txt"
        }

        val tvUserAgreement = dialogView.findViewById<TextView>(R.id.tv_user_agreement)
        var text = FileUtils.readAssetTextFile(context, assetPath)

        if (agreeStatus) {
            val date: String = context.getSharedPreferences(
                dev.xuanran.codebook.bean.Constants.PREFS_NAME,
                android.content.Context.MODE_PRIVATE
            )
                .getString(dev.xuanran.codebook.bean.Constants.KEY_USER_RULE_AGREE_DATE, "")!!
            text += context.getString(R.string.agree_date) + date
        }

        tvUserAgreement.setText(text)

        // 延迟设置滚动方法
        tvUserAgreement.post(Runnable { tvUserAgreement.setMovementMethod(ScrollingMovementMethod()) })

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setEnabled(false)

        checkBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(b)
        })

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(View.OnClickListener { view: View? ->
                okClick.onClick(view)
                alertDialog.dismiss() // 关闭弹窗
            })


        if (agreeStatus) {
            checkBox.setChecked(true)
            checkBox.setEnabled(false)
            val button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setText(R.string.you_already_agree)
            button.setEnabled(false)
        }
    }


    fun showReAuthenticationDialog(
        context: Context,
        onReAuthClicked: DialogInterface.OnClickListener?,
        onExitClicked: DialogInterface.OnClickListener?
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.re_auth)
            .setMessage(R.string.re_auth_tips)
            .setCancelable(false)
            .setPositiveButton(R.string.re_auth, onReAuthClicked)
            .setNegativeButton(R.string.exit, onExitClicked)
            .show()
    }

    fun showPasswordGeneratorDialog(callback: DialogEditTextCallback) {
        val builder = BottomSheetDialog(context)
        val dialogView = inflater.inflate(R.layout.dialog_password_generator, null)
        val includeUppercase = dialogView.findViewById<CheckBox>(R.id.include_uppercase)
        val includeLowercase = dialogView.findViewById<CheckBox>(R.id.include_lowercase)
        val includeNumbers = dialogView.findViewById<CheckBox>(R.id.include_numbers)
        val includeSpecialChars = dialogView.findViewById<CheckBox>(R.id.include_special_chars)
        val passwordLengthSeekBar = dialogView.findViewById<SeekBar>(R.id.password_length_seekbar)
        val passwordLengthValue = dialogView.findViewById<TextView>(R.id.password_length_value)
        val generatePasswordButton = dialogView.findViewById<Button>(R.id.generate_password_button)
        passwordLengthSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                passwordLengthValue.setText(progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        generatePasswordButton.setOnClickListener(View.OnClickListener { v: View? ->
            val upper = includeUppercase.isChecked()
            val lower = includeLowercase.isChecked()
            val numbers = includeNumbers.isChecked()
            val special = includeSpecialChars.isChecked()
            val length = passwordLengthSeekBar.getProgress()
            val password = generatePassword(upper, lower, numbers, special, length)
            callback.onEditTextEntered(password)
            builder.dismiss()
        })
        builder.setContentView(dialogView)
        builder.show()
    }

    private fun generatePassword(
        upper: Boolean,
        lower: Boolean,
        numbers: Boolean,
        special: Boolean,
        length: Int
    ): String {
        val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerChars = "abcdefghijklmnopqrstuvwxyz"
        val numberChars = "0123456789"
        val specialChars = "!@#.%^*()-_=+?"

        val passwordChars = StringBuilder()
        if (upper) passwordChars.append(upperChars)
        if (lower) passwordChars.append(lowerChars)
        if (numbers) passwordChars.append(numberChars)
        if (special) passwordChars.append(specialChars)

        val password = StringBuilder(length)
        val random = Random()
        for (i in 0 until length) {
            password.append(passwordChars.get(random.nextInt(passwordChars.length)))
        }
        return password.toString()
    }
}
