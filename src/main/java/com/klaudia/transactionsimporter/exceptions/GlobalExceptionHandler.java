package com.klaudia.transactionsimporter.exceptions;

import com.klaudia.transactionsimporter.exceptions.imports.DuplicatedImportJobException;
import com.klaudia.transactionsimporter.exceptions.imports.EmptyFileException;
import com.klaudia.transactionsimporter.exceptions.imports.IllegalFileFormatException;
import com.klaudia.transactionsimporter.exceptions.imports.ImportJobNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.CategoryNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.IbanNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.MonthStatisticsNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatedImportJobException.class)
    public ResponseEntity<String> handleDuplicatedImportJobException(DuplicatedImportJobException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler({
            ImportJobNotFoundException.class,
            CategoryNotFoundException.class,
            IbanNotFoundException.class,
            MonthStatisticsNotFoundException.class})
    public ResponseEntity<String> handleNotFoundException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalFileFormatException.class)
    public ResponseEntity<String> handleIllegalFileFormatException(IllegalFileFormatException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(e.getMessage());
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<String> handleEmptyFileException(EmptyFileException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
