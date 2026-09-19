package com.bananovr.mobile

data class VRSettings(
    var ipdMm: Float = 64f,
    var fovDegrees: Float = 90f,
    var lensDistanceMm: Float = 42f,
    var worldScale: Float = 1f,
    var distortion: Boolean = false
)
