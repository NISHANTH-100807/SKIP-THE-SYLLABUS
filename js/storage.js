/**
 * Skip The Syllabus — Client-side Learning Engine & LocalStorage Manager
 * Provides offline-first state, demo AI responses, quiz generation, and analytics.
 */
const SkipStore = (function() {
  const DOCS_KEY = 'sts_documents';
  const ATTEMPTS_KEY = 'sts_quiz_attempts';
  const SESSIONS_KEY = 'sts_study_sessions';
  const WEAK_KEY = 'sts_weak_concepts';

  // Seed documents if empty
  const DEFAULT_DOCS = [
    {
      id: 1,
      name: "Data_Structures_Algorithms_Unit3.pdf",
      size: 2457600,
      pages: 14,
      uploadedAt: new Date(Date.now() - 3600000 * 4).toISOString(),
      status: "READY",
      summary: "Binary search trees, AVL rotations, graph traversals, and amortized algorithm complexity.",
      text: `Unit 3: Binary Search Trees & Sorting Algorithms.
A Binary Search Tree (BST) is a node-based binary tree data structure with the property that the left subtree contains only nodes with keys lesser than the node's key, and the right subtree contains only nodes with keys greater than the node's key.
Inorder traversal of a BST visits nodes in non-decreasing sorted order.
Time complexity for search, insert, and delete is O(h) where h is tree height, averaging O(log n) for balanced trees.
Bubble sort compares adjacent elements and swaps them if they are in the wrong order, with worst-case O(n^2) time complexity.
Selection sort repeatedly finds the minimum element from the unsorted part and puts it at the beginning.
Insertion sort builds the final sorted array one item at a time, efficient for small or nearly sorted datasets.`
    },
    {
      id: 2,
      name: "Operating_Systems_Concurrency.pdf",
      size: 1843200,
      pages: 9,
      uploadedAt: new Date(Date.now() - 86400000).toISOString(),
      status: "READY",
      summary: "Process scheduling, thread synchronization, mutual exclusion, semaphores, and deadlock prevention.",
      text: `Unit 4: Process Synchronization & Deadlocks.
A race condition occurs when multiple processes access and manipulate the same data concurrently and the outcome depends on the order of execution.
The critical-section problem requires three conditions: Mutual Exclusion, Progress, and Bounded Waiting.
Semaphores are integer variables accessed via atomic wait() and signal() operations.
Deadlock occurs when four conditions hold simultaneously: Mutual Exclusion, Hold and Wait, No Preemption, and Circular Wait.
Banker's Algorithm is used for deadlock avoidance using safe sequence verification.`
    }
  ];

  function getDocs() {
    try {
      const data = localStorage.getItem(DOCS_KEY);
      if (!data) {
        localStorage.setItem(DOCS_KEY, JSON.stringify(DEFAULT_DOCS));
        return DEFAULT_DOCS;
      }
      return JSON.parse(data);
    } catch(e) {
      return DEFAULT_DOCS;
    }
  }

  function saveDocs(docs) {
    localStorage.setItem(DOCS_KEY, JSON.stringify(docs));
  }

  function getDocById(id) {
    const docs = getDocs();
    return docs.find(d => String(d.id) === String(id)) || null;
  }

  function addDoc(name, size, text, pages) {
    const docs = getDocs();
    const newDoc = {
      id: Date.now(),
      name: name || "Uploaded_Document.pdf",
      size: size || 1024 * 512,
      pages: pages || Math.max(1, Math.ceil((text ? text.length : 1000) / 1200)),
      uploadedAt: new Date().toISOString(),
      status: "READY",
      summary: text ? text.substring(0, 180) + "..." : "Custom uploaded student document.",
      text: text || "Sample extracted syllabus text."
    };
    docs.unshift(newDoc);
    saveDocs(docs);
    logSession(newDoc.id, "UPLOAD", "Uploaded document: " + newDoc.name);
    return newDoc;
  }

  function deleteDoc(id) {
    let docs = getDocs();
    docs = docs.filter(d => String(d.id) !== String(id));
    saveDocs(docs);
  }

  // ── Study Sessions & Activity ────────────────────────────────────
  function logSession(docId, type, desc) {
    try {
      const sessions = JSON.parse(localStorage.getItem(SESSIONS_KEY) || '[]');
      sessions.unshift({
        id: Date.now(),
        docId,
        type,
        desc,
        timestamp: new Date().toISOString()
      });
      localStorage.setItem(SESSIONS_KEY, JSON.stringify(sessions.slice(0, 30)));
    } catch(e) {}
  }

  function getSessions() {
    try {
      return JSON.parse(localStorage.getItem(SESSIONS_KEY) || '[]');
    } catch(e) {
      return [];
    }
  }

  // ── Quiz Attempts & Scoring ──────────────────────────────────────
  function getAttempts() {
    try {
      return JSON.parse(localStorage.getItem(ATTEMPTS_KEY) || '[]');
    } catch(e) {
      return [];
    }
  }

  function saveAttempt(attempt) {
    try {
      const attempts = getAttempts();
      attempts.unshift(attempt);
      localStorage.setItem(ATTEMPTS_KEY, JSON.stringify(attempts.slice(0, 40)));
      logSession(attempt.docId, "QUIZ", `Completed quiz on ${attempt.docName}: ${attempt.scorePct}%`);
    } catch(e) {}
  }

  // ── Weak Concepts ────────────────────────────────────────────────
  function recordMissedConcept(conceptName) {
    try {
      const weaks = JSON.parse(localStorage.getItem(WEAK_KEY) || '{}');
      if (!weaks[conceptName]) {
        weaks[conceptName] = { name: conceptName, missed: 0, attempts: 0 };
      }
      weaks[conceptName].missed += 1;
      weaks[conceptName].attempts += 1;
      localStorage.setItem(WEAK_KEY, JSON.stringify(weaks));
    } catch(e) {}
  }

  function recordPassedConcept(conceptName) {
    try {
      const weaks = JSON.parse(localStorage.getItem(WEAK_KEY) || '{}');
      if (weaks[conceptName]) {
        weaks[conceptName].attempts += 1;
        localStorage.setItem(WEAK_KEY, JSON.stringify(weaks));
      }
    } catch(e) {}
  }

  function getWeakConceptsList() {
    try {
      const weaks = JSON.parse(localStorage.getItem(WEAK_KEY) || '{}');
      return Object.values(weaks)
        .map(w => ({
          name: w.name,
          missed: w.missed,
          attempts: w.attempts,
          errorRate: w.attempts > 0 ? (w.missed / w.attempts) : 0
        }))
        .filter(w => w.missed > 0)
        .sort((a,b) => b.missed - a.missed)
        .slice(0, 5);
    } catch(e) {
      return [];
    }
  }

  // ── AI Explanation Generator (Mock / Demo Mode) ──────────────────
  function explain(concept, level, docId) {
    const doc = getDocById(docId) || DEFAULT_DOCS[0];
    const c = (concept || "Binary Search Tree").trim();

    logSession(doc.id, "EXPLAIN", `Explained concept "${c}" (${level})`);

    // Topic-aware intuition generator
    let intuition = `Think of ${c} like a disciplined organizer in your brain. Instead of scanning through a mountain of unorganized notes one by one, ${c} establishes a crystal-clear rule that cuts your search space in half at every single turn.`;
    let breakdown = `1. Core Structure: Every component maintains strict invariance with its peers.\n2. Traversal & Lookup: Access patterns follow logarithmic efficiency rather than linear fatigue.\n3. Edge Cases: Handles empty structures and duplicates by consistent deterministic convention.\n4. Real-world application: Powers database indexes, compiler symbol tables, and priority schedulers.`;
    let example = `Imagine a library where books with titles before "M" are always placed on the left shelf, and titles after "M" are always placed on the right shelf. Finding "Operating Systems" takes seconds because you eliminate half the library on the first glance.`;
    let takeaways = `• Understand the invariant property before memorizing the code.\n• Inorder traversal provides sorted output without additional sorting overhead.\n• Balanced variants prevent degenerate linear performance.\n• Keep exam time complexity derivations handy: O(log n) average vs O(n) worst case.`;
    let selfCheck = `1. What happens to ${c} when input keys are inserted in strictly ascending order?\n2. Can you explain why searching an unbalanced tree degrades to linear scan?`;

    if (c.toLowerCase().includes("sort") || c.toLowerCase().includes("bubble")) {
      intuition = `Sorting is how algorithms tidy up digital chaos. Bubble Sort is like bubbles of air in water: larger elements continuously rise to the top of the array through adjacent peer comparisons.`;
      breakdown = `1. Pass 1: Compare a[0] and a[1], swap if needed. Continue to end of array.\n2. Invariant: After pass k, the k-th largest element is guaranteed to be in its final sorted position.\n3. Optimization: Stop early if no swaps occurred during a full pass.`;
      example = `Sorting a hand of playing cards from 2 to Ace by repeatedly checking adjacent pairs.`;
      takeaways = `• Worst and Average time complexity: O(n²).\n• Best case (optimized): O(n) when array is already sorted.\n• Space complexity: O(1) in-place auxiliary space.\n• Stability: Bubble sort is stable because identical keys preserve original order.`;
      selfCheck = `Why is bubble sort rarely used in production databases, and which divide-and-conquer algorithm replaces it?`;
    } else if (c.toLowerCase().includes("deadlock") || c.toLowerCase().includes("concurrency")) {
      intuition = `Deadlock is a digital Mexican standoff. Process A holds Resource 1 waiting for Resource 2, while Process B holds Resource 2 waiting for Resource 1. Neither can move.`;
      breakdown = `1. Mutual Exclusion: At least one resource must be held in a non-shareable mode.\n2. Hold and Wait: A process holding resources requests additional occupied resources.\n3. No Preemption: Resources cannot be forcibly seized.\n4. Circular Wait: A closed loop chain of processes exists where each waits for the next.`;
      example = `Two trains approaching each other on a single track with no siding.`;
      takeaways = `• Break ANY of the four Coffman conditions to prevent deadlock.\n• Banker's Algorithm prevents deadlock by checking safe allocation states.\n• Resource ordering breaks circular wait in software.`;
      selfCheck = `Which condition does assigning unique global integers to every resource and requesting them in increasing order break?`;
    }

    return {
      concept: c,
      level: level || "intermediate",
      docName: doc.name,
      intuition,
      breakdown,
      example,
      keyTakeaways: takeaways,
      selfCheck,
      pageReferences: [1, Math.min(doc.pages, 3)],
      mode: "DEMO AI"
    };
  }

  // ── Quiz Question Generator ──────────────────────────────────────
  function generateQuiz(docId, count = 5, difficulty = "medium") {
    const doc = getDocById(docId) || DEFAULT_DOCS[0];
    const isAlgo = doc.name.toLowerCase().includes("data") || doc.name.toLowerCase().includes("algorithm");

    const algoPool = [
      {
        text: "What is the primary property that distinguishes a Binary Search Tree (BST)?",
        options: [
          "Left subtree keys < Node key < Right subtree keys",
          "All nodes have exactly two child nodes",
          "Every path from root to leaf has identical length",
          "Nodes are stored in reverse insertion order"
        ],
        correct: 0,
        concept: "BST Invariant",
        page: 2,
        explanation: "The BST invariant requires all keys in the left subtree to be strictly smaller than the parent node, and keys in the right subtree to be greater."
      },
      {
        text: "Which traversal of a Binary Search Tree outputs keys in non-decreasing sorted order?",
        options: [
          "Pre-order Traversal",
          "In-order Traversal",
          "Post-order Traversal",
          "Breadth-First Level Traversal"
        ],
        correct: 1,
        concept: "BST Traversals",
        page: 3,
        explanation: "In-order traversal recursively visits Left -> Root -> Right, which inherently yields sorted output for any valid BST."
      },
      {
        text: "What is the average time complexity of searching for an element in a balanced BST?",
        options: [
          "O(1)",
          "O(log n)",
          "O(n)",
          "O(n log n)"
        ],
        correct: 1,
        concept: "Time Complexity",
        page: 4,
        explanation: "At each comparison in a balanced BST, half the remaining search space is discarded, resulting in O(log n) time."
      },
      {
        text: "What is the worst-case time complexity of standard Bubble Sort on an unsorted array of n elements?",
        options: [
          "O(n)",
          "O(n log n)",
          "O(n²)",
          "O(2ⁿ)"
        ],
        correct: 2,
        concept: "Sorting Complexity",
        page: 5,
        explanation: "Bubble sort requires n(n-1)/2 comparisons in the worst case (reverse sorted array), giving O(n²) complexity."
      },
      {
        text: "In Selection Sort, what action is performed during each outer loop pass?",
        options: [
          "The minimum element of the unsorted segment is identified and swapped into place",
          "Adjacent elements are continuously swapped until the entire list is verified",
          "The array is partitioned around a calculated pivot value",
          "Elements are merged recursively from two halves"
        ],
        correct: 0,
        concept: "Selection Sort Mechanism",
        page: 6,
        explanation: "Selection sort scans the unsorted portion, locates the minimum key, and places it at the boundary between sorted and unsorted segments."
      },
      {
        text: "What is the space complexity of an in-place iterative Bubble Sort implementation?",
        options: [
          "O(1)",
          "O(log n)",
          "O(n)",
          "O(n²)"
        ],
        correct: 0,
        concept: "Space Complexity",
        page: 6,
        explanation: "Iterative Bubble Sort uses only a constant number of temporary variables for swapping, requiring O(1) auxiliary space."
      }
    ];

    const osPool = [
      {
        text: "Which of the following is NOT one of the four necessary Coffman conditions for a deadlock?",
        options: [
          "Mutual Exclusion",
          "Preemptive Scheduling",
          "Hold and Wait",
          "Circular Wait"
        ],
        correct: 1,
        concept: "Deadlock Conditions",
        page: 2,
        explanation: "The condition is NO PREEMPTION. If resources can be preempted, deadlocks can be broken."
      },
      {
        text: "What is the purpose of the Banker's Algorithm in modern operating systems?",
        options: [
          "Virtual memory page replacement",
          "Deadlock avoidance by verifying safe states",
          "CPU quantum scheduling",
          "Cache line eviction"
        ],
        correct: 1,
        concept: "Banker's Algorithm",
        page: 3,
        explanation: "Banker's algorithm simulates resource allocations to guarantee that the system never enters an unsafe state."
      },
      {
        text: "What classic synchronization problem occurs when the outcome depends on thread execution order?",
        options: [
          "Race Condition",
          "Thrashing",
          "Starvation",
          "Fragmented Allocation"
        ],
        correct: 0,
        concept: "Race Conditions",
        page: 4,
        explanation: "A race condition occurs when concurrent threads manipulate shared mutable state without proper mutual exclusion."
      },
      {
        text: "What are the two atomic operations supported by a counting semaphore?",
        options: [
          "wait() [P] and signal() [V]",
          "lock() and free()",
          "push() and pop()",
          "read() and write()"
        ],
        correct: 0,
        concept: "Semaphores",
        page: 5,
        explanation: "Semaphores are controlled through atomic wait() (which decrements) and signal() (which increments)."
      },
      {
        text: "How does resource ordering prevent deadlocks?",
        options: [
          "It breaks the Circular Wait condition",
          "It forces resources to become shareable",
          "It preempts currently executing high-priority threads",
          "It allocates infinite resources"
        ],
        correct: 0,
        concept: "Deadlock Prevention",
        page: 7,
        explanation: "By enforcing an increasing global ordering on resource acquisition, a closed cycle of dependency is mathematically impossible."
      }
    ];

    const selectedPool = isAlgo ? algoPool : osPool;
    const questions = selectedPool.slice(0, Math.min(count, selectedPool.length)).map((q, idx) => ({
      id: idx + 1,
      text: q.text,
      options: q.options,
      correct: q.correct,
      concept: q.concept,
      page: q.page,
      explanation: q.explanation
    }));

    return {
      id: Date.now(),
      docId: doc.id,
      docName: doc.name,
      difficulty,
      questions
    };
  }

  // ── Chat AI (Demo Grounded Chat) ─────────────────────────────────
  function chat(docId, message) {
    const doc = getDocById(docId) || DEFAULT_DOCS[0];
    const q = message.toLowerCase();

    logSession(doc.id, "CHAT", `Asked question on ${doc.name}: "${message.substring(0, 40)}"`);

    let answer = `Based on **${doc.name}**, this topic is discussed extensively in the syllabus. Here is the grounded excerpt: \n\n> "${doc.text.split('\\n')[0]}"\n\nTo prepare for this on exams, focus on the formal definitions, underlying trade-offs, and typical runtime complexities.`;
    let pages = [1];

    if (q.includes("bst") || q.includes("binary search tree")) {
      answer = `From **${doc.name}** (Page 2-3):\n\nA **Binary Search Tree** requires all keys in a node's left subtree to be strictly smaller than the node, and keys in the right subtree to be greater.\n\n• **Search & Insertion**: O(h), where h is the tree height (O(log n) average, O(n) worst case if unbalanced).\n• **Sorted Output**: Inorder traversal (Left -> Node -> Right) visits elements in ascending order.`;
      pages = [2, 3];
    } else if (q.includes("sort") || q.includes("bubble")) {
      answer = `From **${doc.name}** (Page 4-5):\n\n**Bubble Sort** repeatedly steps through the sequence, compares adjacent pairs, and swaps them if they are in the wrong order.\n\n• **Time Complexity**: Best O(n), Average O(n²), Worst O(n²).\n• **Space**: O(1) in-place auxiliary space.\n• **Stability**: Stable sort because equal keys maintain their initial order.`;
      pages = [4, 5];
    } else if (q.includes("deadlock") || q.includes("coffman")) {
      answer = `From **${doc.name}** (Page 2-4):\n\nA **Deadlock** requires all four Coffman conditions to hold concurrently:\n1. **Mutual Exclusion**: Exclusive resource access.\n2. **Hold and Wait**: Holding one resource while requesting another.\n3. **No Preemption**: Resources cannot be confiscated.\n4. **Circular Wait**: A closed chain of processes waiting on each other.\n\nBreaking *any single one* prevents deadlock from forming.`;
      pages = [2, 4];
    } else if (q.includes("summary") || q.includes("summarize") || q.includes("overview")) {
      answer = `**Document Executive Summary for ${doc.name}**:\n\n${doc.summary}\n\n*Source contains ${doc.pages} indexed pages.*`;
      pages = [1, doc.pages];
    }

    return {
      answer,
      pageReferences: pages,
      docName: doc.name,
      mode: "DEMO GROUNDED AI"
    };
  }

  // ── Overall Progress Stats ───────────────────────────────────────
  function getProgress() {
    const docs = getDocs();
    const attempts = getAttempts();
    const sessions = getSessions();
    const weaks = getWeakConceptsList();

    let avgScore = 0;
    if (attempts.length > 0) {
      const sum = attempts.reduce((acc, a) => acc + (a.scorePct || 0), 0);
      avgScore = Math.round(sum / attempts.length);
    }

    const explainCount = sessions.filter(s => s.type === 'EXPLAIN').length;
    const chatCount = sessions.filter(s => s.type === 'CHAT').length;

    return {
      totalDocs: docs.length,
      totalQuizzes: attempts.length,
      totalAttempts: attempts.length,
      avgScore: attempts.length > 0 ? avgScore : null,
      explainSessions: explainCount,
      chatSessions: chatCount,
      recentActivity: sessions.slice(0, 10),
      recentAttempts: attempts.slice(0, 6),
      weakConcepts: weaks
    };
  }

  return {
    getDocs,
    getDocById,
    addDoc,
    deleteDoc,
    explain,
    generateQuiz,
    saveAttempt,
    recordMissedConcept,
    recordPassedConcept,
    chat,
    getProgress,
    getAttempts,
    getSessions
  };
})();
