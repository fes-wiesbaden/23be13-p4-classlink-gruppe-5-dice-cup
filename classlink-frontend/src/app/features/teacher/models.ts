// Von Lukas bearbeitet
// Feature models for the Teacher view (frontend-only mocks)

export interface Student {
  id: number;
  name: string;
  class: string;
  avatarUrl?: string;
}

export interface Project {
  id: number;
  name: string;
}

export interface Assignment {
  studentId: number;
  projectId: number;
  assigned: boolean;
}

export interface Scores {
  teacher: string;
  peer: string;
  self: string;
  trendTeacher: string;
  trendPeer: string;
  trendSelf: string;
}

export interface ProjectOption {
  label: string;
  value: number;
}

// Notes sent by teachers to students
export interface Note {
  toStudentId: number;
  projectId: number;
  text: string;
  createdAt: string; // ISO timestamp
}

// Peer evaluation submitted by a student about another student
export interface PeerEvaluation {
  fromStudentId: number;
  toStudentId: number;
  projectId: number;
  grade: number; // numeric grade like 1.0 .. 6.0
  createdAt: string; // ISO timestamp
}
