package com.demo.springkeycloak

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {

    @GetMapping("/")
    fun helloWorld(): ResponseEntity<String> {
        return ResponseEntity("Hello World", HttpStatus.OK)
    }

    @GetMapping("/ping")
    fun pingPong(): ResponseEntity<String> {
        return ResponseEntity("pong", HttpStatus.OK)
    }
}
