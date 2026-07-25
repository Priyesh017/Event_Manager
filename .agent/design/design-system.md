# Design System — EventHub

## Color Palette

### Primary Colors
- **Primary**: `#6C63FF` (Indigo-Violet)
- **Primary Dark**: `#4F46E5`
- **Primary Light**: `#8B83FF`

### Background (Dark Mode)
- **Background**: `#0F0F1A`
- **Surface**: `#1A1A2E`
- **Surface Elevated**: `#16213E`
- **Card**: `#1E1E35`

### Text
- **Text Primary**: `#F1F5F9`
- **Text Secondary**: `#94A3B8`
- **Text Muted**: `#64748B`

### Semantic Colors
- **Success**: `#22C55E`
- **Warning**: `#F59E0B`
- **Error**: `#EF4444`
- **Info**: `#3B82F6`

### Category Badge Colors
- CONFERENCE: `#6C63FF`
- WORKSHOP: `#22C55E`
- WEBINAR: `#3B82F6`
- MEETUP: `#F59E0B`
- SEMINAR: `#EC4899`

---

## Typography

- **Font Family**: `'Inter', 'Segoe UI', sans-serif` (from Google Fonts)
- **Heading**: Bold, gradient text on key headings
- **Body**: 400/500 weight, line-height 1.6
- **Code**: `'Fira Code', monospace`

### Scale
| Token    | Size   | Usage               |
|----------|--------|---------------------|
| `--fs-xs`| 12px   | Labels, badges      |
| `--fs-sm`| 14px   | Secondary text      |
| `--fs-md`| 16px   | Body text           |
| `--fs-lg`| 20px   | Section headings    |
| `--fs-xl`| 28px   | Page headings       |
| `--fs-2xl`| 40px  | Hero headings       |

---

## Spacing

- 4px base unit
- Spacing scale: 4, 8, 12, 16, 24, 32, 48, 64, 96

---

## Component Patterns

### Cards
- Background: `var(--card)`
- Border: `1px solid rgba(255,255,255,0.05)`
- Border-radius: `16px`
- Box-shadow: `0 4px 24px rgba(0,0,0,0.3)`
- Hover: Lift + glow effect

### Buttons
- Primary: Gradient `#6C63FF → #4F46E5`, white text
- Secondary: Outline with primary color
- Danger: `#EF4444`
- Border-radius: `8px`
- Transition: `all 0.2s ease`

### Forms
- Input background: `rgba(255,255,255,0.05)`
- Input border: `1px solid rgba(255,255,255,0.1)`
- Focus border: `var(--primary)`
- Border-radius: `8px`

### Navbar
- Glassmorphism: `backdrop-filter: blur(20px)`
- Background: `rgba(15, 15, 26, 0.8)`
- Border-bottom: `1px solid rgba(255,255,255,0.05)`

---

## Animation Standards

- Hover transitions: `200ms ease`
- Page load fade-in: `400ms ease`
- Card hover lift: `translateY(-4px)` with shadow glow
- Button press: `scale(0.97)`
- Alert/toast: slide-in from top-right

---

## Layout

- **Container max-width**: `1200px`
- **Grid**: CSS Grid + Flexbox
- **Event cards grid**: `repeat(auto-fill, minmax(340px, 1fr))`
- **Responsive breakpoints**:
  - Mobile: `< 640px`
  - Tablet: `640px – 1024px`
  - Desktop: `> 1024px`
