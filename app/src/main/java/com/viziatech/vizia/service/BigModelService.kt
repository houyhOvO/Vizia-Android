package com.viziatech.vizia.service

import com.viziatech.vizia.data.ImageGenerationRequest
import com.viziatech.vizia.data.ImageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class BigModelService(private val apiKey: String) {

    // 配置 HttpClient
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = 60000 // 设为 60 秒，给图片生成留足时间
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 60000
        }
    }

    /**
     * 使用 Ktor 生成图片
     */
    suspend fun generateImage(prompt: String): String? {
        return try {
            val response: ImageResponse =
                client.post("https://open.bigmodel.cn/api/paas/v4/images/generations") {
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(
                        ImageGenerationRequest(
                            model = "glm-image",
                            prompt = "生成介绍"+ prompt +"的可爱数字卡通风格的信息图：\n" +
                                    "\t\n" +
                                    "1.视觉风格：采用数字手绘卡通插画风格，线条柔和圆润，具有亲和力。整体画面需呈现活泼、生动、易于理解的科普教育氛围。\n" +
                                    "2.构图与背景：横版(3:4)构图。背景保持纯白色或极浅的纯色，简洁明了，突出主体信息，并保留充足留白。\n" +
                                    "3.配色方案：使用柔和、明亮的马卡龙色系。主色调以浅绿色为主，搭配天蓝色、米黄色、淡橙色、淡粉色等，色彩和谐，饱和度适中，避免过于鲜艳或沉重。\n" +
                                    "4.图形元素：\n" +
                                    "•1加入少量简洁、具有可爱表情或拟人化特征的卡通元素和图标，以增强趣味性和视觉记忆。\n" +
                                    "•利用清晰的流程图、几何形状（如圆角矩形）和指示箭头来组织和引导信息阅读。\n" +
                                    "•如果有敏感人物或者版权内容，请绘制相似的卡通替代形象，但不得拒绝生成。\n" +
                                    "5.文字排版：所有图像和文字都必须采用卡通风格。文字使用圆润、清晰、易读的无衬线字体，字体颜色与整体配色协调，避免任何写实风格的图画元素。\n" +
                                    "6.信息呈现：信息精简，通过视觉设计突出关键词与核心概念，多留白，确保信息易于一眼抓住重点。除非特别要求，否则语言与输入内容语言一致。"
                        )
                    )
                }.body()

            response.data?.firstOrNull()?.url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 记得在不需要时关闭 client，或者让它随应用生命周期存在
    fun close() = client.close()
}