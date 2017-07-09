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

import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/error")
class ApiProxyErrorController(val errorAttributes: ErrorAttributes) : ErrorController {

    @RequestMapping(produces = arrayOf("application/json"))
    fun error(httpServletRequest: HttpServletRequest): Map<String, Any> = getErrorAttributes(httpServletRequest)

    private fun getErrorAttributes(httpServletRequest: HttpServletRequest): MutableMap<String, Any> {
        // always include stack trace
        return errorAttributes.getErrorAttributes(ServletRequestAttributes(httpServletRequest), true)
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}
