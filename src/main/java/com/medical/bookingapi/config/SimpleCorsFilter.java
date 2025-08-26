package com.medical.bookingapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // run before Spring Security
public class SimpleCorsFilter extends OncePerRequestFilter {

  // allow your frontend + local dev. Adjust if you change domains.
  private static final Pattern ALLOWED =
      Pattern.compile("^https://[a-z0-9-]+-xenios-[a-z0-9]+\\.koyeb\\.app$");

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String origin = req.getHeader("Origin");
    boolean ok =
        origin != null && (ALLOWED.matcher(origin).matches() || "http://localhost:5173".equals(origin));

    if (ok) {
      res.setHeader("Access-Control-Allow-Origin", origin);
      res.setHeader("Vary", "Origin"); // caches behave correctly
      res.setHeader("Access-Control-Allow-Credentials", "true");
      res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
      res.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type,Accept,Origin,X-Requested-With");
      res.setHeader("Access-Control-Expose-Headers", "Authorization,Content-Type");
      res.setHeader("Access-Control-Max-Age", "3600");
    }

    // short-circuit preflight
    if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
      res.setStatus(HttpServletResponse.SC_OK);
      return;
    }

    chain.doFilter(req, res);
  }
}
