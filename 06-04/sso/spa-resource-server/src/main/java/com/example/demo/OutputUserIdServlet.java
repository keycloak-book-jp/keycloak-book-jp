package com.example.demo;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

@WebServlet("/user")
public class OutputUserIdServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // キャッシュの無効化
        response.setHeader("pragma","no-cache");
        response.setHeader("Cache-Control","no-cache");

        // 認証済みユーザーを取得
        Principal userPrincipal = request.getUserPrincipal();

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write("{");
        out.printf("  \"method\" : \"%s\",", request.getMethod());
        out.printf("  \"userId\" : \"%s\"", (userPrincipal != null ? userPrincipal.getName() : ""));
        out.write("}");
    }
}