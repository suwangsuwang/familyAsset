package org.swan.familyasset;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.swan.familyasset.Utils.JwtUtil;


@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer")) {
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }

        try {
            token = token.substring(7);
            Long userId = JwtUtil.parseToken(token);
            request.setAttribute("userId", userId);
            UserContext.setUserId(userId);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.getWriter().write("token 无效");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserContext.clear();
    }
}
