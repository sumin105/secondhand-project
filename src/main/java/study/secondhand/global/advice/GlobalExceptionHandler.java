package study.secondhand.global.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.secondhand.global.exception.DuplicateNicknameException;
import study.secondhand.global.exception.ErrorResponse;
import study.secondhand.global.exception.ProductNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllException(Exception ex) {
        System.out.println("[글로벌 예외] " + ex.getClass().getSimpleName() + " : " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<String> handleDuplicateNicknameException(DuplicateNicknameException ex) {
        System.out.println("[글로벌 예외] DuplicateNicknameException : " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public String handleNotFound(Model model) {
        model.addAttribute("message", "존재하지 않는 상품입니다.");
        return "error/product-not-found";
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/";
    }
}
