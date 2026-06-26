---
name: frontend-design
description: Guidelines and instructions for creating beautiful, responsive, and clean frontend interfaces using HTML, Tailwind CSS, and Thymeleaf templates. Focuses on dashboards, admin UIs, clean layout structures, forms, tables, and consistent design systems.
---

# Frontend Design Guidelines

This skill provides guidelines and best practices for styling, structuring, and maintaining frontend code using HTML, Thymeleaf, and Tailwind CSS matching the custom project interface.

## 1. Design & Typography (Apple & Linear Hybrid)
- **Font**: Use clean modern typography (`Inter`, `-apple-system`) via Google Fonts or system fallbacks.
- **Colors**:
  - Primary Accent: HELUKABEL Red (`#f6171b` / `rgba(246, 23, 27, 1)`).
  - Background: Gentle slate gradient (`#fcfdff` to `#f1f4f9` to `#fff1f2`) overlayed with a subtle Dot Matrix Grid pattern (`radial-gradient(rgba(0, 0, 0, 0.03) 1.2px, transparent 1.2px)` with `background-size: 24px 24px`).
  - Text: Dark charcoal (`#0f0f11`) for maximum contrast and readability.
  - Border: Soft zinc/slate (`#e4e4e7` / `rgba(226, 232, 240, 0.9)`).

## 2. Layout & Card Specifications
- **Dot Matrix Grid Background**: Clean, structural background pattern. Floating decor blobs are disabled.
- **KPI Cards**:
  - Pure white background (`#ffffff`) for maximum contrast.
  - No borders (`border: none !important`) and soft shadow (`0 4px 15px rgba(0, 0, 0, 0.02)`).
  - Contains a soft blue wave chart vector background (`rgba(59, 130, 246, 0.04)`) at the bottom (70px height, no stroke line).
  - Auto-shrinking text logic for long numbers (dynamic scale down via MutationObserver).
- **Chart & Table Containers**:
  - Semi-transparent white background (`rgba(255, 255, 255, 0.55)`).
  - High backdrop blur (`blur(16px) saturate(120%)`) for frosted glass effect.
  - Thin translucent white border (`1px solid rgba(255, 255, 255, 0.6)`).
  - Sharp corners (`rounded-lg` / `8px` rounded corners).
- **Navigation (Sidebar & Header)**:
  - Sidebar: Minimal light background (`#fafafa`), thin right border (`#e4e4e7`). Active item uses soft red tint (`rgba(246, 23, 27, 0.08)`) and brand red text.
  - Header: Semi-transparent white (`rgba(255, 255, 255, 0.5)`) with backdrop blur (`20px`). Search bar is removed.

## 3. Form & Table Controls
- **Inputs**: Solid white backgrounds, sharp borders, focus accent ring in HELUKABEL Red (`rgba(246, 23, 27, 0.1)` glow shadow).
- **Tables**: Alternating clean rows, header in `#fafafa` with uppercase text and `#52525b` color for clean layout structure.
- **Buttons**: Sharp borders (`6px` rounded), light background transitions.

## 4. Thymeleaf Compatibility
- Avoid JSX/TSX conventions. Use standard Thymeleaf attributes:
  - `th:src="@{...}"`
  - `th:href="@{...}"`
  - `th:text="${...}"`
  - `th:classappend="${condition ? 'active-class' : ''}"`
  - Use cache-busting version parameter for CSS links (e.g., `th:href="@{/css/style.css(v=3)}"`).

