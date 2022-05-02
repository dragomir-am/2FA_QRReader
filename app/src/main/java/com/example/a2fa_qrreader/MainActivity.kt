package com.example.a2fa_qrreader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.google.zxing.integration.android.IntentIntegrator
import java.util.*
import android.util.Log
import android.widget.EditText

import java.lang.NullPointerException
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    lateinit var buttonScan: Button
    lateinit var textMessage: TextView
    lateinit var passwordInput: EditText
    lateinit var buttonValidate: Button
    lateinit var pin_result_string: String
    lateinit var pass_result_string: String
    lateinit var decode_result_string: String
    lateinit var decoded_result_base64: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textMessage = findViewById(R.id.textMessage)
        buttonScan = findViewById(R.id.buttonScan)
        passwordInput = findViewById(R.id.passwordMessage)
        buttonValidate = findViewById(R.id.buttonValidate)
        buttonScan.setOnClickListener {
            val scanner = IntentIntegrator(this)
            scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            scanner.setBeepEnabled(false)
            scanner.setOrientationLocked(true)
            scanner.initiateScan()

        }

    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents != null) {

                    passwordInput.visibility = View.VISIBLE
                    buttonScan.visibility = View.INVISIBLE

                    buttonValidate.visibility = View.VISIBLE

                    buttonValidate.setOnClickListener {

                        if (passwordInput.text.isNotEmpty()) {
                            val matchingPasswords = CheckPasswords(result.contents)

                            if (matchingPasswords != "") {

                                passwordInput.visibility = View.INVISIBLE
                                buttonValidate.visibility = View.INVISIBLE

                                textMessage.text = matchingPasswords

                            } else {

                                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_LONG)
                                    .show()
                                exitProcess(0)
                            }
                        } else {
                            Toast.makeText(this, "Please input password!", Toast.LENGTH_LONG).show()
                        }

                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        val byteIterator = chunkedSequence(2)
            .map { it.toInt(16).toByte() }
            .iterator()

        return ByteArray(length / 2) { byteIterator.next() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun CheckPasswords(result: String): String {
        decoded_result_base64 = result.decodeHex().decodeToString()

        decode_result_string = String(Base64.getDecoder().decode(decoded_result_base64))

        pin_result_string = decode_result_string.drop(passwordInput.length())

        pass_result_string = decode_result_string.dropLast(pin_result_string.length)

        if (passwordInput.text.toString() == pass_result_string) {
            println("QR:" + pass_result_string)
            println("User: " + passwordInput.text.toString())
            return pin_result_string
        } else {
            println("QR:" + pass_result_string)
            println("User: " + passwordInput.text.toString())
            return ""
        }


    }

}