package su.grinev.gallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import su.grinev.gallery.exception.ResourceNotFoundException;
import su.grinev.gallery.model.RestErrorInfo;

import javax.servlet.http.HttpServletResponse;
import java.util.zip.DataFormatException;

@ControllerAdvice
public abstract class AbstractRestHandler implements ApplicationEventPublisherAware {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected ApplicationEventPublisher eventPublisher;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataFormatException.class)
    public @ResponseBody RestErrorInfo handleDataStoreException(DataFormatException ex, WebRequest request, HttpServletResponse response) {
        return new RestErrorInfo(ex, "Something went wrong!");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public @ResponseBody RestErrorInfo handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request, HttpServletResponse response) {
        log.info("ResourceNotFoundException handler:" + ex.getMessage());
        return new RestErrorInfo(ex, "Requested resource is not available!");
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public static <T> T checkResourceFound(final T resource) {
        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found!");
        }
        return resource;
    }

}