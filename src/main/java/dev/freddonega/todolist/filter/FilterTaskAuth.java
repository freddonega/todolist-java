package dev.freddonega.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import javax.swing.text.Style;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.freddonega.todolist.user.IUserRespository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
    @Autowired
    private IUserRespository userRespository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                var servletPath = request.getServletPath();
                if(servletPath.contains("/tasks")) {
                    var authorization = request.getHeader("Authorization");

                    if(authorization == null) {
                        response.sendError(401, "Unauthorized");
                        return;
                    }

                    var token = authorization.substring("Basic".length()).trim();

                    var authDecoded = new String(Base64.getDecoder().decode(token));

                    String[] authParts = authDecoded.split(":");
                    String username = authParts[0];
                    String password = authParts[1];

                    var user = this.userRespository.findByUsername(username);
                    if(user == null) {
                        response.sendError(401, "Unauthorized");
                        return;
                    }

                    var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                    if(!passwordVerify.verified) {
                        response.sendError(401, "Unauthorized");
                        return;
                    }

                    request.setAttribute("userId", user.getId());
        
                }

                
                filterChain.doFilter(request, response);
    }
}
