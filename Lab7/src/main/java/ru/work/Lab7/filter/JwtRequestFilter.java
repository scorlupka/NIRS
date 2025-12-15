package ru.work.Lab7.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.work.Lab7.security.JwtUtil;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws SecurityException, IOException{

        String token = null;
        String id = null;

        UsernamePasswordAuthenticationToken authentication = null;
        try{
            final String authorizationHeader = request.getHeader("Authorization");
            if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                token = authorizationHeader.substring(7);
            }
            if (token != null){
                try{
                    id = jwtUtil.extractId(token);
                } catch (ExpiredJwtException e){
                    //TODO
                }
                if(id != null && SecurityContextHolder.getContext().getAuthentication() == null){
                    String role = jwtUtil.extractAllClaims(token).get("role", String.class);


                    var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                    authentication = new UsernamePasswordAuthenticationToken(id, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e){
            //TODO
        }
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка в цепочке фильтров", e);
        }
    }
}
