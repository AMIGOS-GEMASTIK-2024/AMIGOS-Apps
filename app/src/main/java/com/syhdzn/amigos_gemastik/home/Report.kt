package com.syhdzn.amigos_gemastik.home

data class Report(
    val reportNumber: String = "",
    val reporterName: String = "",
    val location: String = "", // Original coordinates
    var locationName: String = "", // Converted address
    val imageUrl: String = ""
)


