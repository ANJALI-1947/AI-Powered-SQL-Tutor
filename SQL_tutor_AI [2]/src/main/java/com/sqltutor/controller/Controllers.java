package com.sqltutor.controller;

import com.sqltutor.ai.AITutorService;
import com.sqltutor.model.ChatMessage;
import com.sqltutor.model.QueryResult;
import com.sqltutor.service.QueryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ═══════════════════════════════════════════════════════════════
//  LOGIN CONTROLLER
// ═══════════════════════════════════════════════════════════════
@Controller
class LoginController {
    @GetMapping("/login")
    public String login(
            @RequestParam(required=false) String error,
            @RequestParam(required=false) String logout,
            @RequestParam(required=false) String expired,
            Model model) {
        if (error   != null) model.addAttribute("errorMsg",   "❌ Invalid username or password.");
        if (logout  != null) model.addAttribute("successMsg", "✅ Logged out successfully.");
        if (expired != null) model.addAttribute("errorMsg",   "⚠️ Session expired. Login again.");
        return "login";
    }
}

// ═══════════════════════════════════════════════════════════════
//  MAIN MVC CONTROLLER
// ═══════════════════════════════════════════════════════════════
@Controller
class TutorController {

    @Autowired QueryService queryService;
    @Autowired AITutorService aiTutorService;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails user,
                        HttpSession session, Model model) {
        String username = user.getUsername();
        model.addAttribute("username",       username);
        model.addAttribute("sessionId",      session.getId());
        model.addAttribute("schema",         queryService.getSchema());
        model.addAttribute("dbHistory",      queryService.getDbHistory(username));
        model.addAttribute("sessionHistory", queryService.getSessionHistory(session));
        model.addAttribute("queryCount",     queryService.getQueryCount(username));
        model.addAttribute("chatHistory",    aiTutorService.getChatHistory(username));
        return "index";
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails user,
                          HttpSession session, Model model) {
        String username = user.getUsername();
        model.addAttribute("username",       username);
        model.addAttribute("sessionId",      session.getId());
        model.addAttribute("dbHistory",      queryService.getDbHistory(username));
        model.addAttribute("sessionHistory", queryService.getSessionHistory(session));
        model.addAttribute("total",          queryService.getQueryCount(username));
        return "history";
    }

    @PostMapping("/history/clear")
    public String clearHistory(@AuthenticationPrincipal UserDetails user, HttpSession session) {
        queryService.clearHistory(user.getUsername());
        session.removeAttribute("sessionHistory");
        return "redirect:/history";
    }
}

// ═══════════════════════════════════════════════════════════════
//  REST API CONTROLLER
// ═══════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api")
class ApiController {

    @Autowired QueryService    queryService;
    @Autowired AITutorService  aiTutorService;

    /** POST /api/query — Execute SQL */
    @PostMapping("/query")
    public ResponseEntity<QueryResult> runQuery(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails user,
            HttpSession session) {
        String sql = body.get("sql");
        System.out.println("[API] User='" + user.getUsername()
            + "' Session=" + session.getId() + " SQL=" + sql);
        QueryResult result = queryService.executeQuery(sql, user.getUsername(), session);
        return result.isSuccess()
            ? ResponseEntity.ok(result)
            : ResponseEntity.badRequest().body(result);
    }

    /** GET /api/schema — DB schema */
    @GetMapping("/schema")
    public ResponseEntity<List<QueryService.TableInfo>> schema() {
        return ResponseEntity.ok(queryService.getSchema());
    }

    /** GET /api/session/history */
    @GetMapping("/session/history")
    public ResponseEntity<List<String>> sessionHistory(HttpSession session) {
        return ResponseEntity.ok(queryService.getSessionHistory(session));
    }

    /** GET /api/session/info */
    @GetMapping("/session/info")
    public ResponseEntity<Map<String, Object>> sessionInfo(
            @AuthenticationPrincipal UserDetails user, HttpSession session) {
        return ResponseEntity.ok(Map.of(
            "loggedIn",  true,
            "username",  user.getUsername(),
            "roles",     user.getAuthorities().toString(),
            "sessionId", session.getId(),
            "timeout",   session.getMaxInactiveInterval() + "s"
        ));
    }

    /** POST /api/chat — AI Chatbot */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails user,
            HttpSession session) {

        String userMessage = body.getOrDefault("message", "").trim();
        String currentSql  = body.getOrDefault("currentSql", "");

        if (userMessage.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Message cannot be empty"));
        }

        String username = user.getUsername();
        List<ChatMessage> history = aiTutorService.getChatHistory(username);
        String aiResponse = aiTutorService.chat(username, userMessage, currentSql, history);

        return ResponseEntity.ok(Map.of(
            "response", aiResponse,
            "username", username
        ));
    }

    /** GET /api/chat/history */
    @GetMapping("/chat/history")
    public ResponseEntity<List<ChatMessage>> chatHistory(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(aiTutorService.getChatHistory(user.getUsername()));
    }

    /** DELETE /api/chat/clear */
    @DeleteMapping("/chat/clear")
    public ResponseEntity<Map<String, String>> clearChat(
            @AuthenticationPrincipal UserDetails user) {
        aiTutorService.clearChatHistory(user.getUsername());
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }

    /** GET /api/ping */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok", "app", "SQL Tutor AI"));
    }
}
