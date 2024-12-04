package com.paba.latihan

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.Date
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private var _nama: MutableList<String> = emptyList<String>().toMutableList()
    private var _tanggal: MutableList<Date> = emptyList<Date>().toMutableList()
    private var _kategori: MutableList<String> = emptyList<String>().toMutableList()
    private var _deskripsi: MutableList<String> = emptyList<String>().toMutableList()
    private var _status: MutableList<String> = emptyList<String>().toMutableList()

    lateinit var sp: SharedPreferences
    private lateinit var _rvTask: RecyclerView
    private var arTask = arrayListOf<task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var _addButton = findViewById<ImageButton>(R.id.addBtn)
        val intentData = intent.getParcelableExtra<task>("entryData", task::class.java)

        if (intentData != null){
            _nama.add(intentData.nama)
            _tanggal.add(intentData.tanggal)
            _kategori.add(intentData.kategori)
            _deskripsi.add(intentData.deskripsi)
            _status.add(intentData.status)

            Log.d("MainActivity", "Nama: ${intentData.nama}")
            Log.d("MainActivity", "Tanggal: ${intentData.tanggal}")
            Log.d("MainActivity", "Kategori: ${intentData.kategori}")
            Log.d("MainActivity", "Deskripsi: ${intentData.deskripsi}")
            Log.d("MainActivity", "Status: ${intentData.status}")

        }

        _addButton.setOnClickListener {
            val intent = Intent(this@MainActivity, pageTambah::class.java)
            startActivity(intent)
        }

        _rvTask = findViewById<RecyclerView>(R.id.rvTasks)
        sp = getSharedPreferences("dataSP", MODE_PRIVATE)
        val gson = Gson()
        val isiSP = sp.getString("spTask", null)
        val type = object : TypeToken<ArrayList<task>>() {}.type
        if(isiSP != null){
            arTask = gson.fromJson(isiSP, type)
        }

        if (arTask.size != 0){
            arTask.forEach{
                _nama.add(it.nama)
                _tanggal.add(it.tanggal)
                _kategori.add(it.kategori)
                _deskripsi.add(it.deskripsi)
                _status.add(it.status)
            }
            arTask.clear()
        }

        tambahData()
        tampilkanData()

    }

    fun siapkanData(){
        _nama = mutableListOf("Name 1", "Name 2", "Name 3")
        _tanggal = mutableListOf(
            Date(2024 - 1900, 0, 1),
            Date(2024 - 1900, 1, 14),
            Date(2024 - 1900, 2, 21)
        )
        _kategori = mutableListOf("Kat 1", "Kat 2", "Kat 3")
        _deskripsi = mutableListOf("Des 1", "Des 2", "Des 3")
        _status = mutableListOf("Kerjakan", "Kerjakan", "Kerjakan")
    }

    fun tambahData(){
        val gson = Gson()
        val editor = sp.edit()
        arTask.clear()
        var intentEdit = intent.getParcelableExtra<task>("editData", task::class.java)

        for (position: Int in _nama.indices){
            var data = task(
                _nama[position],
                _tanggal[position],
                _kategori[position],
                _deskripsi[position],
                _status[position]
            )


            if (intentEdit != null) {
                var intentPos = intent.getIntExtra("pos", -1)
                Log.d("MainActivity", "pos: ${intentPos}")
                Log.d("MainActivity", "position: ${position}")
                if (intentPos != -1) {
                    if (intentPos == position){
                        data = task(
                            intentEdit.nama,
                            intentEdit.tanggal,
                            intentEdit.kategori,
                            intentEdit.deskripsi,
                            intentEdit.status
                        )
                    }
                }
                intent.removeExtra("editData")
                intent.removeExtra("pos")
            }
            arTask.add(data)
        }

        val json = gson.toJson(arTask)
        editor.putString("spTask", json)
        editor.apply()
    }

    fun tampilkanData(){
        _rvTask.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        val adapterTask = adapterRecView(arTask)
        _rvTask.adapter = adapterTask

        adapterTask.setOnItemClickCallback(object: adapterRecView.OnItemClickCallback{
            override fun editData(pos: Int) {
                val intent = Intent(this@MainActivity, pageEdit::class.java)
                intent.putExtra("editData", task(
                    _nama[pos],
                    _tanggal[pos],
                    _kategori[pos],
                    _deskripsi[pos],
                    _status[pos]
                )
                )

                intent.putExtra("pos", pos)

                startActivity(intent)
            }

            override fun dataProgress(pos: Int) {
                if (_status.get(pos) == "Kerjakan") {
                    _status.set(pos, "Selesaikan")
                } else if (_status.get(pos) == "Selesaikan") {
                    _status.set(pos, "Done")
                }

                tambahData()
                tampilkanData()
            }

            override fun delData(pos: Int) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("HAPUS DATA")
                    .setMessage("Apakah Benar Data " + _nama[pos]+" akan dihapus ?")
                    .setPositiveButton(
                        "HAPUS",
                        DialogInterface.OnClickListener { dialog, which ->
                            _nama.removeAt(pos)
                            _tanggal.removeAt(pos)
                            _kategori.removeAt(pos)
                            _deskripsi.removeAt(pos)
                            _status.removeAt(pos)
                            tambahData()
                            tampilkanData()
                        }
                    )
                    .setNegativeButton(
                        "BATAL",
                        DialogInterface.OnClickListener { dialog, which ->
                            Toast.makeText(
                                this@MainActivity,
                                "Data Batal Dihapus",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    ).show()
            }
        })
    }
}