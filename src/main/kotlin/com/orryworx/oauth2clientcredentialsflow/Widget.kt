package com.orryworx.oauth2clientcredentialsflow

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/widget")
class WidgetController {

    @PostMapping
    fun echo(@RequestBody widget: Widget): ResponseEntity<Widget> {
        logger.info { widget }
        return ResponseEntity.ok(widget)
    }
}

data class Widget(val id: Int, val name: String)