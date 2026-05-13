<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>

<%
    String error = request.getParameter("error");
    String logout = request.getParameter("logout");

    String alertType = "";
    String alertIcon = "";
    String alertTitle = "";
    String alertMessage = "";

    if ("invalid".equalsIgnoreCase(error)) {
        alertType = "alert-danger";
        alertIcon = "bi-x-circle-fill";
        alertTitle = "Login Failed";
        alertMessage = "Invalid username, password, role, or inactive account. Please check your credentials.";
    } else if ("missing".equalsIgnoreCase(error)) {
        alertType = "alert-warning";
        alertIcon = "bi-exclamation-triangle-fill";
        alertTitle = "Missing Details";
        alertMessage = "Please enter username, password, and access role.";
    } else if ("invalidRole".equalsIgnoreCase(error)) {
        alertType = "alert-warning";
        alertIcon = "bi-person-badge-fill";
        alertTitle = "Invalid Role";
        alertMessage = "Please select a valid account role before signing in.";
    } else if ("inactive".equalsIgnoreCase(error)) {
        alertType = "alert-danger";
        alertIcon = "bi-person-x-fill";
        alertTitle = "Account Inactive";
        alertMessage = "Your account is not active. Please contact the system administrator.";
    } else if ("sessionExpired".equalsIgnoreCase(error)) {
        alertType = "alert-warning";
        alertIcon = "bi-clock-history";
        alertTitle = "Session Required";
        alertMessage = "Please sign in before accessing the examination platform.";
    } else if ("accessDenied".equalsIgnoreCase(error)) {
        alertType = "alert-danger";
        alertIcon = "bi-shield-lock-fill";
        alertTitle = "Access Denied";
        alertMessage = "You do not have permission to access the requested page.";
    } else if ("success".equalsIgnoreCase(logout)) {
        alertType = "alert-success";
        alertIcon = "bi-check-circle-fill";
        alertTitle = "Logged Out";
        alertMessage = "You have successfully logged out from NextExamLK.";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>NextExamLK | Secure Online Examination Platform</title>

    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#1E3A8A">
    <meta name="description" content="NextExamLK - Secure online examination and result management platform.">
    <meta name="author" content="NextExamLK">
    <meta name="robots" content="noindex, nofollow">

    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate, private">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap"
          rel="stylesheet">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
          rel="stylesheet">

    <link href="<%= request.getContextPath() %>/css/style.css?v=professional-final-2" rel="stylesheet">

    <style>
        :root {
            --home-primary: #2563eb;
            --home-primary-dark: #1e3a8a;
            --home-primary-soft: #eff6ff;
            --home-dark: #0f172a;
            --home-muted: #64748b;
            --home-border: #e2e8f0;
            --home-surface: #ffffff;
            --home-bg: #f8fafc;
            --home-success: #16a34a;
            --home-warning: #d97706;
            --home-danger: #dc2626;
            --home-shadow-sm: 0 14px 36px rgba(15, 23, 42, 0.06);
            --home-shadow-md: 0 22px 54px rgba(15, 23, 42, 0.10);
            --home-shadow-lg: 0 30px 90px rgba(15, 23, 42, 0.16);
        }

        html {
            scroll-behavior: smooth;
        }

        body {
            font-family: "Inter", sans-serif;
            background: var(--home-bg);
            color: var(--home-dark);
            overflow-x: hidden;
        }

        a {
            text-decoration: none;
        }

        .home-page {
            min-height: 100vh;
            background:
                radial-gradient(circle at top left, rgba(37, 99, 235, 0.13), transparent 34%),
                radial-gradient(circle at top right, rgba(14, 165, 233, 0.12), transparent 30%),
                linear-gradient(180deg, #ffffff 0%, #f8fafc 42%, #ffffff 100%);
        }

        /* =========================================================
           NAVBAR
           ========================================================= */

        .home-navbar {
            position: sticky;
            top: 0;
            z-index: 1030;
            background: rgba(255, 255, 255, 0.86);
            backdrop-filter: blur(20px);
            border-bottom: 1px solid rgba(226, 232, 240, 0.95);
        }

        .home-brand {
            display: inline-flex;
            align-items: center;
            gap: 13px;
            color: var(--home-dark);
        }

        .home-brand-icon {
            width: 48px;
            height: 48px;
            border-radius: 18px;
            background:
                radial-gradient(circle at top right, rgba(255, 255, 255, 0.28), transparent 38%),
                linear-gradient(135deg, var(--home-primary-dark), var(--home-primary));
            color: #ffffff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 23px;
            box-shadow: 0 16px 38px rgba(37, 99, 235, 0.24);
        }

        .home-brand-title {
            font-size: 20px;
            font-weight: 950;
            letter-spacing: -0.045em;
            line-height: 1;
        }

        .home-brand-subtitle {
            font-size: 12px;
            font-weight: 750;
            color: var(--home-muted);
            margin-top: 3px;
        }

        .home-nav-link {
            color: #475569;
            font-size: 14px;
            font-weight: 850;
            padding: 10px 13px;
            border-radius: 999px;
            transition: all 0.18s ease;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .home-nav-link:hover {
            color: var(--home-primary);
            background: var(--home-primary-soft);
        }

        .navbar-toggler {
            border-radius: 14px;
            border-color: var(--home-border);
        }

        .navbar-toggler:focus {
            box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
        }

        /* =========================================================
           COMMON
           ========================================================= */

        .home-section {
            padding: 92px 0;
        }

        .section-header {
            max-width: 760px;
            margin: 0 auto 44px;
            text-align: center;
        }

        .section-eyebrow,
        .hero-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 9px 14px;
            border-radius: 999px;
            background: var(--home-primary-soft);
            color: #1d4ed8;
            border: 1px solid #dbeafe;
            font-size: 13px;
            font-weight: 950;
        }

        .section-title {
            font-size: clamp(28px, 3.3vw, 46px);
            font-weight: 950;
            letter-spacing: -0.06em;
            color: var(--home-dark);
            margin: 16px 0 13px;
            line-height: 1.08;
        }

        .section-text {
            color: var(--home-muted);
            line-height: 1.75;
            font-size: 15px;
            margin: 0;
        }

        .text-gradient {
            background: linear-gradient(135deg, var(--home-primary-dark), var(--home-primary), #0ea5e9);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        /* =========================================================
           HERO
           ========================================================= */

        .home-hero {
            padding: 90px 0 78px;
            position: relative;
        }

        .home-hero::before {
            content: "";
            position: absolute;
            width: 460px;
            height: 460px;
            border-radius: 999px;
            background: rgba(37, 99, 235, 0.07);
            left: -190px;
            top: 120px;
            pointer-events: none;
        }

        .hero-title {
            font-size: clamp(40px, 5.4vw, 72px);
            line-height: 1.01;
            font-weight: 950;
            letter-spacing: -0.075em;
            color: var(--home-dark);
            margin: 22px 0;
        }

        .hero-text {
            max-width: 700px;
            color: var(--home-muted);
            font-size: 17px;
            line-height: 1.82;
            margin-bottom: 30px;
        }

        .hero-action-group {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
            margin-bottom: 34px;
        }

        .hero-action-group .btn {
            min-height: 50px;
            border-radius: 16px;
            font-weight: 900;
            padding: 12px 21px;
        }

        .hero-trust-row {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 26px;
        }

        .hero-trust-item {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 13px;
            border-radius: 999px;
            background: #ffffff;
            border: 1px solid var(--home-border);
            color: #475569;
            font-size: 13px;
            font-weight: 850;
            box-shadow: 0 10px 26px rgba(15, 23, 42, 0.04);
        }

        .hero-trust-item i {
            color: var(--home-primary);
        }

        .hero-metrics {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 14px;
            max-width: 640px;
        }

        .hero-metric-card {
            padding: 18px;
            border-radius: 24px;
            background: #ffffff;
            border: 1px solid var(--home-border);
            box-shadow: var(--home-shadow-sm);
            transition: all 0.18s ease;
        }

        .hero-metric-card:hover {
            transform: translateY(-3px);
            box-shadow: var(--home-shadow-md);
        }

        .hero-metric-card strong {
            display: block;
            font-size: 27px;
            font-weight: 950;
            color: var(--home-dark);
            line-height: 1;
        }

        .hero-metric-card small {
            display: block;
            margin-top: 8px;
            color: var(--home-muted);
            font-size: 12px;
            font-weight: 850;
            line-height: 1.45;
        }

        .hero-visual-card {
            background:
                radial-gradient(circle at top right, rgba(56, 189, 248, 0.20), transparent 34%),
                radial-gradient(circle at bottom left, rgba(255, 255, 255, 0.10), transparent 30%),
                linear-gradient(145deg, #0f172a, #1e3a8a 52%, #2563eb);
            border-radius: 36px;
            padding: 30px;
            color: #ffffff;
            box-shadow: 0 34px 92px rgba(30, 58, 138, 0.30);
            position: relative;
            overflow: hidden;
        }

        .hero-visual-card::before {
            content: "";
            position: absolute;
            width: 250px;
            height: 250px;
            border-radius: 999px;
            background: rgba(255, 255, 255, 0.10);
            right: -90px;
            top: -90px;
        }

        .hero-visual-card::after {
            content: "";
            position: absolute;
            width: 170px;
            height: 170px;
            border-radius: 999px;
            background: rgba(14, 165, 233, 0.16);
            left: -70px;
            bottom: -70px;
        }

        .hero-visual-card > * {
            position: relative;
            z-index: 1;
        }

        .visual-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 14px;
            margin-bottom: 26px;
        }

        .visual-icon {
            width: 62px;
            height: 62px;
            border-radius: 24px;
            background: rgba(255, 255, 255, 0.16);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 30px;
        }

        .visual-status {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            border-radius: 999px;
            background: rgba(34, 197, 94, 0.18);
            color: #dcfce7;
            font-size: 12px;
            font-weight: 950;
            white-space: nowrap;
        }

        .visual-status-dot {
            width: 8px;
            height: 8px;
            border-radius: 999px;
            background: #22c55e;
            box-shadow: 0 0 0 5px rgba(34, 197, 94, 0.14);
        }

        .visual-title {
            font-size: 31px;
            font-weight: 950;
            letter-spacing: -0.045em;
            margin-bottom: 10px;
        }

        .visual-text {
            color: rgba(255, 255, 255, 0.78);
            line-height: 1.72;
            margin-bottom: 24px;
        }

        .visual-dashboard {
            background: rgba(255, 255, 255, 0.11);
            border: 1px solid rgba(255, 255, 255, 0.14);
            border-radius: 26px;
            padding: 16px;
            margin-bottom: 18px;
        }

        .visual-dashboard-top {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 14px;
        }

        .visual-pill {
            padding: 8px 10px;
            border-radius: 999px;
            background: rgba(255, 255, 255, 0.14);
            color: rgba(255, 255, 255, 0.86);
            font-size: 12px;
            font-weight: 900;
        }

        .visual-progress-item {
            margin-bottom: 13px;
        }

        .visual-progress-item:last-child {
            margin-bottom: 0;
        }

        .visual-progress-label {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            font-size: 13px;
            font-weight: 850;
            margin-bottom: 7px;
        }

        .visual-progress-track {
            height: 8px;
            border-radius: 999px;
            background: rgba(255, 255, 255, 0.18);
            overflow: hidden;
        }

        .visual-progress-fill {
            height: 100%;
            border-radius: 999px;
            background: #bfdbfe;
        }

        .visual-flow {
            display: grid;
            gap: 12px;
        }

        .visual-flow-item {
            padding: 14px;
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.11);
            border: 1px solid rgba(255, 255, 255, 0.14);
            display: flex;
            gap: 12px;
            align-items: center;
        }

        .visual-flow-item i {
            width: 38px;
            height: 38px;
            border-radius: 15px;
            background: rgba(255, 255, 255, 0.15);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #bfdbfe;
            flex-shrink: 0;
        }

        .visual-flow-item strong {
            display: block;
            font-size: 14px;
        }

        .visual-flow-item small {
            color: rgba(255, 255, 255, 0.72);
        }

        /* =========================================================
           CARDS
           ========================================================= */

        .feature-card,
        .role-card,
        .workflow-card,
        .module-card {
            height: 100%;
            padding: 25px;
            border-radius: 30px;
            background: #ffffff;
            border: 1px solid var(--home-border);
            box-shadow: var(--home-shadow-sm);
            transition: all 0.18s ease;
        }

        .feature-card:hover,
        .role-card:hover,
        .workflow-card:hover,
        .module-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--home-shadow-md);
            border-color: rgba(37, 99, 235, 0.25);
        }

        .feature-icon,
        .role-icon,
        .workflow-step-icon,
        .module-icon {
            width: 56px;
            height: 56px;
            border-radius: 21px;
            background: var(--home-primary-soft);
            color: var(--home-primary);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            margin-bottom: 18px;
        }

        .feature-card h5,
        .role-card h5,
        .workflow-card h5,
        .module-card h5 {
            font-weight: 950;
            color: var(--home-dark);
            letter-spacing: -0.03em;
            margin-bottom: 10px;
        }

        .feature-card p,
        .role-card p,
        .workflow-card p,
        .module-card p {
            color: var(--home-muted);
            line-height: 1.68;
            margin-bottom: 0;
            font-size: 14px;
        }

        .role-card ul {
            padding-left: 0;
            list-style: none;
            margin: 18px 0 0;
            display: grid;
            gap: 10px;
        }

        .role-card li {
            display: flex;
            gap: 10px;
            color: #475569;
            font-size: 14px;
            font-weight: 700;
        }

        .role-card li i {
            color: var(--home-success);
            flex-shrink: 0;
            margin-top: 2px;
        }

        .role-card.student .role-icon {
            background: #f0fdf4;
            color: var(--home-success);
        }

        .role-card.lecturer .role-icon {
            background: #eff6ff;
            color: var(--home-primary);
        }

        .role-card.admin .role-icon {
            background: #fef2f2;
            color: var(--home-danger);
        }

        .workflow-card {
            position: relative;
        }

        .workflow-number {
            width: 36px;
            height: 36px;
            border-radius: 999px;
            background: var(--home-primary);
            color: #ffffff;
            font-size: 13px;
            font-weight: 950;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 14px;
            box-shadow: 0 10px 24px rgba(37, 99, 235, 0.20);
        }

        .module-strip {
            padding: 26px;
            border-radius: 34px;
            background:
                radial-gradient(circle at top right, rgba(37, 99, 235, 0.09), transparent 32%),
                #ffffff;
            border: 1px solid var(--home-border);
            box-shadow: var(--home-shadow-sm);
        }

        .module-card {
            box-shadow: none;
            border-radius: 24px;
            padding: 20px;
        }

        .module-card .module-icon {
            width: 48px;
            height: 48px;
            border-radius: 18px;
            font-size: 21px;
            margin-bottom: 14px;
        }

        /* =========================================================
           LOGIN SECTION
           ========================================================= */

        .login-section {
            padding: 94px 0;
            background:
                radial-gradient(circle at top right, rgba(37, 99, 235, 0.10), transparent 31%),
                radial-gradient(circle at bottom left, rgba(16, 185, 129, 0.08), transparent 30%),
                #ffffff;
            border-top: 1px solid var(--home-border);
        }

        .login-panel {
            border-radius: 36px;
            background: #ffffff;
            border: 1px solid var(--home-border);
            box-shadow: var(--home-shadow-lg);
            overflow: hidden;
        }

        .login-info-panel {
            height: 100%;
            padding: 36px;
            background:
                radial-gradient(circle at top right, rgba(56, 189, 248, 0.17), transparent 34%),
                radial-gradient(circle at bottom left, rgba(255, 255, 255, 0.09), transparent 32%),
                linear-gradient(145deg, #0f172a, #1e3a8a 60%, #2563eb);
            color: #ffffff;
        }

        .login-info-panel h3 {
            font-size: 33px;
            font-weight: 950;
            letter-spacing: -0.05em;
            margin-bottom: 12px;
            line-height: 1.08;
        }

        .login-info-panel p {
            color: rgba(255, 255, 255, 0.76);
            line-height: 1.72;
        }

        .login-info-list {
            display: grid;
            gap: 14px;
            margin-top: 26px;
        }

        .login-info-item {
            display: flex;
            gap: 12px;
            align-items: flex-start;
            padding: 14px;
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.10);
            border: 1px solid rgba(255, 255, 255, 0.12);
        }

        .login-info-item i {
            width: 40px;
            height: 40px;
            border-radius: 15px;
            background: rgba(255, 255, 255, 0.14);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #bfdbfe;
            flex-shrink: 0;
        }

        .login-form-panel {
            padding: 36px;
        }

        .login-mini-icon {
            width: 58px;
            height: 58px;
            border-radius: 22px;
            background: linear-gradient(135deg, #dbeafe, #cffafe);
            color: var(--home-primary);
            align-items: center;
            justify-content: center;
            font-size: 26px;
            flex-shrink: 0;
        }

        .login-input-group .input-group-text {
            background: #ffffff;
        }

        .login-submit-btn {
            min-height: 50px;
            border-radius: 16px;
            font-weight: 950;
        }

        .home-divider {
            margin: 28px 0 18px;
            display: flex;
            align-items: center;
            gap: 12px;
            color: var(--home-muted);
            font-size: 12px;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }

        .home-divider::before,
        .home-divider::after {
            content: "";
            height: 1px;
            background: var(--home-border);
            flex: 1;
        }

        .demo-role-btn {
            min-height: 44px;
            border-radius: 14px;
            font-weight: 850;
        }

        .login-help {
            padding: 16px;
            border-radius: 22px;
            background: #f8fafc;
            border: 1px solid var(--home-border);
        }

        .login-help-icon {
            width: 42px;
            height: 42px;
            border-radius: 16px;
            background: #dbeafe;
            color: var(--home-primary);
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }

        /* =========================================================
           CTA + FOOTER
           ========================================================= */

        .final-cta {
            padding: 54px 0;
            background: #0f172a;
            color: #ffffff;
        }

        .final-cta-card {
            padding: 34px;
            border-radius: 34px;
            background:
                radial-gradient(circle at top right, rgba(56, 189, 248, 0.18), transparent 34%),
                linear-gradient(135deg, rgba(37, 99, 235, 0.24), rgba(30, 58, 138, 0.12));
            border: 1px solid rgba(255, 255, 255, 0.10);
        }

        .final-cta h2 {
            font-weight: 950;
            letter-spacing: -0.045em;
            margin-bottom: 8px;
        }

        .final-cta p {
            color: rgba(255, 255, 255, 0.72);
            margin-bottom: 0;
        }

        .home-footer {
            padding: 28px 0;
            background: #020617;
            color: rgba(255, 255, 255, 0.70);
        }

        .home-footer strong {
            color: #ffffff;
        }

        /* =========================================================
           RESPONSIVE
           ========================================================= */

        @media (max-width: 991px) {
            .navbar-collapse {
                padding-top: 16px;
            }

            .home-nav-link {
                width: 100%;
                justify-content: flex-start;
            }

            .home-hero {
                padding-top: 62px;
            }

            .hero-visual-card {
                margin-top: 18px;
            }

            .home-section,
            .login-section {
                padding: 66px 0;
            }

            .login-info-panel,
            .login-form-panel {
                padding: 28px;
            }
        }

        @media (max-width: 768px) {
            .hero-metrics {
                grid-template-columns: 1fr;
            }

            .visual-header {
                align-items: flex-start;
                flex-direction: column;
            }

            .home-brand-subtitle {
                display: none;
            }
        }

        @media (max-width: 576px) {
            .home-brand-icon {
                width: 44px;
                height: 44px;
            }

            .hero-title {
                font-size: 35px;
                letter-spacing: -0.055em;
            }

            .hero-text {
                font-size: 15px;
            }

            .hero-action-group .btn {
                width: 100%;
            }

            .home-section,
            .login-section {
                padding: 50px 0;
            }

            .feature-card,
            .role-card,
            .workflow-card,
            .module-card {
                padding: 21px;
                border-radius: 25px;
            }

            .login-panel {
                border-radius: 28px;
            }

            .login-info-panel,
            .login-form-panel {
                padding: 23px;
            }

            .final-cta-card {
                padding: 24px;
                border-radius: 28px;
            }
        }
    </style>
</head>

<body>

<div class="home-page">

    <!-- Navbar -->
    <header class="home-navbar">
        <nav class="navbar navbar-expand-lg">
            <div class="container py-2">
                <a href="#home" class="home-brand">
                    <div class="home-brand-icon">
                        <i class="bi bi-mortarboard-fill"></i>
                    </div>

                    <div>
                        <div class="home-brand-title">NextExamLK</div>
                        <div class="home-brand-subtitle">Secure Examination Platform</div>
                    </div>
                </a>

                <button class="navbar-toggler"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#homeNavbar"
                        aria-controls="homeNavbar"
                        aria-expanded="false"
                        aria-label="Toggle navigation">
                    <i class="bi bi-list"></i>
                </button>

                <div class="collapse navbar-collapse" id="homeNavbar">
                    <div class="ms-lg-auto d-lg-flex align-items-center gap-1">
                        <a href="#home" class="home-nav-link">Home</a>
                        <a href="#features" class="home-nav-link">Features</a>
                        <a href="#portals" class="home-nav-link">Portals</a>
                        <a href="#workflow" class="home-nav-link">Workflow</a>
                        <a href="#modules" class="home-nav-link">Modules</a>
                    </div>

                    <div class="ms-lg-3 mt-3 mt-lg-0">
                        <a href="#login" class="btn btn-primary btn-sm px-3 w-100">
                            <i class="bi bi-box-arrow-in-right me-1"></i>
                            Login
                        </a>
                    </div>
                </div>
            </div>
        </nav>
    </header>

    <main>

        <!-- Hero -->
        <section class="home-hero" id="home">
            <div class="container">
                <div class="row align-items-center g-5">
                    <div class="col-lg-7">
                        <span class="hero-badge">
                            <i class="bi bi-shield-check"></i>
                            Secure Academic Assessment Platform
                        </span>

                        <h1 class="hero-title">
                            A professional platform for <span class="text-gradient">online exams and results.</span>
                        </h1>

                        <p class="hero-text">
                            NextExamLK helps academic teams manage examinations, question banks, student submissions,
                            result workflows, notices, and feedback through a secure role-based web platform.
                        </p>

                        <div class="hero-trust-row">
                            <span class="hero-trust-item">
                                <i class="bi bi-person-badge-fill"></i>
                                Role-based access
                            </span>

                            <span class="hero-trust-item">
                                <i class="bi bi-shield-lock-fill"></i>
                                Protected sessions
                            </span>

                            <span class="hero-trust-item">
                                <i class="bi bi-check2-circle"></i>
                                MCQ + Essay support
                            </span>
                        </div>

                        <div class="hero-action-group">
                            <a href="#login" class="btn btn-primary">
                                <i class="bi bi-box-arrow-in-right me-2"></i>
                                Sign In Now
                            </a>

                            <a href="#features" class="btn btn-outline-primary bg-white">
                                <i class="bi bi-grid-fill me-2"></i>
                                Explore Platform
                            </a>
                        </div>

                        <div class="hero-metrics">
                            <div class="hero-metric-card">
                                <strong>3</strong>
                                <small>Dedicated user workspaces</small>
                            </div>

                            <div class="hero-metric-card">
                                <strong>MCQ</strong>
                                <small>Automatic marking support</small>
                            </div>

                            <div class="hero-metric-card">
                                <strong>Essay</strong>
                                <small>Manual review workflow</small>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-5">
                        <div class="hero-visual-card">
                            <div class="visual-header">
                                <div class="visual-icon">
                                    <i class="bi bi-laptop-fill"></i>
                                </div>

                                <div class="visual-status">
                                    <span class="visual-status-dot"></span>
                                    Platform Online
                                </div>
                            </div>

                            <h2 class="visual-title">Digital Exam Workspace</h2>

                            <p class="visual-text">
                                A unified workspace where students, lecturers, and administrators access the right tools
                                based on their assigned role.
                            </p>

                            <div class="visual-dashboard">
                                <div class="visual-dashboard-top">
                                    <span class="visual-pill">
                                        <i class="bi bi-activity me-1"></i>
                                        Assessment Flow
                                    </span>

                                    <span class="visual-pill">
                                        <i class="bi bi-check2-circle me-1"></i>
                                        Ready
                                    </span>
                                </div>

                                <div class="visual-progress-item">
                                    <div class="visual-progress-label">
                                        <span>Question Bank</span>
                                        <span>90%</span>
                                    </div>
                                    <div class="visual-progress-track">
                                        <div class="visual-progress-fill" style="width: 90%;"></div>
                                    </div>
                                </div>

                                <div class="visual-progress-item">
                                    <div class="visual-progress-label">
                                        <span>Exam Console</span>
                                        <span>91%</span>
                                    </div>
                                    <div class="visual-progress-track">
                                        <div class="visual-progress-fill" style="width: 91%;"></div>
                                    </div>
                                </div>

                                <div class="visual-progress-item">
                                    <div class="visual-progress-label">
                                        <span>Feedback & Notices</span>
                                        <span>90%</span>
                                    </div>
                                    <div class="visual-progress-track">
                                        <div class="visual-progress-fill" style="width: 90%;"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="visual-flow">
                                <div class="visual-flow-item">
                                    <i class="bi bi-person-check-fill"></i>
                                    <div>
                                        <strong>Role-based login</strong>
                                        <small>Admin, Lecturer, and Student access paths.</small>
                                    </div>
                                </div>

                                <div class="visual-flow-item">
                                    <i class="bi bi-patch-question-fill"></i>
                                    <div>
                                        <strong>Question bank</strong>
                                        <small>Manage MCQ and essay questions professionally.</small>
                                    </div>
                                </div>

                                <div class="visual-flow-item">
                                    <i class="bi bi-bar-chart-fill"></i>
                                    <div>
                                        <strong>Result workflow</strong>
                                        <small>Publish student results after review.</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Features -->
        <section class="home-section" id="features">
            <div class="container">
                <div class="section-header">
                    <span class="section-eyebrow">
                        <i class="bi bi-stars"></i>
                        Platform Features
                    </span>

                    <h2 class="section-title">Everything needed for academic exam management.</h2>

                    <p class="section-text">
                        NextExamLK combines examination operations, question preparation, submissions,
                        notices, feedback, and result workflows inside one structured system.
                    </p>
                </div>

                <div class="row g-4">
                    <div class="col-md-6 col-xl-4">
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="bi bi-journal-check"></i>
                            </div>
                            <h5>Exam Management</h5>
                            <p>Create, schedule, activate, complete, and publish exam records through a controlled lifecycle.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-4">
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="bi bi-patch-question-fill"></i>
                            </div>
                            <h5>Question Bank</h5>
                            <p>Prepare MCQ and essay questions with marks, statuses, model answers, and visibility rules.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-4">
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="bi bi-laptop-fill"></i>
                            </div>
                            <h5>Exam Console</h5>
                            <p>Students can attempt exams through a clean interface with timer, navigation, and review flags.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-4">
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="bi bi-check-circle-fill"></i>
                            </div>
                            <h5>Submission Processing</h5>
                            <p>MCQ answers can be auto-marked while essay-based answers can be routed for staff review.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-4">
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="bi bi-megaphone-fill"></i>
                            </div>
                            <h5>Notices</h5>
                            <p>Publish role-targeted announcements for students, lecturers, admins, or all users.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-4">
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="bi bi-chat-dots-fill"></i>
                            </div>
                            <h5>Feedback Center</h5>
                            <p>Students can submit feedback while staff can track review progress and resolve requests.</p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Portals -->
        <section class="home-section bg-white" id="portals">
            <div class="container">
                <div class="section-header">
                    <span class="section-eyebrow">
                        <i class="bi bi-person-badge-fill"></i>
                        Role-Based Portals
                    </span>

                    <h2 class="section-title">Focused workspaces for every user role.</h2>

                    <p class="section-text">
                        Each role receives the right tools, keeping the platform organized, safer, and easier to use.
                    </p>
                </div>

                <div class="row g-4">
                    <div class="col-lg-4">
                        <div class="role-card student">
                            <div class="role-icon">
                                <i class="bi bi-mortarboard-fill"></i>
                            </div>

                            <h5>Student Portal</h5>
                            <p>Students get a focused workspace for exams, results, notices, and feedback.</p>

                            <ul>
                                <li><i class="bi bi-check-circle-fill"></i> Attempt available exams</li>
                                <li><i class="bi bi-check-circle-fill"></i> View own results</li>
                                <li><i class="bi bi-check-circle-fill"></i> Read published notices</li>
                                <li><i class="bi bi-check-circle-fill"></i> Submit and track feedback</li>
                            </ul>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="role-card lecturer">
                            <div class="role-icon">
                                <i class="bi bi-person-video3"></i>
                            </div>

                            <h5>Lecturer Portal</h5>
                            <p>Lecturers can manage assessment content and review student activity.</p>

                            <ul>
                                <li><i class="bi bi-check-circle-fill"></i> Manage exams and questions</li>
                                <li><i class="bi bi-check-circle-fill"></i> Review submissions</li>
                                <li><i class="bi bi-check-circle-fill"></i> Manage notices</li>
                                <li><i class="bi bi-check-circle-fill"></i> Review student feedback</li>
                            </ul>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="role-card admin">
                            <div class="role-icon">
                                <i class="bi bi-shield-lock-fill"></i>
                            </div>

                            <h5>Admin Portal</h5>
                            <p>Admins control system records, users, notices, results, and dashboards.</p>

                            <ul>
                                <li><i class="bi bi-check-circle-fill"></i> Manage users and students</li>
                                <li><i class="bi bi-check-circle-fill"></i> Control exam records</li>
                                <li><i class="bi bi-check-circle-fill"></i> Publish notices</li>
                                <li><i class="bi bi-check-circle-fill"></i> Monitor system data</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Workflow -->
        <section class="home-section" id="workflow">
            <div class="container">
                <div class="section-header">
                    <span class="section-eyebrow">
                        <i class="bi bi-diagram-3-fill"></i>
                        How It Works
                    </span>

                    <h2 class="section-title">A clear workflow from login to results.</h2>

                    <p class="section-text">
                        The platform follows a simple assessment process so each user can complete tasks confidently.
                    </p>
                </div>

                <div class="row g-4">
                    <div class="col-md-6 col-xl-3">
                        <div class="workflow-card">
                            <div class="workflow-number">1</div>
                            <div class="workflow-step-icon">
                                <i class="bi bi-box-arrow-in-right"></i>
                            </div>
                            <h5>Login by Role</h5>
                            <p>Users sign in as Admin, Lecturer, or Student and are routed to the correct workspace.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="workflow-card">
                            <div class="workflow-number">2</div>
                            <div class="workflow-step-icon">
                                <i class="bi bi-journal-plus"></i>
                            </div>
                            <h5>Prepare Exams</h5>
                            <p>Staff create exam records and prepare MCQ or essay questions inside the question bank.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="workflow-card">
                            <div class="workflow-number">3</div>
                            <div class="workflow-step-icon">
                                <i class="bi bi-pencil-square"></i>
                            </div>
                            <h5>Attempt & Submit</h5>
                            <p>Students enter the exam console, answer questions, flag items, and submit securely.</p>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="workflow-card">
                            <div class="workflow-number">4</div>
                            <div class="workflow-step-icon">
                                <i class="bi bi-bar-chart-line-fill"></i>
                            </div>
                            <h5>Review Results</h5>
                            <p>Staff review submissions and students access published results from their portal.</p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Modules -->
        <section class="home-section bg-white" id="modules">
            <div class="container">
                <div class="section-header">
                    <span class="section-eyebrow">
                        <i class="bi bi-grid-1x2-fill"></i>
                        System Modules
                    </span>

                    <h2 class="section-title">Organized modules for a complete examination platform.</h2>

                    <p class="section-text">
                        The system is structured into clear modules so staff and students can work efficiently.
                    </p>
                </div>

                <div class="module-strip">
                    <div class="row g-3">
                        <div class="col-md-6 col-xl-3">
                            <div class="module-card">
                                <div class="module-icon">
                                    <i class="bi bi-people-fill"></i>
                                </div>
                                <h5>User Access</h5>
                                <p>Role-based dashboard routing and session-controlled access.</p>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-3">
                            <div class="module-card">
                                <div class="module-icon">
                                    <i class="bi bi-ui-checks-grid"></i>
                                </div>
                                <h5>Assessment</h5>
                                <p>Exam records, question bank, and student exam console.</p>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-3">
                            <div class="module-card">
                                <div class="module-icon">
                                    <i class="bi bi-award-fill"></i>
                                </div>
                                <h5>Results</h5>
                                <p>Submission scoring, review flow, and student result access.</p>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-3">
                            <div class="module-card">
                                <div class="module-icon">
                                    <i class="bi bi-chat-heart-fill"></i>
                                </div>
                                <h5>Communication</h5>
                                <p>Role-aware notices and feedback management workflows.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Login -->
        <section class="login-section" id="login">
            <div class="container">
                <div class="section-header">
                    <span class="section-eyebrow">
                        <i class="bi bi-lock-fill"></i>
                        Secure Login
                    </span>

                    <h2 class="section-title">Sign in to your NextExamLK workspace.</h2>

                    <p class="section-text">
                        Use your username or email, password, and assigned role to continue.
                    </p>
                </div>

                <div class="login-panel">
                    <div class="row g-0">
                        <div class="col-lg-5">
                            <div class="login-info-panel">
                                <span class="badge bg-light text-primary mb-3 px-3 py-2">
                                    <i class="bi bi-shield-check me-1"></i>
                                    Protected Access
                                </span>

                                <h3>One login, three academic workspaces.</h3>

                                <p>
                                    NextExamLK validates username or email, password, selected role,
                                    and account status before allowing dashboard access.
                                </p>

                                <div class="login-info-list">
                                    <div class="login-info-item">
                                        <i class="bi bi-person-check-fill"></i>
                                        <div>
                                            <div class="fw-bold">Student access</div>
                                            <small class="opacity-75">Attempt exams, view notices, results, and feedback.</small>
                                        </div>
                                    </div>

                                    <div class="login-info-item">
                                        <i class="bi bi-person-video3"></i>
                                        <div>
                                            <div class="fw-bold">Lecturer access</div>
                                            <small class="opacity-75">Manage assessments and review academic records.</small>
                                        </div>
                                    </div>

                                    <div class="login-info-item">
                                        <i class="bi bi-shield-lock-fill"></i>
                                        <div>
                                            <div class="fw-bold">Admin access</div>
                                            <small class="opacity-75">Control system data, users, notices, and reports.</small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-lg-7">
                            <div class="login-form-panel">
                                <div class="d-flex justify-content-between align-items-start gap-3 mb-4">
                                    <div>
                                        <span class="badge badge-soft-primary mb-3">
                                            <i class="bi bi-lock-fill me-1"></i>
                                            Account Login
                                        </span>

                                        <h3 class="fw-bold mb-1">Welcome back</h3>

                                        <p class="text-secondary mb-0">
                                            Enter your account details to continue.
                                        </p>
                                    </div>

                                    <div class="d-none d-md-flex login-mini-icon">
                                        <i class="bi bi-person-check-fill"></i>
                                    </div>
                                </div>

                                <% if (!alertMessage.isEmpty()) { %>
                                    <div class="alert <%= alertType %> d-flex gap-3 align-items-start"
                                         data-auto-close="5500">
                                        <i class="bi <%= FileUtil.h(alertIcon) %> fs-5"></i>

                                        <div>
                                            <div class="fw-bold"><%= FileUtil.h(alertTitle) %></div>
                                            <div><%= FileUtil.h(alertMessage) %></div>
                                        </div>
                                    </div>
                                <% } %>

                                <form action="<%= request.getContextPath() %>/login"
                                      method="post"
                                      class="needs-validation"
                                      novalidate>

                                    <div class="mb-3">
                                        <label class="form-label">Email or Username</label>

                                        <div class="input-group login-input-group">
                                            <span class="input-group-text">
                                                <i class="bi bi-person"></i>
                                            </span>

                                            <input type="text"
                                                   name="username"
                                                   class="form-control"
                                                   placeholder="Enter email or username"
                                                   autocomplete="username"
                                                   required>

                                            <div class="invalid-feedback">
                                                Email or username is required.
                                            </div>
                                        </div>
                                    </div>

                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <label class="form-label mb-0">Password</label>
                                            <span class="small fw-bold text-secondary">Session timeout: 30 minutes</span>
                                        </div>

                                        <div class="input-group login-input-group mt-2">
                                            <span class="input-group-text">
                                                <i class="bi bi-lock"></i>
                                            </span>

                                            <input type="password"
                                                   name="password"
                                                   class="form-control"
                                                   placeholder="Enter password"
                                                   autocomplete="current-password"
                                                   required>

                                            <button class="btn btn-outline-secondary"
                                                    type="button"
                                                    id="togglePasswordBtn"
                                                    aria-label="Show or hide password">
                                                <i class="bi bi-eye" id="togglePasswordIcon"></i>
                                            </button>

                                            <div class="invalid-feedback">
                                                Password is required.
                                            </div>
                                        </div>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label">Access Role</label>

                                        <select name="role" class="form-select" required>
                                            <option value="">Select account role</option>
                                            <option value="Admin">Admin</option>
                                            <option value="Lecturer">Lecturer</option>
                                            <option value="Student">Student</option>
                                        </select>

                                        <div class="invalid-feedback">
                                            Please select your role.
                                        </div>
                                    </div>

                                    <button type="submit" class="btn btn-primary w-100 login-submit-btn">
                                        <i class="bi bi-box-arrow-in-right me-2"></i>
                                        Sign In
                                    </button>
                                </form>

                                <div class="home-divider">
                                    <span>demo access</span>
                                </div>

                                <div class="row g-2 mb-4">
                                    <div class="col-md-4">
                                        <button class="btn btn-outline-primary w-100 demo-role-btn"
                                                type="button"
                                                data-demo-username="admin"
                                                data-demo-role="Admin">
                                            <i class="bi bi-shield-lock me-1"></i>
                                            Admin
                                        </button>
                                    </div>

                                    <div class="col-md-4">
                                        <button class="btn btn-outline-primary w-100 demo-role-btn"
                                                type="button"
                                                data-demo-username="lecturer01"
                                                data-demo-role="Lecturer">
                                            <i class="bi bi-person-video3 me-1"></i>
                                            Lecturer
                                        </button>
                                    </div>

                                    <div class="col-md-4">
                                        <button class="btn btn-outline-primary w-100 demo-role-btn"
                                                type="button"
                                                data-demo-username="student01"
                                                data-demo-role="Student">
                                            <i class="bi bi-mortarboard me-1"></i>
                                            Student
                                        </button>
                                    </div>
                                </div>

                                <div class="login-help">
                                    <div class="d-flex gap-3 align-items-start">
                                        <div class="login-help-icon">
                                            <i class="bi bi-info-circle-fill"></i>
                                        </div>

                                        <div>
                                            <div class="fw-bold">Demo credentials available</div>
                                            <small class="text-secondary">
                                                Select a demo role to auto-fill sample credentials for testing the login flow.
                                                Login validates account role and status before opening the workspace.
                                            </small>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Final CTA -->
        <section class="final-cta">
            <div class="container">
                <div class="final-cta-card">
                    <div class="d-flex justify-content-between align-items-center gap-4 flex-wrap">
                        <div>
                            <h2>Ready to continue?</h2>
                            <p>Sign in using your assigned role and continue to the correct academic workspace.</p>
                        </div>

                        <a href="#login" class="btn btn-light">
                            <i class="bi bi-box-arrow-in-right me-2"></i>
                            Go to Login
                        </a>
                    </div>
                </div>
            </div>
        </section>

    </main>

    <footer class="home-footer">
        <div class="container">
            <div class="d-flex justify-content-between align-items-center gap-3 flex-wrap">
                <div>
                    <strong>NextExamLK</strong>
                    <span class="ms-2">Secure Online Examination & Result Management Platform</span>
                </div>

                <small>© 2026 NextExamLK. All rights reserved.</small>
            </div>
        </div>
    </footer>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>

<script src="<%= request.getContextPath() %>/js/app.js?v=professional-final-2"></script>

</body>
</html>