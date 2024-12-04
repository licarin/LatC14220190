package com.paba.latihan

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
data class task (
    var nama : String,
    var tanggal : Date,
    var kategori : String,
    var deskripsi : String,
    var status : String,
    ) : Parcelable