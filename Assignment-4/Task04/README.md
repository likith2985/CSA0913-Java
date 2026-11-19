# Task 04: Event-Handling Forensics & Drawing Persistence

## 1. Forensic Diagnosis of Defect 1 (Task 4.1)
- **Observed Symptom:** Mouse clicks inside the window register no visual change; the status label remains fixed at `"Clicks: 0"`.
- **Root Cause:**
  ```java
  // Original defective code:
  public void mouseclicked(MouseEvent e) { ... }
  ```
  Java is strictly case-sensitive. The `MouseListener` interface specifies:
  ```java
  void mouseClicked(MouseEvent e); // Note capital 'C'
  ```
  Because the developer wrote `mouseclicked` (all lowercase), the compiler treated it as an arbitrary new helper method rather than an interface implementation. The underlying `MouseAdapter` provides an empty default implementation of `mouseClicked(MouseEvent e)`. Consequently, whenever the AWT Event Dispatch Thread detected a mouse click, it invoked the adapter's no-op method. The custom `mouseclicked` was never invoked!
- **Defensive Programming Practice:**
  Always annotate event listener methods with `@Override`:
  ```java
  @Override
  public void mouseClicked(MouseEvent e) { ... }
  ```
  If `@Override` had been present on `mouseclicked`, the `javac` compiler would immediately have flagged the error at build time:
  `error: method does not override or implement a method from a supertype`.

---

## 2. Forensic Diagnosis of Defect 2 (Task 4.2)
- **Observed Symptom:** Dots drawn on the canvas vanish completely whenever the window is minimized, restored, moved off-screen, or overlapped by another window.
- **Root Cause (Immediate Mode Anti-Pattern):**
  The original code drew directly to the screen using:
  ```java
  Graphics g = getGraphics();
  g.fillOval(x, y, 6, 6);
  ```
  In GUI windowing systems (AWT/Swing, X11, Win32), the OS display server does not retain pixel bitplanes when a window is covered or minimized. When restored, the OS generates an asynchronous `PAINT` event, causing the runtime to clear the damaged surface and call `paint(Graphics g)`.
  Because the coordinates were never stored in an in-memory data structure, `paint()` redrew a blank canvas, erasing all previous user dots!

---

## 3. Repaired Code Completion (Task 4.3 & 4.4)
The repairs in `DrawPad.java` fulfill both TODO requirements:
```java
// TODO 1: Correct method signature and coordinate retention
@Override
public void mouseClicked(MouseEvent e) {
    points.add(new Point(e.getX(), e.getY()));
    status.setText("Clicks: " + points.size());
    repaint(); // Asks EDT to schedule paint() invocation
}

// TODO 2: Persistent retained rendering inside paint(Graphics g)
@Override
public void paint(Graphics g) {
    super.paint(g);
    g.setColor(Color.RED);
    for (Point p : points) {
        g.fillOval(p.x - 3, p.y - 3, 6, 6);
    }
}
```

---

## 4. Alternative Concept Exploration: Retained Vector Loop vs Off-Screen `BufferedImage`

| Architectural Criterion | Retained Vector Loop (`List<Point>` + `paint()`) | Off-Screen Raster Blitting (`BufferedImage`) |
|---|---|---|
| **Render Time Complexity** | $O(n)$ where $n$ is total clicks recorded. | $O(1)$ constant time (blits cached bitmap via `g.drawImage()`). |
| **Space Complexity** | $O(n)$ memory (storing $n$ small `Point` objects: $\approx 24$ bytes each). | $O(W \times H)$ memory for raster buffer (e.g., $1920 \times 1080 \times 4$ bytes $\approx 8.3$ MB). |
| **Resolution Scalability** | **Infinite vector fidelity:** Points can be scaled cleanly on High-DPI without blur. | **Pixelated:** Resizing requires buffer recreation and causes scaling artifacts. |
| **Undo / Manipulation** | Trivial: popping elements from `List<Point>` enables instant Undo/Redo. | Hard: modifying or removing single past strokes requires full layer history. |
| **Best Used For** | Vector drawing, CAD diagrams, click trackers ($n < 50,000$). | High-frequency continuous brush painting (e.g. Photoshop / MS Paint). |
