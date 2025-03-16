package app.web;


import app.exception.NotificationServiceFeignCallException;
import app.exception.UsernameAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ExceptionAdvice {

    // DEMO
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    @ExceptionHandler(UsernameAlreadyExistException.class)
//    public ModelAndView handleUsernameAlreadyExist() {
//
//        ModelAndView modelAndView = new ModelAndView();
//
//        modelAndView.setViewName("not-found");
//
//        return modelAndView;
//    }

//    @ExceptionHandler(RuntimeException.class)
//    public ModelAndView handleRuntimeException() {
//
//        ModelAndView modelAndView = new ModelAndView();
//
//        modelAndView.setViewName("internal-server-error");
//
//        return modelAndView;
//    }


    // 1. (First) POST HTTP Request -> /register -> redirect:/register
    // 2. (Second) GET HTTP Request -> /register -> register.html view

    // ВАЖНО: При redirect не връщаме @ResponseStatus(...)!!! автоматично се връща 302 Found
    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExist(RedirectAttributes redirectAttributes,
                                             UsernameAlreadyExistException exception, HttpServletRequest request) {

        // Option 1
        String username = request.getParameter("username");
        String errorMessage = "%s is already in use!".formatted(username);
//        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", errorMessage);


        // Option 2
        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", message);

        return "redirect:/register";
    }

    @ExceptionHandler(NotificationServiceFeignCallException.class)
    public String handleNotificationFeignCallException(RedirectAttributes redirectAttributes,
                                                       NotificationServiceFeignCallException exception) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("clearHistoryErrorMessage", message);

        return "redirect:/notifications";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class, // Когато се опитва да достъпи endpoint, до който не му е позволено/нямам достъп
            NoResourceFoundException.class, // Когато се опитва да достъпи невалиден endpoint
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions() {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}