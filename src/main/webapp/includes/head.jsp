<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>

<%
    String safePageTitle = "NextExamLK";

    try {
        safePageTitle = pageTitle != null && !pageTitle.trim().isEmpty()
                ? pageTitle.trim()
                : "NextExamLK";
    } catch (Exception e) {
        safePageTitle = "NextExamLK";
    }

    String appName = "NextExamLK";
    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title><%= FileUtil.h(safePageTitle) %> | <%= appName %></title>

    <!-- Basic Meta -->
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- App Meta -->
    <meta name="theme-color" content="#1E3A8A">
    <meta name="description" content="NextExamLK - Secure online examination, student assessment, and result management platform.">
    <meta name="keywords" content="NextExamLK, online examination, result management, student portal, exam system">
    <meta name="author" content="NextExamLK">
    <meta name="robots" content="noindex, nofollow">

    <!-- Cache Control for Protected Pages -->
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate, private">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">

    <!-- Browser UI -->
    <meta name="color-scheme" content="light">
    <meta name="format-detection" content="telephone=no">

    <!-- Google Font: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap"
          rel="stylesheet">

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">

    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
          rel="stylesheet">

    <!-- Main Custom CSS -->
    <link href="<%= contextPath %>/css/style.css?v=professional-final-2" rel="stylesheet">
</head>
<body>