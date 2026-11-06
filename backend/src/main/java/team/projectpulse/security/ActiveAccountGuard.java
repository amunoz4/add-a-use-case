package team.projectpulse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.io.IOException;

@Component
public class ActiveAccountGuard extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final AccessDeniedHandler accessDeniedHandler;

    public ActiveAccountGuard(UserRepository userRepository, AccessDeniedHandler accessDeniedHandler) {
        this.userRepository = userRepository;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Integer userId = readUserId(jwt);
            boolean active = userId != null && this.userRepository.findById(userId)
                    .map(PeerEvaluationUser::isEnabled)
                    .orElse(false);
            if (!active) {
                this.accessDeniedHandler.handle(request, response,
                        new AccessDeniedException("The user account is deactivated."));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Integer readUserId(Jwt jwt) {
        Object claim = jwt.getClaim("userId");
        if (claim instanceof Integer value) return value;
        if (claim instanceof Long value) return value.intValue();
        if (claim instanceof String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
