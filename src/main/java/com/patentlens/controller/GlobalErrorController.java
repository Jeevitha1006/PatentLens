package com.patentlens.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        model.addAttribute("statusCode", status != null ? status.toString() : "500");
        model.addAttribute("errorMessage", message != null && !message.toString().isBlank()
                ? message.toString()
                : "An unexpected error occurred while processing your patent request.");

        if (exception != null) {
            model.addAttribute("exceptionDetails", exception.toString());
        }

        return "error";
    }
}
