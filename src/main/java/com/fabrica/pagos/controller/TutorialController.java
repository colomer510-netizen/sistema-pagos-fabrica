package com.fabrica.pagos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tutorial")
public class TutorialController {

    @GetMapping
    public String tutorial() {
        return "tutorial";
    }
}
