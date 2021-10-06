package com.demo;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

@WebServlet("/getUserPrincipal")
public class OutputUserPrincipalServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        outputUserPrincipal(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        outputUserPrincipal(request, response);
    }
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        outputUserPrincipal(request, response);
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        outputUserPrincipal(request, response);
    }
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        outputUserPrincipal(request, response);
    }

    private void outputUserPrincipal(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // キャッシュ無効化
        response.setHeader("progma","no-cache");
        response.setHeader("Cache-Control","no-cache");

        // CORSアクセスの許可（http://localhostからのGET、POST、PUT、DELETEメソッドを許可）
        response.setHeader("Access-Control-Allow-Origin","http://localhost");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");

        // HTTPリクエストから認証済ユーザーを取得
        Principal userPrincipal = request.getUserPrincipal();

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write("{");
        out.printf("  \"method\" : \"%s\",", request.getMethod());
        out.printf("  \"userId\" : \"%s\"", (userPrincipal != null ? userPrincipal.getName() : ""));
        out.write("}");
    }
}