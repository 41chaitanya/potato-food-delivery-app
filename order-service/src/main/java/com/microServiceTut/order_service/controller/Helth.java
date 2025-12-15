package com.microServiceTut.order_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ok")
public class Helth {
    @GetMapping
    public String ok(){
        return "ok";
    }
}
