package com.ims.expo.security;

import com.ims.expo.entity.Role;
import com.ims.expo.entity.User;
import com.ims.expo.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Extract data from GitHub
        String githubId = oAuth2User.getAttribute("id").toString();
        String username = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        // Check if student exists
        Optional<User> userOptional = userRepository.findByGithubId(githubId);
        User user;

        if (userOptional.isEmpty()) {
            // NEW STUDENT REGISTRATION
            user = new User();
            user.setGithubId(githubId);
            user.setUsername(username);
            user.setEmail(email != null ? email : githubId + "@no-email.github.com");
            user.setPhotoUrl(avatarUrl);
            user.setRole(Role.STUDENT);
            
            // --- THE IMS GATEKEEPER LOGIC ---
            if (email != null && email.endsWith("@imsnoida.com")) {
                user.setApproved(true);  // Auto-approve official emails
            } else {
                user.setApproved(false); // Wait for your manual admin approval
            }
            
            user.setProfileComplete(false); // Force them to the onboarding form
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        // Redirect back to the Angular frontend with the User ID
        // (In a production app, we would generate a JWT token here instead of sending the raw ID)
        String targetUrl = frontendUrl + "/oauth2/redirect?userId=" + user.getId() + 
                           "&approved=" + user.isApproved() + 
                           "&profileComplete=" + user.isProfileComplete();
                           
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}