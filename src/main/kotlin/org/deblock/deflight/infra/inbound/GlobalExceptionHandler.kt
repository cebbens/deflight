package org.deblock.deflight.infra.inbound

import org.deblock.deflight.domain.exceptions.InvalidDatePeriodException
import org.deblock.deflight.infra.inbound.models.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(exception: MethodArgumentNotValidException): ErrorResponse =
        ErrorResponse(errors = exception.fieldErrors.map { ErrorResponse.Error(source = it.field, message = it.defaultMessage) })

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handle(exception: HttpMessageNotReadableException): ErrorResponse =
        ErrorResponse(errors = listOf(ErrorResponse.Error( message = exception.localizedMessage)))

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidDatePeriodException::class)
    fun handle(exception: InvalidDatePeriodException): ErrorResponse =
        ErrorResponse(errors = listOf(ErrorResponse.Error( message = exception.localizedMessage)))
}
