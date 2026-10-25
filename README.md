# ⚡ SKIP THE SYLLABUS

> **"Your syllabus is cooked. Let's cook back."**
>
> *Less staring at PDFs. More actually understanding things.*

A polished, cinematic, client-side AI learning website designed for students facing exam crunches. Turn dry, intimidating lecture notes and syllabus PDFs into structured breakdowns, interactive quizzes, document-grounded conversations, and interactive 3D algorithm visualizers.

---

## 🎨 Visual Design System

Built strictly following the brand direction:
- **Background**: Near-Black (`#050605`)
- **Charcoal Surface**: Elevated Dark Panels (`#0B0E0B`, `#111511`)
- **Primary Accent**: Electric Lime (`#C5FF4A`)
- **Muted Accent**: Moss Green (`#8BAA52`)
- **Typography & Borders**: Off-White (`#F4F6EF`), Muted Olive Grey (`#969D91`), Subtle Border (`#282D26`)
- **Zero Purple**: Absolutely NO purple, violet, magenta, or purple gradients.

---

## ⚡ Core Features

1. **Cinematic 3D Landing Page (`index.html`)**
   - Floating Three.js 3D Knowledge Core with responsive orbit geometry, dynamic particle starfield, and subtle cursor parallax.
   - Interactive **Quick Concept Explainer** widget that previews breakdowns for Binary Search Trees, Bubble Sort, Deadlock, and Polymorphism.
   - Witty, student-first brand voice and streamlined navigation.

2. **Concept Explainer (`document.html`)**
   - Enter any topic or keyword from your syllabus.
   - Generates a structured 5-part breakdown:
     - 🧠 **Intuition**: Plain English mental model.
     - 🔍 **Breakdown**: Numbered architectural concepts and invariants.
     - 💡 **Real-World Analogy**: Concrete practical application.
     - 📌 **Key Exam Takeaways**: Bullet points for rapid revision.
     - ❓ **Self-Check Questions**: Quick questions to verify retention.
   - Toggle difficulty levels: *Beginner*, *Intermediate*, *Exam Ready*.

3. **Interactive Quiz Lab (`quiz.html`)**
   - Configure question count (2–10 questions) and difficulty level.
   - Interactive options (A, B, C, D) with immediate selection feedback.
   - Instant scoring with humorous, witty student feedback ("Genuinely terrifying", "Solid A", "Currently cooked").
   - Detailed review with answer keys, explanations, and automatic weak-concept tracking.

4. **Document Workspace & Grounded Chat (`document.html`)**
   - Dedicated study environment for any selected syllabus document.
   - **Grounded Chat**: Ask questions directly about the document with simulated AI responses and verifiable page citations.
   - Clearly labeled demo AI badges—no deceptive claims or external API keys needed.

5. **Interactive 3D Data Structure & Algorithm Explorer (`explorer.html`)**
   - **3D Binary Search Tree**:
     - Insert custom numeric nodes into a real-time 3D rendered tree.
     - Animate traversals: In-order (sorted), Pre-order, and Post-order with glowing node highlighting.
     - Camera controls: 3D orbit rotation, zoom, and pan.
     - Side-by-side Java OOP code viewer.
   - **3D Sorting Visualizer**:
     - 3D glowing columns representing array elements.
     - Visualizes Bubble Sort, Selection Sort, and Insertion Sort.
     - Color-coded comparison, swapping, and sorted states.
     - Step-by-step next, pause/play, speed slider, and Java OOP code reference.

6. **Study Progress & Analytics (`dashboard.html`)**
   - Real-time learning metrics: total documents, quizzes taken, average score percentage, and study sessions.
   - **Concepts Needing Attention**: Dynamically flags topics you missed during quizzes.
   - Recent activity stream and quick action launch pads.

7. **Client-Side Document Ingestion (`upload.html`)**
   - Drag-and-drop or select PDF, TXT, and Markdown files.
   - Real-time client-side text parsing via HTML5 `FileReader`.
   - Simulated extraction progress bar with witty student status messages.
   - Persists documents directly into `localStorage`.

8. **Settings & Data Management (`settings.html`)**
   - Offline storage inspector showing item counts and estimated byte consumption.
   - One-click "Reset to Default Sample Notes".
   - One-click "Export My Study Data (JSON)" backup download.
   - "Clear All Storage" reset option.

---

## 🛠️ Architecture: 100% Client-Side & Zero-Backend

- **No Backend Server Required**: Runs entirely in the browser using HTML5, CSS3, ES6 JavaScript, and `localStorage`.
- **No Dependencies or Builds**: No Node.js, npm, Maven, Spring Boot, or database setups are required.
- **Offline Three.js**: Bundled locally in [`js/three.min.js`](file:///c:/Users/bhatn/Documents/NMAMIT/SKIP-THE-SYLLABUS/js/three.min.js) (r128) for dependable 3D visuals without CDN reliance.
- **Local Engine**: [`js/storage.js`](file:///c:/Users/bhatn/Documents/NMAMIT/SKIP-THE-SYLLABUS/js/storage.js) (`SkipStore`) handles document storage, mock AI explanations, quiz generation, session logging, and progress computation.

---

## 🚀 How to Run the Website

### Method 1: Direct File Open (Easiest)
Simply double-click [`index.html`](file:///c:/Users/bhatn/Documents/NMAMIT/SKIP-THE-SYLLABUS/index.html) in your file explorer, or right-click and choose **Open With > Google Chrome / Microsoft Edge / Firefox**.

### Method 2: Local Static Server (Recommended)
If you prefer running through `http://localhost`:

#### Using Python 3 (built into Windows / macOS / Linux):
```powershell
python -m http.server 3000
```
Then open [http://localhost:3000](http://localhost:3000) in your browser.

#### Using Node.js:
```powershell
npx serve .
```

---

## 📂 Project Structure

```
SKIP-THE-SYLLABUS/
├── index.html            # Landing page with 3D Knowledge Core & Quick Explainer
├── dashboard.html        # Learning analytics, recent quizzes, weak concepts
├── upload.html           # Document ingestion (PDF/TXT) via FileReader
├── document.html         # Document study space (Concept Explainer + Grounded Chat)
├── quiz.html             # Interactive Quiz Lab with scoring and review
├── explorer.html         # 3D Binary Search Tree & 3D Sorting Visualizer
├── settings.html         # Storage inspector, data export/reset controls
├── js/
│   ├── storage.js        # SkipStore client-side state engine & demo AI synthesizer
│   └── three.min.js      # Bundled Three.js r128 (offline 3D graphics)
└── README.md             # Complete project guide and documentation
```
