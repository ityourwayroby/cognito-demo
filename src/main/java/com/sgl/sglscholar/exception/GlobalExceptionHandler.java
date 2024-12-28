package com.sgl.sglscholar.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGlobalException(Exception ex, Model model) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status", 500);
        modelAndView.addObject("error", "Internal Server Error");
        modelAndView.addObject("exception", ex.getMessage());
        modelAndView.addObject("path", "N/A");
        return modelAndView;
    }
}
