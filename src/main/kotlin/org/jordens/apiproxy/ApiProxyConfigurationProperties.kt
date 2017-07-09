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

import com.squareup.okhttp.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.annotation.PostConstruct

@ConfigurationProperties
data class ApiProxyConfigurationProperties(var proxies: List<ProxyConfig> = mutableListOf()) {
    @PostConstruct
    fun postConstruct() {
        for (proxy in proxies) {
            // initialize the `okHttpClient` for each proxy
            proxy.init()
        }
    }
}

data class ProxyConfig(var id: String = "",
                       var uri: String = "",
                       var skipHostnameVerification: Boolean = false,
                       var methods: List<String> = mutableListOf()) {

    companion object {
        val logger = LoggerFactory.getLogger(OkHttpClient::class.java)
    }

    var okHttpClient = OkHttpClient()

    fun init() {
        if (skipHostnameVerification) {
            this.okHttpClient = okHttpClient.setHostnameVerifier({ hostname, _ ->
                logger.warn("Skipping hostname verification on request to $hostname (id: $id)")
                true
            })
        }
    }
}
