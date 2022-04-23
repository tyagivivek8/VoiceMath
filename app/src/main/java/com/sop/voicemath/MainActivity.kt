package com.sop.voicemath


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val rQSREC = 102                    //request code
    private var number_of_vars = 4              //number of max bool vars
    private var array = BooleanArray(number_of_vars)    // array to store bool values with mapping A as index 0
    private var rr = true   //Result bool variable
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rQSREC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val r = result!![0].lowercase(Locale.getDefault()) //Result from voice to text

            val set = Regex("set .")    //set variable
            val reset = Regex("reset .") //reset variable regex
            val or = Regex(". \\+ .") // or operation regex with a + b
            val orResEnd = Regex("result \\+ .") //result + a regex
            val orResStart = Regex(". \\+ result") // a + result regex
            val xor = Regex(". xor .") //xor regex
            val and = Regex(". and .") // and regex
            if (r.matches(set)) {
                val out = set.find(r)!!.value[4]    //variable alphabet to be set
                if (out > 'a' + number_of_vars) // should not exceed maximum vars
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                else {
                    array[out.code - 97] = true // set value in array
                }
                display(array, rr.toString()) // display on textview


            } else if (r.matches(reset)) {
                val out = reset.find(r)!!.value[6]
                if (out > 'a' + number_of_vars)
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                else {
                    array[out.code - 97] = false
                }
                display(array, rr.toString())


            } else if (r.matches(orResStart)) {
                val a1 = orResStart.find(r)!!.value
                if (a1[9] > 'a' + number_of_vars)
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                rr = rr.or(array[a1[9].code - 97])
                display(array, rr.toString(), r)


            } else if (r.matches(orResEnd)) {
                val a1 = orResEnd.find(r)!!.value

                if (a1[0] > 'a' + number_of_vars)
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                rr = rr.or(array[a1[0].code - 97])
                display(array, rr.toString(), r)
            } else if (r.matches(or)) {
                val a1 = or.find(r)!!.value

                if (a1[0] > 'a' + number_of_vars || a1[4] > 'a' + number_of_vars)
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                else {
                    rr = (array[a1[0].code - 97].or(array[a1[4].code - 97]))
                }
                display(array, rr.toString(), r)
            } else if (r.matches(and)) {
                val a1 = and.find(r)!!.value

                if (a1[0] > 'a' + number_of_vars || a1[6] > 'a' + number_of_vars)
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                else {
                    rr = (array[a1[0].code - 97].and(array[a1[6].code - 97]))
                }
                display(array, rr.toString(), r)
            } else if (r.matches(xor)) {
                val a1 = xor.find(r)!!.value
                if (a1[0] > 'a' + number_of_vars || a1[6] > 'a' + number_of_vars)
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                else {
                    rr = (array[a1[0].code - 97].xor(array[a1[6].code - 97]))
                }
                display(array, rr.toString(), r)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val a = arrayOf(Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, a, 1)
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
        val setBtn = findViewById<Button>(R.id.button2)

        display(array, rr.toString())

        setBtn.setOnClickListener {
            val numvar = findViewById<EditText>(R.id.textView)
            number_of_vars = numvar.text!!.toString().toInt()
            array = BooleanArray(number_of_vars)
            display(array, rr.toString())
        }

        val imageButton = findViewById<ImageButton>(R.id.imageButton)
        imageButton.setOnClickListener {
            askSpeechInput()
        }
    }


    private fun askSpeechInput() {

        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak")
        startActivityForResult(i, rQSREC)


    }

    private fun display(arr: BooleanArray, res: String = "", exp: String = "") {
        var op = ""
        var chr = 'A'
        for (i in 0 until number_of_vars) {
            op = op + chr + " : " + arr[i].toString()
            op += "\n"
            chr++
        }
        op += "Result: $exp = $res"
        val et = findViewById<TextView>(R.id.edittext)
        et.text = op
    }

}



