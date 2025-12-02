// Von Lukas bearbeitet
import { Injectable } from '@angular/core';
import { Assignment, Note, PeerEvaluation, Project, Scores, Student } from './models';

@Injectable({ providedIn: 'root' })
export class TeacherMockService {
  // Simple class storage for teacher-managed classes
  private classesData: string[] = ['10A', '10B', '10C'];
  private readonly studentsData: Student[] = [
    { id: 1, name: 'Anna Schmidt', class: '10A', avatarUrl: 'assets/bear.png' },
    { id: 2, name: 'Lukas Weber', class: '10A', avatarUrl: 'assets/chicken.png' },
    { id: 3, name: 'Sophia Klein', class: '10B', avatarUrl: 'assets/dog.png' },
    { id: 4, name: 'Max Fischer', class: '10B', avatarUrl: 'assets/gorilla.png' },
    { id: 5, name: 'Mara Fuchs', class: '10C', avatarUrl: 'assets/meerkat.png' },
    { id: 6, name: 'Felix Braun', class: '10C', avatarUrl: 'assets/rabbit.png' },
  ];

  private readonly projectsData: Project[] = [
    { id: 101, name: 'Robotik' },
    { id: 102, name: 'Digitale Medien' },
    { id: 103, name: 'Energie der Zukunft' },
  ];

  private assignmentsData: Assignment[] = [
    { studentId: 1, projectId: 101, assigned: true },
    { studentId: 2, projectId: 101, assigned: false },
    { studentId: 3, projectId: 101, assigned: true },
    { studentId: 4, projectId: 101, assigned: false },
  ];

  private notesData: Note[] = [];
  private peerEvaluations: PeerEvaluation[] = [];

  // Synthetic history cache per student+project
  private scoresHistoryCache = new Map<
    string,
    { labels: string[]; teacher: number[]; peer: number[]; self: number[] }
  >();

  // Liefert Schüler/Klassen/Projekte/Aufgaben als Kopie zurück
  getStudents(): Student[] {
    return [...this.studentsData];
  }
  getClasses(): string[] {
    return [...this.classesData];
  }
  getProjects(): Project[] {
    return [...this.projectsData];
  }
  getAssignments(): Assignment[] {
    return this.assignmentsData.map((a) => ({ ...a }));
  }

  // Neue Klasse hinzufügen (ohne Duplikate)
  addClass(name: string): void {
    const n = (name || '').trim();
    if (!n) return;
    if (!this.classesData.includes(n)) {
      this.classesData = [...this.classesData, n];
    }
  }

  // Weist einem Schüler eine Klasse zu (Klasse wird bei Bedarf angelegt)
  setStudentClass(studentId: number, className: string): void {
    const n = (className || '').trim();
    const st = this.studentsData.find((s) => s.id === studentId);
    if (!st || !n) return;
    // allow free assignment; auto-add class if missing
    if (!this.classesData.includes(n)) {
      this.classesData = [...this.classesData, n];
    }
    st.class = n;
  }

  // Schaltet die Projektzuweisung für einen Schüler um
  toggleAssignment(studentId: number, projectId: number): void {
    const found = this.assignmentsData.find(
      (a) => a.studentId === studentId && a.projectId === projectId,
    );
    if (found) {
      found.assigned = !found.assigned;
    } else {
      this.assignmentsData.push({ studentId, projectId, assigned: true });
    }
  }

  // Teacher -> Student notes
  // Speichert eine kurze Notiz vom Lehrer an den Schüler
  addNote(toStudentId: number, projectId: number, text: string): void {
    const createdAt = new Date().toISOString();
    this.notesData.unshift({ toStudentId, projectId, text, createdAt });
  }

  // Liest alle Notizen zu einem Schüler/Projekt
  getNotesFor(studentId: number, projectId: number): Note[] {
    return this.notesData.filter((n) => n.toStudentId === studentId && n.projectId === projectId);
  }

  // Peer evaluations
  // Speichert eine Peer-Bewertung (Schüler bewertet Schüler)
  submitPeerEvaluation(
    fromStudentId: number,
    toStudentId: number,
    projectId: number,
    grade: number,
  ): void {
    const createdAt = new Date().toISOString();
    this.peerEvaluations.push({ fromStudentId, toStudentId, projectId, grade, createdAt });
  }

  // Durchschnitt der Peer-Bewertungen berechnen
  private getPeerAverageFor(studentId: number, projectId: number): number | null {
    const items = this.peerEvaluations.filter(
      (pe) => pe.toStudentId === studentId && pe.projectId === projectId,
    );
    if (!items.length) return null;
    const sum = items.reduce((acc, it) => acc + it.grade, 0);
    return sum / items.length;
  }

  // Noten für Lehrer/Peer/Selbst als einfache Mock-Berechnung
  getScores(studentId: number, projectId: number): Scores {
    const base = (studentId * 7 + projectId) % 3; // 0..2 pseudo-deterministic
    const teacher = 1.7 + base * 0.2;
    const self = 1.9 + (base === 1 ? 0.0 : 0.1);

    const peerAvg = this.getPeerAverageFor(studentId, projectId);
    const peer = peerAvg ?? 2.1 + (2 - base) * 0.1; // fallback mock

    return {
      teacher: teacher.toFixed(1),
      peer: peer.toFixed(1),
      self: self.toFixed(1),
      trendTeacher: '+1%',
      trendPeer: peerAvg ? 'neu' : '-5%',
      trendSelf: '+3%',
    };
  }

  // Returns a synthetic but deterministic score history for charts
  // Baut eine kleine Verlaufskurve (Labels + drei Werte-Arrays)
  getScoreHistory(studentId: number, projectId: number, points = 6) {
    const key = `${studentId}_${projectId}_${points}`;
    const cached = this.scoresHistoryCache.get(key);
    if (cached) return cached;

    const labels: string[] = [];
    const teacher: number[] = [];
    const peer: number[] = [];
    const self: number[] = [];

    // base seeds
    const seed = (studentId * 13 + projectId * 3) >>> 0;
    let r = seed;
    const rand = () => {
      r = (r * 1664525 + 1013904223) >>> 0;
      return (r % 1000) / 1000;
    };

    // start from current latest values
    const cur = this.getScores(studentId, projectId);
    let t = Number(cur.teacher);
    let p = Number(cur.peer);
    let s = Number(cur.self);

    // Build history from oldest → newest
    const now = new Date();
    const items = points;
    const tmp: { label: string; t: number; p: number; s: number }[] = [];
    for (let i = items - 1; i >= 0; i--) {
      const d = new Date(now);
      d.setDate(now.getDate() - i * 7);
      const label = `${d.getDate().toString().padStart(2, '0')}.${(d.getMonth() + 1).toString().padStart(2, '0')}`;

      // small drift per step (German grading: keep around 1.0–6.0)
      t = Math.max(1.0, Math.min(6.0, t + (rand() - 0.5) * 0.2));
      p = Math.max(1.0, Math.min(6.0, p + (rand() - 0.5) * 0.25));
      s = Math.max(1.0, Math.min(6.0, s + (rand() - 0.5) * 0.25));
      tmp.push({ label, t, p, s });
    }

    for (const it of tmp) {
      labels.push(it.label);
      teacher.push(Number(it.t.toFixed(1)));
      peer.push(Number(it.p.toFixed(1)));
      self.push(Number(it.s.toFixed(1)));
    }

    const res = { labels, teacher, peer, self };
    this.scoresHistoryCache.set(key, res);
    return res;
  }
}
