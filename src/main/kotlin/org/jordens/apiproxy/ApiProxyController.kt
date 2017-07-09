/*
 * Copyright 2017 Adam Jordens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jordens.apiproxy

import com.fasterxml.jackson.annotation.JsonInclude
import com.squareup.okhttp.Request
import org.jordens.apiproxy.config.ApiProxyConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(value = "/proxies")
class ApiProxyController(val apiProxyConfigurationProperties: ApiProxyConfigurationProperties) {
    @GetMapping(value = "/{proxy}/**")
    fun get(@PathVariable(value = "proxy") proxy: String,
            @RequestParam requestParams: Map<String, String>,
            httpServletRequest: HttpServletRequest): GenericResponse {

        val proxyConfig = apiProxyConfigurationProperties
                .proxies
                .find { it.id.equals(proxy, true) } ?: throw NotFoundException("No proxy config found with id '$proxy'")

        val proxyPath = httpServletRequest
                .getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                .toString()
                .substringAfter("/proxies/$proxy")

        val proxiedUrlBuilder = Request.Builder().url(proxyConfig.uri + proxyPath).build().httpUrl().newBuilder()
        for ((key, value) in requestParams) {
            proxiedUrlBuilder.addQueryParameter(key, value)
        }
        val proxiedUrl = proxiedUrlBuilder.build()

        val response = proxyConfig.okHttpClient.newCall(
                Request.Builder().url(proxiedUrl).build()
        ).execute()

        return GenericResponse.ok(mapOf(
                Pair("proxiedUrl", proxiedUrl.url()),
                Pair("response", response.body().string()),
                Pair("responseContentType", response.header("Content-type"))
        ))
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class GenericResponse(val status: String,
                           val code: Int,
                           val messages: List<String>,
                           val result: Map<String, Any>) {

    companion object Factory {
        fun ok(result: Map<String, Any>) = GenericResponse("ok", 200, emptyList(), result)
        fun ok(result: Pair<String, Any>) = GenericResponse("ok", 200, emptyList(), mapOf(result))
    }
}

class NotFoundException(msg: String? = null, cause: Throwable? = null) : Exception(msg, cause)

@ControllerAdvice
class ExceptionHandlers {
    companion object {
        val logger = LoggerFactory.getLogger(ExceptionHandlers::class.java)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: Throwable, response: HttpServletResponse, request: HttpServletRequest) {
        response.sendError(HttpStatus.NOT_FOUND.value(), ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Throwable, response: HttpServletResponse, request: HttpServletRequest) {
        logger.error("Internal Server Error", ex)
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message)
    }
}
