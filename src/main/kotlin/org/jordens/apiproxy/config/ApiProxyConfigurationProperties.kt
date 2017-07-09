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

package org.jordens.apiproxy.config

import com.squareup.okhttp.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.File
import javax.annotation.PostConstruct
import javax.net.ssl.KeyManagerFactory
import java.security.KeyStore
import javax.net.ssl.SSLContext

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
                       var keyStore: String = "",
                       var keyStorePassword: String = "",
                       var keyStorePasswordFile: String = "",
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

        if (!keyStore.isNullOrEmpty()) {
            val keyStorePassword = if (!keyStorePassword.isNullOrEmpty()) {
                keyStorePassword
            } else if (!keyStorePasswordFile.isNullOrEmpty()) {
                File(keyStorePasswordFile).readText()
            } else {
                throw IllegalStateException("No `keystorePassword` or `keyStorePasswordFile` specified (id: $id)")
            }

            val jksKeyStore = KeyStore.getInstance("JKS")

            File(this.keyStore).inputStream().use {
                jksKeyStore.load(it, keyStorePassword.toCharArray())
            }

            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(jksKeyStore, keyStorePassword.toCharArray());

            val keyManagers = kmf.keyManagers
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, null, null);

            this.okHttpClient = okHttpClient.setSslSocketFactory(sslContext.socketFactory)
        }
    }
}
