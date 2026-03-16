package com.viziatech.vizia.data

import kotlinx.serialization.Serializable


@Serializable
data class ImageGenerationRequest(
    val model: String,
    val prompt: String,
    val size: String = "1280x1280"
)

@Serializable
data class ImageResponse(
    val data: List<ImageData>? = null,
    val error: ErrorDetail? = null
)

@Serializable
data class ImageData(val url: String)

@Serializable
data class ErrorDetail(val message: String, val code: String)