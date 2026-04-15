package com.hackassist.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RootController {

    @GetMapping("/")
    @ResponseBody
    public String serveRoot() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>HackAssist AI - Student Management System</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        padding: 20px;
                    }
                    .container {
                        background: white;
                        border-radius: 15px;
                        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                        padding: 60px 40px;
                        max-width: 500px;
                        text-align: center;
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 15px;
                        font-size: 2.5em;
                    }
                    .subtitle {
                        color: #666;
                        font-size: 1.1em;
                        margin-bottom: 40px;
                    }
                    .button-group {
                        display: flex;
                        gap: 15px;
                        margin-bottom: 30px;
                    }
                    .btn {
                        flex: 1;
                        padding: 15px 25px;
                        border: none;
                        border-radius: 8px;
                        font-size: 1em;
                        font-weight: 600;
                        cursor: pointer;
                        transition: all 0.3s ease;
                        text-decoration: none;
                        display: inline-block;
                    }
                    .btn-primary {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                    }
                    .btn-primary:hover {
                        transform: translateY(-3px);
                        box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
                    }
                    .btn-secondary {
                        background: #f5f5f5;
                        color: #333;
                        border: 2px solid #667eea;
                    }
                    .btn-secondary:hover {
                        background: #667eea;
                        color: white;
                    }
                    .info-box {
                        background: #f0f4ff;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        text-align: left;
                        border-radius: 8px;
                        margin-top: 30px;
                    }
                    .info-box h3 {
                        color: #667eea;
                        margin-bottom: 10px;
                    }
                    .info-box p {
                        color: #666;
                        font-size: 0.95em;
                        line-height: 1.6;
                    }
                    .features {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 15px;
                        margin: 30px 0;
                        text-align: left;
                    }
                    .feature {
                        background: #f9f9f9;
                        padding: 15px;
                        border-radius: 8px;
                    }
                    .feature strong {
                        color: #667eea;
                        display: block;
                        margin-bottom: 5px;
                    }
                    .feature p {
                        font-size: 0.9em;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎓 HackAssist AI</h1>
                    <p class="subtitle">Smart Project Assistant with Automated Task Evaluation</p>
                    
                    <div class="button-group">
                        <a href="/oauth2/authorization/google" class="btn btn-primary">Sign in with Google</a>
                        <a href="/oauth2/authorization/github" class="btn btn-secondary">Sign in with GitHub</a>
                    </div>
                    
                    <div class="features">
                        <div class="feature">
                            <strong>📊 Smart Tracking</strong>
                            <p>Automatic task progress evaluation</p>
                        </div>
                        <div class="feature">
                            <strong>🤖 AI Powered</strong>
                            <p>Semantic analysis of commits</p>
                        </div>
                        <div class="feature">
                            <strong>🔗 GitHub Integrated</strong>
                            <p>Webhook-based automation</p>
                        </div>
                        <div class="feature">
                            <strong>📈 Real-time Reports</strong>
                            <p>Live project insights</p>
                        </div>
                    </div>
                    
                    <div class="info-box">
                        <h3>ℹ️ About This System</h3>
                        <p>HackAssist AI is an intelligent project management system that automatically evaluates GitHub commits against project tasks using AI and rule-based analysis. Perfect for managing student projects, hackathons, and team development.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
