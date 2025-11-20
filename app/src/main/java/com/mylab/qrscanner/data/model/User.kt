package com.mylab.qrscanner.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var uid: String = "",
    var email: String = "",
    var nama: String = "",
    var role: String = "mahasiswa", // "mahasiswa" or "petugas"
    var photoUrl: String? = null,
    var createdAt: String? = null
) {
    constructor() : this(
        uid = "",
        email = "",
        nama = "",
        role = "mahasiswa",
        photoUrl = null,
        createdAt = null
    )
    
    fun isPetugas(): Boolean = role == "petugas"
    fun isMahasiswa(): Boolean = role == "mahasiswa"
}

