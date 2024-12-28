package com.sgl.sglscholar.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        model.addAttribute("status", status != null ? status : "N/A");
        model.addAttribute("error", "An error occurred");
        model.addAttribute("exception", exception != null ? exception.toString() : "N/A");
        model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

        return "error"; // This will load the `error.html` template
    }
}
