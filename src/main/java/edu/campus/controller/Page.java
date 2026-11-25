package edu.campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class Page {
  @GetMapping("/") public String index() { return "index"; }
  @GetMapping("/hall") public String hall(HttpSession session) { return "STUDENT".equals(session.getAttribute("role"))?"hall":"redirect:/"; }
  @GetMapping("/student") public String student(HttpSession session) { return "STUDENT".equals(session.getAttribute("role"))?"student":"redirect:/"; }
  @GetMapping({"/student/history","/student/points"}) public String studentPages(HttpSession session) { return student(session); }
  @GetMapping("/owner") public String owner(HttpSession session) { return "OWNER".equals(session.getAttribute("role"))?"owner":"redirect:/"; }
  @GetMapping({"/owner/new","/owner/ended"}) public String ownerPages(HttpSession session) { return owner(session); }
  @GetMapping("/admin") public String admin(HttpSession session) { return "ADMIN".equals(session.getAttribute("role"))?"admin-dashboard":"redirect:/"; }
  @GetMapping("/admin/students") public String students(HttpSession session) { return "ADMIN".equals(session.getAttribute("role"))?"admin-students":"redirect:/"; }
  @GetMapping("/admin/audits") public String audits(HttpSession session) { return "ADMIN".equals(session.getAttribute("role"))?"admin":"redirect:/"; }
}
