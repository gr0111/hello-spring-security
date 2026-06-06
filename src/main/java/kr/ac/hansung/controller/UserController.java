package kr.ac.hansung.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.dto.PasswordChangeDto;
import kr.ac.hansung.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 비밀번호 변경 폼 표시
    @GetMapping("/user/password")
    public String passwordForm(Model model) {
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        return "user/password"; // templates/user/password.html 파일
    }

    // 비밀번호 변경 처리
    @PostMapping("/user/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra) {

        // 1. 입력값 검증 에러 체크
        if (bindingResult.hasErrors()) {
            return "user/password";
        }

        // 2. 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "새 비밀번호가 일치하지 않습니다");
            return "user/password";
        }

        // 3. 서비스 계층을 통해 비밀번호 변경 시도
        try {
            userService.changePassword(userDetails.getUsername(), dto.getCurrentPassword(), dto.getNewPassword());
            ra.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            // 현재 비밀번호가 틀린 경우 에러 메시지 처리
            bindingResult.rejectValue("currentPassword", "wrong", e.getMessage());
            return "user/password";
        }

        return "redirect:/home";
    }
}